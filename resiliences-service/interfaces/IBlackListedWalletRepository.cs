using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using resiliences_service.Models;

namespace resiliences_service.interfaces
{
    public interface IBlackListedWalletRepository
    {
        Task<bool> ExistsByWalletIdAsync(uint walletId);
        Task<BlackListedWallet?> FindByWalletIdAsync(uint walletId);
        Task DeleteByWalletIdAsync(uint walletId);
        Task SaveAsync(BlackListedWallet wallet);
        Task<long> CountBlacklistedWalletsAsync();
    }
}