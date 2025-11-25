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
    // Helper method for safe integer parsing
    private static int parseIntSafe(String value, int defaultValue) {
        try {
            if (value == null || value.trim().isEmpty() || value.trim().equalsIgnoreCase("null")) {
                return defaultValue;
            }
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            System.err.println("Failed to parse int from value: " + value + ", using default: " + defaultValue);
            return defaultValue;
        }
    }
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

        String lastNameValue = data.get("lastName");
        if (lastNameValue == null || lastNameValue.trim().equalsIgnoreCase("null")) {
            lastNameValue = "";
        }
        person.setLastName(lastNameValue); 
        person.setRoleId(PersonController.parseIntSafe(data.get("roleId"), 0));
        person.setAddressId(PersonController.parseIntSafe(data.get("addressId"), 0));
        person.setContactId(PersonController.parseIntSafe(data.get("contactId"), 0));
        person.setAge(PersonController.parseIntSafe(data.get("age"), 0));
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
    
    @org.springframework.web.bind.annotation.DeleteMapping("/delete/{personId}")
    public void deletePerson(@org.springframework.web.bind.annotation.PathVariable Integer personId) throws IOException {
        List<PersonDTO> persons = PersonCsvReader.readPersonsFromCsv("person.csv");
        persons.removeIf(p -> p.getPersonId().equals(personId));
        
        // Rewrite the CSV file
        java.io.File file = new java.io.File("src/main/resources/person.csv");
        try (java.io.FileWriter fw = new java.io.FileWriter(file, false)) {
            fw.write("personId,firstName,lastName,roleId,addressId,contactId,age\n");
            for (PersonDTO person : persons) {
                fw.write(person.getPersonId() + "," + person.getFirstName() + "," + 
                        (person.getLastName() != null ? person.getLastName() : "") + "," + 
                        person.getRoleId() + "," + person.getAddressId() + "," + 
                        person.getContactId() + "," + person.getAge() + "\n");
            }
        }
    }
}
