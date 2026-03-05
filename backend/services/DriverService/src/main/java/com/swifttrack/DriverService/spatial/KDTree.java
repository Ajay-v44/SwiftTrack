package com.swifttrack.DriverService.spatial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * 2D KD-Tree for lat/lon points.
 * Build is O(n log n) average using quickselect partitioning.
 * KNN query is O(log n) average, O(n) in the worst case.
 */
public final class KDTree {

    private static final KDTree EMPTY = new KDTree(null, 0);
    private static final double EARTH_RADIUS_KM = 6_371.0088d;

    private final DriverNode root;
    private final int size;

    private KDTree(DriverNode root, int size) {
        this.root = root;
        this.size = size;
    }

    public static KDTree empty() {
        return EMPTY;
    }

    public static KDTree build(List<DriverNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return EMPTY;
        }

        List<DriverNode> buffer = new ArrayList<>(nodes);
        DriverNode root = build(buffer, 0, buffer.size() - 1, 0);
        return new KDTree(root, buffer.size());
    }

    public int size() {
        return size;
    }

    public List<DriverDistance> findKNearest(double latitude, double longitude, int k) {
        if (k <= 0 || root == null) {
            return Collections.emptyList();
        }

        PriorityQueue<DriverDistance> maxHeap = new PriorityQueue<>(
                Comparator.comparingDouble(DriverDistance::distanceKm).reversed());

        searchKNearest(root, latitude, longitude, k, 0, maxHeap);

        List<DriverDistance> nearest = new ArrayList<>(maxHeap);
        nearest.sort(Comparator.comparingDouble(DriverDistance::distanceKm));
        return nearest;
    }

    private static DriverNode build(List<DriverNode> nodes, int left, int right, int depth) {
        if (left > right) {
            return null;
        }

        int axis = depth & 1;
        int median = (left + right) >>> 1;
        nthElement(nodes, left, right, median, axis);

        DriverNode node = nodes.get(median);
        node.setLeft(build(nodes, left, median - 1, depth + 1));
        node.setRight(build(nodes, median + 1, right, depth + 1));
        return node;
    }

    private static void nthElement(List<DriverNode> nodes, int left, int right, int n, int axis) {
        int l = left;
        int r = right;

        while (l < r) {
            int pivotIndex = partition(nodes, l, r, (l + r) >>> 1, axis);
            if (pivotIndex == n) {
                return;
            }
            if (n < pivotIndex) {
                r = pivotIndex - 1;
            } else {
                l = pivotIndex + 1;
            }
        }
    }

    private static int partition(List<DriverNode> nodes, int left, int right, int pivotIndex, int axis) {
        DriverNode pivot = nodes.get(pivotIndex);
        swap(nodes, pivotIndex, right);
        int storeIndex = left;

        for (int i = left; i < right; i++) {
            if (compareByAxis(nodes.get(i), pivot, axis) < 0) {
                swap(nodes, storeIndex++, i);
            }
        }

        swap(nodes, right, storeIndex);
        return storeIndex;
    }

    private static void swap(List<DriverNode> nodes, int i, int j) {
        DriverNode temp = nodes.get(i);
        nodes.set(i, nodes.get(j));
        nodes.set(j, temp);
    }

    private static int compareByAxis(DriverNode a, DriverNode b, int axis) {
        double primaryA = axis == 0 ? a.getLatitude() : a.getLongitude();
        double primaryB = axis == 0 ? b.getLatitude() : b.getLongitude();
        int primaryCompare = Double.compare(primaryA, primaryB);
        if (primaryCompare != 0) {
            return primaryCompare;
        }

        double secondaryA = axis == 0 ? a.getLongitude() : a.getLatitude();
        double secondaryB = axis == 0 ? b.getLongitude() : b.getLatitude();
        return Double.compare(secondaryA, secondaryB);
    }

    private static void searchKNearest(
            DriverNode node,
            double targetLat,
            double targetLon,
            int k,
            int depth,
            PriorityQueue<DriverDistance> maxHeap) {

        if (node == null) {
            return;
        }

        double distance = haversineKm(targetLat, targetLon, node.getLatitude(), node.getLongitude());
        if (maxHeap.size() < k) {
            maxHeap.offer(new DriverDistance(node.getDriverId(), distance));
        } else if (distance < maxHeap.peek().distanceKm()) {
            maxHeap.poll();
            maxHeap.offer(new DriverDistance(node.getDriverId(), distance));
        }

        int axis = depth & 1;
        double targetCoordinate = axis == 0 ? targetLat : targetLon;
        double nodeCoordinate = axis == 0 ? node.getLatitude() : node.getLongitude();

        DriverNode near = targetCoordinate < nodeCoordinate ? node.getLeft() : node.getRight();
        DriverNode far = near == node.getLeft() ? node.getRight() : node.getLeft();

        searchKNearest(near, targetLat, targetLon, k, depth + 1, maxHeap);

        double worstDistance = maxHeap.size() < k ? Double.POSITIVE_INFINITY : maxHeap.peek().distanceKm();
        double axisDistanceKm = axisDistanceKm(axis, targetLat, targetCoordinate, nodeCoordinate);

        if (axisDistanceKm <= worstDistance) {
            searchKNearest(far, targetLat, targetLon, k, depth + 1, maxHeap);
        }
    }

    private static double axisDistanceKm(int axis, double targetLat, double targetCoordinate, double nodeCoordinate) {
        double diff = Math.abs(targetCoordinate - nodeCoordinate);
        if (axis == 0) {
            return 111.32d * diff;
        }
        return 111.32d * diff * Math.cos(Math.toRadians(targetLat));
    }

    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double sinLat = Math.sin(dLat / 2.0d);
        double sinLon = Math.sin(dLon / 2.0d);

        double a = sinLat * sinLat
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * sinLon * sinLon;

        double c = 2.0d * Math.atan2(Math.sqrt(a), Math.sqrt(1.0d - a));
        return EARTH_RADIUS_KM * c;
    }
}
