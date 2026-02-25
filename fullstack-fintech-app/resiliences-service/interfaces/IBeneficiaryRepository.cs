using resiliences_service.Models;
using resiliences_service.Enums;

namespace resiliences_service.interfaces
{
    public interface IBeneficiaryRepository
    {
        Task<Beneficiary?> FindByIdAsync(uint id, uint userId);
        Task<Beneficiary?> FindByUserIdAsync(uint userId);
        Task<List<Beneficiary>> FindAllByUserIdAsync(uint userId);
        Task<List<Beneficiary>> FindByTypeAsync(uint userId, BeneficiaryType type);
        Task<List<Beneficiary>> SearchAsync(uint userId, string searchTerm);
        Task<Beneficiary?> FindBankByAccountNumberAsync(uint userId, string accountNumber);
        Task<Beneficiary?> FindUserByUsernameAsync(uint userId, string username);
        Task SaveAsync(Beneficiary beneficiary);
        Task UpdateAsync(Beneficiary beneficiary);
        Task DeleteAsync(uint id, uint userId);
        Task DeleteByIdsAsync(List<uint> ids, uint userId);
        Task HardDeleteAsync(uint id, uint userId);
    }
}