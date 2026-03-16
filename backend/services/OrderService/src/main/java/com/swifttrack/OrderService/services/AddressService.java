package com.swifttrack.OrderService.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swifttrack.FeignClient.AuthInterface;
import com.swifttrack.OrderService.dto.AddressRequest;
import com.swifttrack.OrderService.dto.AddressResponse;
import com.swifttrack.OrderService.models.UserAddress;
import com.swifttrack.OrderService.models.enums.AddressOwnerType;
import com.swifttrack.OrderService.repositories.UserAddressRepository;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.TokenResponse;
import com.swifttrack.enums.BillingAndSettlement.BookingChannel;
import com.swifttrack.enums.UserType;
import com.swifttrack.exception.CustomException;

@Service
@Transactional
public class AddressService {

    private final UserAddressRepository userAddressRepository;
    private final AuthInterface authInterface;

    public AddressService(UserAddressRepository userAddressRepository, AuthInterface authInterface) {
        this.userAddressRepository = userAddressRepository;
        this.authInterface = authInterface;
    }

    public List<AddressResponse> getAddresses(String token) {
        AddressContext context = resolveContext(token);
        return getAddressesForContext(context).stream().map(this::toResponse).toList();
    }

    public AddressResponse getDefaultAddress(String token) {
        AddressContext context = resolveContext(token);
        return getAddressesForContext(context).stream()
                .filter(UserAddress::isDefault)
                .findFirst()
                .map(this::toResponse)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Default address not found"));
    }

    public AddressResponse createAddress(String token, AddressRequest request) {
        AddressContext context = resolveContext(token);
        validateRequest(request);

        UserAddress address = new UserAddress();
        address.setTenantId(context.tenantId());
        address.setOwnerUserId(context.ownerUserId());
        address.setOwnerType(context.ownerType());
        applyRequest(address, request);

        List<UserAddress> existing = getAddressesForContext(context);
        boolean makeDefault = Boolean.TRUE.equals(request.isDefault()) || existing.isEmpty();
        address.setDefault(makeDefault);
        if (makeDefault) {
            clearDefault(existing);
        }

        return toResponse(userAddressRepository.save(address));
    }

    public AddressResponse updateAddress(String token, UUID addressId, AddressRequest request) {
        AddressContext context = resolveContext(token);
        validateRequest(request);

        UserAddress address = getAddressForContext(context, addressId);
        applyRequest(address, request);

        if (Boolean.TRUE.equals(request.isDefault())) {
            clearDefault(getAddressesForContext(context), address.getId());
            address.setDefault(true);
        }

        return toResponse(userAddressRepository.save(address));
    }

    public Message deleteAddress(String token, UUID addressId) {
        AddressContext context = resolveContext(token);
        UserAddress address = getAddressForContext(context, addressId);
        boolean wasDefault = address.isDefault();
        userAddressRepository.delete(address);

        if (wasDefault) {
            List<UserAddress> remaining = getAddressesForContext(context);
            if (!remaining.isEmpty()) {
                UserAddress nextDefault = remaining.get(0);
                nextDefault.setDefault(true);
                userAddressRepository.save(nextDefault);
            }
        }

        return new Message("Address deleted successfully");
    }

    public AddressResponse setDefaultAddress(String token, UUID addressId) {
        AddressContext context = resolveContext(token);
        UserAddress address = getAddressForContext(context, addressId);
        clearDefault(getAddressesForContext(context), address.getId());
        address.setDefault(true);
        return toResponse(userAddressRepository.save(address));
    }

    public UserAddress resolveAddress(TokenResponse tokenResponse, BookingChannel bookingChannel, UUID addressId) {
        if (addressId == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "addressId is required");
        }

        return switch (bookingChannel) {
            case TENANT -> tokenResponse.tenantId()
                    .flatMap(tenantId -> userAddressRepository.findByIdAndTenantIdAndOwnerType(addressId, tenantId,
                            AddressOwnerType.TENANT))
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Address not found"));
            case CONSUMER -> userAddressRepository
                    .findByIdAndOwnerUserIdAndOwnerType(addressId, tokenResponse.id(), AddressOwnerType.CONSUMER)
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Address not found"));
            default -> throw new CustomException(HttpStatus.BAD_REQUEST,
                    "Saved addresses are supported only for tenant and consumer");
        };
    }

    private AddressContext resolveContext(String token) {
        TokenResponse tokenResponse = Optional.ofNullable(authInterface.getUserDetails(token).getBody())
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "Invalid token"));

        if (tokenResponse.tenantId().isPresent()) {
            return new AddressContext(tokenResponse.tenantId().get(), tokenResponse.id(), AddressOwnerType.TENANT);
        }

        if (tokenResponse.id() != null && tokenResponse.userType().filter(type -> type == UserType.CONSUMER).isPresent()) {
            return new AddressContext(null, tokenResponse.id(), AddressOwnerType.CONSUMER);
        }

        throw new CustomException(HttpStatus.FORBIDDEN, "Address APIs are available only for tenant and consumer users");
    }

    private List<UserAddress> getAddressesForContext(AddressContext context) {
        if (context.ownerType() == AddressOwnerType.TENANT) {
            return userAddressRepository.findByTenantIdAndOwnerTypeOrderByIsDefaultDescCreatedAtDesc(context.tenantId(),
                    context.ownerType());
        }
        return userAddressRepository.findByOwnerUserIdAndOwnerTypeOrderByIsDefaultDescCreatedAtDesc(context.ownerUserId(),
                context.ownerType());
    }

    private UserAddress getAddressForContext(AddressContext context, UUID addressId) {
        return (context.ownerType() == AddressOwnerType.TENANT
                ? userAddressRepository.findByIdAndTenantIdAndOwnerType(addressId, context.tenantId(), context.ownerType())
                : userAddressRepository.findByIdAndOwnerUserIdAndOwnerType(addressId, context.ownerUserId(),
                        context.ownerType()))
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Address not found"));
    }

    private void applyRequest(UserAddress address, AddressRequest request) {
        address.setLabel(trimToNull(request.label()));
        address.setLine1(request.line1().trim());
        address.setLine2(trimToNull(request.line2()));
        address.setCity(request.city().trim());
        address.setState(request.state().trim());
        address.setCountry(request.country().trim());
        address.setPincode(request.pincode().trim());
        address.setLocality(trimToNull(request.locality()));
        address.setLatitude(BigDecimal.valueOf(request.latitude()));
        address.setLongitude(BigDecimal.valueOf(request.longitude()));
        address.setContactName(request.contactName().trim());
        address.setContactPhone(request.contactPhone().trim());
        address.setBusinessName(trimToNull(request.businessName()));
        address.setNotes(trimToNull(request.notes()));
    }

    private void validateRequest(AddressRequest request) {
        if (request == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Address request is required");
        }
        if (isBlank(request.line1()) || isBlank(request.city()) || isBlank(request.state()) || isBlank(request.country())
                || isBlank(request.pincode()) || request.latitude() == null || request.longitude() == null
                || isBlank(request.contactName()) || isBlank(request.contactPhone())) {
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    "line1, city, state, country, pincode, latitude, longitude, contactName and contactPhone are required");
        }
    }

    private void clearDefault(List<UserAddress> addresses) {
        clearDefault(addresses, null);
    }

    private void clearDefault(List<UserAddress> addresses, UUID excludedId) {
        for (UserAddress existing : addresses) {
            if (excludedId != null && excludedId.equals(existing.getId())) {
                continue;
            }
            if (existing.isDefault()) {
                existing.setDefault(false);
                userAddressRepository.save(existing);
            }
        }
    }

    private AddressResponse toResponse(UserAddress address) {
        return new AddressResponse(
                address.getId(),
                address.getLabel(),
                address.getLine1(),
                address.getLine2(),
                address.getCity(),
                address.getState(),
                address.getCountry(),
                address.getPincode(),
                address.getLocality(),
                address.getLatitude() != null ? address.getLatitude().doubleValue() : null,
                address.getLongitude() != null ? address.getLongitude().doubleValue() : null,
                address.getContactName(),
                address.getContactPhone(),
                address.getBusinessName(),
                address.getNotes(),
                address.isDefault());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record AddressContext(UUID tenantId, UUID ownerUserId, AddressOwnerType ownerType) {
    }
}
