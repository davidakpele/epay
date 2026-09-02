package com.epay.domain.notification.input;


import lombok.Data;

@Data
public class BlockUserWallet {
    private String email;
    private String firstName;
    private String lastName;
    private String message;

    public BlockUserWallet() {
    }

    public BlockUserWallet(String email, String firstName, String lastName, String message) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.message = message;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
}
