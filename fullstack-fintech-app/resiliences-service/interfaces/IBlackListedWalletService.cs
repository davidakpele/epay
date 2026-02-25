using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using resiliences_service.Enums;
using resiliences_service.Models;

namespace resiliences_service.interfaces
{
    public interface IBlackListedWalletService
    {
        Task<bool> IsWalletBlacklistedAsync(uint walletId);
        Task<BlackListedWallet?> GetBlacklistedWalletAsync(uint walletId);
        Task RemoveBlacklistedWalletAsync(uint walletId);
        Task AddToBlackListAsync(uint walletId, BannedReasons reason, bool isBlock);
        Task<long> CountBlacklistedWalletsAsync();
    }
}