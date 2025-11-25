package com.fiserv.userservice.controller;

import com.fiserv.userservice.dto.UserDTO;
import com.fiserv.userservice.util.UserCsvReader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import java.io.IOException;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;

@RestController 
public class UserController {
    @PostMapping("/user-service/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> signupData) {
        try {
            // 1. Call address-service to create address
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> addressRequest = new HashMap<>();
            addressRequest.put("houseNumber", signupData.get("houseNumber"));
            addressRequest.put("streetNumber", signupData.get("streetNumber"));
            addressRequest.put("city", signupData.get("city"));
            addressRequest.put("state", signupData.get("state"));
            addressRequest.put("country", signupData.get("country"));
            addressRequest.put("pincode", signupData.get("pincode"));
            ResponseEntity<Map<String, Object>> addressResponse = restTemplate.postForEntity(
                "http://localhost:8087/address-service/address", addressRequest, (Class<Map<String, Object>>)(Class<?>)Map.class);
            Integer addressId = Integer.valueOf(addressResponse.getBody().get("addressId").toString());

            // 2. Call contact-service to create contact
            Map<String, Object> contactRequest = new HashMap<>();
            contactRequest.put("primaryContact", signupData.get("primaryContact"));
            contactRequest.put("secondaryContact", signupData.get("secondaryContact"));
            contactRequest.put("email", signupData.get("email"));
            contactRequest.put("secondaryEmail", signupData.get("secondaryEmail"));
            ResponseEntity<Map<String, Object>> contactResponse = restTemplate.postForEntity(
                "http://localhost:8087/contact-service/contact", contactRequest, (Class<Map<String, Object>>)(Class<?>)Map.class);
            Integer contactId = Integer.valueOf(contactResponse.getBody().get("contactId").toString());

            // 3. Call person-service to create person with addressId and contactId
            Map<String, Object> personRequest = new HashMap<>();
            personRequest.put("name", signupData.get("name"));
            personRequest.put("lastName", signupData.get("lastName"));
            personRequest.put("age", signupData.get("age"));
            personRequest.put("addressId", addressId);
            personRequest.put("contactId", contactId);
            if (signupData.containsKey("roleId")) {
                personRequest.put("roleId", signupData.get("roleId"));
            }
            ResponseEntity<Map<String, Object>> personResponse = restTemplate.postForEntity(
                "http://localhost:8087/person-service/person", personRequest, (Class<Map<String, Object>>)(Class<?>)Map.class);
            Integer personId = Integer.valueOf(personResponse.getBody().get("personId").toString());

            // 4. Save user info to src/main/resources/user.csv
            List<UserDTO> users = UserCsvReader.readUsersFromCsv("user.csv");
            int maxUserId = 0;
            for (UserDTO u : users) {
                if (u.getUserId() != null && u.getUserId() > maxUserId) maxUserId = u.getUserId();
            }
            int userId = maxUserId + 1;
            String loginName = (String)signupData.get("loginName");
            String password = (String)signupData.get("password");
            UserDTO newUser = new UserDTO();
            newUser.setUserId(userId);
            newUser.setLoginName(loginName);
            newUser.setPassword(password);
            newUser.setPersonId(personId);
            newUser.setRetry(0);
            UserCsvReader.appendUserToCsv("src/main/resources/user.csv", newUser);

            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("loginName", loginName);
            result.put("personId", personId);
            result.put("addressId", addressId);
            result.put("contactId", contactId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Signup failed: " + e.getMessage());
        }
    }
    @GetMapping("/user-service/login")
    public ResponseEntity<?> login(@RequestParam String loginName, @RequestParam String password) throws IOException {
    List<UserDTO> users = UserCsvReader.readUsersFromCsv("src/main/resources/user.csv");
        String loginNameTrimmed = loginName.trim();
        String passwordTrimmed = password.trim();
        for (UserDTO user : users) {
            if (user.getLoginName().trim().equals(loginNameTrimmed) && user.getPassword().trim().equals(passwordTrimmed)) {
                Map<String, Object> result = new HashMap<>();
                result.put("user", user);
                // Fetch person from person-service using REST call
                RestTemplate restTemplate = new RestTemplate();
                String personServiceUrl = "http://localhost:8087/person-service/person/" + user.getPersonId();
                ResponseEntity<Object> personResponse = restTemplate.getForEntity(personServiceUrl, Object.class);
                result.put("person", personResponse.getBody());

                // Fetch contact and address from respective services using REST call
                Object personObj = personResponse.getBody();
                Integer contactId = null;
                Integer addressId = null;
                if (personObj instanceof Map) {
                    Object contactIdObj = ((Map<?,?>)personObj).get("contactId");
                    if (contactIdObj instanceof Integer) {
                        contactId = (Integer) contactIdObj;
                    } else if (contactIdObj != null) {
                        contactId = Integer.valueOf(contactIdObj.toString());
                    }
                    Object addressIdObj = ((Map<?,?>)personObj).get("addressId");
                    if (addressIdObj instanceof Integer) {
                        addressId = (Integer) addressIdObj;
                    } else if (addressIdObj != null) {
                        addressId = Integer.valueOf(addressIdObj.toString());
                    }
                }
                if (contactId != null) {
                    String contactServiceUrl = "http://localhost:8087/contact-service/contact?contactId=" + contactId;
                    ResponseEntity<Object> contactResponse = restTemplate.getForEntity(contactServiceUrl, Object.class);
                    result.put("contact", contactResponse.getBody());
                }
                if (addressId != null) {
                    String addressServiceUrl = "http://localhost:8087/address-service/address?addressId=" + addressId;
                    ResponseEntity<Object> addressResponse = restTemplate.getForEntity(addressServiceUrl, Object.class);
                    result.put("address", addressResponse.getBody());
                }
                return ResponseEntity.ok(result);
            }
        }
        // Authentication failed
        Map<String, String> error = new HashMap<>();
        error.put("error", "Authentication failed: Invalid username or password.");
        return ResponseEntity.status(401).body(error);
    }

    @GetMapping("/user-service/users")
    public ResponseEntity<?> getAllUsers() throws IOException {
        try {
            List<UserDTO> users = UserCsvReader.readUsersFromCsv("src/main/resources/user.csv");
            java.util.List<Map<String, Object>> userDetailsList = new java.util.ArrayList<>();
            RestTemplate restTemplate = new RestTemplate();
            
            for (UserDTO user : users) {
                Map<String, Object> userDetails = new HashMap<>();
                userDetails.put("userId", user.getUserId());
                userDetails.put("loginName", user.getLoginName());
                userDetails.put("personId", user.getPersonId());
                
                // Fetch person details
                try {
                    String personServiceUrl = "http://localhost:8087/person-service/person/" + user.getPersonId();
                    ResponseEntity<Map> personResponse = restTemplate.getForEntity(personServiceUrl, Map.class);
                    Map<String, Object> person = personResponse.getBody();
                    if (person != null) {
                        userDetails.put("firstName", person.get("firstName"));
                        userDetails.put("lastName", person.get("lastName"));
                        userDetails.put("age", person.get("age"));
                        userDetails.put("roleId", person.get("roleId"));
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching person for userId " + user.getUserId() + ": " + e.getMessage());
                }
                
                userDetailsList.add(userDetails);
            }
            
            return ResponseEntity.ok(userDetailsList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching users: " + e.getMessage());
        }
    }

    @PostMapping("/user-service/delete-user")
    public ResponseEntity<?> deleteUser(@RequestParam Integer userId) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            // First, find the user to get personId, addressId, contactId
            List<UserDTO> users = UserCsvReader.readUsersFromCsv("src/main/resources/user.csv");
            UserDTO userToDelete = users.stream()
                .filter(u -> u.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
            
            if (userToDelete == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            
            Integer personId = userToDelete.getPersonId();
            
            // Delete from person-service (which will give us addressId and contactId)
            String personUrl = "http://localhost:8087/person-service/person/" + personId;
            ResponseEntity<Map> personResponse = restTemplate.getForEntity(personUrl, Map.class);
            Integer addressId = null;
            Integer contactId = null;
            
            if (personResponse.getStatusCode() == HttpStatus.OK && personResponse.getBody() != null) {
                addressId = (Integer) personResponse.getBody().get("addressId");
                contactId = (Integer) personResponse.getBody().get("contactId");
            }
            
            // Delete from address-service
            if (addressId != null) {
                String deleteAddressUrl = "http://localhost:8087/address-service/delete/" + addressId;
                try {
                    restTemplate.delete(deleteAddressUrl);
                } catch (Exception e) {
                    System.out.println("Error deleting address: " + e.getMessage());
                }
            }
            
            // Delete from contact-service
            if (contactId != null) {
                String deleteContactUrl = "http://localhost:8087/contact-service/delete/" + contactId;
                try {
                    restTemplate.delete(deleteContactUrl);
                } catch (Exception e) {
                    System.out.println("Error deleting contact: " + e.getMessage());
                }
            }
            
            // Delete from person-service
            String deletePersonUrl = "http://localhost:8087/person-service/delete/" + personId;
            try {
                restTemplate.delete(deletePersonUrl);
            } catch (Exception e) {
                System.out.println("Error deleting person: " + e.getMessage());
            }
            
            // Delete from user.csv
            users.removeIf(u -> u.getUserId().equals(userId));
            
            // Rewrite the user CSV file
            java.io.File file = new java.io.File("src/main/resources/user.csv");
            try (java.io.FileWriter fw = new java.io.FileWriter(file, false)) {
                fw.write("userId,loginName,password,personId,retry\n");
                for (UserDTO user : users) {
                    fw.write(user.getUserId() + "," + user.getLoginName() + "," + user.getPassword() + "," + user.getPersonId() + "," + user.getRetry() + "\n");
                }
            }
            
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user: " + e.getMessage());
        }
    }
}
