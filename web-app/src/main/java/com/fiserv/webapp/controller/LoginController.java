package com.fiserv.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/web-app")
public class LoginController {
    @Autowired
    private RestTemplate restTemplate;
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/login")
    public String processLogin(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            String gatewayBaseUrl = "http://localhost:8087";
            String userUrl = gatewayBaseUrl + "/user-service/login?loginName=" + username + "&password=" + password;
            
            org.springframework.http.ResponseEntity<java.util.LinkedHashMap> response = restTemplate.getForEntity(userUrl, java.util.LinkedHashMap.class);
            int statusCode = response.getStatusCodeValue();
            java.util.LinkedHashMap<String, Object> user = (java.util.LinkedHashMap<String, Object>) response.getBody(); // Safe cast for map response
            java.util.LinkedHashMap<String, Object> userObj = null;
            if (statusCode == 200 && user != null) {
                userObj = (java.util.LinkedHashMap<String, Object>) user.get("user");
            }
            if (userObj != null && username.equals(userObj.get("loginName")) && password.equals(userObj.get("password"))) {
                model.addAttribute("username", username);
                model.addAttribute("password", password);
                // Extract personId, addressId, contactId from userObj
                Object personIdObj = userObj.get("personId");
                Integer personId = personIdObj != null ? Integer.valueOf(personIdObj.toString()) : null;
                // Fetch person
                String personUrl = gatewayBaseUrl + "/person-service/person/" + personId;
                java.util.LinkedHashMap<String, Object> person = (java.util.LinkedHashMap<String, Object>) restTemplate.getForObject(personUrl, java.util.LinkedHashMap.class);
                // Extract addressId and contactId from person
                Object addressIdObj = person != null ? person.get("addressId") : null;
                Object contactIdObj = person != null ? person.get("contactId") : null;
                Integer addressId = addressIdObj != null ? Integer.valueOf(addressIdObj.toString()) : null;
                Integer contactId = contactIdObj != null ? Integer.valueOf(contactIdObj.toString()) : null;
                // Fetch address
                String addressUrl = gatewayBaseUrl + "/address-service/address?addressId=" + addressId;
                java.util.LinkedHashMap<String, Object> address = (java.util.LinkedHashMap<String, Object>) restTemplate.getForObject(addressUrl, java.util.LinkedHashMap.class);
                // Fetch contact
                String contactUrl = gatewayBaseUrl + "/contact-service/contact?contactId=" + contactId;
                java.util.LinkedHashMap<String, Object> contact = (java.util.LinkedHashMap<String, Object>) restTemplate.getForObject(contactUrl, java.util.LinkedHashMap.class);

                // Fetch role
                Object roleIdObj = person != null ? person.get("roleId") : null;
                Integer roleId = roleIdObj != null ? Integer.valueOf(roleIdObj.toString()) : null;
                String roleName = "N/A";
                String description = "N/A";
                if (roleId != null && roleId > 0) {
                    try {
                        String roleUrl = gatewayBaseUrl + "/role-service/role/" + roleId;
                        java.util.LinkedHashMap<String, Object> role = (java.util.LinkedHashMap<String, Object>) restTemplate.getForObject(roleUrl, java.util.LinkedHashMap.class);
                        if (role != null) {
                            roleName = role.getOrDefault("roleName", "N/A").toString();
                            description = role.getOrDefault("description", "N/A").toString();
                        }
                    } catch (Exception e) {
                        System.err.println("Error fetching role: " + e.getMessage());
                    }
                }
                model.addAttribute("roleName", roleName);
                model.addAttribute("description", description);
                model.addAttribute("roleId", roleId != null ? roleId : 0);
                model.addAttribute("currentUserId", userObj.get("userId"));

                if (person != null) {
                    String firstName = person.getOrDefault("firstName", "").toString();
                    String lastName = person.getOrDefault("lastName", "").toString();
                    model.addAttribute("personName", firstName + " " + lastName);
                    model.addAttribute("age", person.getOrDefault("age", "30"));
                } else {
                    model.addAttribute("personName", "N/A");
                    model.addAttribute("age", "N/A");
                }

                // Fetch all users for ADMIN (roleId=1) or SUPER_USER (roleId=2)
                if (roleId != null && (roleId == 1 || roleId == 2)) {
                    try {
                        String allUsersUrl = gatewayBaseUrl + "/user-service/users";
                        java.util.List<?> allUsers = restTemplate.getForObject(allUsersUrl, java.util.List.class);
                        model.addAttribute("allUsers", allUsers);
                    } catch (Exception e) {
                        System.err.println("Error fetching all users: " + e.getMessage());
                        model.addAttribute("allUsers", new java.util.ArrayList<>());
                    }
                }

                if (address != null) {
                    model.addAttribute("houseNumber", address.getOrDefault("houseNumber", ""));
                    model.addAttribute("streetNumber", address.getOrDefault("streetNumber", ""));
                    model.addAttribute("city", address.getOrDefault("city", ""));
                    model.addAttribute("town", "");
                    model.addAttribute("state", address.getOrDefault("state", ""));
                    model.addAttribute("country", address.getOrDefault("country", ""));
                    model.addAttribute("pincode", address.getOrDefault("pinCode", ""));
                } else {
                    model.addAttribute("houseNumber", "N/A");
                    model.addAttribute("streetNumber", "N/A");
                    model.addAttribute("city", "N/A");
                    model.addAttribute("town", "N/A");
                    model.addAttribute("state", "N/A");
                    model.addAttribute("country", "N/A");
                    model.addAttribute("pincode", "N/A");
                }

                if (contact != null) {
                    model.addAttribute("primaryContact", contact.getOrDefault("primaryMobileNumber", ""));
                    model.addAttribute("secondaryContact", contact.getOrDefault("secondaryMobileNumber", ""));
                    model.addAttribute("primaryEmail", contact.getOrDefault("primaryEmail", ""));
                    model.addAttribute("secondaryEmail", contact.getOrDefault("secondaryEmail", ""));
                } else {
                    model.addAttribute("primaryContact", "N/A");
                    model.addAttribute("secondaryContact", "N/A");
                    model.addAttribute("primaryEmail", "N/A");
                    model.addAttribute("secondaryEmail", "N/A");
                }
                return "person-details";
            } else if (statusCode == 401 && user != null && user.containsKey("error")) {
                // Failed login with remaining attempts message
                model.addAttribute("error", user.get("error"));
                return "login";
            } else if (statusCode == 423 && user != null && user.containsKey("error")) {
                // Account locked message with remaining time
                model.addAttribute("error", user.get("error"));
                return "login";
            } else {
                model.addAttribute("error", "Invalid username or password");
                return "login";
            }
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            // Handle HTTP error responses from user-service
            if (ex.getStatusCode().value() == 401 || ex.getStatusCode().value() == 423) {
                try {
                    // Parse the error response body
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.Map<String, String> errorBody = mapper.readValue(ex.getResponseBodyAsString(), java.util.Map.class);
                    if (errorBody.containsKey("error")) {
                        model.addAttribute("error", errorBody.get("error"));
                    } else {
                        model.addAttribute("error", "Invalid username or password");
                    }
                } catch (Exception parseEx) {
                    model.addAttribute("error", "Invalid username or password");
                }
            } else {
                model.addAttribute("error", "An error occurred during login. Please try again.");
            }
            return "login";
        } catch (Exception ex) {
            model.addAttribute("error", "An error occurred during login. Please try again.");
            return "login";
        }
}

@GetMapping("/logout")
public String logout(HttpServletRequest request) {
    String forwardedHost = request.getHeader("X-Forwarded-Host");
    String forwardedProto = request.getHeader("X-Forwarded-Proto");
    if (forwardedHost != null && forwardedProto != null) {
        return "redirect:" + forwardedProto + "://" + forwardedHost + "/web-app/login";
    }
    return "redirect:/web-app/login";
}

@PostMapping("/delete-user")
public String deleteUser(@RequestParam Integer userId, @RequestParam String username, @RequestParam String password, Model model) {
    try {
        String gatewayBaseUrl = "http://localhost:8087";
        String deleteUrl = gatewayBaseUrl + "/user-service/delete-user?userId=" + userId;
        restTemplate.postForEntity(deleteUrl, null, String.class);
        
        // After successful deletion, redirect to login with credentials to reload the page
        return "redirect:/web-app/login-and-continue?username=" + username + "&password=" + password;
    } catch (Exception e) {
        e.printStackTrace();
        // On error, still try to reload
        return "redirect:/web-app/login-and-continue?username=" + username + "&password=" + password;
    }
}

@GetMapping("/login-and-continue")
public String loginAndContinue(@RequestParam String username, @RequestParam String password, Model model) {
    // Reuse the login logic
    return processLogin(username, password, model);
}

@SuppressWarnings("unchecked")
@GetMapping("/edit-profile")
public String showEditProfile(@RequestParam String username, @RequestParam String password, Model model) {
    try {
        String gatewayBaseUrl = "http://localhost:8087";
        String userUrl = gatewayBaseUrl + "/user-service/login?loginName=" + username + "&password=" + password;
        
        org.springframework.http.ResponseEntity<java.util.LinkedHashMap> response = restTemplate.getForEntity(userUrl, java.util.LinkedHashMap.class);
        java.util.LinkedHashMap<String, Object> user = (java.util.LinkedHashMap<String, Object>) response.getBody();
        
        if (user != null) {
            java.util.LinkedHashMap<String, Object> userObj = (java.util.LinkedHashMap<String, Object>) user.get("user");
            Object personIdObj = userObj.get("personId");
            Integer personId = personIdObj != null ? Integer.valueOf(personIdObj.toString()) : null;
            
            // Fetch person details
            String personUrl = gatewayBaseUrl + "/person-service/person/" + personId;
            java.util.LinkedHashMap<String, Object> person = (java.util.LinkedHashMap<String, Object>) restTemplate.getForObject(personUrl, java.util.LinkedHashMap.class);
            
            Object addressIdObj = person != null ? person.get("addressId") : null;
            Object contactIdObj = person != null ? person.get("contactId") : null;
            Integer addressId = addressIdObj != null ? Integer.valueOf(addressIdObj.toString()) : null;
            Integer contactId = contactIdObj != null ? Integer.valueOf(contactIdObj.toString()) : null;
            
            // Fetch address details
            String addressUrl = gatewayBaseUrl + "/address-service/address?addressId=" + addressId;
            java.util.LinkedHashMap<String, Object> address = (java.util.LinkedHashMap<String, Object>) restTemplate.getForObject(addressUrl, java.util.LinkedHashMap.class);
            
            // Fetch contact details
            String contactUrl = gatewayBaseUrl + "/contact-service/contact?contactId=" + contactId;
            java.util.LinkedHashMap<String, Object> contact = (java.util.LinkedHashMap<String, Object>) restTemplate.getForObject(contactUrl, java.util.LinkedHashMap.class);
            
            // Populate form with current values
            model.addAttribute("username", username);
            model.addAttribute("password", password);
            model.addAttribute("personId", personId);
            model.addAttribute("addressId", addressId);
            model.addAttribute("contactId", contactId);
            
            // Person details
            model.addAttribute("firstName", person != null ? person.getOrDefault("firstName", "") : "");
            model.addAttribute("lastName", person != null ? person.getOrDefault("lastName", "") : "");
            model.addAttribute("age", person != null ? person.getOrDefault("age", 30) : 30);
            
            // Address details
            model.addAttribute("houseNumber", address != null ? address.getOrDefault("houseNumber", "") : "");
            model.addAttribute("streetNumber", address != null ? address.getOrDefault("streetNumber", "") : "");
            model.addAttribute("city", address != null ? address.getOrDefault("city", "") : "");
            model.addAttribute("state", address != null ? address.getOrDefault("state", "") : "");
            model.addAttribute("country", address != null ? address.getOrDefault("country", "") : "");
            model.addAttribute("pincode", address != null ? address.getOrDefault("pinCode", "") : "");
            
            // Contact details
            model.addAttribute("primaryContact", contact != null ? contact.getOrDefault("primaryMobileNumber", "") : "");
            model.addAttribute("secondaryContact", contact != null ? contact.getOrDefault("secondaryMobileNumber", "") : "");
            model.addAttribute("primaryEmail", contact != null ? contact.getOrDefault("primaryEmail", "") : "");
            model.addAttribute("secondaryEmail", contact != null ? contact.getOrDefault("secondaryEmail", "") : "");
            
            return "edit-profile";
        } else {
            return "redirect:/web-app/login";
        }
    } catch (Exception e) {
        e.printStackTrace();
        return "redirect:/web-app/login";
    }
}

@PostMapping("/edit-profile")
public String processEditProfile(@RequestParam String username, @RequestParam String password,
                                 @RequestParam Integer personId, @RequestParam Integer addressId, 
                                 @RequestParam Integer contactId,
                                 @RequestParam String firstName, @RequestParam String lastName,
                                 @RequestParam Integer age, @RequestParam String houseNumber,
                                 @RequestParam String streetNumber, @RequestParam String city,
                                 @RequestParam String state, @RequestParam String country,
                                 @RequestParam String pincode, @RequestParam String primaryContact,
                                 @RequestParam String secondaryContact, @RequestParam String primaryEmail,
                                 @RequestParam String secondaryEmail, Model model) {
    try {
        String gatewayBaseUrl = "http://localhost:8087";
        
        // Update person details
        java.util.Map<String, String> personData = new java.util.HashMap<>();
        personData.put("firstName", firstName);
        personData.put("lastName", lastName);
        personData.put("age", age.toString());
        
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        org.springframework.http.HttpEntity<java.util.Map<String, String>> personRequest = new org.springframework.http.HttpEntity<>(personData, headers);
        
        restTemplate.exchange(gatewayBaseUrl + "/person-service/person/" + personId, 
                            org.springframework.http.HttpMethod.PUT, personRequest, Object.class);
        
        // Update address details
        java.util.Map<String, String> addressData = new java.util.HashMap<>();
        addressData.put("houseNumber", houseNumber);
        addressData.put("streetNumber", streetNumber);
        addressData.put("city", city);
        addressData.put("state", state);
        addressData.put("country", country);
        addressData.put("pincode", pincode);
        
        org.springframework.http.HttpEntity<java.util.Map<String, String>> addressRequest = new org.springframework.http.HttpEntity<>(addressData, headers);
        
        restTemplate.exchange(gatewayBaseUrl + "/address-service/address/" + addressId, 
                            org.springframework.http.HttpMethod.PUT, addressRequest, Object.class);
        
        // Update contact details
        java.util.Map<String, String> contactData = new java.util.HashMap<>();
        contactData.put("primaryContact", primaryContact);
        contactData.put("secondaryContact", secondaryContact);
        contactData.put("primaryEmail", primaryEmail);
        contactData.put("secondaryEmail", secondaryEmail);
        
        org.springframework.http.HttpEntity<java.util.Map<String, String>> contactRequest = new org.springframework.http.HttpEntity<>(contactData, headers);
        
        restTemplate.exchange(gatewayBaseUrl + "/contact-service/contact/" + contactId, 
                            org.springframework.http.HttpMethod.PUT, contactRequest, Object.class);
        
        // Redirect to person-details page to reload updated data
        return "redirect:/web-app/login-and-continue?username=" + username + "&password=" + password;
    } catch (Exception e) {
        e.printStackTrace();
        model.addAttribute("error", "Failed to update profile. Please try again.");
        return showEditProfile(username, password, model);
    }
}

@GetMapping("/edit-user")
public String showEditUserForm(@RequestParam Integer userId, Model model) {
    try {
        String gatewayBaseUrl = "http://localhost:8087";
        
        // Fetch user details
        String userUrl = gatewayBaseUrl + "/user-service/user/" + userId;
        java.util.LinkedHashMap<String, Object> user = restTemplate.getForObject(userUrl, java.util.LinkedHashMap.class);
        
        if (user != null) {
            Object personIdObj = user.get("personId");
            Integer personId = personIdObj != null ? Integer.valueOf(personIdObj.toString()) : null;

            // Fetch person details
            String personUrl = gatewayBaseUrl + "/person-service/person/" + personId;
            java.util.LinkedHashMap<String, Object> person = restTemplate.getForObject(personUrl, java.util.LinkedHashMap.class);

            Object addressIdObj = person != null ? person.get("addressId") : null;
            Object contactIdObj = person != null ? person.get("contactId") : null;
            Integer addressId = addressIdObj != null ? Integer.valueOf(addressIdObj.toString()) : null;
            Integer contactId = contactIdObj != null ? Integer.valueOf(contactIdObj.toString()) : null;

            // Fetch address details
            String addressUrl = gatewayBaseUrl + "/address-service/address?addressId=" + addressId;
            java.util.LinkedHashMap<String, Object> address = restTemplate.getForObject(addressUrl, java.util.LinkedHashMap.class);

            // Fetch contact details
            String contactUrl = gatewayBaseUrl + "/contact-service/contact?contactId=" + contactId;
            java.util.LinkedHashMap<String, Object> contact = restTemplate.getForObject(contactUrl, java.util.LinkedHashMap.class);

            // Populate form with current values
            model.addAttribute("userId", userId);
            model.addAttribute("personId", personId);
            model.addAttribute("addressId", addressId);
            model.addAttribute("contactId", contactId);

            // Person details
            model.addAttribute("firstName", person != null ? person.getOrDefault("firstName", "") : "");
            model.addAttribute("lastName", person != null ? person.getOrDefault("lastName", "") : "");
            model.addAttribute("age", person != null ? person.getOrDefault("age", 30) : 30);

            // Address details
            model.addAttribute("houseNumber", address != null ? address.getOrDefault("houseNumber", "") : "");
            model.addAttribute("streetNumber", address != null ? address.getOrDefault("streetNumber", "") : "");
            model.addAttribute("city", address != null ? address.getOrDefault("city", "") : "");
            model.addAttribute("state", address != null ? address.getOrDefault("state", "") : "");
            model.addAttribute("country", address != null ? address.getOrDefault("country", "") : "");
            model.addAttribute("pincode", address != null ? address.getOrDefault("pinCode", "") : "");

            // Contact details
            model.addAttribute("primaryContact", contact != null ? contact.getOrDefault("primaryMobileNumber", "") : "");
            model.addAttribute("secondaryContact", contact != null ? contact.getOrDefault("secondaryMobileNumber", "") : "");
            model.addAttribute("primaryEmail", contact != null ? contact.getOrDefault("primaryEmail", "") : "");
            model.addAttribute("secondaryEmail", contact != null ? contact.getOrDefault("secondaryEmail", "") : "");

            return "edit-user";
        } else {
            model.addAttribute("error", "User not found.");
            return "redirect:/web-app/person-details";
        }
    } catch (Exception e) {
        e.printStackTrace();
        model.addAttribute("error", "An error occurred while fetching user details.");
        return "redirect:/web-app/person-details";
    }
}

@PostMapping("/edit-user")
public String processEditUser(@RequestParam Integer userId, @RequestParam Integer personId, 
                              @RequestParam Integer addressId, @RequestParam Integer contactId,
                              @RequestParam String firstName, @RequestParam String lastName,
                              @RequestParam Integer age, @RequestParam String houseNumber,
                              @RequestParam String streetNumber, @RequestParam String city,
                              @RequestParam String state, @RequestParam String country,
                              @RequestParam String pincode, @RequestParam String primaryContact,
                              @RequestParam String secondaryContact, @RequestParam String primaryEmail,
                              @RequestParam String secondaryEmail, Model model) {
    try {
        String gatewayBaseUrl = "http://localhost:8087";

        // Update person details
        java.util.Map<String, String> personData = new java.util.HashMap<>();
        personData.put("firstName", firstName);
        personData.put("lastName", lastName);
        personData.put("age", age.toString());

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        org.springframework.http.HttpEntity<java.util.Map<String, String>> personRequest = new org.springframework.http.HttpEntity<>(personData, headers);

        restTemplate.exchange(gatewayBaseUrl + "/person-service/person/" + personId, 
                            org.springframework.http.HttpMethod.PUT, personRequest, Object.class);

        // Update address details
        java.util.Map<String, String> addressData = new java.util.HashMap<>();
        addressData.put("houseNumber", houseNumber);
        addressData.put("streetNumber", streetNumber);
        addressData.put("city", city);
        addressData.put("state", state);
        addressData.put("country", country);
        addressData.put("pincode", pincode);

        org.springframework.http.HttpEntity<java.util.Map<String, String>> addressRequest = new org.springframework.http.HttpEntity<>(addressData, headers);

        restTemplate.exchange(gatewayBaseUrl + "/address-service/address/" + addressId, 
                            org.springframework.http.HttpMethod.PUT, addressRequest, Object.class);

        // Update contact details
        java.util.Map<String, String> contactData = new java.util.HashMap<>();
        contactData.put("primaryContact", primaryContact);
        contactData.put("secondaryContact", secondaryContact);
        contactData.put("primaryEmail", primaryEmail);
        contactData.put("secondaryEmail", secondaryEmail);

        org.springframework.http.HttpEntity<java.util.Map<String, String>> contactRequest = new org.springframework.http.HttpEntity<>(contactData, headers);

        restTemplate.exchange(gatewayBaseUrl + "/contact-service/contact/" + contactId, 
                            org.springframework.http.HttpMethod.PUT, contactRequest, Object.class);

        model.addAttribute("success", "User updated successfully.");
        return "redirect:/web-app/person-details";
    } catch (Exception e) {
        e.printStackTrace();
        model.addAttribute("error", "Failed to update user. Please try again.");
        return "redirect:/web-app/person-details";
    }
}
}