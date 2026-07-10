package com.example.demo.controllers.login;

import com.fasterxml.jackson.databind.ObjectMapper;
//import eg.com.khales.paymentgateway.helper.MsgLogging;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.models.ERole;
import com.example.demo.models.Role;
import com.example.demo.models.User;
import com.example.demo.payload.login.request.LoginRequest;
import com.example.demo.payload.login.request.SignupRequest;
import com.example.demo.payload.login.response.JwtResponse;
import com.example.demo.payload.login.response.MessageResponse;
import com.example.demo.repository.login.RoleRepository;
import com.example.demo.repository.login.UserRepository;
import com.example.demo.security.jwt.JwtUtils;
import com.example.demo.security.services.UserDetailsImpl;

import java.sql.SQLException;
import java.util.Optional;

import org.springframework.web.bind.annotation.CrossOrigin;

//@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
//@CrossOrigin(allowCredentials = "true")
@CrossOrigin(origins = "http://192.168.1.249:4200")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest)  {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            Optional<User> user = userRepository.findByUsername(loginRequest.getUsername());
            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles, user.get()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        }
    }

    @PostMapping("/changePassword")
    public ResponseEntity<?> changePasswordUser(@Valid @RequestBody SignupRequest signUpRequest)  {

        try {
            Optional<User> user = userRepository.findByUsername(signUpRequest.getUsername());
            if (user.isPresent()) {

//                if(!user.get().getPassword().equals(encoder.encode(signUpRequest.getOldPassword()))){
//                    return ResponseEntity
//                            .badRequest()
//                            .body(new MessageResponse("Error: Old Password is incorrect"));
//                }
                if (!signUpRequest.getPassword().equals(signUpRequest.getEmail())) {
                    return ResponseEntity
                            .badRequest()
                            .body(new MessageResponse("Error: Password dis-match", 1));
                }
                User user1 = user.get();
                user1.setPassword(encoder.encode(signUpRequest.getPassword()));
                userRepository.save(user1);
                return ResponseEntity.ok(new MessageResponse("Password Change successfully!", 1));
            } else {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: User Not Found", 1));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(e.toString());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest){
        try {

            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Username is already taken!", 1));
            }

            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Email is already in use!", 1));
            }

            // Create new user's account
            User user = new User(signUpRequest.getUsername(),
                    signUpRequest.getEmail(),
                    encoder.encode(signUpRequest.getPassword()), signUpRequest.getRole()
                    , signUpRequest.isViewProject(), signUpRequest.isAddProject(), signUpRequest.isEditProject(), signUpRequest.isDeleteProject()
                    , signUpRequest.isViewPand(), signUpRequest.isAddPand(), signUpRequest.isEditPand(), signUpRequest.isDeletePand()
                    , signUpRequest.isViewJobOrder(), signUpRequest.isAddJobOrder(), signUpRequest.isEditJobOrder(), signUpRequest.isDeleteJobOrder()
                    , signUpRequest.isViewExitJobOrder(), signUpRequest.isAddExitJobOrder(), signUpRequest.isEditExitJobOrder(),signUpRequest.isDeleteExitJobOrder()
                    ,signUpRequest.isShowReports()
                    , signUpRequest.isRecordDelivered(), signUpRequest.isAllAuth(),signUpRequest.isViewOnly(),signUpRequest.isEditUser(),signUpRequest.isManufacturingManager(),signUpRequest.isStoreManager(),signUpRequest.isPurchasingManager(), signUpRequest.isUser());

            Set<Role> roles = new HashSet<>();

            if(signUpRequest.isAllAuth()) {
                Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(adminRole);
            }
            else if (signUpRequest.isManufacturingManager()){
                Role userRole = roleRepository.findByName(ERole.ROLE_MANUFACTURING)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            }
            else if(signUpRequest.isStoreManager()){
                Role userRole = roleRepository.findByName(ERole.ROLE_STORE)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            }
            else if (signUpRequest.isPurchasingManager()){
                Role userRole = roleRepository.findByName(ERole.ROLE_PURCHASING)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            }else{
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);

                user.setUser(true);
            };

            user.setRoles(roles);
            userRepository.save(user);

            return ResponseEntity.ok(new MessageResponse("User registered successfully!", 1));
        } catch (Exception e) {

            return ResponseEntity.ok(e.toString());
        }
    }

//    @GetMapping("/logout")
//    public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth != null) {
//            new SecurityContextLogoutHandler().logout(request, response, auth);
//        }
//        return "redirect:/signin";
//    }
}
