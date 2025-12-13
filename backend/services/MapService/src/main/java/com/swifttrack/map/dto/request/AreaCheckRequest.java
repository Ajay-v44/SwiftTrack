package com.swifttrack.map.dto.request;

import com.swifttrack.map.dto.Coordinates;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for Serviceability/Area Check API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaCheckRequest {
    
    @NotNull(message = "Point is required")
    @Valid
    private Coordinates point;
    
    @NotEmpty(message = "Polygon must have at least 3 points")
    @Size(min = 3, message = "Polygon must have at least 3 points")
    @Valid
    private List<Coordinates> polygon;
}
