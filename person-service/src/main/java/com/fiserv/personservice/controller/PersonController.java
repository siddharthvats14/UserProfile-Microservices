package com.fiserv.personservice.controller;
import com.fiserv.personservice.dto.PersonDTO;
import com.fiserv.personservice.util.PersonCsvReader;
import java.io.IOException;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/person-service")
public class PersonController {
    @org.springframework.web.bind.annotation.PostMapping(value = "/person", consumes = "application/json")
    public java.util.Map<String, Object> createPerson(@org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> data) throws IOException {
        // Generate new personId
        java.util.List<PersonDTO> persons = com.fiserv.personservice.util.PersonCsvReader.readPersonsFromCsv("person.csv");
        int maxId = 0;
        for (PersonDTO p : persons) {
            if (p.getPersonId() != null && p.getPersonId() > maxId) maxId = p.getPersonId();
        }
        int personId = maxId + 1;
        PersonDTO person = new PersonDTO();
        person.setPersonId(personId);
        person.setFirstName(data.getOrDefault("name", ""));
        person.setLastName("");
        person.setRoleId(data.containsKey("roleId") ? Integer.valueOf(data.get("roleId")) : 0);
        person.setAddressId(data.containsKey("addressId") ? Integer.valueOf(data.get("addressId")) : 0);
        person.setContactId(data.containsKey("contactId") ? Integer.valueOf(data.get("contactId")) : 0);
        person.setAge(data.containsKey("age") ? Integer.valueOf(data.get("age")) : 0);
        com.fiserv.personservice.util.PersonCsvReader.appendPersonToCsv("src/main/resources/person.csv", person);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("personId", personId);
        return result;
    }
    @GetMapping("/person/{personId}")
    public PersonDTO getPerson(@org.springframework.web.bind.annotation.PathVariable Integer personId) throws IOException {
        List<PersonDTO> persons = PersonCsvReader.readPersonsFromCsv("person.csv");
        for (PersonDTO person : persons) {
            if (person.getPersonId().equals(personId)) {
                return person;
            }
        }
        return null;
    }
}
