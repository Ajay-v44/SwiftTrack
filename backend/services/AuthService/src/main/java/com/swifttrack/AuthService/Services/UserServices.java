package com.swifttrack.AuthService.Services;

import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.swifttrack.AuthService.Dto.LoginResponse;
import com.swifttrack.AuthService.Dto.LoginUser;
import com.swifttrack.AuthService.Dto.MobileNumAuth;
import com.swifttrack.AuthService.Dto.RegisterUser;
import com.swifttrack.AuthService.Dto.TokenResponse;
import com.swifttrack.AuthService.Models.UserModel;
import com.swifttrack.AuthService.Models.Enum.VerificationStatus;
import com.swifttrack.AuthService.Repository.UserRepo;
import com.swifttrack.AuthService.conf.Cryptography;
import com.swifttrack.AuthService.util.JwtUtil;
import com.swifttrack.AuthService.util.UserMapper;
import com.swifttrack.exception.ResourceNotFoundException;
import com.swifttrack.exception.CustomException;

@Service
public class UserServices {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private BCryptPasswordEncoder cryptography;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    UserMapper userMapper;

    public String registerUser(RegisterUser registerUser) {
        // validation
        if (userRepo.findByEmail(registerUser.email()) != null)
            throw new CustomException(HttpStatus.CONFLICT, "Email already taken");
        if (userRepo.findByMobile(registerUser.mobile()) != null)
            throw new CustomException(HttpStatus.CONFLICT, "Mobile already taken");
        UserModel userModel = new UserModel();
        userModel.setName(registerUser.name());
        userModel.setEmail(registerUser.email());
        userModel.setMobile(registerUser.mobile());
        userModel.setPasswordHash(cryptography.encode(registerUser.password()));
        userModel.setStatus(false);
        userModel.setVerificationStatus(VerificationStatus.PENDING);
        userRepo.save(userModel);

        return "User registered Successfully";
    }

    public LoginResponse loginUserEmailAndPassword(LoginUser input) {
        UserModel userModel = userRepo.findByEmail(input.email());

        if (userModel == null)
            throw new ResourceNotFoundException("Account doesn't exist");
        
        if (userModel.getStatus() == false)
            throw new CustomException(HttpStatus.FORBIDDEN, "Account not activated");

        if (cryptography.matches(input.password(), userModel.getPasswordHash()))
            return new LoginResponse("Bearer Token", jwtUtil.generateToken(userModel.getId(), userModel.getMobile()));
        throw new CustomException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    public LoginResponse loginMobileAndOtp(MobileNumAuth entity) {
        UserModel userModel = userRepo.findByMobile(entity.mobileNum());

        if (userModel == null)
            throw new ResourceNotFoundException("User account doesn't exist");
            
        if (userModel.getStatus() == false)
            throw new CustomException(HttpStatus.FORBIDDEN, "User account is not verified");

        if (entity.otp().isPresent()) {
            if (Objects.equals(userModel.getOtp(), entity.otp().get())) {
                return new LoginResponse("Bearer Token",
                        jwtUtil.generateToken(userModel.getId(), userModel.getMobile()));
            }
        } else {
            // Todo : Implement Otp service
        }
        throw new CustomException(HttpStatus.UNAUTHORIZED, "Invalid OTP");
    }

    public TokenResponse getUserDetails(String token) {
        Map<String, Object> map = jwtUtil.decodeToken(token);
        if (map.containsKey("mobile")) {
            String mobileNum = (String) map.get("mobile");
            UserModel userModel = userRepo.findByMobile(mobileNum);
            if (userModel == null)
                throw new ResourceNotFoundException("User account doesn't exist");
                
            if (userModel.getStatus() == false)
                throw new CustomException(HttpStatus.FORBIDDEN, "User account is not verified");
            return userMapper.userModelTokenResponse(userModel);
        }
        throw new CustomException(HttpStatus.UNAUTHORIZED, "Invalid Token");
    }
}