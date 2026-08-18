package com.backend.bilanko.controller.home;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String healthCheck() {
        return "Bilanko Backend is running successfully!";
    }
}