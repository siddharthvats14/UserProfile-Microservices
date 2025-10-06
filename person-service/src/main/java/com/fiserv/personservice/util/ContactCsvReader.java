package com.fiserv.personservice.util;

import com.fiserv.personservice.dto.ContactDTO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ContactCsvReader {
    public static List<ContactDTO> readContactsFromCsv(String resourcePath) throws IOException {
        List<ContactDTO> contacts = new ArrayList<>();
        InputStream is = ContactCsvReader.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                String[] parts = line.split(",");
                if (parts.length < 5) continue;
                ContactDTO contact = new ContactDTO();
                contact.setContactId(Integer.valueOf(parts[0]));
                contact.setPrimaryMobileNumber(parts[1]);
                contact.setSecondaryMobileNumber(parts[2]);
                contact.setPrimaryEmail(parts[3]);
                contact.setSecondaryEmail(parts[4]);
                contacts.add(contact);
            }
        }
        return contacts;
    }
}
