
package com.temple.controller;

import com.temple.entity.Admin;
import com.temple.repository.AdminRepository;
import com.temple.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AdminRepository adminRepo;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Admin admin){
        Admin existing = adminRepo.findByUsername(admin.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(!existing.getPassword().equals(admin.getPassword()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String token = jwtUtil.generateToken(existing.getUsername());
        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }
}
