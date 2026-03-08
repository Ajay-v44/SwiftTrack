package com.swifttrack.AuthService.Services;

import java.util.Map;
import java.util.Objects;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;

import com.swifttrack.AuthService.Dto.LoginResponse;
import com.swifttrack.AuthService.Dto.LoginUser;
import com.swifttrack.AuthService.Dto.MobileNumAuth;
import com.swifttrack.AuthService.Dto.RegisterUser;
import com.swifttrack.AuthService.Dto.TokenResponse;
import com.swifttrack.AuthService.Models.UserModel;
import com.swifttrack.AuthService.Models.Enum.VerificationStatus;
import com.swifttrack.AuthService.Repository.UserRepo;
import com.swifttrack.AuthService.util.JwtUtil;
import com.swifttrack.AuthService.util.UserMapper;
import com.swifttrack.FeignClient.BillingAndSettlementInterface;
import com.swifttrack.dto.AddTenantUsers;
import com.swifttrack.dto.ListOfTenantUsers;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.RegisterDriverResponse;
import com.swifttrack.dto.authDto.GetDriverUsers;
import com.swifttrack.dto.authDto.UpdateUserStatusVerificationRequest;
import com.swifttrack.dto.driverDto.AddTenantDriver;
import com.swifttrack.dto.driverDto.AddTennatDriverResponse;
import com.swifttrack.enums.UserType;
import com.swifttrack.enums.BillingAndSettlement.AccountType;
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
    @Autowired
    BillingAndSettlementInterface billingAndSettlementInterface;

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
        userModel.setStatus(true);
        userModel.setType(registerUser.userType());
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

            // Fetch user roles
            List<String> roleNames = userModel.getUserRoles().stream()
                    .map(userRole -> userRole.getRoles().getName().toString())
                    .collect(Collectors.toList());

            return new TokenResponse(
                    userModel.getId(),
                    Optional.ofNullable(userModel.getTenantId()),
                    Optional.ofNullable(userModel.getProviderId()),
                    Optional.ofNullable(userModel.getType()),
                    userModel.getName(),
                    userModel.getMobile(),
                    roleNames);
        }
        throw new CustomException(HttpStatus.UNAUTHORIZED, "Invalid Token");
    }

    public Message assignAdmin(String token, UUID id) {
        Map<String, Object> map = jwtUtil.decodeToken(token);
        if (map.containsKey("mobile")) {
            String mobileNum = (String) map.get("mobile");
            UserModel callingUser = userRepo.findByMobile(mobileNum);
            if (callingUser == null)
                throw new ResourceNotFoundException("Calling user account doesn't exist");

            if (callingUser.getStatus() == false)
                throw new CustomException(HttpStatus.FORBIDDEN, "Calling user account is not verified");

            if (callingUser.getType() != UserType.SUPER_ADMIN && callingUser.getType() != UserType.SYSTEM_ADMIN
                    && callingUser.getType() != UserType.SYSTEM_USER) {
                throw new CustomException(HttpStatus.FORBIDDEN, "User does not have permission to assign admin");
            }

            Optional<UserModel> optionalUser = userRepo.findById(id);
            if (!optionalUser.isPresent()) {
                throw new ResourceNotFoundException("User to be updated doesn't exist");
            }
            UserModel userModel = optionalUser.get();

            userModel.setType(UserType.TENANT_ADMIN);
            userModel.setTenantId(userModel.getId());
            userModel.setStatus(true);
            userModel.setVerificationStatus(VerificationStatus.APPROVED);
            userRepo.save(userModel);
            return new Message("Admin role assigned successfully");

        }
        throw new CustomException(HttpStatus.UNAUTHORIZED, "Invalid Token");
    }

    public Message addTenantUsers(String token, List<AddTenantUsers> entity) {
        Map<String, Object> map = jwtUtil.decodeToken(token);
        if (map.containsKey("mobile")) {
            String mobileNum = (String) map.get("mobile");
            UserModel userModel = userRepo.findByMobile(mobileNum);
            if (userModel == null)
                throw new ResourceNotFoundException("User account doesn't exist");

            if (userModel.getStatus() == false)
                throw new CustomException(HttpStatus.FORBIDDEN, "User account is not verified");
            if (userModel.getTenantId() == null)
                throw new CustomException(HttpStatus.FORBIDDEN, "You are not part of any organization");
            if (userModel.getType() != UserType.TENANT_ADMIN)
                throw new CustomException(HttpStatus.FORBIDDEN, "User is not a tenant admin");

            // if (userModel.getType() != com.swifttrack.enums.UserType.TENANT_ADMIN)
            // throw new CustomException(HttpStatus.FORBIDDEN, "User is not a tenant
            // admin");
            for (AddTenantUsers addTenantUser : entity) {
                if (userRepo.findByMobile(addTenantUser.mobile()) != null
                        || userRepo.findByEmail(addTenantUser.email()) != null)
                    throw new CustomException(HttpStatus.FORBIDDEN, "User already exists");
                UserModel userModel1 = new UserModel();
                userModel1.setName(addTenantUser.name());
                userModel1.setEmail(addTenantUser.email());
                userModel1.setMobile(addTenantUser.mobile());
                userModel1.setTenantId(userModel.getTenantId());
                userModel1.setPasswordHash(cryptography.encode(addTenantUser.password()));
                userModel1.setStatus(true);
                userModel1.setType(addTenantUser.userType());
                userModel1.setVerificationStatus(VerificationStatus.APPROVED);
                userRepo.save(userModel1);
                if (userModel1.getType() == UserType.TENANT_DRIVER) {
                    billingAndSettlementInterface.createAccount(token, userModel1.getId(), AccountType.TENANT_DRIVER);
                }
            }
            return new Message("Users added successfully");
        }
        throw new CustomException(HttpStatus.UNAUTHORIZED, "Invalid Token");
    }

    public List<ListOfTenantUsers> getTenantUsers(String token, UserType userType) {
        Map<String, Object> map = jwtUtil.decodeToken(token);
        if (map.containsKey("mobile")) {
            String mobileNum = (String) map.get("mobile");
            UserModel userModel = userRepo.findByMobile(mobileNum);
            if (userModel == null)
                throw new ResourceNotFoundException("User account doesn't exist");

            if (userModel.getStatus() == false)
                throw new CustomException(HttpStatus.FORBIDDEN, "User account is not verified");
            if (userModel.getTenantId() == null && userModel.getType() != UserType.SUPER_ADMIN
                    && userModel.getType() != UserType.SYSTEM_ADMIN)
                throw new CustomException(HttpStatus.FORBIDDEN, "You are not part of any organization");

            // if (userModel.getType() != com.swifttrack.enums.UserType.TENANT_ADMIN)
            // throw new CustomException(HttpStatus.FORBIDDEN, "User is not a tenant
            // admin");
            if (userModel.getType() == UserType.SUPER_ADMIN || userModel.getType() == UserType.SYSTEM_ADMIN) {
                List<UserType> userTypes = Arrays.asList(UserType.TENANT_ADMIN, UserType.TENANT_USER,
                        UserType.TENANT_DRIVER);
                List<UserModel> userModel1 = userRepo.findByType(userTypes);
                return userModel1.stream().map(userMapper::toTenantUser).collect(Collectors.toList());
            }
            UUID tenantId = userModel.getTenantId();
            // if (userModel.getType() == UserType.TENANT_ADMIN)
            // tenantId = userModel.getId();

            List<UserModel> userModel1 = userRepo.findByTenantId(tenantId, userType);
            return userModel1.stream().map(userMapper::toTenantUser).collect(Collectors.toList());
        }
        throw new CustomException(HttpStatus.UNAUTHORIZED, "Invalid Token");
    }

    public RegisterDriverResponse registerDriver(RegisterUser registerUser) {
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
        userModel.setType(registerUser.userType());
        userModel.setVerificationStatus(VerificationStatus.PENDING);
        userRepo.save(userModel);

        return new RegisterDriverResponse(userModel.getId());
    }

    public List<GetDriverUsers> getDriverUsers(String token, VerificationStatus status) {

        Map<String, Object> map = jwtUtil.decodeToken(token);
        if (map.containsKey("mobile")) {
            String mobileNum = (String) map.get("mobile");
            UserModel userModel = userRepo.findByMobile(mobileNum);
            if (userModel == null)
                throw new ResourceNotFoundException("User account doesn't exist");

            if (userModel.getStatus() == false)
                throw new CustomException(HttpStatus.FORBIDDEN, "User account is not verified");
            if (userModel.getTenantId() == null && userModel.getType() != UserType.SUPER_ADMIN
                    && userModel.getType() != UserType.SYSTEM_ADMIN)
                throw new CustomException(HttpStatus.FORBIDDEN, "You are not part of any organization");

            // if (userModel.getType() != com.swifttrack.enums.UserType.TENANT_ADMIN)
            // throw new CustomException(HttpStatus.FORBIDDEN, "User is not a tenant
            // admin");
            if (userModel.getType() == UserType.SUPER_ADMIN || userModel.getType() == UserType.SYSTEM_ADMIN) {
                return userRepo.findByDriverTypeAndStatus(UserType.DRIVER_USER, status).stream()
                        .map(userMapper::toDriverUser).collect(Collectors.toList());
            }
        }
        throw new CustomException(HttpStatus.UNAUTHORIZED, "Invalid Token");
    }

    public Message updateUserStatusAndVerification(String token, UpdateUserStatusVerificationRequest request) {
        Map<String, Object> map = jwtUtil.decodeToken(token);
        if (!map.containsKey("mobile"))
            throw new CustomException(HttpStatus.UNAUTHORIZED, "Invalid Token");

        String mobileNum = (String) map.get("mobile");
        UserModel callingUser = userRepo.findByMobile(mobileNum);
        if (callingUser == null)
            throw new ResourceNotFoundException("User account doesn't exist");
        if (callingUser.getStatus() == false)
            throw new CustomException(HttpStatus.FORBIDDEN, "User account is not verified");
        if (callingUser.getType() != UserType.TENANT_ADMIN && callingUser.getType() != UserType.TENANT_USER
                && callingUser.getType() != UserType.SUPER_ADMIN && callingUser.getType() != UserType.SYSTEM_ADMIN)
            throw new CustomException(HttpStatus.FORBIDDEN,
                    "Only tenant admin or tenant user or super admin or system admin can update status");
        if (request.userId() == null || request.status() == null || request.verificationStatus() == null)
            throw new CustomException(HttpStatus.BAD_REQUEST, "userId, status and verificationStatus are required");

        UserModel targetUser = userRepo.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User to be updated doesn't exist"));
        if (!Objects.equals(callingUser.getTenantId(), targetUser.getTenantId()))
            throw new CustomException(HttpStatus.FORBIDDEN, "Cannot update user from another tenant");

        targetUser.setStatus(request.status());
        targetUser.setVerificationStatus(VerificationStatus.valueOf(request.verificationStatus().name()));
        userRepo.save(targetUser);
        return new Message("User status and verification updated successfully");
    }
}
