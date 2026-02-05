package pesco.example.authentication_service.payloads;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OTPRequest {
    private String otp;


    public OTPRequest() {
    }

    public OTPRequest(String otp) {
        this.otp = otp;
    }

    public String getOtp() {
        return this.otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

}