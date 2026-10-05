package com.project.manage.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.project.manage.Dao.UserRepository;
import com.project.manage.entities.User;
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
    	System.out.println("loadUserByUsername called");
        User user = userRepository.getUserByUserEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        System.out.println("DB Email: " + user.getEmail());
        System.out.println("DB Password (hash): " + user.getPassword());

        return new CustomerUserDetails(user);
    }
}