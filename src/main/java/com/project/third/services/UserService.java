package com.project.third.services;


import com.project.third.Repositories.UsersRepository;
import com.project.third.models.Roles;
import com.project.third.models.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {

    final
    UsersRepository userRepository;

    final
    BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UsersRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Users user = userRepository.findByEmail(s);
        if (user.getIsActive()) {
            Set<GrantedAuthority> roles = new HashSet<>();
            for (Roles r : user.getRoles()) {
                roles.add(new SimpleGrantedAuthority(r.getRole()));
            }
            return new User(user.getEmail(), user.getPassword(), roles);
        }
        else {
            return null;
        }
    }

//    public Users registerUser(Users user){
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        return userRepository.save(user);
//    }

}
