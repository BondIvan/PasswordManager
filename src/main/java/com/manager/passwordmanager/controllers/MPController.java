package com.manager.passwordmanager.controllers;

import com.manager.passwordmanager.services.MPService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/master-password")
public class MPController {

    private final MPService mpService;

    public MPController(MPService mpService) {
        this.mpService = mpService;
    }

    @GetMapping("/create")
    public String createMasterPassword() {

        return "create_master_password";
    }

    @PostMapping("/create")
    public String create(@RequestParam("secret") String secret) {

        mpService.createMP(secret);

        return "redirect:/master-password";
    }

    @GetMapping
    public String showPageWithMasterPasswordField() {

        System.out.println("field with master password");

        if(!mpService.mpExists())
            return "redirect:/master-password/create";

        return "master_password";
    }

    @PostMapping
    public String checkMasterPassword(@RequestParam("secret") String secret, HttpSession session) {

        System.out.println("start check master password...");

        if(mpService.checkMP(secret)) {
            session.setAttribute("authenticated", true);
            return "redirect:/";
        } else {
            System.out.println("master password is wrong: " + secret);
            return "redirect:/master-password";
        }

    }

}

