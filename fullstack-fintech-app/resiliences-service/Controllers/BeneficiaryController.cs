using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using resiliences_service.Enums;
using resiliences_service.interfaces;
using resiliences_service.Models;
using resiliences_service.Payloads;

namespace resiliences_service.Controllers
{
    [ApiController]
    [Route("beneficiaries")]
    [Authorize]
    public class BeneficiaryController : ControllerBase
    {
        private readonly IBeneficiaryService            _service;
        private readonly ILogger<BeneficiaryController> _logger;

        public BeneficiaryController(IBeneficiaryService service, ILogger<BeneficiaryController> logger)
        {
            _service = service;
            _logger  = logger;
        }

        // POST /beneficiary/create
        [HttpPost("create")]
        public async Task<IActionResult> Create([FromBody] CreateBeneficiaryRequest req)
        {
            if (!ModelState.IsValid)
                return BadRequest(new { message =  "Invalid request payload" });

            if (req.BeneficiaryType == BeneficiaryType.bank)
            {
                if (string.IsNullOrEmpty(req.AccountNumber))
                    return BadRequest(new { message =  "accountNumber is required for bank beneficiaries" });
                if (string.IsNullOrEmpty(req.AccountName))
                    return BadRequest(new { message =  "accountName is required for bank beneficiaries" });
                if (string.IsNullOrEmpty(req.BankCode))
                    return BadRequest(new { message =  "bankCode is required for bank beneficiaries" });
                if (string.IsNullOrEmpty(req.BankName))
                    return BadRequest(new { message =  "bankName is required for bank beneficiaries" });
            }
            else if (req.BeneficiaryType == BeneficiaryType.user)
            {
                if (string.IsNullOrEmpty(req.RecipientUsername))
                    return BadRequest(new { message =  "recipientUsername is required for user beneficiaries" });
            }

            var beneficiary = new Beneficiary
            {
                UserId          = req.UserId,
                BeneficiaryType = req.BeneficiaryType,
                BeneficiaryName = req.BeneficiaryName,
                Currency        = req.Currency,
                IsActive        = true
            };

            if (req.BeneficiaryType == BeneficiaryType.bank)
            {
                beneficiary.AccountNumber     = req.AccountNumber;
                beneficiary.AccountName       = req.AccountName;
                beneficiary.BankCode          = req.BankCode;
                beneficiary.BankName          = req.BankName;
                beneficiary.RecipientUsername = null;
            }
            else
            {
                beneficiary.RecipientUsername = req.RecipientUsername;
                beneficiary.AccountNumber     = null;
                beneficiary.AccountName       = null;
                beneficiary.BankCode          = null;
                beneficiary.BankName          = null;
            }

            try
            {
                await _service.CreateAsync(beneficiary);
                return StatusCode(201, new { status = "success", message = "Beneficiary created successfully" });
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(new { message =  ex.Message });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[BeneficiaryController] Create failed");
                return StatusCode(500, new { message =  "Failed to create beneficiary" });
            }
        }

        // GET /beneficiaries/{id}
        [HttpGet("{id}")]
        public async Task<IActionResult> GetById(uint id)
        {
            try
            {
                var beneficiary = await _service.GetByUserIdAsync(id);
                if (beneficiary == null)
                    return NotFound(new { message =  "Beneficiary not found" });

                return Ok(new { status = "success", message = "Beneficiary retrieved successfully", data = beneficiary });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[BeneficiaryController] GetById failed");
                return StatusCode(500, new { message =  "Failed to fetch beneficiary" });
            }
        }

        // GET /beneficiaries/{userId}/all
        [HttpGet("{userId}/all")]
        public async Task<IActionResult> GetAllByUserId(uint userId)
        {
            try
            {
                var beneficiaries = await _service.GetAllByUserIdAsync(userId);
                return Ok(new { status = "success", message = "Beneficiaries retrieved successfully", data = beneficiaries });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[BeneficiaryController] GetAllByUserId failed");
                return StatusCode(500, new { message =  "Failed to fetch beneficiaries" });
            }
        }

        // GET /beneficiaries/{userId}/type?type=bank
        [HttpGet("{userId}/type")]
        public async Task<IActionResult> GetByType(uint userId, [FromQuery] string type)
        {
            if (!Enum.TryParse<BeneficiaryType>(type, true, out var beneficiaryType) ||
                (beneficiaryType != BeneficiaryType.bank && beneficiaryType != BeneficiaryType.user))
                return BadRequest(new { message =  "Type must be 'bank' or 'user'" });

            try
            {
                var beneficiaries = await _service.GetByTypeAsync(userId, beneficiaryType);
                return Ok(new { status = "success", message = "Beneficiaries retrieved successfully", data = beneficiaries });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[BeneficiaryController] GetByType failed");
                return StatusCode(500, new { message =  "Failed to fetch beneficiaries" });
            }
        }

        // GET /beneficiaries/{userId}/search?search=query
        [HttpGet("{userId}/search")]
        public async Task<IActionResult> Search(uint userId, [FromQuery] string search)
        {
            if (string.IsNullOrEmpty(search))
                return BadRequest(new { message =  "Provide a search query parameter" });

            try
            {
                var beneficiaries = await _service.SearchAsync(userId, search);
                return Ok(new { status = "success", message = "Search completed successfully", data = beneficiaries });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[BeneficiaryController] Search failed");
                return StatusCode(500, new { message =  "Failed to search beneficiaries" });
            }
        }

        // GET /beneficiaries/{id}/verify
        [HttpGet("{id}/verify")]
        public async Task<IActionResult> Verify(uint id)
        {
            try
            {
                var beneficiary = await _service.GetByUserIdAsync(id);
                if (beneficiary == null)
                    return NotFound(new { message =  "Beneficiary not found" });

                return Ok(new { status = "success", message = "Beneficiary verified", data = beneficiary });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[BeneficiaryController] Verify failed");
                return StatusCode(500, new { message =  "Failed to verify beneficiary" });
            }
        }

        // PUT /beneficiaries/{id}
        [HttpPut("{id}")]
        public async Task<IActionResult> Update(uint id, [FromBody] CreateBeneficiaryRequest req)
        {
            if (!ModelState.IsValid)
                return BadRequest(new { message =  "Invalid request payload" });

            var beneficiary = new Beneficiary
            {
                Id              = id,
                UserId          = req.UserId,
                BeneficiaryType = req.BeneficiaryType,
                BeneficiaryName = req.BeneficiaryName,
                Currency        = req.Currency
            };

            if (req.BeneficiaryType == BeneficiaryType.bank)
            {
                beneficiary.AccountNumber     = req.AccountNumber;
                beneficiary.AccountName       = req.AccountName;
                beneficiary.BankCode          = req.BankCode;
                beneficiary.BankName          = req.BankName;
                beneficiary.RecipientUsername = null;
            }
            else
            {
                beneficiary.RecipientUsername = req.RecipientUsername;
                beneficiary.AccountNumber     = null;
                beneficiary.AccountName       = null;
                beneficiary.BankCode          = null;
                beneficiary.BankName          = null;
            }

            try
            {
                await _service.UpdateAsync(beneficiary);
                return Ok(new { status = "success", message = "Beneficiary updated successfully" });
            }
            catch (KeyNotFoundException ex)
            {
                return NotFound(new { message =  ex.Message });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[BeneficiaryController] Update failed");
                return StatusCode(500, new { message =  "Failed to update beneficiary" });
            }
        }

        // DELETE /beneficiaries/{id}
        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(uint id, [FromQuery] uint userId)
        {
            try
            {
                await _service.DeleteAsync(id, userId);
                return Ok(new { status = "success", message = "Beneficiary deleted successfully" });
            }
            catch (KeyNotFoundException ex)
            {
                return NotFound(new { message =  ex.Message });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[BeneficiaryController] Delete failed");
                return StatusCode(500, new { message =  "Failed to delete beneficiary" });
            }
        }

        // DELETE /beneficiaries/bulk
        [HttpDelete("bulk")]
        public async Task<IActionResult> DeleteByIds([FromBody] DeleteBeneficiariesRequest req)
        {
            if (req.Ids == null || req.Ids.Count == 0)
                return BadRequest(new { message =  "Provide a valid list of IDs" });

            try
            {
                await _service.DeleteByIdsAsync(req.Ids, req.UserId);
                return Ok(new { status = "success", message = "Beneficiaries deleted successfully" });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[BeneficiaryController] DeleteByIds failed");
                return StatusCode(500, new { message =  "Failed to delete beneficiaries" });
            }
        }

        // GET /beneficiaries/{userId}/username/{recipientUsername}
        [HttpGet("{userId}/username/{recipientUsername}")]
        public async Task<IActionResult> CheckUserBeneficiary(uint userId, string recipientUsername)
        {
            if (string.IsNullOrWhiteSpace(recipientUsername))
                return BadRequest(new { message =  "recipientUsername is required" });

            try
            {
                var beneficiary = await _service.GetByUserIdAndUsernameAsync(userId, recipientUsername);
                
                if (beneficiary == null)
                    return NotFound(new { message =  "Beneficiary not found" });

                return Ok(new
                {
                    status = "success",
                    message = "Beneficiary retrieved successfully",
                    data = beneficiary
                });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[BeneficiaryController] CheckUserBeneficiary failed");
                return StatusCode(500, new { message =  "Failed to check beneficiary" });
            }
        }

    }
}