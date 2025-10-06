package com.fiserv.contactservice.dto;

public class ContactDTO {
    private Integer contactId;
    private String primaryMobileNumber;
    private String secondaryMobileNumber;
    private String primaryEmail;
    private String secondaryEmail;

    // Getters and Setters
    public Integer getContactId() { return contactId; }
    public void setContactId(Integer contactId) { this.contactId = contactId; }
    public String getPrimaryMobileNumber() { return primaryMobileNumber; }
    public void setPrimaryMobileNumber(String primaryMobileNumber) { this.primaryMobileNumber = primaryMobileNumber; }
    public String getSecondaryMobileNumber() { return secondaryMobileNumber; }
    public void setSecondaryMobileNumber(String secondaryMobileNumber) { this.secondaryMobileNumber = secondaryMobileNumber; }
    public String getPrimaryEmail() { return primaryEmail; }
    public void setPrimaryEmail(String primaryEmail) { this.primaryEmail = primaryEmail; }
    public String getSecondaryEmail() { return secondaryEmail; }
    public void setSecondaryEmail(String secondaryEmail) { this.secondaryEmail = secondaryEmail; }
}
