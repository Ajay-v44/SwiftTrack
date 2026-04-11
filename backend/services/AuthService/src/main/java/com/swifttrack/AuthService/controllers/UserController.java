package com.swifttrack.AuthService.controllers;

import java.util.List;
import java.util.UUID;

import com.swifttrack.AuthService.Dto.PaginatedTenantUsersResponse;
import com.swifttrack.AuthService.Dto.UserTypeGroupResponse;
import com.swifttrack.enums.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.swifttrack.AuthService.Dto.LoginResponse;
import com.swifttrack.AuthService.Dto.LoginUser;
import com.swifttrack.AuthService.Dto.MobileNumAuth;
import com.swifttrack.AuthService.Dto.RegisterUser;
import com.swifttrack.AuthService.Dto.TokenResponse;
import com.swifttrack.AuthService.Models.Enum.VerificationStatus;
import com.swifttrack.AuthService.Services.UserTypeCatalogService;
import com.swifttrack.AuthService.Services.UserServices;
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

@RestController
@RequestMapping("/api/users/")
@Tag(name = "User Authentication", description = "User registration, login, and authentication endpoints")
public class UserController {

        @Autowired
        private UserServices userService;
        @Autowired
        private UserTypeCatalogService userTypeCatalogService;

        @PostMapping("v1/register")
        @Operation(summary = "Register a new user", description = "Create a new user account with email, mobile, and password")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User registered successfully"),
                        @ApiResponse(responseCode = "409", description = "Email or mobile already taken")
        })
        public ResponseEntity<String> registerUser(@RequestBody RegisterUser input) {
                return ResponseEntity.ok(userService.registerUser(input));
        }

        @PostMapping("v1/login/emailAndPassword")
        @Operation(summary = "Login with email and password", description = "Authenticate user using email and password credentials")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Login successful, returns JWT token"),
                        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                        @ApiResponse(responseCode = "403", description = "Account not activated")
        })
        public ResponseEntity<LoginResponse> loginUserEmailAndPassword(@RequestBody LoginUser input) {
                return ResponseEntity.ok(userService.loginUserEmailAndPassword(input));
        }

        @PostMapping("v1/login/mobileNumAndOtp")
        @Operation(summary = "Login with mobile and OTP", description = "Authenticate user using mobile number and one-time password")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Login successful, returns JWT token"),
                        @ApiResponse(responseCode = "401", description = "Invalid OTP"),
                        @ApiResponse(responseCode = "403", description = "Account not verified")
        })
        public ResponseEntity<LoginResponse> loginUserMobileAndOtp(@RequestBody MobileNumAuth entity) {
                return ResponseEntity.ok(userService.loginMobileAndOtp(entity));
        }

        @PostMapping("v1/getUserDetails")
        @Operation(summary = "Get user details from token", description = "Retrieve authenticated user details using JWT token")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User details retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Invalid token"),
                        @ApiResponse(responseCode = "404", description = "User account not found")
        })
        public ResponseEntity<TokenResponse> getUserDetails(@RequestParam("token") String token) {
                return ResponseEntity.ok(userService.getUserDetails(token));
        }

        @PostMapping("v1/assignAdmin")
        @Operation(summary = "Assign admin role to a user", description = "Assign Admin Role For A User Who Is Creating A Tenant Account")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Admin role assigned successfully"),
                        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                        @ApiResponse(responseCode = "403", description = "Account not activated")
        })
        public ResponseEntity<Message> assignAdmin(@RequestParam("token") String token, @RequestParam UUID id) {
                return ResponseEntity.ok(userService.assignAdmin(token, id));
        }

        @PostMapping("v1/addTenantUsers")
        @Operation(summary = "Add tenant users", description = "Add users to a tenant account")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Users added successfully"),
                        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                        @ApiResponse(responseCode = "403", description = "Account not activated")
        })
        public ResponseEntity<Message> addTenantUsers(@RequestParam("token") String token,
                        @RequestBody List<AddTenantUsers> entity) {
                return ResponseEntity.ok(userService.addTenantUsers(token, entity));
        }

        @PostMapping("v1/admin/createManagedUser")
        @Operation(summary = "Create a managed user", description = "Create a tenant-scoped or managed user by tenant admin or platform admin")
        public ResponseEntity<ManagedUserResponse> createManagedUser(@RequestParam("token") String token,
                        @RequestBody CreateManagedUserRequest request) {
                return ResponseEntity.ok(userService.createManagedUser(token, request));
        }

        @PostMapping("v1/getTenantUsers")
        @Operation(summary = "Get tenant users", description = "Get users of a tenant")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                        @ApiResponse(responseCode = "403", description = "Account not activated")
        })
        public ResponseEntity<List<ListOfTenantUsers>> getTenantUsers(@RequestParam String token,
                        @RequestParam UserType userType) {
                return ResponseEntity.ok(userService.getTenantUsers(token, userType));
        }

        @PostMapping(value = "v1/getTenantUsers", params = { "page", "size" })
        @Operation(summary = "Get tenant users with pagination via POST", description = "Backward-compatible paginated tenant user endpoint for clients that still call POST")
        public ResponseEntity<PaginatedTenantUsersResponse> getTenantUsersPaginatedPost(
                        @RequestParam String token,
                        @RequestParam(required = false) String query,
                        @RequestParam(required = false) List<UserType> userTypes,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "false") boolean includeRequestingUser) {
                return ResponseEntity.ok(
                                userService.getTenantUsers(token, query, userTypes, page, size, includeRequestingUser));
        }

        @GetMapping("v1/getTenantUsers")
        @Operation(summary = "Get tenant users with pagination", description = "Get tenant users for the authenticated tenant with pagination, filters, and role details")
        public ResponseEntity<PaginatedTenantUsersResponse> getTenantUsersPaginated(
                        @RequestParam String token,
                        @RequestParam(required = false) String query,
                        @RequestParam(required = false) List<UserType> userTypes,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "false") boolean includeRequestingUser) {
                return ResponseEntity.ok(
                                userService.getTenantUsers(token, query, userTypes, page, size, includeRequestingUser));
        }

        @GetMapping("v1/user-types")
        @Operation(summary = "Get user type catalog", description = "Returns user type groups and mapped user types for dynamic filtering")
        public ResponseEntity<List<UserTypeGroupResponse>> getUserTypeCatalog() {
                return ResponseEntity.ok(userTypeCatalogService.getActiveUserTypeGroups());
        }

        @PostMapping("v1/registerDriver")
        @Operation(summary = "Register a new driver", description = "Create a new driver account with email, mobile, and password")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Driver registered successfully"),
                        @ApiResponse(responseCode = "409", description = "Email or mobile already taken")
        })
        public ResponseEntity<RegisterDriverResponse> registerDriver(@RequestBody RegisterUser input) {
                return ResponseEntity.ok(userService.registerDriver(input));
        }

        @GetMapping("v1/getDriverUsers")
        public ResponseEntity<List<GetDriverUsers>> getDriverUsers(@RequestParam String token,
                        @RequestParam VerificationStatus status) {
                return ResponseEntity.ok(userService.getDriverUsers(token, status));
        }

        @PostMapping("v1/updateUserStatusAndVerification")
        @Operation(summary = "Update user status and verification", description = "Update user active status and verification status")
        public ResponseEntity<Message> updateUserStatusAndVerification(@RequestParam String token,
                        @RequestBody UpdateUserStatusVerificationRequest request) {
                return ResponseEntity.ok(userService.updateUserStatusAndVerification(token, request));
        }
}
