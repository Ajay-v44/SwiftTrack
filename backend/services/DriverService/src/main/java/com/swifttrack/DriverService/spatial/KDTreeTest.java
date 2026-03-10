package com.swifttrack.DriverService.spatial;

import java.util.*;

import com.swifttrack.DriverService.enums.DriverType;

public class KDTreeTest {
    public static void main(String[] args) {
        List<DriverNode> nodes = new ArrayList<>();
        nodes.add(new DriverNode("A", 12.9716, 77.5946, null, DriverType.PLATFORM_DRIVER)); // Bangalore
        nodes.add(new DriverNode("B", 28.7041, 77.1025, null, DriverType.PLATFORM_DRIVER)); // Delhi
        nodes.add(new DriverNode("C", 19.0760, 72.8777, null, DriverType.PLATFORM_DRIVER)); // Mumbai

        KDTree tree = KDTree.build(nodes);

        List<DriverDistance> nearest = tree.findKNearest(13.0, 77.6, 2);
        for (DriverDistance d : nearest) {
            System.out.println(d.driverId() + ": " + d.distanceKm());
        }
    }
}
