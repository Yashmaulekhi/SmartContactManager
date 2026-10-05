package com.project.manage.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.project.manage.Dao.ContactRepository;
import com.project.manage.Dao.MyOrderRepository;
import com.project.manage.Dao.UserRepository;
import com.project.manage.entities.Contact;
import com.project.manage.entities.MyOrders;
import com.project.manage.entities.User;
import com.project.manage.helper.Message;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.websocket.Session;

@Controller
	@RequestMapping("/user")
	public class UserController {
	@Autowired
	UserRepository userRepository;
	@Autowired
	ContactRepository contactRepository;
	@Autowired 
	private BCryptPasswordEncoder  bCryptPasswordEncoder;
	@Autowired
	private MyOrderRepository myOrderRepository;
	
	
	//creating order for payment
	@Value("${razorpay.key.id}")
	private String keyId;
	
	@Value("${razorpay.key.secret}")
	private String keySecret;
	@ModelAttribute
	public void commomMethod(Model m,Principal p) {

		    String userName = p.getName();
		    System.out.println(userName);

		    User user = userRepository.getUserByUserEmail(userName);
		    System.out.println(user);

		    m.addAttribute("user", user); 
	}
	@GetMapping("/index")
	public String dashboard(Model m, Principal p) {
		m.addAttribute("title", "User Dashboard");
		m.addAttribute("activePage", "home");
	     // <-- Missing line

	    return "normal/user_dashboard";
	}
	@GetMapping("/add-contact")
	public String contact(Model m){
		m.addAttribute("title", "Add Contact");
		m.addAttribute("activePage", "add-contact"); 
		m.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	@PostMapping("/process-contact")
	public String seeContact( @ModelAttribute Contact contact,
            BindingResult result,
            Principal principal,
            @RequestParam("profileImage") MultipartFile file,
            HttpSession session,Model m) {

	    try {
	        String name = principal.getName();
	        User user = this.userRepository.getUserByUserEmail(name);
	       
	        contact.setUser(user);
	        

	        if (!file.isEmpty()) {
	            String fileName = file.getOriginalFilename();
	            File saveFile=new ClassPathResource("static/image").getFile();
	            Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
	            Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
	            // TODO: actually save file bytes to disk/S3 here
	            contact.setImage(fileName);
	        } else {
	            contact.setImage("contact.png"); // default placeholder
	        }

	        user.getContact().add(contact);
	        this.userRepository.save(user);

	        System.out.println("Data " + contact);
	        System.out.println("Contact included in database");

	        
	        session.setAttribute("message", new Message("Added Succesfully " ,"success"));

	    } catch (Exception e) {
	        e.printStackTrace();
	        session.setAttribute("message", new Message("Something went wrong: " , "danger"));
	    }

	    return "normal/add_contact_form";
	}
	@GetMapping("/see-contact/{page}")
	public String seeContact(@PathVariable("page") Integer page,Model m ,Principal principal) {
		m.addAttribute("title", "Show Contact");
		m.addAttribute("activePage", "contacts"); 
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserEmail(userName);
		Pageable pageable = PageRequest.of(page, 5);

		Page<Contact> contacts =
		        this.contactRepository.findContactsByUser(
		                user.getId(), pageable);

		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		return "normal/see_contact";
	}
	@GetMapping("/see-contact")
	public String redirectToFirstPage() {
	    return "redirect:/user/see-contact/0";
	}

	
	@GetMapping("/contact/{cId}")
	public String showContactDetails(@PathVariable("cId") Integer cid,Model m,Principal principal) {
		m.addAttribute("activePage", "contacts"); 
		Optional<Contact> contactOptional=this.contactRepository.findById(cid);
		Contact contact=contactOptional.get();
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserEmail(userName);
		if(user.getId()==contact.getUser().getId())
			{m.addAttribute("contact",contact);
			m.addAttribute("title", contact.getName());
			}
		return "normal/current_details";
	}
	@GetMapping("/delete/{cId}")
	public String deleteContactDetails(
	        @PathVariable("cId") Integer cid,
	        Principal principal,
	        HttpSession session) {

	    Optional<Contact> contactOptional = contactRepository.findById(cid);

	    if (contactOptional.isPresent()) {

	        Contact contact = contactOptional.get();

	        String userName = principal.getName();
	        User user = userRepository.getUserByUserEmail(userName);

	        if (user.getId()==(contact.getUser().getId())) {
	            user.getContact().remove(contact);
	            userRepository.save(user);

	            session.setAttribute("message",
	                    new Message("Contact deleted successfully", "success"));
	        }
	    }

	    return "redirect:/user/see-contact/0";
	}
	
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId")int cId,Model m) {
		  m.addAttribute("activePage", "contacts"); 
		m.addAttribute("title","Update Contact");
		Contact contact=this.contactRepository.findById(cId).get();
		m.addAttribute("contact", contact);
		return "normal/update_form";
		
		
		
		
	}
	@PostMapping("/process-update")
	public String updateForms(@ModelAttribute Contact contact,
	                          Model m,
	                          @RequestParam("profileImage") MultipartFile file,
	                          HttpSession session) {

	    m.addAttribute("title", "Update Contact");

	    Contact oldContact = this.contactRepository.findById(contact.getcId()).get();
	
	    try {

	        if (!file.isEmpty()) {

	            // Delete old image
	            File deleteFile = new ClassPathResource("static/image").getFile();
	            File file1 = new File(deleteFile, oldContact.getImage());
	            file1.delete();

	            // Save new image
	            File saveFile = new ClassPathResource("static/image").getFile();
	            Path path = Paths.get(saveFile.getAbsolutePath()
	                    + File.separator
	                    + file.getOriginalFilename());

	            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

	            contact.setImage(file.getOriginalFilename());

	        } else {

	            // Keep old image
	            contact.setImage(oldContact.getImage());
	        }

	        // Keep old user (Very Important)
	        contact.setUser(oldContact.getUser());

	        // Save updated contact
	        this.contactRepository.save(contact);

	        session.setAttribute("message",
	                new Message("Contact Updated Successfully", "success"));

	    } catch (Exception e) {

	        e.printStackTrace();

	        session.setAttribute("message",
	                new Message("Something went wrong!", "danger"));
	    }

	    return "redirect:/user/contact/" + contact.getcId();
	}
	@GetMapping("/profile")
	public String profiles(Model m) {
		 m.addAttribute("activePage", "profile");
		return "normal/profile";
	}
	
	//open setting handler 
	@GetMapping("/settings")
	public String openSettings(Model m) {
		m.addAttribute("activePage", "settings");
		return "normal/Settings";
	}
	//changing Password 
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session  ) {
		String userName=principal.getName();
		User currentUser=this.userRepository.getUserByUserEmail(userName);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			//change password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message",
					new Message("Your Password has been Changed", "success"));
		}else {
			 session.setAttribute("message",
		                new Message("Wrong password", "danger"));
			 return "redirect:/user/settings";
		}
		return "redirect:/user/index";
	}

	@PostMapping("/create_order")
	public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> data,Principal principal) {
	    try {
	        int amt = Integer.parseInt(data.get("amount").toString());

	        RazorpayClient client = new RazorpayClient(keyId, keySecret);

	        JSONObject orderRequest = new JSONObject();
	        orderRequest.put("amount", amt * 100); // Razorpay expects amount in paise
	        orderRequest.put("currency", "INR");
	        orderRequest.put("receipt", "receipt_" + System.currentTimeMillis());

	        Order order = client.orders.create(orderRequest);

	        // Save this order attempt in your DB
	        String userName = principal.getName();
	        User user = userRepository.getUserByUserEmail(userName);

	        MyOrders myOrder = new MyOrders();
	        myOrder.setAmount(String.valueOf(amt));
	        myOrder.setReciept(order.get("receipt"));
	        myOrder.setStatus("CREATED");   // not paid yet, just created
	        myOrder.setUser(user);
	        myOrder.setPaymentId(order.get("id")); // storing razorpay order_id here temporarily

	        myOrderRepository.save(myOrder);

	        return ResponseEntity.ok(order.toString());

	    } catch (RazorpayException e) {
	        return ResponseEntity.status(500).body("Error creating order: " + e.getMessage());
	    }
	}
	@PostMapping("/verify-payment")
	public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> data) {
	    try {
	        String orderId = data.get("razorpay_order_id");
	        String paymentId = data.get("razorpay_payment_id");
	        String signature = data.get("razorpay_signature");

	        JSONObject options = new JSONObject();
	        options.put("razorpay_order_id", orderId);
	        options.put("razorpay_payment_id", paymentId);
	        options.put("razorpay_signature", signature);

	        boolean isValid = com.razorpay.Utils.verifyPaymentSignature(options, keySecret);

	        // Find the order we saved earlier using razorpay order_id (stored in paymentId field)
	        MyOrders myOrder = myOrderRepository.findByPaymentId(orderId);

	        if (myOrder != null) {
	            myOrder.setStatus(isValid ? "SUCCESS" : "FAILED");
	            myOrder.setPaymentId(paymentId); // now overwrite with actual payment_id
	            myOrderRepository.save(myOrder);
	        }

	        if (isValid) {
	            return ResponseEntity.ok(Map.of("status", "success"));
	        } else {
	            return ResponseEntity.ok(Map.of("status", "failed"));
	        }
	        

	    } catch (Exception e) {
	        return ResponseEntity.status(500).body(Map.of("status", "error", "message", e.getMessage()));
	    }
	}
	}

