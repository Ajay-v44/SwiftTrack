package com.swifttrack.FeignClient;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.swifttrack.dto.ListOfTenantUsers;
import com.swifttrack.dto.LoginResponse;
import com.swifttrack.dto.LoginUser;
import com.swifttrack.dto.MobileNumAuth;
import com.swifttrack.dto.TokenResponse;
import com.swifttrack.dto.driverDto.AddTenantDriver;
import com.swifttrack.dto.driverDto.AddTennatDriverResponse;
import com.swifttrack.enums.UserType;

@FeignClient(name = "authservice", url = "http://localhost:8080/authservice")
public interface AuthInterface {

    @PostMapping("/api/users/v1/login/emailAndPassword")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginUser loginUser);

    @PostMapping("/api/users/v1/login/mobileNumAndOtp")
    public ResponseEntity<LoginResponse> loginMobileNumAndOtp(@RequestBody MobileNumAuth mobileNumAuth);

    @PostMapping("/api/users/v1/getUserDetails")
    public ResponseEntity<TokenResponse> getUserDetails(@RequestParam("token") String token);

    @PostMapping("/api/users/v1/addTenantDrivers")
    public ResponseEntity<AddTennatDriverResponse> addTenantDrivers(@RequestParam String token,
            @RequestBody AddTenantDriver entity);

    @PostMapping("/api/users/v1/getTenantUsers")
    public ResponseEntity<List<ListOfTenantUsers>> getTenantUsers(@RequestParam String token,
            @RequestParam UserType userType);
}
