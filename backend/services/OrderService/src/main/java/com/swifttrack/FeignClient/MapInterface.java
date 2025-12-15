package com.swifttrack.FeignClient;

import com.swifttrack.dto.map.ApiResponse;
import com.swifttrack.dto.map.DistanceResult;
import com.swifttrack.dto.map.MatrixRequest;
import com.swifttrack.dto.map.MatrixResponse;
import com.swifttrack.dto.map.NormalizedLocation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "MAPSERVICE", url = "http://localhost:8080/map")
public interface MapInterface {

    @GetMapping("/reverse")
    ApiResponse<NormalizedLocation> reverseGeocode(
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng);

    @GetMapping("/distance")
    ApiResponse<DistanceResult> calculateDistance(
            @RequestParam("origin_lat") double originLat,
            @RequestParam("origin_lng") double originLng,
            @RequestParam("dest_lat") double destLat,
            @RequestParam("dest_lng") double destLng);

    @PostMapping("/matrix")
    ApiResponse<MatrixResponse> calculateMatrix(
            @RequestBody MatrixRequest request);
}
