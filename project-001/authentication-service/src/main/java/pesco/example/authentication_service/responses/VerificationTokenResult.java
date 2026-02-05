package pesco.example.authentication_service.responses;

import lombok.Data;

@Data
public class VerificationTokenResult {
    private boolean success;
    private Object data;


    public VerificationTokenResult() {
    }

    public VerificationTokenResult(boolean success, Object data) {
        this.success = success;
        this.data = data;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public boolean getSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }


}
