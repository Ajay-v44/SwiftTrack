package com.swifttrack.AuthService.exception;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
        Throwable throwable = getError(webRequest);

        if (throwable instanceof CustomException) {
            CustomException customException = (CustomException) throwable;
            errorAttributes.put("status", customException.getStatus().value());
            errorAttributes.put("message", customException.getMessage());
        } else if (throwable instanceof ResourceNotFoundException) {
            ResourceNotFoundException ex = (ResourceNotFoundException) throwable;
            errorAttributes.put("status", HttpStatus.NOT_FOUND.value());
            errorAttributes.put("message", ex.getMessage());
        }

        return errorAttributes;
    }
}
