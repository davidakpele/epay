using resiliences_service.Enums;
using resiliences_service.interfaces;
using resiliences_service.Models;


namespace resiliences_service.Services
{
    public class BlackListedWalletService : IBlackListedWalletService
    {
        private readonly IBlackListedWalletRepository _repo;

        public BlackListedWalletService(IBlackListedWalletRepository repo)
        {
            _repo = repo;
        }

        public Task<bool> IsWalletBlacklistedAsync(uint walletId)
            => _repo.ExistsByWalletIdAsync(walletId);

        public Task<BlackListedWallet?> GetBlacklistedWalletAsync(uint walletId)
            => _repo.FindByWalletIdAsync(walletId);

        public Task RemoveBlacklistedWalletAsync(uint walletId)
            => _repo.DeleteByWalletIdAsync(walletId);

        public async Task AddToBlackListAsync(uint walletId, BannedReasons reason, bool isBlock)
        {
            var wallet = new BlackListedWallet
            {
                WalletId         = walletId,
                BankBannedReason = reason,
                IsBlock          = isBlock,
                Timestamp        = DateTime.UtcNow,
                CreatedAt        = DateTime.UtcNow
            };

            await _repo.SaveAsync(wallet);
        }

        public Task<long> CountBlacklistedWalletsAsync()
            => _repo.CountBlacklistedWalletsAsync();
    }
}