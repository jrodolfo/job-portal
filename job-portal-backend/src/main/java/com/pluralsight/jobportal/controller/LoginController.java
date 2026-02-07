package com.pluralsight.jobportal.controller;


import com.pluralsight.jobportal.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginController {

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Map<String, String> login(Principal principal) {
        String username = principal.getName(); // fetched from Spring Security
        System.out.println("username: " + username);
        // Generate token
        String token = jwtUtil.generateToken(username);
        System.out.println("token: " + token);

        // Return as JSON
        Map<String, String> response = new HashMap<>();
        response.put("token", token);

        return response;
    }

    @GetMapping("/details")
    public Map<String, Object> getUserDetails(Principal principal) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            response.put("username", userDetails.getUsername());
            response.put("roles", userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
        } else {
            response.put("error", "User NOT authenticated");
            System.out.println("\n\n\t\tUser not authenticated\n\n\n");
        }
        return response;
    }
}
