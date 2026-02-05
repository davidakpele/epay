// services/cache_service.go
package services

import (
	"context"
	"encoding/json"
	"fmt"
	"history-service/models"
	"history-service/repository"
	"runtime"
	"sync"
	"time"

	"github.com/redis/go-redis/v9"
	"go.uber.org/zap"
)

// Constants
const (
	AllHistoriesKey           = "histories:all"
	UserHistoriesKey          = "histories:user:%d"
	CacheCleanupInterval      = 10 * time.Minute  
	MaxCacheSizeBytes         = 10 * 1024 * 1024 
	DefaultPageSize           = 500
	MaxConcurrentCacheWorkers = 5
)

type Metrics interface {
	IncrementCounter(name string, tags ...string)
	RecordHistogram(name string, value float64, tags ...string)
	SetGauge(name string, value float64, tags ...string)
}

type CacheConfig struct {
	PageSize              int
	MaxCacheSizeBytes     int64
	WarmupInterval        time.Duration
	MaxConcurrentWorkers  int
	RecentHistoryDuration time.Duration
}

func DefaultCacheConfig() CacheConfig {
	return CacheConfig{
		PageSize:              DefaultPageSize,
		MaxCacheSizeBytes:     MaxCacheSizeBytes,
		WarmupInterval:        CacheCleanupInterval,
		MaxConcurrentWorkers:  MaxConcurrentCacheWorkers,
		RecentHistoryDuration: 30 * 24 * time.Hour,
	}
}

type HistoryCacheService struct {
	redisClient *redis.Client
	historyRepo *repository.HistoryRepository
	logger      *zap.Logger
	metrics     Metrics
	config      CacheConfig
	mu          sync.RWMutex
	warmupInProgress bool
}

func NewHistoryCacheService(
	redisClient *redis.Client,
	historyRepo *repository.HistoryRepository,
	logger *zap.Logger,
	metrics Metrics,
	config CacheConfig,
) *HistoryCacheService {
	if logger == nil {
		logger, _ = zap.NewProduction()
	}

	return &HistoryCacheService{
		redisClient: redisClient,
		historyRepo: historyRepo,
		logger:      logger,
		metrics:     metrics,
		config:      config,
	}
}

func (s *HistoryCacheService) StartCacheWarmup(ctx context.Context) {
	ticker := time.NewTicker(s.config.WarmupInterval)
	defer ticker.Stop()

	go func() {
		s.logger.Info("Starting cache warmup service",
			zap.Duration("interval", s.config.WarmupInterval))
		s.WarmupCache()

		for {
			select {
			case <-ticker.C:
				s.WarmupCache()
			case <-ctx.Done():
				s.logger.Info("Stopping cache warmup service")
				return
			}
		}
	}()
}


func (s *HistoryCacheService) WarmupCache() {
	s.mu.Lock()
	if s.warmupInProgress {
		s.mu.Unlock()
		s.logger.Warn("Cache warmup already in progress, skipping")
		s.metrics.IncrementCounter("cache.warmup.skipped_already_running")
		return
	}
	s.warmupInProgress = true
	s.mu.Unlock()

	defer func() {
		s.mu.Lock()
		s.warmupInProgress = false
		s.mu.Unlock()
	}()

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Minute)
	defer cancel()

	s.logger.Info("Starting cache warmup")
	startTime := time.Now()

	var successCount int
	operations := []struct {
		name string
		fn   func(context.Context) error
	}{
		{"cache_all_histories", s.cacheAllHistories},
		{"cache_recent_users", s.cacheRecentActiveUsers},
	}

	for _, op := range operations {
		if err := op.fn(ctx); err != nil {
			s.logger.Error("Cache warmup operation failed",
				zap.String("operation", op.name),
				zap.Error(err))
			s.metrics.IncrementCounter("cache.warmup.operation_failed", "operation:"+op.name)
		} else {
			successCount++
			s.metrics.IncrementCounter("cache.warmup.operation_success", "operation:"+op.name)
		}
	}

	duration := time.Since(startTime)
	s.metrics.RecordHistogram("cache.warmup.duration", duration.Seconds())
	s.metrics.SetGauge("cache.warmup.successful_operations", float64(successCount))

	s.logger.Info("Cache warmup completed",
		zap.Int("successful_operations", successCount),
		zap.Int("total_operations", len(operations)),
		zap.Duration("duration", duration))
}

func (s *HistoryCacheService) cacheAllHistories(ctx context.Context) error {
	startTime := time.Now()
	s.logger.Info("Starting cacheAllHistories operation")

	defer func() {
		duration := time.Since(startTime).Seconds()
		s.metrics.RecordHistogram("cache.all_histories.duration", duration)
		s.logger.Info("Completed cacheAllHistories operation",
			zap.Float64("duration_seconds", duration))
	}()

	s.logger.Debug("Fetching histories from database", 
		zap.Duration("recent_duration", s.config.RecentHistoryDuration))

	page := 1
	var allHistories []models.History
	var mu sync.Mutex
	var wg sync.WaitGroup

	workerPool := make(chan struct{}, s.config.MaxConcurrentWorkers)
	errors := make(chan error, 1)
	pageCtx, cancel := context.WithTimeout(ctx, 2*time.Minute)
	defer cancel()

	for {
		select {
		case <-pageCtx.Done():
			s.logger.Error("cacheAllHistories cancelled due to timeout")
			return fmt.Errorf("cacheAllHistories cancelled: %w", pageCtx.Err())
		default:
			workerPool <- struct{}{} 
			wg.Add(1)

			go func(pageNum int) {
				defer func() {
					<-workerPool 
					wg.Done()

					if r := recover(); r != nil {
						s.logger.Error("panic in cacheAllHistories worker",
							zap.Any("recover", r),
							zap.Int("page", pageNum))
						s.metrics.IncrementCounter("cache.all_histories.panic")
					}
				}()

				s.logger.Debug("Fetching page", zap.Int("page", pageNum))
				histories, err := s.fetchHistoriesPage(pageCtx, pageNum)
				if err != nil {
					s.logger.Error("Error fetching page", 
						zap.Int("page", pageNum), 
						zap.Error(err))
					select {
					case errors <- fmt.Errorf("page %d: %w", pageNum, err):
					default:
					}
					return
				}

				s.logger.Debug("Fetched page results", 
					zap.Int("page", pageNum), 
					zap.Int("records", len(histories)))

				if len(histories) == 0 {
					s.logger.Debug("No more data, stopping pagination", zap.Int("page", pageNum))
					return 
				}

				mu.Lock()
				allHistories = append(allHistories, histories...)

				if s.estimateMemoryUsage(allHistories) > s.config.MaxCacheSizeBytes {
					s.logger.Warn("Exceeding max cache size, truncating results",
						zap.Int("total_records", len(allHistories)))
					allHistories = allHistories[:len(allHistories)-len(histories)] // Remove last batch
					mu.Unlock()
					return
				}
				mu.Unlock()

				s.metrics.IncrementCounter("cache.all_histories.page_processed",
					fmt.Sprintf("page:%d", pageNum))
			}(page)

			page++
		}

		if page > 50 { 
			s.logger.Debug("Reached safety limit of 50 pages")
			break
		}
	}

	wg.Wait()

	select {
	case err := <-errors:
		s.logger.Error("Error in cacheAllHistories", zap.Error(err))
		return err
	default:
	}

	s.logger.Info("Successfully fetched histories from database", 
		zap.Int("total_records", len(allHistories)),
		zap.Int("total_pages", page-1))

	if err := s.setCache(ctx, AllHistoriesKey, allHistories); err != nil {
		s.metrics.IncrementCounter("cache.all_histories.set_cache_error")
		s.logger.Error("Failed to set cache", zap.Error(err))
		return fmt.Errorf("failed to set cache: %w", err)
	}

	s.metrics.SetGauge("cache.all_histories.cached_records", float64(len(allHistories)))
	s.logger.Info("Successfully cached all histories",
		zap.Int("total_records", len(allHistories)),
		zap.Int("total_pages", page-1))

	return nil
}

func (s *HistoryCacheService) fetchHistoriesPage(ctx context.Context, page int) ([]models.History, error) {
	offset := (page - 1) * s.config.PageSize
	since := time.Now().Add(-s.config.RecentHistoryDuration)

	var histories []models.History
	
	err := s.historyRepo.DB.WithContext(ctx).
		Where("timestamp >= ?", since).
		Order("timestamp DESC").
		Offset(offset).
		Limit(s.config.PageSize).
		Find(&histories).Error

	if err != nil {
		s.metrics.IncrementCounter("cache.all_histories.db_error")
		return nil, fmt.Errorf("database error: %w", err)
	}

	return histories, nil
}


func (s *HistoryCacheService) cacheRecentActiveUsers(ctx context.Context) error {
	startTime := time.Now()
	defer func() {
		s.metrics.RecordHistogram("cache.recent_users.duration", time.Since(startTime).Seconds())
	}()

	since := time.Now().Add(-7 * 24 * time.Hour)
	
	var recentUserIDs []uint64
	
	err := s.historyRepo.DB.WithContext(ctx).
		Model(&models.History{}).
		Distinct("user_id").
		Where("timestamp >= ?", since).
		Pluck("user_id", &recentUserIDs).Error

	if err != nil {
		return fmt.Errorf("failed to get recent users: %w", err)
	}

	s.logger.Info("Caching histories for recent active users",
		zap.Int("user_count", len(recentUserIDs)))

	var wg sync.WaitGroup
	semaphore := make(chan struct{}, s.config.MaxConcurrentWorkers)
	errors := make(chan error, len(recentUserIDs))

	for _, userID := range recentUserIDs {
		wg.Add(1)
		semaphore <- struct{}{}

		go func(uid uint64) {
			defer func() {
				<-semaphore
				wg.Done()
			}()

			if err := s.CacheUserHistories(ctx, uid); err != nil {
				errors <- fmt.Errorf("user %d: %w", uid, err)
			}
		}(userID)
	}

	wg.Wait()
	close(errors)

	var errorCount int
	for err := range errors {
		s.logger.Warn("Failed to cache user histories", zap.Error(err))
		errorCount++
	}

	if errorCount > 0 {
		s.metrics.IncrementCounter("cache.recent_users.errors", fmt.Sprintf("count:%d", errorCount))
	}

	s.metrics.SetGauge("cache.recent_users.cached", float64(len(recentUserIDs)-errorCount))
	return nil
}

func (s *HistoryCacheService) CacheUserHistories(ctx context.Context, userID uint64) error {
	histories, err := s.historyRepo.FindByUserID(userID)
	if err != nil {
		return fmt.Errorf("failed to fetch user histories: %w", err)
	}

	data, err := json.Marshal(histories)
	if err != nil {
		return fmt.Errorf("failed to marshal user histories: %w", err)
	}

	key := fmt.Sprintf(UserHistoriesKey, userID)
	
	return s.redisClient.Set(ctx, key, data, 0).Err()
}

func (s *HistoryCacheService) GetCachedUserHistories(ctx context.Context, userID uint64) ([]models.History, error) {
	key := fmt.Sprintf(UserHistoriesKey, userID)
	data, err := s.redisClient.Get(ctx, key).Bytes()
	if err == redis.Nil {
		return nil, nil 
	} else if err != nil {
		return nil, fmt.Errorf("redis get error: %w", err)
	}

	var histories []models.History
	if err := json.Unmarshal(data, &histories); err != nil {
		return nil, fmt.Errorf("unmarshal error: %w", err)
	}

	return histories, nil
}

func (s *HistoryCacheService) GetCachedAllHistories(ctx context.Context) ([]models.History, error) {
	data, err := s.redisClient.Get(ctx, AllHistoriesKey).Bytes()
	if err == redis.Nil {
		return nil, nil 
	} else if err != nil {
		return nil, fmt.Errorf("redis get error: %w", err)
	}

	var histories []models.History
	if err := json.Unmarshal(data, &histories); err != nil {
		return nil, fmt.Errorf("unmarshal error: %w", err)
	}

	return histories, nil
}

func (s *HistoryCacheService) InvalidateUserCache(ctx context.Context, userID uint64) error {
	key := fmt.Sprintf(UserHistoriesKey, userID)
	err := s.redisClient.Del(ctx, key).Err()
	if err != nil {
		return fmt.Errorf("failed to invalidate user cache: %w", err)
	}
	
	s.metrics.IncrementCounter("cache.invalidated", "type:user")
	return nil
}

func (s *HistoryCacheService) InvalidateAllCache(ctx context.Context) error {
	iter := s.redisClient.Scan(ctx, 0, "histories:*", 0).Iterator()
	var keys []string
	
	for iter.Next(ctx) {
		keys = append(keys, iter.Val())
	}
	
	if err := iter.Err(); err != nil {
		return fmt.Errorf("scan error: %w", err)
	}
	
	if len(keys) > 0 {
		err := s.redisClient.Del(ctx, keys...).Err()
		if err != nil {
			return fmt.Errorf("delete error: %w", err)
		}
	}
	
	s.metrics.IncrementCounter("cache.invalidated", "type:all")
	s.logger.Info("Invalidated all history caches", zap.Int("keys_deleted", len(keys)))
	return nil
}

func (s *HistoryCacheService) setCache(ctx context.Context, key string, data interface{}) error {
	jsonData, err := json.Marshal(data)
	if err != nil {
		return fmt.Errorf("marshal error: %w", err)
	}

	if int64(len(jsonData)) > s.config.MaxCacheSizeBytes {
		return fmt.Errorf("data too large for cache: %d bytes", len(jsonData))
	}

	return s.redisClient.Set(ctx, key, jsonData, 0).Err()
}

func (s *HistoryCacheService) estimateMemoryUsage(histories []models.History) int64 {
	const avgHistorySize = 500 
	return int64(len(histories) * avgHistorySize)
}

func (s *HistoryCacheService) HealthCheck(ctx context.Context) error {
	if err := s.redisClient.Ping(ctx).Err(); err != nil {
		return fmt.Errorf("redis health check failed: %w", err)
	}

	var count int64
	if err := s.historyRepo.DB.WithContext(ctx).Model(&models.History{}).Count(&count).Error; err != nil {
		return fmt.Errorf("database health check failed: %w", err)
	}

	return nil
}

func (s *HistoryCacheService) GetCacheStats(ctx context.Context) (map[string]interface{}, error) {
	stats := make(map[string]interface{})

	keys, err := s.redisClient.Keys(ctx, "histories:*").Result()
	if err != nil {
		return nil, fmt.Errorf("failed to get keys: %w", err)
	}
	stats["cached_keys_count"] = len(keys)

	info, err := s.redisClient.Info(ctx, "memory").Result()
	if err == nil {
		stats["redis_info"] = info
	}

	var m runtime.MemStats
	runtime.ReadMemStats(&m)
	stats["allocated_memory"] = m.Alloc
	stats["total_allocated"] = m.TotalAlloc
	stats["system_memory"] = m.Sys
	stats["num_gc"] = m.NumGC

	s.mu.RLock()
	stats["warmup_in_progress"] = s.warmupInProgress
	s.mu.RUnlock()

	return stats, nil
}

func (s *HistoryCacheService) IsWarmupInProgress() bool {
	s.mu.RLock()
	defer s.mu.RUnlock()
	return s.warmupInProgress
}