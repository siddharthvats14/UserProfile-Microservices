package com.fiserv.contactservice.controller;

import com.fiserv.contactservice.dto.ContactDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contact-service")
public class ContactController {
    @org.springframework.web.bind.annotation.PostMapping(value = "/contact", consumes = "application/json")
    public java.util.Map<String, Object> createContact(@org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> data) throws java.io.IOException {
        java.util.List<com.fiserv.contactservice.dto.ContactDTO> contacts = com.fiserv.contactservice.util.ContactCsvReader.readContactsFromCsv("contact.csv");
        int maxId = 0;
        for (com.fiserv.contactservice.dto.ContactDTO c : contacts) {
            if (c.getContactId() != null && c.getContactId() > maxId) maxId = c.getContactId();
        }
        int contactId = maxId + 1;
        com.fiserv.contactservice.dto.ContactDTO contact = new com.fiserv.contactservice.dto.ContactDTO();
        contact.setContactId(contactId);
        contact.setPrimaryMobileNumber(data.getOrDefault("primaryContact", ""));
        contact.setSecondaryMobileNumber(data.getOrDefault("secondaryContact", ""));
        contact.setPrimaryEmail(data.getOrDefault("email", ""));
        contact.setSecondaryEmail(data.getOrDefault("secondaryEmail", ""));
    com.fiserv.contactservice.util.ContactCsvReader.appendContactToCsv("src/main/resources/contact.csv", contact);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("contactId", contactId);
        return result;
    }
    @GetMapping("/contact")
    public ContactDTO getContact(@org.springframework.web.bind.annotation.RequestParam int contactId) throws java.io.IOException {
    java.util.List<com.fiserv.contactservice.dto.ContactDTO> contacts = com.fiserv.contactservice.util.ContactCsvReader.readContactsFromCsv("contact.csv");
        for (com.fiserv.contactservice.dto.ContactDTO contact : contacts) {
            if (contact.getContactId() == contactId) {
                return contact;
            }
        }
        return null;
    }
}
