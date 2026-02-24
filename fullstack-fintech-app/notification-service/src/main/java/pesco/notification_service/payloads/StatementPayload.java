package pesco.notification_service.payloads;

public class StatementPayload {
    private String email;
    private String username;
    private byte[] pdfBytes;
    private String period;


    public StatementPayload(String email, String username, byte[] pdfBytes, String period) {
        this.email = email;
        this.username = username;
        this.pdfBytes = pdfBytes;
        this.period = period;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getPdfBytes() {
        return this.pdfBytes;
    }

    public void setPdfBytes(byte[] pdfBytes) {
        this.pdfBytes = pdfBytes;
    }

    public String getPeriod() {
        return this.period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

}
