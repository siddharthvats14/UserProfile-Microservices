package com.fiserv.personservice.util;

import com.fiserv.personservice.dto.AddressDTO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AddressCsvReader {
    public static List<AddressDTO> readAddressesFromCsv(String resourcePath) throws IOException {
        List<AddressDTO> addresses = new ArrayList<>();
        InputStream is = AddressCsvReader.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IOException("Resource not found: " + resourcePath);
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
