package com.swifttrack.services;

import com.swifttrack.Models.DeliveryOption;
import com.swifttrack.Models.TenantDeliveryConfiguration;
import com.swifttrack.Models.TenantModel;
import com.swifttrack.dto.AddDeliveryOptionInput;
import com.swifttrack.dto.DeliveryOptionResponse;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.TenantDeliveryPriorityInput;
import com.swifttrack.dto.TokenResponse;
import com.swifttrack.dto.tenantDto.TenantDeliveryConf;
import com.swifttrack.enums.UserType;
import com.swifttrack.exception.CustomException;
import com.swifttrack.exception.ResourceNotFoundException;
import com.swifttrack.FeignClients.AuthInterface;
import com.swifttrack.repositories.CompanyRepository;
import com.swifttrack.repositories.DeliveryOptionRepository;
import com.swifttrack.repositories.TenantDeliveryConfigurationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TenantDeliveryConfigurationService {
    private static final List<String> DEFAULT_DELIVERY_OPTIONS = List.of(
            "EXTERNAL_PROVIDERS",
            "TENANT_DRIVERS",
            "LOCAL_DRIVERS");

    private final DeliveryOptionRepository deliveryOptionRepository;
    private final TenantDeliveryConfigurationRepository tenantDeliveryConfigurationRepository;
    private final CompanyRepository companyRepository;
    private final AuthInterface authInterface;

    public TenantDeliveryConfigurationService(
            DeliveryOptionRepository deliveryOptionRepository,
            TenantDeliveryConfigurationRepository tenantDeliveryConfigurationRepository,
            CompanyRepository companyRepository,
            AuthInterface authInterface) {
        this.deliveryOptionRepository = deliveryOptionRepository;
        this.tenantDeliveryConfigurationRepository = tenantDeliveryConfigurationRepository;
        this.companyRepository = companyRepository;
        this.authInterface = authInterface;
    }

    public List<DeliveryOptionResponse> getActiveDeliveryOptions() {
        ensureDefaultDeliveryOptions();
        return deliveryOptionRepository.findByActiveTrue().stream()
                .map(option -> new DeliveryOptionResponse(
                        option.getId(),
                        option.getOptionType(),
                        option.isActive()))
                .toList();
    }

    public Message addDeliveryOption(AddDeliveryOptionInput input) {
        if (input == null || input.optionType() == null || input.optionType().isBlank()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "optionType is required");
        }
        String optionType = input.optionType().trim();
        if (deliveryOptionRepository.findByOptionTypeIgnoreCase(optionType).isPresent()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Delivery option already exists: " + optionType);
        }

        deliveryOptionRepository.save(DeliveryOption.builder()
                .optionType(optionType)
                .active(true)
                .build());
        return new Message("Delivery option added successfully");
    }

    @Transactional
    public Message configureTenantDeliverySystem(String token, List<TenantDeliveryPriorityInput> configurationInput) {
        if (configurationInput == null || configurationInput.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Delivery configuration input cannot be empty");
        }
        TokenResponse userDetails = authInterface.getUserDetails(token).getBody();
        System.out.println(userDetails.userType().get());
        if (userDetails.userType() == null
                || (userDetails.userType().get() != UserType.TENANT_ADMIN
                        && userDetails.userType().get() != UserType.TENANT_USER)) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "Unauthorized to perform this action");
        }
        UUID tenantId = userDetails.id();
        validateConfigurationInput(configurationInput);
        ensureDefaultDeliveryOptions();

        Set<String> optionTypes = configurationInput.stream()
                .map(input -> input.deliveryOption().trim())
                .collect(Collectors.toSet());

        Map<String, DeliveryOption> optionMap = deliveryOptionRepository.findByOptionTypeIn(optionTypes).stream()
                .collect(Collectors.toMap(option -> option.getOptionType().trim(), option -> option));

        if (optionMap.size() != optionTypes.size()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "One or more delivery options are invalid");
        }

        for (String optionType : optionTypes) {
            DeliveryOption option = optionMap.get(optionType);
            if (option == null || !option.isActive()) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "Delivery option is inactive: " + optionType);
            }
        }

        tenantDeliveryConfigurationRepository.deleteByTenantId(tenantId);
        tenantDeliveryConfigurationRepository.flush();

        List<TenantDeliveryConfiguration> configurations = new ArrayList<>();
        for (TenantDeliveryPriorityInput input : configurationInput) {
            configurations.add(TenantDeliveryConfiguration.builder()
                    .tenantId(tenantId)
                    .deliveryOption(optionMap.get(input.deliveryOption().trim()))
                    .priority(input.priority())
                    .build());
        }

        tenantDeliveryConfigurationRepository.saveAll(configurations);
        return new Message("Tenant delivery system configured successfully");
    }

    private void validateConfigurationInput(List<TenantDeliveryPriorityInput> configurationInput) {
        Set<String> seenOptions = new HashSet<>();
        Set<Integer> seenPriorities = new HashSet<>();

        for (TenantDeliveryPriorityInput input : configurationInput) {
            if (input.deliveryOption() == null || input.deliveryOption().isBlank()) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "deliveryOption is required");
            }
            if (input.priority() == null || input.priority() <= 0) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "priority must be a positive number");
            }
            String normalizedOption = input.deliveryOption().trim();
            if (!seenOptions.add(normalizedOption)) {
                throw new CustomException(HttpStatus.BAD_REQUEST,
                        "Duplicate deliveryOption found: " + normalizedOption);
            }
            if (!seenPriorities.add(input.priority())) {
                throw new CustomException(HttpStatus.BAD_REQUEST,
                        "Duplicate priority found: " + input.priority());
            }
        }
    }

    private void ensureDefaultDeliveryOptions() {
        for (String optionType : DEFAULT_DELIVERY_OPTIONS) {
            deliveryOptionRepository.findByOptionTypeIgnoreCase(optionType)
                    .orElseGet(() -> deliveryOptionRepository.save(
                            DeliveryOption.builder()
                                    .optionType(optionType)
                                    .active(true)
                                    .build()));
        }
    }

    private UUID getAuthorizedTenantId(String token) {
        TokenResponse tokenResponse = authInterface.getUserDetails(token).getBody();
        if (tokenResponse == null || tokenResponse.id() == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "Invalid token or user not found");
        }

        UserType userType = tokenResponse.userType().orElse(null);
        boolean isAuthorizedUser = userType == UserType.TENANT_ADMIN || userType == UserType.TENANT_USER;
        if (!isAuthorizedUser) {
            throw new CustomException(HttpStatus.FORBIDDEN,
                    "Only TENANT_ADMIN or TENANT_USER can configure delivery system");
        }

        return tokenResponse.tenantId()
                .orElseThrow(() -> new CustomException(HttpStatus.FORBIDDEN, "Tenant ID not found in token"));
    }

    public List<TenantDeliveryConf> getTenantDeliveryConfiguration(String token) {
        UUID tenantId = getAuthorizedTenantId(token);
        List<TenantDeliveryConfiguration> configurations = tenantDeliveryConfigurationRepository
                .findByTenantId(tenantId);
        return configurations.stream()
                .sorted(Comparator.comparing(TenantDeliveryConfiguration::getPriority))
                .map(configuration -> new TenantDeliveryConf(
                        configuration.getDeliveryOption().getOptionType(),
                        configuration.getPriority()))
                .toList();
    }
}
