package com.example.deliveryproductservice.controller;



import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestController {

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        log.info("🏓 Test ping endpoint called");
        return ResponseEntity.ok("pong");
    }

    @PostMapping("/echo")
    public ResponseEntity<String> echo(@RequestBody String message) {
        log.info("📢 Test echo endpoint called with: {}", message);
        return ResponseEntity.ok("Echo: " + message);
    }
}
