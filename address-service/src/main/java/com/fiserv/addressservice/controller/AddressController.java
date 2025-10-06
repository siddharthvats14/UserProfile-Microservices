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
}
