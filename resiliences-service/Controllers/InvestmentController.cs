using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using resiliences_service.Enums;
using resiliences_service.interfaces;
using resiliences_service.Payloads;

namespace resiliences_service.Controllers
{
    [ApiController]
    [Route("investments")]
    [Authorize]
    public class InvestmentController : ControllerBase
    {
        private readonly IInvestmentService            _service;
        private readonly ILogger<InvestmentController> _logger;

        public InvestmentController(IInvestmentService service, ILogger<InvestmentController> logger)
        {
            _service = service;
            _logger  = logger;
        }

        // GET /investments/plans
        // Returns all available investment plans with current rates.
        [HttpGet("plans")]
        public IActionResult GetPlans()
        {
            var plans = new[]
            {
                new { duration = "WEEKLY",    label = "1 Week",    annualRate = 8m,  durationDays = 7   },
                new { duration = "MONTHLY",   label = "1 Month",   annualRate = 12m, durationDays = 30  },
                new { duration = "QUARTERLY", label = "3 Months",  annualRate = 18m, durationDays = 90  },
                new { duration = "YEARLY",    label = "12 Months", annualRate = 24m, durationDays = 365 }
            };

            return Ok(new { status = "success", data = plans });
        }

        // POST /investments/calculate
        // Preview returns for a given principal + duration before committing.
        [HttpPost("calculate")]
        public async Task<IActionResult> Calculate([FromBody] CalculateInvestmentRequest req)
        {
            if (!ModelState.IsValid)
                return BadRequest(new { message = "Invalid request payload" });

            try
            {
                var result = await _service.CalculateReturnsAsync(req.Principal, req.Duration, req.CurrencyCode);
                return Ok(new { status = "success", data = result });
            }
            catch (ArgumentException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[InvestmentController] Calculate failed");
                return StatusCode(500, new { message = "Failed to calculate investment returns" });
            }
        }

        // POST /investments/create
        // Create a new investment — deducts principal from wallet immediately.
        [HttpPost("create")]
        public async Task<IActionResult> Create([FromBody] CreateInvestmentRequest req)
        {
            if (!ModelState.IsValid)
                return BadRequest(new { message = "Invalid request payload" });

            try
            {
                var investment = await _service.CreateInvestmentAsync(req);
                return StatusCode(201, new
                {
                    status  = "success",
                    message = "Investment created successfully. Funds are locked until maturity.",
                    data    = investment
                });
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[InvestmentController] Create failed");
                return StatusCode(500, new { message = "Failed to create investment" });
            }
        }

        // GET /investments/user/{userId}
        // Get all investments for the authenticated user.
        [HttpGet("user/{userId:long}")]
        public async Task<IActionResult> GetByUserId(long userId)
        {
            try
            {
                var investments = await _service.GetByUserIdAsync(userId);
                return Ok(new { status = "success", data = investments });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[InvestmentController] GetByUserId failed");
                return StatusCode(500, new { message = "Failed to retrieve investments" });
            }
        }

        // GET /investments/{id}?userId=
        // Get a single investment by ID (user-scoped).
        [HttpGet("{id:long}")]
        public async Task<IActionResult> GetById(long id, [FromQuery] long userId)
        {
            try
            {
                var investment = await _service.GetByIdAsync(id, userId);
                if (investment == null)
                    return NotFound(new { message = "Investment not found" });

                return Ok(new { status = "success", data = investment });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[InvestmentController] GetById failed");
                return StatusCode(500, new { message = "Failed to retrieve investment" });
            }
        }
    }
}
