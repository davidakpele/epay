package com.example.auth_user_service.payloads;

import jakarta.validation.constraints.*;

public class UpdateProfilePayload {

    @NotBlank(message = "Date of birth is required")
    private String dob;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^(male|female|other|prefer-not-to-say)$", message = "Gender must be male, female, other, or prefer-not-to-say")
    private String gender;

    @NotBlank(message = "City is required")
    private String city;

    private String address; 

    @NotBlank(message = "Country is required")
    private String country;

    private String state; 

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Please enter a valid phone number")
    private String telephone;

    @NotBlank(message = "BVN is required")
    @Size(min = 11, max = 11, message = "BVN must be exactly 11 digits")
    @Pattern(regexp = "^[0-9]{11}$", message = "BVN must contain only digits")
    private String bvn;


    public UpdateProfilePayload() {
    }

    public UpdateProfilePayload(String dob, String email, String firstName, String lastName, String gender, String city, String address, String country, String state, String telephone, String bvn) {
        this.dob = dob;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.city = city;
        this.address = address;
        this.country = country;
        this.state = state;
        this.telephone = telephone;
        this.bvn = bvn;
    }

    public String getDob() { return this.dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return this.firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return this.lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getGender() { return this.gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getCity() { return this.city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return this.address; }
    public void setAddress(String address) { this.address = address; }

    public String getCountry() { return this.country; }
    public void setCountry(String country) { this.country = country; }

    public String getState() { return this.state; }
    public void setState(String state) { this.state = state; }

    public String getTelephone() { return this.telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getBvn() { return this.bvn; }
    public void setBvn(String bvn) { this.bvn = bvn; }
}