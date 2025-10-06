package com.fiserv.addressservice.util;

import com.fiserv.addressservice.dto.AddressDTO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AddressCsvReader {
    public static void appendAddressToCsv(String filePath, AddressDTO address) throws IOException {
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
                fw.write("addressId,houseNumber,streetNumber,city,state,country,pinCode\n");
            }
            if (needsNewline) {
                fw.write("\n");
            }
            fw.write(address.getAddressId() + "," + address.getHouseNumber() + "," + address.getStreetNumber() + "," + address.getCity() + "," + address.getState() + "," + address.getCountry() + "," + address.getPinCode() + "\n");
        }
    }
    public static List<AddressDTO> readAddressesFromCsv(String resourceName) throws IOException {
        List<AddressDTO> addresses = new ArrayList<>();
        InputStream is = AddressCsvReader.class.getClassLoader().getResourceAsStream(resourceName);
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
                AddressDTO address = new AddressDTO();
                address.setAddressId(Integer.valueOf(parts[0]));
                address.setHouseNumber(parts[1]);
                address.setStreetNumber(parts[2]);
                address.setCity(parts[3]);
                address.setState(parts[4]);
                address.setCountry(parts[5]);
                address.setPinCode(parts[6]);
                addresses.add(address);
            }
        }
        return addresses;
    }
}
