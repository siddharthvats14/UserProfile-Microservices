package com.fiserv.addressservice.controller;
import java.util.List;
import java.io.IOException;
import org.springframework.web.bind.annotation.RequestParam;

import com.fiserv.addressservice.dto.AddressDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/address-service")
public class AddressController {
    @org.springframework.web.bind.annotation.PostMapping(value = "/address", consumes = "application/json")
    public java.util.Map<String, Object> createAddress(@org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> data) throws IOException {
    java.util.List<AddressDTO> addresses = com.fiserv.addressservice.util.AddressCsvReader.readAddressesFromCsv("address.csv");
        int maxId = 0;
        for (AddressDTO a : addresses) {
            if (a.getAddressId() != null && a.getAddressId() > maxId) maxId = a.getAddressId();
        }
        int addressId = maxId + 1;
        AddressDTO address = new AddressDTO();
        address.setAddressId(addressId);
        address.setHouseNumber(data.getOrDefault("houseNumber", ""));
        address.setStreetNumber(data.getOrDefault("streetNumber", ""));
        address.setCity(data.getOrDefault("city", ""));
        address.setState(data.getOrDefault("state", ""));
        address.setCountry(data.getOrDefault("country", ""));
        address.setPinCode(data.getOrDefault("pincode", ""));
    com.fiserv.addressservice.util.AddressCsvReader.appendAddressToCsv("src/main/resources/address.csv", address);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("addressId", addressId);
        return result;
    }
    @GetMapping("/address")
    public AddressDTO getAddress(@RequestParam(required = false) Integer addressId) throws IOException {
        if (addressId == null) addressId = 1;
        List<AddressDTO> addresses = com.fiserv.addressservice.util.AddressCsvReader.readAddressesFromCsv("address.csv");
        for (AddressDTO address : addresses) {
            if (address.getAddressId().equals(addressId)) {
                return address;
            }
        }
        return null;
    }
    
    @org.springframework.web.bind.annotation.DeleteMapping("/delete/{addressId}")
    public void deleteAddress(@org.springframework.web.bind.annotation.PathVariable Integer addressId) throws IOException {
        List<AddressDTO> addresses = com.fiserv.addressservice.util.AddressCsvReader.readAddressesFromCsv("address.csv");
        addresses.removeIf(a -> a.getAddressId().equals(addressId));
        
        // Rewrite the CSV file
        java.io.File file = new java.io.File("src/main/resources/address.csv");
        try (java.io.FileWriter fw = new java.io.FileWriter(file, false)) {
            fw.write("addressId,houseNumber,streetNumber,city,state,country,pinCode\n");
            for (AddressDTO address : addresses) {
                fw.write(address.getAddressId() + "," + address.getHouseNumber() + "," + 
                        address.getStreetNumber() + "," + address.getCity() + "," + 
                        address.getState() + "," + address.getCountry() + "," + 
                        address.getPinCode() + "\n");
            }
        }
    }
    
    @org.springframework.web.bind.annotation.PutMapping("/address/{addressId}")
    public AddressDTO updateAddress(@org.springframework.web.bind.annotation.PathVariable Integer addressId, 
                                     @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> data) throws IOException {
        List<AddressDTO> addresses = com.fiserv.addressservice.util.AddressCsvReader.readAddressesFromCsv("address.csv");
        AddressDTO addressToUpdate = null;
        
        for (AddressDTO address : addresses) {
            if (address.getAddressId().equals(addressId)) {
                addressToUpdate = address;
                break;
            }
        }
        
        if (addressToUpdate != null) {
            if (data.containsKey("houseNumber")) addressToUpdate.setHouseNumber(data.get("houseNumber"));
            if (data.containsKey("streetNumber")) addressToUpdate.setStreetNumber(data.get("streetNumber"));
            if (data.containsKey("city")) addressToUpdate.setCity(data.get("city"));
            if (data.containsKey("state")) addressToUpdate.setState(data.get("state"));
            if (data.containsKey("country")) addressToUpdate.setCountry(data.get("country"));
            if (data.containsKey("pincode")) addressToUpdate.setPinCode(data.get("pincode"));
            
            // Rewrite the CSV file
            java.io.File file = new java.io.File("src/main/resources/address.csv");
            try (java.io.FileWriter fw = new java.io.FileWriter(file, false)) {
                fw.write("addressId,houseNumber,streetNumber,city,state,country,pinCode\n");
                for (AddressDTO address : addresses) {
                    fw.write(address.getAddressId() + "," + address.getHouseNumber() + "," + 
                            address.getStreetNumber() + "," + address.getCity() + "," + 
                            address.getState() + "," + address.getCountry() + "," + 
                            address.getPinCode() + "\n");
                }
            }
        }
        
        return addressToUpdate;
    }
}
