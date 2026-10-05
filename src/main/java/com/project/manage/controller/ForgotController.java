package com.project.manage.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.manage.Dao.UserRepository;
import com.project.manage.entities.User;
import com.project.manage.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	@Autowired
	UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder bCrypt;
	
	 private final EmailService emailService ;
	 public ForgotController(EmailService emailService) {
	        this.emailService = emailService;
	    }
	//email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForms() 
	{
		
		return "forgot_email_form";
	}
	@PostMapping("/send-otp")
	public String openEmailForm(@RequestParam("email") String email,
	                            HttpSession session) {
		  System.out.println("🔥 SEND OTP METHOD CALLED!");

		    System.out.println("EMAIL = " + email);

	    Random random = new Random();
	    int otp = 1000 + random.nextInt(9000);

	    String subject = "OTP From SCM";
	    String message = ""+
	    				"<div border:2px solid #e2e2e2; padding:20px;>"
	    				+ "<h1>"
	    				+"OTP is"
	    				+"<b> "
	    				+otp
	    				+"</b>"
	    				+"</h1>"
	    				+  "</div>";

	    boolean flag = emailService.sendEmail(email, subject, message);

	    if (flag) {
	        session.setAttribute("myotp", otp);
	        session.setAttribute("email", email);

	        return "verify_otp";
	    } else {
	        session.setAttribute("message", "Check your email id!");
	        return "forgot_email_form";
	    }
	}
	//verify otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp,HttpSession session) {
		 System.out.println("🔥 VERIFY OTP METHOD CALLED!");
		int myOtp=(int)session.getAttribute("myotp");
		String email=(String )session.getAttribute("email");
		if(myOtp==otp) {
			//password change
			session.removeAttribute("myotp");
			User user=this.userRepository.getUserByUserEmail(email);
			if(user==null) {
				session.setAttribute("message","User does not exist!!");
				return "forgot_email_form";
			}
			else {
				
			}
			//password change form
			return "password_changer_form";
		}
		else {
			session.setAttribute("message","You have entered wrong otp");
			return "verify_otp";
		}
		
	}
	//change password
	@PostMapping("/change-password")
	public  String changePassword(@RequestParam("newPassword") String newpassword,HttpSession session) {
		String email=(String)session.getAttribute("email");
		User user=this.userRepository.getUserByUserEmail(email);
		
		user.setPassword(this.bCrypt.encode(newpassword));
		this.userRepository.save(user);
		session.removeAttribute("newpassword");
		
		return "redirect:/signin?change-password changed succesfully";
				
		
		
		
		
	}
}
