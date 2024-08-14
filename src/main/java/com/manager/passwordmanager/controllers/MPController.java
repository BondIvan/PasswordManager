package com.manager.passwordmanager.controllers;

import com.manager.passwordmanager.services.MPService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/master-password")
public class MPController {

    private final MPService mpService;

    public MPController(MPService mpService) {
        this.mpService = mpService;
    }

    @GetMapping("/create")
    public String showPageForCreateMasterPassword() {

        return "create_master_password";
    }

    @PostMapping("/create")
    public String create(@RequestParam("secret") String secret) {

        mpService.createMP(secret);

        return "redirect:/master-password";
    }

    @GetMapping
    public String showPageWithMasterPasswordField() {

        if(!mpService.mpExists())
            return "redirect:/master-password/create";

        return "master_password";
    }

    @PostMapping
    public String checkMasterPassword(@RequestParam("secret") String secret, HttpSession session, RedirectAttributes redirectAttributes) {

        if(!mpService.checkMP(secret)) {
            redirectAttributes.addFlashAttribute("error", "master password is wrong");
            return "redirect:/master-password";
        }

        session.setAttribute("authenticated", true);

        return "redirect:/notes";
    }

}

