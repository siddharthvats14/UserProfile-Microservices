package com.fiserv.personservice.dto;

public class PersonDTO {
    private Integer personId;
    private String firstName;
    private String lastName;
    private Integer roleId;
    private Integer addressId;
    private Integer contactId;
    private Integer age;

    // Getters and Setters
    public Integer getPersonId() { return personId; }
    public void setPersonId(Integer personId) { this.personId = personId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
    public Integer getAddressId() { return addressId; }
    public void setAddressId(Integer addressId) { this.addressId = addressId; }
    public Integer getContactId() { return contactId; }
    public void setContactId(Integer contactId) { this.contactId = contactId; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}
