namespace resiliences_service.Enums
{
    public enum TargetSavingsStatus
    {
        ACTIVE,    // Saving is still in progress
        COMPLETED, // Target amount reached
        WITHDRAWN, // User manually withdrew before or after target
        CANCELLED  // User cancelled the savings plan
    }
}
