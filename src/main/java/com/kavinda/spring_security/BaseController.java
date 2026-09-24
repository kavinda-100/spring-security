package com.kavinda.spring_security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class BaseController {

    @GetMapping
    public String home() {
        return "Hello from home";
    }


//    @GetMapping("/profile")
//    public ResponseEntity<Map<String, Object>> profile(Authentication authentication) {
//
//        return ResponseEntity.ok(
//                Map.of(
//                        "name", authentication.getName(),
//                        "authenticated", authentication.isAuthenticated(),
//                        "authorities", authentication.getAuthorities()
//                )
//        );
//    }
}
