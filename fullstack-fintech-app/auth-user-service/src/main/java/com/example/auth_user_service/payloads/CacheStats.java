package com.example.auth_user_service.payloads;

public class CacheStats {
    private boolean allUsersCached;
    private int cachedUsersCount;
    private int individualUserKeysCount;
    private boolean warmupInProgress;

    public CacheStats() {}
    public CacheStats(boolean allUsersCached, int cachedUsersCount, int individualUserKeysCount, boolean warmupInProgress) {
        this.allUsersCached = allUsersCached;
        this.cachedUsersCount = cachedUsersCount;
        this.individualUserKeysCount = individualUserKeysCount;
        this.warmupInProgress = warmupInProgress;
    }

    public boolean isAllUsersCached() { return allUsersCached; }
    public int getCachedUsersCount() { return cachedUsersCount; }
    public int getIndividualUserKeysCount() { return individualUserKeysCount; }
    public boolean isWarmupInProgress() { return warmupInProgress; }

    public void setAllUsersCached(boolean allUsersCached) { this.allUsersCached = allUsersCached; }
    public void setCachedUsersCount(int cachedUsersCount) { this.cachedUsersCount = cachedUsersCount; }
    public void setIndividualUserKeysCount(int individualUserKeysCount) { this.individualUserKeysCount = individualUserKeysCount; }
    public void setWarmupInProgress(boolean warmupInProgress) { this.warmupInProgress = warmupInProgress; }
}
