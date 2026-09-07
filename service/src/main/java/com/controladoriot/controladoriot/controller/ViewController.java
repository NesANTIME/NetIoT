package com.controladoriot.controladoriot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    @GetMapping("/")
    public String inicio() {
        return "pages/inicio";
    }

    @GetMapping("/login")
    public String login() {
        return "pages/login";
    }

    @GetMapping("/register")
    public String register() {
        return "pages/register";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "pages/dashboard";
    }
}
