package com.fiserv.contactservice.util;

import com.fiserv.contactservice.dto.ContactDTO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ContactCsvReader {
    public static void appendContactToCsv(String filePath, ContactDTO contact) throws IOException {
        java.io.File file = new java.io.File(filePath);
        boolean fileExists = file.exists();
        boolean needsNewline = false;
        if (fileExists && file.length() > 0) {
            try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(file, "r")) {
                raf.seek(file.length() - 1);
                int lastByte = raf.read();
                if (lastByte != '\n') {
                    needsNewline = true;
                }
            }
        }
        try (java.io.FileWriter fw = new java.io.FileWriter(file, true)) {
            if (!fileExists) {
                fw.write("contactId,primaryMobileNumber,secondaryMobileNumber,primaryEmail,secondaryEmail\n");
            }
            if (needsNewline) {
                fw.write("\n");
            }
            fw.write(contact.getContactId() + "," + contact.getPrimaryMobileNumber() + "," + contact.getSecondaryMobileNumber() + "," + contact.getPrimaryEmail() + "," + contact.getSecondaryEmail() + "\n");
        }
    }
    public static List<ContactDTO> readContactsFromCsv(String resourceName) throws IOException {
        List<ContactDTO> contacts = new ArrayList<>();
        InputStream is = ContactCsvReader.class.getClassLoader().getResourceAsStream(resourceName);
        if (is == null) {
            throw new IOException("Resource not found: " + resourceName);
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
