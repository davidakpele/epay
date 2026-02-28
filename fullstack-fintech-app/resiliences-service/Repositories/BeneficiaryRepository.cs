using Microsoft.EntityFrameworkCore;
using resiliences_service.Configs;
using resiliences_service.Models;
using resiliences_service.Enums;
using resiliences_service.interfaces;

namespace resiliences_service.Repositories
{
    public class BeneficiaryRepository : IBeneficiaryRepository
    {
        private readonly AppDbContext _context;

        public BeneficiaryRepository(AppDbContext context)
        {
            _context = context;
        }

        public async Task<Beneficiary?> FindByIdAsync(uint id, uint userId)
        {
            return await _context.Beneficiaries
                .FirstOrDefaultAsync(b => b.Id == id && b.UserId == userId && b.IsActive);
        }

        public async Task<Beneficiary?> FindByUserIdAsync(uint userId)
        {
            return await _context.Beneficiaries
                .FirstOrDefaultAsync(b => b.UserId == userId && b.IsActive);
        }

        public async Task<List<Beneficiary>> FindAllByUserIdAsync(uint userId)
        {
            return await _context.Beneficiaries
                .Where(b => b.UserId == userId && b.IsActive)
                .OrderBy(b => b.BeneficiaryName)
                .ToListAsync();
        }

        public async Task<List<Beneficiary>> FindByTypeAsync(uint userId, BeneficiaryType type)
        {
            return await _context.Beneficiaries
                .Where(b => b.UserId == userId && b.BeneficiaryType == type && b.IsActive)
                .OrderBy(b => b.BeneficiaryName)
                .ToListAsync();
        }

        public async Task<List<Beneficiary>> SearchAsync(uint userId, string searchTerm)
        {
            var pattern = searchTerm.ToLower();
            return await _context.Beneficiaries
                .Where(b => b.UserId == userId && b.IsActive && (
                    b.BeneficiaryName.ToLower().Contains(pattern) ||
                    (b.AccountNumber != null && b.AccountNumber.Contains(pattern)) ||
                    (b.RecipientUsername != null && b.RecipientUsername.ToLower().Contains(pattern))
                ))
                .OrderBy(b => b.BeneficiaryName)
                .ToListAsync();
        }

        public async Task<Beneficiary?> FindBankByAccountNumberAsync(uint userId, string accountNumber)
        {
            return await _context.Beneficiaries
                .FirstOrDefaultAsync(b =>
                    b.UserId == userId &&
                    b.BeneficiaryType == BeneficiaryType.bank &&
                    b.AccountNumber == accountNumber &&
                    b.IsActive);
        }

        public async Task<Beneficiary?> FindUserByUsernameAsync(uint userId, string username)
        {
            return await _context.Beneficiaries
                .FirstOrDefaultAsync(b =>
                    b.UserId == userId &&
                    b.BeneficiaryType == BeneficiaryType.user &&
                    b.RecipientUsername == username &&
                    b.IsActive);
        }

        public async Task SaveAsync(Beneficiary beneficiary)
        {
            _context.Beneficiaries.Add(beneficiary);
            await _context.SaveChangesAsync();
        }

        public async Task UpdateAsync(Beneficiary beneficiary)
        {
            _context.Beneficiaries.Update(beneficiary);
            await _context.SaveChangesAsync();
        }

        public async Task DeleteAsync(uint id, uint userId)
        {
            var beneficiary = await FindByIdAsync(id, userId);
            if (beneficiary != null)
            {
                beneficiary.IsActive = false;
                await _context.SaveChangesAsync();
            }
        }

        public async Task DeleteByIdsAsync(List<uint> ids, uint userId)
        {
            var beneficiaries = await _context.Beneficiaries
                .Where(b => ids.Contains(b.Id) && b.UserId == userId)
                .ToListAsync();

            foreach (var b in beneficiaries)
                b.IsActive = false;

            await _context.SaveChangesAsync();
        }

        public async Task HardDeleteAsync(uint id, uint userId)
        {
            var beneficiary = await FindByIdAsync(id, userId);
            if (beneficiary != null)
            {
                _context.Beneficiaries.Remove(beneficiary);
                await _context.SaveChangesAsync();
            }
        }
    }
}