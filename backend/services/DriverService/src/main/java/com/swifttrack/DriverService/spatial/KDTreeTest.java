package com.swifttrack.DriverService.spatial;

import java.util.*;

public class KDTreeTest {
    public static void main(String[] args) {
        List<DriverNode> nodes = new ArrayList<>();
        nodes.add(new DriverNode("A", 12.9716, 77.5946)); // Bangalore
        nodes.add(new DriverNode("B", 28.7041, 77.1025)); // Delhi
        nodes.add(new DriverNode("C", 19.0760, 72.8777)); // Mumbai

        KDTree tree = KDTree.build(nodes);

        List<DriverDistance> nearest = tree.findKNearest(13.0, 77.6, 2);
        for (DriverDistance d : nearest) {
            System.out.println(d.driverId() + ": " + d.distanceKm());
        }
    }
}
