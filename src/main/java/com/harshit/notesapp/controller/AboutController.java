package com.harshit.notesapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/about")
public class AboutController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAboutInfo() {
        return ResponseEntity.ok(Map.of(
                "name", "Harshit Yadav",
                "email", "harshit@gmail.com",
                "my features", Map.of(
                        "Star/Pin Notes", "A feature allowing users to toggle a 'starred' status on their notes. I chose this because in real-world applications like Google Keep, users heavily rely on starring/pinning to quickly access their most important notes among hundreds of entries."
                )
        ));
    }
}
