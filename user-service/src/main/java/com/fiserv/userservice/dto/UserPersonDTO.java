package com.fiserv.userservice.dto;

public class UserPersonDTO {
    private UserDTO user;
    private PersonDTO person;

    public UserDTO getUser() {
        return user;
    }
    public void setUser(UserDTO user) {
        this.user = user;
    }
    public PersonDTO getPerson() {
        return person;
    }
    public void setPerson(PersonDTO person) {
        this.person = person;
    }
}
