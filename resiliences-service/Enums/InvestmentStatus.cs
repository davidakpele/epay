namespace resiliences_service.Enums
{
    public enum InvestmentStatus
    {
        ACTIVE,   // Investment is locked and running
        MATURED,  // Investment period ended — payout due
        PAID_OUT, // Principal + returns credited back to wallet
        FAILED    // Payout attempt failed (manual intervention needed)
    }
}
