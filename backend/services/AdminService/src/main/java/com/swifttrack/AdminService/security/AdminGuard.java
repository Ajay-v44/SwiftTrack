package com.swifttrack.AdminService.security;

import com.swifttrack.AdminService.clients.AuthClient;
import com.swifttrack.AdminService.conf.AdminAccessDeniedException;
import com.swifttrack.dto.TokenResponse;
import com.swifttrack.enums.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

/**
 * Central security guard for AdminService.
 * Validates tokens via AuthService and enforces admin-only access.
 * Only SUPER_ADMIN, SYSTEM_ADMIN, and ADMIN_USER are permitted.
 */
@Component
@RequiredArgsConstructor
public class AdminGuard {

    private static final Set<UserType> ADMIN_TYPES = EnumSet.of(
            UserType.SUPER_ADMIN,
            UserType.SYSTEM_ADMIN,
            UserType.ADMIN_USER
    );

    private static final Set<UserType> USER_MANAGEMENT_TYPES = EnumSet.of(
            UserType.SUPER_ADMIN,
            UserType.SYSTEM_ADMIN,
            UserType.ADMIN_USER,
            UserType.TENANT_ADMIN
    );

    private final AuthClient authClient;

    /**
     * Validates the token and verifies the caller is a SwiftTrack admin.
     * Throws AdminAccessDeniedException if the user is not an admin.
     *
     * @param token the JWT token from the request header
     * @return TokenResponse with the resolved user details
     */
    public TokenResponse requireAdmin(String token) {
        if (token == null || token.isBlank()) {
            throw new AdminAccessDeniedException("Missing admin token");
        }
        TokenResponse user;
        try {
            user = authClient.getUserDetails(token).getBody();
        } catch (Exception e) {
            throw new AdminAccessDeniedException("Invalid or expired token: " + e.getMessage());
        }
        if (user == null) {
            throw new AdminAccessDeniedException("Token resolved to no user");
        }
        UserType userType = user.userType().orElse(null);
        if (!ADMIN_TYPES.contains(userType)) {
            throw new AdminAccessDeniedException(
                    "Access denied. Required: SUPER_ADMIN / SYSTEM_ADMIN / ADMIN_USER. Found: " + userType);
        }
        return user;
    }

    /**
     * Same as requireAdmin but additionally checks the user is SUPER_ADMIN.
     * Used for highly sensitive operations like deleting users, platform config changes.
     */
    public TokenResponse requireSuperAdmin(String token) {
        TokenResponse user = requireAdmin(token);
        UserType userType = user.userType().orElse(null);
        if (userType != UserType.SUPER_ADMIN) {
            throw new AdminAccessDeniedException(
                    "This action requires SUPER_ADMIN privileges. Found: " + userType);
        }
        return user;
    }

    public TokenResponse requireUserManagementAdmin(String token) {
        TokenResponse user = resolveToken(token);
        UserType userType = user.userType().orElse(null);
        if (!USER_MANAGEMENT_TYPES.contains(userType)) {
            throw new AdminAccessDeniedException(
                    "This action requires tenant admin or platform admin privileges. Found: " + userType);
        }
        return user;
    }

    /**
     * Validates token and returns user details, without role check.
     * Useful for endpoints that might serve both admin and non-admin but need identity.
     */
    public TokenResponse resolveToken(String token) {
        if (token == null || token.isBlank()) {
            throw new AdminAccessDeniedException("Missing token");
        }
        try {
            TokenResponse user = authClient.getUserDetails(token).getBody();
            if (user == null) {
                throw new AdminAccessDeniedException("Token resolved to no user");
            }
            return user;
        } catch (AdminAccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new AdminAccessDeniedException("Token validation failed: " + e.getMessage());
        }
    }
}
