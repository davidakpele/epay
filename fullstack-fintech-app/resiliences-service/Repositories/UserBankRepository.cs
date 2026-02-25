using Microsoft.EntityFrameworkCore;
using resiliences_service.Configs;
using resiliences_service.Models;
using resiliences_service.interfaces;

namespace resiliences_service.Repositories
{
    public class UserBankRepository : IUserBankRepository
    {
        private readonly AppDbContext _context;

        public UserBankRepository(AppDbContext context)
        {
            _context = context;
        }

        public async Task CreateAsync(UserBankList bank)
        {
            _context.UserBankLists.Add(bank);
            await _context.SaveChangesAsync();
        }

        public async Task<UserBankList?> FindByIdAsync(uint id)
        {
            return await _context.UserBankLists.FirstOrDefaultAsync(b => b.Id == id);
        }

        public async Task<UserBankList?> FindByAccountNumberAsync(string accountNumber)
        {
            return await _context.UserBankLists
                .FirstOrDefaultAsync(b => b.AccountNumber == accountNumber);
        }

        public async Task<List<UserBankList>> FindByUserIdAsync(uint userId)
        {
            return await _context.UserBankLists
                .Where(b => b.UserId == userId)
                .ToListAsync();
        }

        public async Task DeleteByIdsAsync(List<uint> ids)
        {
            var banks = await _context.UserBankLists
                .Where(b => ids.Contains(b.Id))
                .ToListAsync();

            _context.UserBankLists.RemoveRange(banks);
            await _context.SaveChangesAsync();
        }

        public async Task<bool> FindByAccountNumberAndBankCodeAsync(string accountNumber, string bankCode)
        {
            return await _context.UserBankLists
                .AnyAsync(b => b.AccountNumber == accountNumber && b.BankCode == bankCode);
        }

        public async Task<bool> FindByAccountNumberAndBankNameAsync(string accountNumber, string bankName)
        {
            return await _context.UserBankLists
                .AnyAsync(b => b.AccountNumber == accountNumber && b.BankName == bankName);
        }

        public async Task<UserBankList?> FindInternalAsync(string accountNumber, string bankCode)
        {
            return await _context.UserBankLists
                .FirstOrDefaultAsync(b => b.AccountNumber == accountNumber && b.BankCode == bankCode);
        }
    }
}