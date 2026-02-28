using resiliences_service.Models;
using resiliences_service.Enums;
using resiliences_service.interfaces;

namespace resiliences_service.Services
{
    public class BeneficiaryService : IBeneficiaryService
    {
        private readonly IBeneficiaryRepository _repo;

        public BeneficiaryService(IBeneficiaryRepository repo)
        {
            _repo = repo;
        }

        public async Task<Beneficiary?> GetByIdAsync(uint id, uint userId) =>
            await _repo.FindByIdAsync(id, userId);

        public async Task<Beneficiary?> GetByUserIdAsync(uint userId) =>
            await _repo.FindByUserIdAsync(userId);

        public async Task<List<Beneficiary>> GetAllByUserIdAsync(uint userId) =>
            await _repo.FindAllByUserIdAsync(userId);

        public async Task<List<Beneficiary>> GetByTypeAsync(uint userId, BeneficiaryType type)
        {
            if (type != BeneficiaryType.bank && type != BeneficiaryType.user)
                throw new ArgumentException("Invalid beneficiary type. Must be 'bank' or 'user'");

            return await _repo.FindByTypeAsync(userId, type);
        }

        public async Task<List<Beneficiary>> SearchAsync(uint userId, string searchTerm)
        {
            if (string.IsNullOrEmpty(searchTerm))
                throw new ArgumentException("Search term cannot be empty");

            return await _repo.SearchAsync(userId, searchTerm);
        }

        public async Task CreateAsync(Beneficiary beneficiary)
        {
            if (beneficiary.BeneficiaryType != BeneficiaryType.bank &&
                beneficiary.BeneficiaryType != BeneficiaryType.user)
                throw new ArgumentException("Invalid beneficiary type");

            if (string.IsNullOrEmpty(beneficiary.BeneficiaryName))
                throw new ArgumentException("Beneficiary name is required");

            if (string.IsNullOrEmpty(beneficiary.Currency))
                throw new ArgumentException("Currency is required");

            if (beneficiary.BeneficiaryType == BeneficiaryType.bank)
            {
                if (string.IsNullOrEmpty(beneficiary.AccountNumber))
                    throw new ArgumentException("Account number is required for bank beneficiaries");
                if (string.IsNullOrEmpty(beneficiary.AccountName))
                    throw new ArgumentException("Account name is required for bank beneficiaries");
                if (string.IsNullOrEmpty(beneficiary.BankCode))
                    throw new ArgumentException("Bank code is required for bank beneficiaries");
                if (string.IsNullOrEmpty(beneficiary.BankName))
                    throw new ArgumentException("Bank name is required for bank beneficiaries");

                var existing = await _repo.FindBankByAccountNumberAsync(beneficiary.UserId, beneficiary.AccountNumber);
                if (existing != null)
                    throw new InvalidOperationException("Beneficiary with this account number already exists");
            }
            else
            {
                if (string.IsNullOrEmpty(beneficiary.RecipientUsername))
                    throw new ArgumentException("Recipient username is required for user beneficiaries");

                var existing = await _repo.FindUserByUsernameAsync(beneficiary.UserId, beneficiary.RecipientUsername);
                if (existing != null)
                    throw new InvalidOperationException("Beneficiary with this username already exists");
            }

            await _repo.SaveAsync(beneficiary);
        }

        public async Task UpdateAsync(Beneficiary beneficiary)
        {
            var existing = await _repo.FindByIdAsync(beneficiary.Id, beneficiary.UserId)
                ?? throw new KeyNotFoundException("Beneficiary not found");

            await _repo.UpdateAsync(beneficiary);
        }

        public async Task DeleteAsync(uint id, uint userId)
        {
            _ = await _repo.FindByIdAsync(id, userId)
                ?? throw new KeyNotFoundException("Beneficiary not found");

            await _repo.DeleteAsync(id, userId);
        }

        public async Task DeleteByIdsAsync(List<uint> ids, uint userId)
        {
            if (ids.Count == 0)
                throw new ArgumentException("No beneficiary IDs provided");

            await _repo.DeleteByIdsAsync(ids, userId);
        }
    }
}