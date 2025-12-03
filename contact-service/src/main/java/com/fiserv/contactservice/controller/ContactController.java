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
    
    @org.springframework.web.bind.annotation.DeleteMapping("/delete/{contactId}")
    public void deleteContact(@org.springframework.web.bind.annotation.PathVariable Integer contactId) throws java.io.IOException {
        java.util.List<ContactDTO> contacts = com.fiserv.contactservice.util.ContactCsvReader.readContactsFromCsv("contact.csv");
        contacts.removeIf(c -> c.getContactId().equals(contactId));
        
        // Rewrite the CSV file
        java.io.File file = new java.io.File("src/main/resources/contact.csv");
        try (java.io.FileWriter fw = new java.io.FileWriter(file, false)) {
            fw.write("contactId,primaryMobileNumber,secondaryMobileNumber,primaryEmail,secondaryEmail\n");
            for (ContactDTO contact : contacts) {
                fw.write(contact.getContactId() + "," + contact.getPrimaryMobileNumber() + "," + 
                        contact.getSecondaryMobileNumber() + "," + contact.getPrimaryEmail() + "," + 
                        contact.getSecondaryEmail() + "\n");
            }
        }
    }
    
    @org.springframework.web.bind.annotation.PutMapping("/contact/{contactId}")
    public ContactDTO updateContact(@org.springframework.web.bind.annotation.PathVariable Integer contactId, 
                                     @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> data) throws java.io.IOException {
        java.util.List<ContactDTO> contacts = com.fiserv.contactservice.util.ContactCsvReader.readContactsFromCsv("contact.csv");
        ContactDTO contactToUpdate = null;
        
        for (ContactDTO contact : contacts) {
            if (contact.getContactId().equals(contactId)) {
                contactToUpdate = contact;
                break;
            }
        }
        
        if (contactToUpdate != null) {
            if (data.containsKey("primaryContact")) contactToUpdate.setPrimaryMobileNumber(data.get("primaryContact"));
            if (data.containsKey("secondaryContact")) contactToUpdate.setSecondaryMobileNumber(data.get("secondaryContact"));
            if (data.containsKey("email")) contactToUpdate.setPrimaryEmail(data.get("email"));
            if (data.containsKey("secondaryEmail")) contactToUpdate.setSecondaryEmail(data.get("secondaryEmail"));
            
            // Rewrite the CSV file
            java.io.File file = new java.io.File("src/main/resources/contact.csv");
            try (java.io.FileWriter fw = new java.io.FileWriter(file, false)) {
                fw.write("contactId,primaryMobileNumber,secondaryMobileNumber,primaryEmail,secondaryEmail\n");
                for (ContactDTO contact : contacts) {
                    fw.write(contact.getContactId() + "," + contact.getPrimaryMobileNumber() + "," + 
                            contact.getSecondaryMobileNumber() + "," + contact.getPrimaryEmail() + "," + 
                            contact.getSecondaryEmail() + "\n");
                }
            }
        }
        
        return contactToUpdate;
    }
}
