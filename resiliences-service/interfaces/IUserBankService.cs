using resiliences_service.Models;
using resiliences_service.Payloads;

namespace resiliences_service.interfaces
{
    public interface IUserBankService
    {
        Task CreateBankAsync(UserBankList bank);
        Task<UserBankList?> FindByIdAsync(uint id);
        Task<UserBankList?> FindByAccountNumberAsync(string accountNumber);
        Task<List<UserBankList>> FindByUserIdAsync(uint userId);
        Task DeleteByIdsAsync(List<uint> ids);
        Task<bool> FindByAccountNumberAndBankNameAsync(string accountNumber, string bankName);
        Task<UserBankList?> FindInternalAsync(string accountNumber, string bankCode);
        Task<List<PayStackBankList>> FetchAllBanksAsync();
        Task<PaystackAccountData?> VerifyExternalAsync(string accountNumber, string bankCode);
    }
}