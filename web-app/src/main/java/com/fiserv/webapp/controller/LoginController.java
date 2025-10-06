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
            System.out.println("LoginController: username='" + username + "' (len=" + username.length() + ")");
            System.out.println("LoginController: password='" + password + "' (len=" + password.length() + ")");
            String gatewayBaseUrl = "http://localhost:8087";
            String userUrl = gatewayBaseUrl + "/user-service/login?loginName=" + username + "&password=" + password;
            System.out.println("LoginController: userUrl=" + userUrl);
            org.springframework.http.ResponseEntity<java.util.LinkedHashMap> response = restTemplate.getForEntity(userUrl, java.util.LinkedHashMap.class);
            int statusCode = response.getStatusCodeValue();
            java.util.LinkedHashMap<String, Object> user = (java.util.LinkedHashMap<String, Object>) response.getBody(); // Safe cast for map response
            System.out.println("LoginController: user-service response=" + user);
            java.util.LinkedHashMap<String, Object> userObj = null;
            if (statusCode == 200 && user != null) {
                userObj = (java.util.LinkedHashMap<String, Object>) user.get("user");
                System.out.println("LoginController: userObj=" + userObj);
            }
            if (userObj != null && username.equals(userObj.get("loginName")) && password.equals(userObj.get("password"))) {
                model.addAttribute("username", username);
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

                if (person != null) {
                    String firstName = person.getOrDefault("firstName", "").toString();
                    String lastName = person.getOrDefault("lastName", "").toString();
                    model.addAttribute("personName", firstName + " " + lastName);
                    model.addAttribute("age", person.getOrDefault("age", "30"));
                } else {
                    model.addAttribute("personName", "N/A");
                    model.addAttribute("age", "N/A");
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
                model.addAttribute("error", user.get("error"));
                return "login";
            } else {
                model.addAttribute("error", "Invalid username or password");
                return "login";
            }
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
}