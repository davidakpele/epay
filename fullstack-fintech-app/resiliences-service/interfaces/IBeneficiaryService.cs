using resiliences_service.Models;
using resiliences_service.Enums;

namespace resiliences_service.interfaces
{
    public interface IBeneficiaryService
    {
        Task<Beneficiary?> GetByIdAsync(uint id, uint userId);
        Task<Beneficiary?> GetByUserIdAsync(uint userId);
        Task<List<Beneficiary>> GetAllByUserIdAsync(uint userId);
        Task<List<Beneficiary>> GetByTypeAsync(uint userId, BeneficiaryType type);
        Task<List<Beneficiary>> SearchAsync(uint userId, string searchTerm);
        Task CreateAsync(Beneficiary beneficiary);
        Task UpdateAsync(Beneficiary beneficiary);
        Task DeleteAsync(uint id, uint userId);
        Task DeleteByIdsAsync(List<uint> ids, uint userId);
        Task<Beneficiary?> GetByUserIdAndUsernameAsync(uint userId, string recipientUsername);
    }
}