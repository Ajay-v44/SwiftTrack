package com.swifttrack.OrderService.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swifttrack.OrderService.dto.AddressRequest;
import com.swifttrack.OrderService.dto.AddressResponse;
import com.swifttrack.OrderService.services.AddressService;
import com.swifttrack.dto.Message;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/order/addresses")
@Tag(name = "Addresses", description = "Saved pickup address management")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/v1")
    public ResponseEntity<List<AddressResponse>> getAddresses(@RequestHeader("token") String token) {
        return ResponseEntity.ok(addressService.getAddresses(token));
    }

    @GetMapping("/v1/default")
    public ResponseEntity<AddressResponse> getDefaultAddress(@RequestHeader("token") String token) {
        return ResponseEntity.ok(addressService.getDefaultAddress(token));
    }

    @PostMapping("/v1")
    public ResponseEntity<AddressResponse> createAddress(@RequestHeader("token") String token,
            @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.createAddress(token, request));
    }

    @PutMapping("/v1/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(@RequestHeader("token") String token,
            @PathVariable UUID addressId,
            @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(token, addressId, request));
    }

    @PostMapping("/v1/{addressId}/default")
    public ResponseEntity<AddressResponse> setDefaultAddress(@RequestHeader("token") String token,
            @PathVariable UUID addressId) {
        return ResponseEntity.ok(addressService.setDefaultAddress(token, addressId));
    }

    @DeleteMapping("/v1/{addressId}")
    public ResponseEntity<Message> deleteAddress(@RequestHeader("token") String token, @PathVariable UUID addressId) {
        return ResponseEntity.ok(addressService.deleteAddress(token, addressId));
    }
}
