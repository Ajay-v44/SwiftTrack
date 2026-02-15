package com.swifttrack.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.swifttrack.FeignClients.AuthInterface;
import com.swifttrack.Models.NotificationPreferences;
import com.swifttrack.dto.NotificationPreferenceDto;
import com.swifttrack.dto.TokenResponse;
import com.swifttrack.exception.CustomException;
import com.swifttrack.mappers.NotificationMapper;
import com.swifttrack.repositories.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final AuthInterface authInterface;

    public NotificationService(NotificationRepository notificationRepository, NotificationMapper notificationMapper,
            AuthInterface authInterface) {
        this.authInterface = authInterface;
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    public NotificationPreferenceDto getNotificationPreferences(String token) {
        TokenResponse tokenResponse = authInterface.getUserDetails(token).getBody();
        NotificationPreferences notificationPreferences = notificationRepository.findByTenantId(
                tokenResponse.tenantId().orElseThrow(() -> new RuntimeException("Tenant ID not found in token")));
        if (notificationPreferences == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Notification preferences not found");
        return notificationMapper.toNotificationPreferenceDto(notificationPreferences);
    }

    public NotificationPreferenceDto updateNotificationPreferences(String token,
            NotificationPreferenceDto notificationPreferenceDto) {
        TokenResponse tokenResponse = authInterface.getUserDetails(token).getBody();
        NotificationPreferences notificationPreferences = notificationRepository.findByTenantId(
                tokenResponse.tenantId().orElseThrow(() -> new RuntimeException("Tenant ID not found in token")));
        if (notificationPreferences != null)
            return notificationMapper.toNotificationPreferenceDto(notificationRepository
                    .save(notificationMapper.toNotificationPreference(notificationPreferenceDto)));
        NotificationPreferences newNotificationPreferences = notificationMapper
                .toNotificationPreference(notificationPreferenceDto);
        return notificationMapper.toNotificationPreferenceDto(notificationRepository.save(newNotificationPreferences));
    }
}
