package repository

import (
    "revenue-service/internal/models"
    "gorm.io/gorm"
)

type RevenueRepository interface {
    Create(revenue *models.Revenue) error
    FindByID(id uint) (*models.Revenue, error)
    GetOrCreate() (*models.Revenue, error)
    Update(revenue *models.Revenue) error
}

type revenueRepository struct {
    db *gorm.DB
}

func NewRevenueRepository(db *gorm.DB) RevenueRepository {
    return &revenueRepository{db: db}
}

func (r *revenueRepository) Create(revenue *models.Revenue) error {
    return r.db.Create(revenue).Error
}

func (r *revenueRepository) FindByID(id uint) (*models.Revenue, error) {
    var revenue models.Revenue
    err := r.db.Preload("Balances").First(&revenue, id).Error
    if err != nil {
        return nil, err
    }
    return &revenue, nil
}

func (r *revenueRepository) GetOrCreate() (*models.Revenue, error) {
    var revenue models.Revenue
    
    err := r.db.Preload("Balances").First(&revenue).Error
    
    if err != nil {
        if err == gorm.ErrRecordNotFound {
            revenue = models.Revenue{
                Password: "", 
            }
            if err := r.db.Create(&revenue).Error; err != nil {
                return nil, err
            }
            return &revenue, nil
        }
        return nil, err
    }
    
    return &revenue, nil
}

func (r *revenueRepository) Update(revenue *models.Revenue) error {
    return r.db.Save(revenue).Error
}