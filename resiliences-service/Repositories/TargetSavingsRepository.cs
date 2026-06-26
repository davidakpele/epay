using Microsoft.EntityFrameworkCore;
using resiliences_service.Configs;
using resiliences_service.Enums;
using resiliences_service.interfaces;
using resiliences_service.Models;

namespace resiliences_service.Repositories
{
    public class TargetSavingsRepository : ITargetSavingsRepository
    {
        private readonly AppDbContext _context;

        public TargetSavingsRepository(AppDbContext context)
        {
            _context = context;
        }

        public async Task<TargetSavings> CreateAsync(TargetSavings savings)
        {
            _context.TargetSavings.Add(savings);
            await _context.SaveChangesAsync();
            return savings;
        }

        public async Task<TargetSavings?> FindByIdAsync(long id) =>
            await _context.TargetSavings.FirstOrDefaultAsync(s => s.Id == id);

        public async Task<TargetSavings?> FindByIdAndUserIdAsync(long id, long userId) =>
            await _context.TargetSavings
                .FirstOrDefaultAsync(s => s.Id == id && s.UserId == userId);

        public async Task<List<TargetSavings>> FindByUserIdAsync(long userId) =>
            await _context.TargetSavings
                .Where(s => s.UserId == userId)
                .OrderByDescending(s => s.CreatedOn)
                .ToListAsync();

        public async Task<List<TargetSavings>> FindByUserIdAndStatusAsync(long userId, TargetSavingsStatus status) =>
            await _context.TargetSavings
                .Where(s => s.UserId == userId && s.Status == status)
                .OrderByDescending(s => s.CreatedOn)
                .ToListAsync();

        public async Task<TargetSavings> UpdateAsync(TargetSavings savings)
        {
            _context.TargetSavings.Update(savings);
            await _context.SaveChangesAsync();
            return savings;
        }
    }
}
