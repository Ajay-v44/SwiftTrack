package com.swifttrack.AuthService.Services;

import java.util.Map;
import java.util.Objects;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;

import lombok.extern.slf4j.Slf4j;

import com.swifttrack.AuthService.Dto.PaginatedTenantUsersResponse;
import com.swifttrack.AuthService.Dto.TenantUserListItemResponse;
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
import com.swifttrack.dto.adminDto.CreateManagedUserRequest;
import com.swifttrack.dto.adminDto.ManagedUserResponse;
import com.swifttrack.dto.authDto.GetDriverUsers;
import com.swifttrack.dto.authDto.UpdateUserStatusVerificationRequest;
import com.swifttrack.dto.driverDto.AddTenantDriver;
import com.swifttrack.dto.driverDto.AddTennatDriverResponse;
import com.swifttrack.enums.UserType;
import com.swifttrack.enums.BillingAndSettlement.AccountType;
import com.swifttrack.exception.ResourceNotFoundException;
import com.swifttrack.exception.CustomException;

@Service
@Slf4j
public class UserServices {
    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

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
    @Autowired
    UserTypeCatalogService userTypeCatalogService;

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
        userModel.setType(registerUser.userType());
        boolean autoApproveConsumer = registerUser.userType() == UserType.CONSUMER
                || registerUser.userType() == UserType.PROVIDER_USER;
        userModel.setStatus(true);
        userModel.setVerificationStatus(autoApproveConsumer ? VerificationStatus.APPROVED : VerificationStatus.PENDING);
        userRepo.save(userModel);

        if (autoApproveConsumer) {
            try {
                billingAndSettlementInterface.createAccountInternal(userModel.getId(), AccountType.CONSUMER,
                        SYSTEM_USER_ID);
            } catch (Exception e) {
                log.warn("User registered but billing account bootstrap failed for userId={}: {}",
                        userModel.getId(), e.getMessage());
            }
        }

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
            // Simulation: Set OTP to 123456 and save
            userModel.setOtp("123456");
            userRepo.save(userModel);
            // In a real scenario, you'd call an SMS service here
            return new LoginResponse("OTP_SENT", "OTP has been sent to your mobile number");
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

    public ManagedUserResponse createManagedUser(String token, CreateManagedUserRequest request) {
        if (request == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Request is required");
        }
        if (request.name() == null || request.name().trim().isEmpty()
                || request.password() == null || request.password().trim().isEmpty()
                || request.email() == null || request.email().trim().isEmpty()
                || request.mobile() == null || request.mobile().trim().isEmpty()
                || request.userType() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    "name, password, email, mobile and userType are required");
        }

        Map<String, Object> map = jwtUtil.decodeToken(token);
        if (!map.containsKey("mobile")) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        UserModel caller = userRepo.findByMobile((String) map.get("mobile"));
        if (caller == null) {
            throw new ResourceNotFoundException("Calling user account doesn't exist");
        }
        if (Boolean.FALSE.equals(caller.getStatus())) {
            throw new CustomException(HttpStatus.FORBIDDEN, "Calling user account is not verified");
        }

        boolean tenantAdmin = caller.getType() == UserType.TENANT_ADMIN;
        boolean platformAdmin = caller.getType() == UserType.SUPER_ADMIN
                || caller.getType() == UserType.SYSTEM_ADMIN
                || caller.getType() == UserType.SYSTEM_USER
                || caller.getType() == UserType.ADMIN_USER;

        if (!tenantAdmin && !platformAdmin) {
            throw new CustomException(HttpStatus.FORBIDDEN,
                    "Only tenant admin or platform admin can create managed users");
        }

        if (userRepo.findByEmail(request.email().trim()) != null) {
            throw new CustomException(HttpStatus.CONFLICT, "Email already taken");
        }
        if (userRepo.findByMobile(request.mobile().trim()) != null) {
            throw new CustomException(HttpStatus.CONFLICT, "Mobile already taken");
        }

        UUID tenantId = null;
        if (tenantAdmin) {
            tenantId = caller.getTenantId();
            if (tenantId == null) {
                throw new CustomException(HttpStatus.FORBIDDEN, "Tenant admin is not linked to any tenant");
            }
            if (request.tenantId() != null && !request.tenantId().equals(tenantId)) {
                throw new CustomException(HttpStatus.FORBIDDEN, "Tenant admin cannot create users for another tenant");
            }
            if (request.userType() == UserType.SUPER_ADMIN || request.userType() == UserType.SYSTEM_ADMIN
                    || request.userType() == UserType.SYSTEM_USER || request.userType() == UserType.ADMIN_USER) {
                throw new CustomException(HttpStatus.FORBIDDEN, "Tenant admin cannot create platform admin users");
            }
        } else {
            tenantId = request.tenantId();
        }

        if ((request.userType() == UserType.TENANT_ADMIN
                || request.userType() == UserType.TENANT_USER
                || request.userType() == UserType.TENANT_DRIVER
                || request.userType() == UserType.TENANT_MANAGER
                || request.userType() == UserType.TENANT_STAFF)
                && tenantId == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "tenantId is required for tenant-scoped users");
        }

        UserModel userModel = new UserModel();
        userModel.setName(request.name().trim());
        userModel.setEmail(request.email().trim());
        userModel.setMobile(request.mobile().trim());
        userModel.setPasswordHash(cryptography.encode(request.password()));
        userModel.setType(request.userType());
        userModel.setTenantId(tenantId);
        userModel.setStatus(request.enabled() == null ? true : request.enabled());
        userModel.setVerificationStatus(VerificationStatus.APPROVED);
        userRepo.save(userModel);

        if (userModel.getType() == UserType.TENANT_DRIVER) {
            if (tenantAdmin) {
                billingAndSettlementInterface.createAccount(token, userModel.getId(), AccountType.TENANT_DRIVER);
            } else {
                billingAndSettlementInterface.createAccountInternal(userModel.getId(), AccountType.TENANT_DRIVER,
                        caller.getId() != null ? caller.getId() : SYSTEM_USER_ID);
            }
        }
        if (userModel.getType() == UserType.CONSUMER) {
            billingAndSettlementInterface.createAccountInternal(userModel.getId(), AccountType.CONSUMER,
                    caller.getId() != null ? caller.getId() : SYSTEM_USER_ID);
        }

        return new ManagedUserResponse(
                userModel.getId(),
                userModel.getTenantId(),
                userModel.getName(),
                userModel.getEmail(),
                userModel.getMobile(),
                userModel.getType(),
                userModel.getStatus(),
                com.swifttrack.enums.VerificationStatus.valueOf(userModel.getVerificationStatus().name()));
    }

    @Transactional(readOnly = true)
    public List<ListOfTenantUsers> getTenantUsers(String token, UserType userType) {
        Map<String, Object> map = jwtUtil.decodeToken(token);
        if (map.containsKey("mobile")) {
            String mobileNum = (String) map.get("mobile");
            UserModel userModel = userRepo.findByMobile(mobileNum);
            if (userModel == null)
                throw new ResourceNotFoundException("User account doesn't exist");

            if (userModel.getStatus() == false)
                throw new CustomException(HttpStatus.FORBIDDEN, "User account is not verified");
            boolean isPlatformAdmin = isPlatformAdmin(userModel.getType());
            if (userModel.getTenantId() == null && !isPlatformAdmin)
                throw new CustomException(HttpStatus.FORBIDDEN, "You are not part of any organization");

            if (isPlatformAdmin) {
                List<UserModel> users = userRepo.findAllByType(userType);
                return users.stream().map(userMapper::toTenantUser).collect(Collectors.toList());
            }

            if (!isTenantScopedUser(userType)) {
                throw new CustomException(HttpStatus.FORBIDDEN,
                        "Tenant admins can only view tenant-scoped users");
            }

            UUID tenantId = userModel.getTenantId();
            List<UserModel> users = userRepo.findByTenantId(tenantId, userType);
            return users.stream().map(userMapper::toTenantUser).collect(Collectors.toList());
        }
        throw new CustomException(HttpStatus.UNAUTHORIZED, "Invalid Token");
    }

    @Transactional(readOnly = true)
    public PaginatedTenantUsersResponse getTenantUsers(
            String token,
            String query,
            List<UserType> userTypes,
            int page,
            int size,
            boolean includeRequestingUser) {
        userTypeCatalogService.ensureSeedData();

        UserModel callingUser = getAuthenticatedUser(token);
        boolean platformAdmin = isPlatformAdmin(callingUser.getType());
        if (callingUser.getTenantId() == null && !platformAdmin) {
            throw new CustomException(HttpStatus.FORBIDDEN, "You are not part of any organization");
        }

        List<UserType> resolvedUserTypes = normalizeRequestedUserTypes(userTypes, platformAdmin);
        UUID tenantId = platformAdmin ? null : callingUser.getTenantId();
        UUID excludedUserId = includeRequestingUser ? null : callingUser.getId();

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<UserModel> specification = buildTenantUsersSpecification(
                tenantId,
                excludedUserId,
                query,
                resolvedUserTypes);

        Page<UserModel> result = userRepo.findAll(specification, pageable);

        List<TenantUserListItemResponse> content = result.getContent().stream()
                .map(this::toTenantUserListItem)
                .toList();

        return new PaginatedTenantUsersResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                userTypeCatalogService.getActiveUserTypeGroups());
    }

    private boolean isPlatformAdmin(UserType userType) {
        return userType == UserType.SUPER_ADMIN
                || userType == UserType.SYSTEM_ADMIN
                || userType == UserType.ADMIN_USER;
    }

    private boolean isTenantScopedUser(UserType userType) {
        return userType == UserType.TENANT_ADMIN
                || userType == UserType.TENANT_USER
                || userType == UserType.TENANT_DRIVER
                || userType == UserType.TENANT_MANAGER
                || userType == UserType.TENANT_STAFF;
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
        boolean platformAdmin = callingUser.getType() == UserType.SUPER_ADMIN
                || callingUser.getType() == UserType.SYSTEM_ADMIN;
        if (!platformAdmin && !Objects.equals(callingUser.getTenantId(), targetUser.getTenantId()))
            throw new CustomException(HttpStatus.FORBIDDEN, "Cannot update user from another tenant");

        targetUser.setStatus(request.status());
        targetUser.setVerificationStatus(VerificationStatus.valueOf(request.verificationStatus().name()));
        userRepo.save(targetUser);
        return new Message("User status and verification updated successfully");
    }

    private UserModel getAuthenticatedUser(String token) {
        Map<String, Object> map = jwtUtil.decodeToken(token);
        if (!map.containsKey("mobile")) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        UserModel userModel = userRepo.findByMobile((String) map.get("mobile"));
        if (userModel == null) {
            throw new ResourceNotFoundException("User account doesn't exist");
        }
        if (Boolean.FALSE.equals(userModel.getStatus())) {
            throw new CustomException(HttpStatus.FORBIDDEN, "User account is not verified");
        }
        return userModel;
    }

    private List<UserType> normalizeRequestedUserTypes(List<UserType> userTypes, boolean platformAdmin) {
        if (userTypes == null || userTypes.isEmpty()) {
            return null;
        }

        if (!platformAdmin) {
            List<UserType> invalidTenantTypes = userTypes.stream()
                    .filter(type -> !isTenantScopedUser(type))
                    .toList();
            if (!invalidTenantTypes.isEmpty()) {
                throw new CustomException(HttpStatus.FORBIDDEN, "Tenant admins can only filter tenant-scoped users");
            }
        }

        return userTypes.stream().distinct().toList();
    }

    private Specification<UserModel> buildTenantUsersSpecification(
            UUID tenantId,
            UUID excludedUserId,
            String query,
            List<UserType> userTypes) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            if (tenantId != null) {
                predicates.add(criteriaBuilder.equal(root.get("tenantId"), tenantId));
            }
            if (excludedUserId != null) {
                predicates.add(criteriaBuilder.notEqual(root.get("id"), excludedUserId));
            }
            if (query != null && !query.trim().isEmpty()) {
                String normalizedQuery = "%" + query.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), normalizedQuery),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), normalizedQuery),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("mobile")), normalizedQuery)));
            }
            if (userTypes != null && !userTypes.isEmpty()) {
                predicates.add(root.get("type").in(userTypes));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private TenantUserListItemResponse toTenantUserListItem(UserModel userModel) {
        List<String> roles = userModel.getUserRoles() == null ? List.of()
                : userModel.getUserRoles().stream()
                        .filter(userRole -> userRole.getRoles() != null)
                        .map(userRole -> userRole.getRoles().getName())
                        .distinct()
                        .sorted()
                        .toList();

        return new TenantUserListItemResponse(
                userModel.getId(),
                userModel.getName(),
                userModel.getMobile(),
                userModel.getEmail(),
                userModel.getStatus(),
                userModel.getVerificationStatus() == null
                        ? null
                        : com.swifttrack.enums.VerificationStatus.valueOf(userModel.getVerificationStatus().name()),
                userModel.getType(),
                roles,
                userModel.getCreatedAt(),
                userModel.getUpdatedAt());
    }
}
