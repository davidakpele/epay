using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using resiliences_service.interfaces;
using resiliences_service.Models;
using resiliences_service.Payloads;

namespace resiliences_service.Controllers
{
    [ApiController]
    [Route("bank")]
    [Authorize]
    public class UserBankController : ControllerBase
    {
        private readonly IUserBankService _service;
        private readonly ILogger<UserBankController> _logger;

        public UserBankController(IUserBankService service, ILogger<UserBankController> logger)
        {
            _service = service;
            _logger  = logger;
        }

        [HttpPost("create")]
        public async Task<IActionResult> CreateBank([FromBody] CreateBankPayload payload)
        {
            if (!ModelState.IsValid)
                return BadRequest(new { message =  "Invalid request payload" });

            if (string.IsNullOrEmpty(payload.BankCode))
                return BadRequest(new { message =  "Bank code is required" });
            if (string.IsNullOrEmpty(payload.BankName))
                return BadRequest(new { message =  "Bank name is required" });
            if (string.IsNullOrEmpty(payload.AccountHolderName))
                return BadRequest(new { message =  "Account holder name is required" });
            if (string.IsNullOrEmpty(payload.AccountNumber))
                return BadRequest(new { message =  "Account number is required" });

            try
            {
                var exists = await _service.FindByAccountNumberAndBankNameAsync(
                    payload.AccountNumber, payload.BankName);

                if (exists)
                    return BadRequest(new { message =  "This bank details is already registered to a user in this platform" });

                var bank = new UserBankList
                {
                    BankCode          = payload.BankCode,
                    BankName          = payload.BankName,
                    AccountHolderName = payload.AccountHolderName,
                    AccountNumber     = payload.AccountNumber,
                    UserId            = payload.UserId
                };

                await _service.CreateBankAsync(bank);
                return StatusCode(201, new { status = 201, message = "Bank created successfully" });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[UserBankController] CreateBank failed");
                return StatusCode(500, new { message =  "Failed to create bank" });
            }
        }

        [HttpGet("bank-list")]
        public async Task<IActionResult> FetchAllBanks()
        {
            try
            {
                var banks = await _service.FetchAllBanksAsync();
                return Ok(banks);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[UserBankController] FetchAllBanks failed");
                return StatusCode(500, new { message =  "Failed to fetch bank list" });
            }
        }

        [HttpGet("{id}")]
        public async Task<IActionResult> GetById(uint id)
        {
            var bank = await _service.FindByIdAsync(id);
            return bank == null
                ? NotFound(new { message =  "Bank not found" })
                : Ok(new { message = "Success", data = bank });
        }

        [HttpGet("account/{accountNumber}")]
        public async Task<IActionResult> GetByAccountNumber(string accountNumber)
        {
            var bank = await _service.FindByAccountNumberAsync(accountNumber);
            return bank == null
                ? NotFound(new { message =  "Bank not found" })
                : Ok(new { message = "Success", data = bank });
        }

        [HttpGet("users/{id}")]
        public async Task<IActionResult> GetByUserId(uint id)
        {
            try
            {
                var banks = await _service.FindByUserIdAsync(id);
                return Ok(new { message = "Success", data = banks });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[UserBankController] GetByUserId failed");
                return StatusCode(500, new { message =  "Failed to fetch banks" });
            }
        }

        [HttpDelete]
        public async Task<IActionResult> DeleteByIds([FromBody] DeleteBanksPayload payload)
        {
            if (payload.Ids == null || payload.Ids.Count == 0)
                return BadRequest(new { message =  "Provide a valid list of IDs for deletion" });

            try
            {
                await _service.DeleteByIdsAsync(payload.Ids);
                return Ok(new { message = "Banks deleted successfully" });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[UserBankController] DeleteByIds failed");
                return StatusCode(500, new { message =  "Failed to delete banks" });
            }
        }

        [HttpGet("verify/internal")]
        public async Task<IActionResult> VerifyInternal(
            [FromQuery] string accountNumber,
            [FromQuery] string bankCode)
        {
            try
            {
                var account = await _service.FindInternalAsync(accountNumber, bankCode);
                if (account == null)
                    return NotFound(new { message =  "Account not found internally" });

                return Ok(new
                {
                    status  = true,
                    message = "Account number resolved",
                    data    = new
                    {
                        accountNumber = account.AccountNumber,
                        accountName   = account.AccountHolderName,
                        bankId        = account.Id
                    }
                });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[UserBankController] VerifyInternal failed");
                return StatusCode(500, new { message =  "Error verifying bank account" });
            }
        }

        [HttpGet("verify-user-bank-details")]
        public async Task<IActionResult> VerifyExternal(
            [FromQuery] string accountNumber,
            [FromQuery] string bankCode)
        {
            try
            {
                var result = await _service.VerifyExternalAsync(accountNumber, bankCode);
                return Ok(new { status = true, message = "Account number resolved", data = result });
            }
            catch (Exception)
            {
                return StatusCode(500, new { message =  "Error verifying bank account" });
            }
        }
    }
}