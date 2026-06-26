using Microsoft.EntityFrameworkCore;
using resiliences_service.Configs;
using resiliences_service.interfaces;
using resiliences_service.Models;

namespace resiliences_service.Repositories
{
    public class BlackListedWalletRepository : IBlackListedWalletRepository
    {
        private readonly AppDbContext _context;

        public BlackListedWalletRepository(AppDbContext context)
        {
            _context = context;
        }

        public async Task<bool> ExistsByWalletIdAsync(uint walletId)
        {
            return await _context.BlackListedWallets
                .AnyAsync(w => w.WalletId == walletId);
        }

        public async Task<BlackListedWallet?> FindByWalletIdAsync(uint walletId)
        {
            return await _context.BlackListedWallets
                .FirstOrDefaultAsync(w => w.WalletId == walletId);
        }

        public async Task DeleteByWalletIdAsync(uint walletId)
        {
            var wallet = await FindByWalletIdAsync(walletId);
            if (wallet != null)
            {
                _context.BlackListedWallets.Remove(wallet);
                await _context.SaveChangesAsync();
            }
        }

        public async Task SaveAsync(BlackListedWallet wallet)
        {
            await _context.BlackListedWallets.AddAsync(wallet);
            await _context.SaveChangesAsync();
        }

        public async Task<long> CountBlacklistedWalletsAsync()
        {
            return await _context.BlackListedWallets.LongCountAsync();
        }
    }
}