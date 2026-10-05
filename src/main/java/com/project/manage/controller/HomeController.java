package com.project.manage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.project.manage.Dao.UserRepository;
import com.project.manage.entities.User;
import com.project.manage.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("title", "Home - Smart Contact Manager");
        return "home";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About - Smart Contact Manager");
        return "about";
    }

    @GetMapping("/signup")
    public String signUp(Model model) {

        model.addAttribute("title", "Register");
        model.addAttribute("user", new User());

        return "signup";
    }

    @PostMapping("/do_register")
    public String register(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            @RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
            Model model,
            HttpSession session) {

        try {

            if (!agreement) {
                throw new Exception("Please accept Terms & Conditions");
            }

            if (result.hasErrors()) {
                model.addAttribute("user", user);
                return "signup";
            }
            System.out.println("User = " + user);
            System.out.println("Password = " + user.getPassword());
            // Encode Password
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Default Values
            user.setRole("ROLE_USER");
            user.setEnabled("True");
            user.setImageUrl("default.png");

            userRepository.save(user);

            model.addAttribute("user", new User());

            session.setAttribute(
                    "message",
                    new Message("Successfully Registered !!", "alert-success"));

        } catch (Exception e) {

            e.printStackTrace();

            model.addAttribute("user", user);

            session.setAttribute(
                    "message",
                    new Message(e.getMessage(), "alert-danger"));
        }

        return "signup";
    }
    @GetMapping("/signin")
    public String login(Model model) {

    	model.addAttribute("title", "Login - Smart Contact Manager");
       
        return "login";
    }
    @GetMapping("/logout")
    public String logins(Model model) {

    	model.addAttribute("title", "Logout");
       
        return "redirect:/home";
    }
}