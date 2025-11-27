package com.swifttrack.AuthService.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.swifttrack.AuthService.Dto.LoginResponse;
import com.swifttrack.AuthService.Dto.LoginUser;
import com.swifttrack.AuthService.Dto.RegisterUser;
import com.swifttrack.AuthService.Models.UserModel;
import com.swifttrack.AuthService.Models.Enum.VerificationStatus;
import com.swifttrack.AuthService.Repository.UserRepo;
import com.swifttrack.AuthService.conf.Cryptography;
import com.swifttrack.AuthService.util.JwtUtil;

@Service
public class UserServices {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private BCryptPasswordEncoder cryptography;
    @Autowired
    private JwtUtil jwtUtil;

    public String registerUser(RegisterUser registerUser) {
        // validation
        if(userRepo.findByEmail(registerUser.email())!=null) throw new IllegalStateException("email already taken");
        if(userRepo.findByMobile(registerUser.mobile())!=null) throw new IllegalStateException("mobile already taken");
        UserModel userModel=new UserModel();
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

        if (userModel == null || userModel.getStatus() == false)
            throw new IllegalStateException("Account doesnt Exists Or Not Activated");

        if (cryptography.matches(input.password(),userModel.getPasswordHash()))
            return new LoginResponse("Bearer Token", jwtUtil.generateToken(userModel.getId(), userModel.getMobile()));
        throw new IllegalStateException("InValid Credentials");
    }

}