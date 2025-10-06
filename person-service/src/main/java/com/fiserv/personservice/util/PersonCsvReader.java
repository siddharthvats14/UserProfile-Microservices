package com.fiserv.personservice.util;
import com.fiserv.personservice.dto.PersonDTO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PersonCsvReader {
    public static void appendPersonToCsv(String ignoredFilePath, PersonDTO person) throws IOException {
        // Always use the absolute path to src/main/resources/person.csv for writing
        String filePath = "src/main/resources/person.csv";
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
                fw.write("personId,firstName,lastName,roleId,addressId,contactId,age\n");
            }
            if (needsNewline) {
                fw.write("\n");
            }
            fw.write(person.getPersonId() + "," + person.getFirstName() + "," + person.getLastName() + "," + (person.getRoleId() != null ? person.getRoleId() : 0) + "," + (person.getAddressId() != null ? person.getAddressId() : 0) + "," + (person.getContactId() != null ? person.getContactId() : 0) + "," + (person.getAge() != null ? person.getAge() : 0) + "\n");
        }
    }
    public static List<PersonDTO> readPersonsFromCsv(String resourceName) throws IOException {
        List<PersonDTO> persons = new ArrayList<>();
        InputStream is = PersonCsvReader.class.getClassLoader().getResourceAsStream(resourceName);
        if (is == null) {
            throw new IOException("Resource not found: " + resourceName);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                String[] parts = line.split(",");
                if (parts.length < 7) continue;
                PersonDTO person = new PersonDTO();
                person.setPersonId(Integer.valueOf(parts[0]));
                person.setFirstName(parts[1]);
                person.setLastName(parts[2]);
                person.setRoleId(Integer.valueOf(parts[3]));
                person.setAddressId(Integer.valueOf(parts[4]));
                person.setContactId(Integer.valueOf(parts[5]));
                person.setAge(Integer.valueOf(parts[6]));
                persons.add(person);
            }
        }
        return persons;
    }
}
