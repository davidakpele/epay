using Microsoft.EntityFrameworkCore;
using resiliences_service.Configs;
using resiliences_service.Enums;
using resiliences_service.interfaces;
using resiliences_service.Models;

namespace resiliences_service.Repositories
{
    public class InvestmentRepository : IInvestmentRepository
    {
        private readonly AppDbContext _context;

        public InvestmentRepository(AppDbContext context)
        {
            _context = context;
        }

        public async Task<Investment> CreateAsync(Investment investment)
        {
            _context.Investments.Add(investment);
            await _context.SaveChangesAsync();
            return investment;
        }

        public async Task<Investment?> FindByIdAsync(long id) =>
            await _context.Investments.FirstOrDefaultAsync(i => i.Id == id);

        public async Task<List<Investment>> FindByUserIdAsync(long userId) =>
            await _context.Investments
                .Where(i => i.UserId == userId)
                .OrderByDescending(i => i.CreatedOn)
                .ToListAsync();

        public async Task<List<Investment>> FindByStatusAsync(InvestmentStatus status) =>
            await _context.Investments
                .Where(i => i.Status == status)
                .ToListAsync();

        public async Task<List<Investment>> FindMaturedUnpaidAsync() =>
            await _context.Investments
                .Where(i => i.Status == InvestmentStatus.ACTIVE && i.MaturityDate <= DateTime.UtcNow)
                .ToListAsync();

        public async Task<Investment> UpdateAsync(Investment investment)
        {
            _context.Investments.Update(investment);
            await _context.SaveChangesAsync();
            return investment;
        }
    }
}
