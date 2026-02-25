using Microsoft.AspNetCore.Mvc;
using resiliences_service.DTOs;
using resiliences_service.interfaces;
using resiliences_service.Payloads;
using resiliences_service.Resopones;
using resiliences_service.Services;

namespace resiliences_service.Controllers
{
    [ApiController]
    [Route("api/blacklist")]
    public class BlackListedWalletController : ControllerBase
    {
        private readonly IBlackListedWalletService _service;

        public BlackListedWalletController(IBlackListedWalletService service)
        {
            _service = service;
        }

        [HttpGet]
        public IActionResult DefaultHome()
        {
            return Ok(ApiResponse.Success("Welcome to the Blacklisted Wallet Service!"));
        }

        [HttpGet("check/{walletId}")]
        public async Task<IActionResult> CheckWalletBlacklistStatus(uint walletId)
        {
            try
            {
                var isBlacklisted = await _service.IsWalletBlacklistedAsync(walletId);
                if (!isBlacklisted)
                    return NotFound(ApiResponse.NotFound($"Wallet {walletId} is not blacklisted."));

                var wallet = await _service.GetBlacklistedWalletAsync(walletId);
                return Ok(ApiResponse.Success(wallet));
            }
            catch (Exception)
            {
                return StatusCode(500, ApiResponse.ServerError());
            }
        }

        [HttpDelete("{walletId}")]
        public async Task<IActionResult> RemoveBlacklistedWallet(uint walletId)
        {
            try
            {
                var exists = await _service.IsWalletBlacklistedAsync(walletId);
                if (!exists)
                    return NotFound(ApiResponse.NotFound($"Wallet {walletId} is not blacklisted."));

                await _service.RemoveBlacklistedWalletAsync(walletId);
                return Ok(ApiResponse.Success("Wallet removed from blacklist."));
            }
            catch (Exception)
            {
                return StatusCode(500, ApiResponse.ServerError());
            }
        }

        [HttpPost]
        public async Task<IActionResult> AddToBlackList([FromBody] AddToBlackListRequest request)
        {
            if (!ModelState.IsValid)
                return BadRequest(ApiResponse.Error("Invalid request payload."));

            try
            {
                var exists = await _service.IsWalletBlacklistedAsync(request.WalletId);
                if (exists)
                    return Conflict(ApiResponse.Error($"Wallet {request.WalletId} is already blacklisted."));

                await _service.AddToBlackListAsync(request.WalletId, request.Reason, request.IsBlock);
                return StatusCode(201, ApiResponse.Success("Wallet added to blacklist."));
            }
            catch (Exception)
            {
                return StatusCode(500, ApiResponse.ServerError());
            }
        }

        [HttpGet("count")]
        public async Task<IActionResult> CountBlacklistedWallets()
        {
            try
            {
                var count = await _service.CountBlacklistedWalletsAsync();
                return Ok(ApiResponse.Success(count));
            }
            catch (Exception)
            {
                return StatusCode(500, ApiResponse.ServerError());
            }
        }
    }
}