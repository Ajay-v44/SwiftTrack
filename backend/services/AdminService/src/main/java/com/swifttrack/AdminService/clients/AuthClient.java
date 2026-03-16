package com.swifttrack.AdminService.clients;

import com.swifttrack.dto.Message;
import com.swifttrack.dto.TokenResponse;
import com.swifttrack.dto.authDto.GetDriverUsers;
import com.swifttrack.dto.authDto.UpdateUserStatusVerificationRequest;
import com.swifttrack.enums.UserType;
import com.swifttrack.enums.VerificationStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "authservice", url = "http://localhost:8080/authservice")
public interface AuthClient {

    @PostMapping("/api/users/v1/getUserDetails")
    ResponseEntity<TokenResponse> getUserDetails(@RequestParam("token") String token);

    @PostMapping("/api/users/v1/getTenantUsers")
    ResponseEntity<List<com.swifttrack.dto.ListOfTenantUsers>> getTenantUsers(
            @RequestParam String token,
            @RequestParam UserType userType);

    @GetMapping("/api/users/v1/getDriverUsers")
    ResponseEntity<List<GetDriverUsers>> getDriverUsers(
            @RequestParam String token,
            @RequestParam VerificationStatus status);

    @PostMapping("/api/users/v1/updateUserStatusAndVerification")
    ResponseEntity<Message> updateUserStatusAndVerification(
            @RequestParam String token,
            @RequestBody UpdateUserStatusVerificationRequest request);

    @PostMapping("/api/users/v1/assignAdmin")
    ResponseEntity<Message> assignAdmin(
            @RequestParam String token,
            @RequestParam UUID id);
}
