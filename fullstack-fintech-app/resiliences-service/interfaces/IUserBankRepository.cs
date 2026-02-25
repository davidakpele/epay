using resiliences_service.Models;

namespace resiliences_service.interfaces
{
    public interface IUserBankRepository
    {
        Task CreateAsync(UserBankList bank);
        Task<UserBankList?> FindByIdAsync(uint id);
        Task<UserBankList?> FindByAccountNumberAsync(string accountNumber);
        Task<List<UserBankList>> FindByUserIdAsync(uint userId);
        Task DeleteByIdsAsync(List<uint> ids);
        Task<bool> FindByAccountNumberAndBankCodeAsync(string accountNumber, string bankCode);
        Task<bool> FindByAccountNumberAndBankNameAsync(string accountNumber, string bankName);
        Task<UserBankList?> FindInternalAsync(string accountNumber, string bankCode);
    }
}