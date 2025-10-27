package com.fiserv.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/web-app")
public class SignupController {
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/signup")
    public String showSignupForm() {
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(@RequestParam Map<String, String> params, Model model) {
    try {
            System.out.println("[SignupController] Incoming params: " + params);
            String gatewayBaseUrl = "http://localhost:8087";
            String signupUrl = gatewayBaseUrl + "/user-service/signup";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> signupData = new HashMap<>();
            signupData.put("loginName", params.getOrDefault("loginName", ""));
            signupData.put("password", params.getOrDefault("password", ""));
            signupData.put("name", params.getOrDefault("name", ""));
            signupData.put("lastName", params.getOrDefault("lastName", ""));
            signupData.put("age", params.getOrDefault("age", ""));
            signupData.put("houseNumber", params.getOrDefault("houseNumber", ""));
            signupData.put("streetNumber", params.getOrDefault("streetNumber", ""));
            signupData.put("city", params.getOrDefault("city", ""));
            signupData.put("state", params.getOrDefault("state", ""));
            signupData.put("country", params.getOrDefault("country", ""));
            signupData.put("pincode", params.getOrDefault("pincode", ""));
            signupData.put("primaryContact", params.getOrDefault("primaryContact", ""));
            signupData.put("secondaryContact", params.getOrDefault("secondaryContact", ""));
            signupData.put("email", params.getOrDefault("email", ""));
            signupData.put("secondaryEmail", params.getOrDefault("secondaryEmail", ""));
            System.out.println("[SignupController] Constructed signupData: " + signupData);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(signupData, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(signupUrl, request, Map.class);
            if (response.getStatusCodeValue() == 200) {
                model.addAttribute("message", "Signup successful! Your login name is: " + response.getBody().get("loginName"));
                return "signup-success";
            } else {
                model.addAttribute("error", "Signup failed: " + response.getBody());
                return "signup";
            }
        } catch (Exception ex) {
            model.addAttribute("error", "Signup error: " + ex.getMessage());
            ex.printStackTrace();
            return "signup";
        }
    }
}
