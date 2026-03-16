package com.swifttrack.AdminService.controllers;

import com.swifttrack.AdminService.clients.AuthClient;
import com.swifttrack.AdminService.dto.AdminUpdateUserRequest;
import com.swifttrack.AdminService.security.AdminGuard;
import com.swifttrack.AdminService.services.AuditService;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.TokenResponse;
import com.swifttrack.dto.adminDto.AssignRolesRequest;
import com.swifttrack.dto.adminDto.CreateManagedUserRequest;
import com.swifttrack.dto.adminDto.CreatePermissionRoleRequest;
import com.swifttrack.dto.adminDto.ManagedUserResponse;
import com.swifttrack.dto.adminDto.RoleViewResponse;
import com.swifttrack.dto.authDto.UpdateUserStatusVerificationRequest;
import com.swifttrack.enums.UserType;
import com.swifttrack.enums.VerificationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Section: User Management
 * Admin APIs to list, verify, activate/deactivate, and promote users.
 */
@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin - User Management", description = "Full user lifecycle management. View, verify, activate, deactivate, and promote users. Restricted to SwiftTrack admins.")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminGuard adminGuard;
    private final AuthClient authClient;
    private final AuditService auditService;

    @GetMapping("/v1/list")
    @Operation(summary = "List users by type", description = "Get all users filtered by UserType. Admin-only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users listed successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<?> listUsersByType(
            @RequestHeader String token,
            @RequestParam UserType userType) {
        adminGuard.requireUserManagementAdmin(token);
        return authClient.getTenantUsers(token, userType);
    }

    @PostMapping("/v1/create")
    @Operation(summary = "Create a managed user",
            description = "Create an enabled org user by tenant admin or platform admin. Supports tenant staff, support agents, tenant drivers, and other user types.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ManagedUserResponse> createManagedUser(
            @RequestHeader String token,
            @RequestBody CreateManagedUserRequest request) {
        TokenResponse admin = adminGuard.requireUserManagementAdmin(token);
        ResponseEntity<ManagedUserResponse> response = authClient.createManagedUser(token, request);

        ManagedUserResponse created = response.getBody();
        auditService.log(admin,
                "USER_CREATE",
                "USER",
                created != null ? created.id() : null,
                "USER",
                String.format("email=%s, mobile=%s, userType=%s, tenantId=%s, enabled=%s",
                        request.email(), request.mobile(), request.userType(), request.tenantId(), request.enabled()));

        return response;
    }

    @GetMapping("/v1/drivers")
    @Operation(summary = "List driver users by verification status",
            description = "Get all platform driver users filtered by verification status. Admin-only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Drivers listed"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<?> listDriverUsers(
            @RequestHeader String token,
            @RequestParam VerificationStatus status) {
        adminGuard.requireAdmin(token);
        return authClient.getDriverUsers(token, status);
    }

    @PostMapping("/v1/updateStatus")
    @Operation(summary = "Update user status and verification",
            description = "Activate/deactivate a user and update their verification status. Admin-only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Message> updateUserStatus(
            @RequestHeader String token,
            @RequestBody AdminUpdateUserRequest request) {
        TokenResponse admin = adminGuard.requireUserManagementAdmin(token);

        UpdateUserStatusVerificationRequest authRequest = new UpdateUserStatusVerificationRequest(
                request.userId(),
                request.status(),
                request.verificationStatus()
        );
        ResponseEntity<Message> response = authClient.updateUserStatusAndVerification(token, authRequest);

        auditService.log(admin,
                "USER_STATUS_UPDATE",
                "USER",
                request.userId(),
                "USER",
                String.format("status=%s, verification=%s, reason=%s",
                        request.status(), request.verificationStatus(), request.reason()));

        return response;
    }

    @PostMapping("/v1/assignRoles")
    @Operation(summary = "Assign multiple roles to user",
            description = "Assign multiple roles or permission-style roles such as view_order_history to a user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles assigned"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Message> assignRoles(
            @RequestHeader String token,
            @RequestBody AssignRolesRequest request) {
        TokenResponse admin = adminGuard.requireUserManagementAdmin(token);
        ResponseEntity<Message> response = authClient.assignRoles(token, request);

        auditService.log(admin,
                "USER_ROLE_ASSIGN",
                "USER",
                request.userId(),
                "USER",
                "Assigned roles: " + request.roleIds());

        return response;
    }

    @GetMapping("/v1/roles")
    @Operation(summary = "List available roles",
            description = "List all active or inactive roles/permissions that can be assigned to users.")
    public ResponseEntity<List<RoleViewResponse>> listRoles(
            @RequestHeader String token,
            @RequestParam(defaultValue = "true") Boolean status) {
        adminGuard.requireUserManagementAdmin(token);
        return authClient.getRoles(status);
    }

    @PostMapping("/v1/roles")
    @Operation(summary = "Create a role or permission",
            description = "Create a flexible role name such as view_order_history, manage_users, or support_ticket_update.")
    public ResponseEntity<String> createRole(
            @RequestHeader String token,
            @RequestBody CreatePermissionRoleRequest request) {
        TokenResponse admin = adminGuard.requireUserManagementAdmin(token);
        ResponseEntity<String> response = authClient.createRole(request);

        auditService.log(admin,
                "ROLE_CREATE",
                "ROLE",
                null,
                "ROLE",
                String.format("role=%s, description=%s", request.name(), request.description()));

        return response;
    }

    @PostMapping("/v1/assignAdmin")
    @Operation(summary = "Assign admin role to user",
            description = "Promote a user to TENANT_ADMIN. Only SUPER_ADMIN can perform this action.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Admin role assigned"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Message> assignAdmin(
            @RequestHeader String token,
            @RequestParam UUID userId) {
        TokenResponse admin = adminGuard.requireSuperAdmin(token);
        ResponseEntity<Message> response = authClient.assignAdmin(token, userId);

        auditService.log(admin, "ASSIGN_ADMIN", "USER", userId, "USER",
                "Assigned TENANT_ADMIN role to user " + userId);

        return response;
    }

    @GetMapping("/v1/me")
    @Operation(summary = "Get admin profile", description = "Get the calling admin's own profile information.")
    public ResponseEntity<TokenResponse> getMyProfile(@RequestHeader String token) {
        TokenResponse admin = adminGuard.requireAdmin(token);
        return ResponseEntity.ok(admin);
    }
}
