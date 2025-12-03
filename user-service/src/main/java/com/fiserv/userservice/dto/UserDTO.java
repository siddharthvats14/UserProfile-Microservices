package com.fiserv.userservice.dto;

public class UserDTO {
    private Integer userId;
    private String loginName;
    private String password;
    private Integer personId;
    private Integer retry;
    private Long lockedUntil; // Timestamp when account will be unlocked

    // Getters and Setters
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getLoginName() { return loginName; }
    public void setLoginName(String loginName) { this.loginName = loginName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Integer getPersonId() { return personId; }
    public void setPersonId(Integer personId) { this.personId = personId; }
    public Integer getRetry() { return retry; }
    public void setRetry(Integer retry) { this.retry = retry; }
    public Long getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Long lockedUntil) { this.lockedUntil = lockedUntil; }
}
