using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using resiliences_service.interfaces;
using resiliences_service.Payloads;

namespace resiliences_service.Controllers
{
    [ApiController]
    [Route("savings")]
    [Authorize]
    public class TargetSavingsController : ControllerBase
    {
        private readonly ITargetSavingsService            _service;
        private readonly ILogger<TargetSavingsController> _logger;

        public TargetSavingsController(ITargetSavingsService service, ILogger<TargetSavingsController> logger)
        {
            _service = service;
            _logger  = logger;
        }

        // POST /savings/create
        // Create a new savings goal (no initial deposit).
        [HttpPost("create")]
        public async Task<IActionResult> Create([FromBody] CreateTargetSavingsRequest req)
        {
            if (!ModelState.IsValid)
                return BadRequest(new { message = "Invalid request payload" });

            try
            {
                var savings = await _service.CreateGoalAsync(req);
                return StatusCode(201, new
                {
                    status  = "success",
                    message = $"Savings goal \"{savings.GoalName}\" created successfully.",
                    data    = savings
                });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[TargetSavingsController] Create failed");
                return StatusCode(500, new { message = "Failed to create savings goal" });
            }
        }

        // POST /savings/{id}/topup
        // Deposit funds from the main wallet into a savings goal.
        [HttpPost("{id:long}/topup")]
        public async Task<IActionResult> TopUp(long id, [FromBody] TopUpTargetSavingsRequest req)
        {
            if (!ModelState.IsValid)
                return BadRequest(new { message = "Invalid request payload" });

            try
            {
                var savings = await _service.TopUpAsync(id, req);
                return Ok(new
                {
                    status  = "success",
                    message = $"Successfully added funds to \"{savings.GoalName}\".",
                    data    = savings
                });
            }
            catch (KeyNotFoundException ex)
            {
                return NotFound(new { message = ex.Message });
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[TargetSavingsController] TopUp failed for savings {Id}", id);
                return StatusCode(500, new { message = "Failed to top up savings goal" });
            }
        }

        // POST /savings/{id}/withdraw
        // Withdraw all accumulated savings back to the main wallet.
        [HttpPost("{id:long}/withdraw")]
        public async Task<IActionResult> Withdraw(long id, [FromBody] WithdrawTargetSavingsRequest req)
        {
            if (!ModelState.IsValid)
                return BadRequest(new { message = "Invalid request payload" });

            try
            {
                var savings = await _service.WithdrawAsync(id, req);
                return Ok(new
                {
                    status  = "success",
                    message = $"Funds from \"{savings.GoalName}\" have been returned to your wallet.",
                    data    = savings
                });
            }
            catch (KeyNotFoundException ex)
            {
                return NotFound(new { message = ex.Message });
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[TargetSavingsController] Withdraw failed for savings {Id}", id);
                return StatusCode(500, new { message = "Failed to withdraw savings" });
            }
        }

        // GET /savings/user/{userId}
        // Retrieve all savings goals for a user.
        [HttpGet("user/{userId:long}")]
        public async Task<IActionResult> GetByUserId(long userId)
        {
            try
            {
                var goals = await _service.GetByUserIdAsync(userId);
                return Ok(new { status = "success", data = goals });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[TargetSavingsController] GetByUserId failed");
                return StatusCode(500, new { message = "Failed to retrieve savings goals" });
            }
        }

        // GET /savings/{id}?userId=
        // Get a single savings goal by ID.
        [HttpGet("{id:long}")]
        public async Task<IActionResult> GetById(long id, [FromQuery] long userId)
        {
            try
            {
                var savings = await _service.GetByIdAsync(id, userId);
                if (savings == null)
                    return NotFound(new { message = "Savings goal not found" });

                return Ok(new { status = "success", data = savings });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[TargetSavingsController] GetById failed");
                return StatusCode(500, new { message = "Failed to retrieve savings goal" });
            }
        }

        // DELETE /savings/{id}?userId=
        // Cancel a savings goal (only if no funds remain).
        [HttpDelete("{id:long}")]
        public async Task<IActionResult> Cancel(long id, [FromQuery] long userId)
        {
            try
            {
                await _service.CancelAsync(id, userId);
                return Ok(new { status = "success", message = "Savings goal cancelled." });
            }
            catch (KeyNotFoundException ex)
            {
                return NotFound(new { message = ex.Message });
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[TargetSavingsController] Cancel failed for savings {Id}", id);
                return StatusCode(500, new { message = "Failed to cancel savings goal" });
            }
        }
    }
}
