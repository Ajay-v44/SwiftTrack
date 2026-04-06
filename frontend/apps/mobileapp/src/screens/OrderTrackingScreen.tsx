import React, { useEffect, useMemo, useState } from 'react';
import { ActivityIndicator, Linking, ScrollView, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store/store';
import { getAcceptedOrders, getPendingOrders, respondToAssignment, updateOrderStatus } from '../store/ordersSlice';
import { ArrowLeft, CheckCircle, Clock3, MapPinned, Navigation, PackageCheck, XCircle } from 'lucide-react-native';
import { useNavigation, useRoute } from '@react-navigation/native';
import * as Burnt from 'burnt';
import apiClient from '../api/client';
import { Colors } from '../theme/colors';
import OlaMapView from '../components/OlaMapView';

type TrackingStatus = 'PICKED_UP' | 'IN_TRANSIT' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'FAILED';

type OrderDetails = {
  id: string;
  customerReferenceId?: string | null;
  orderStatus?: string | null;
  trackingStatus?: TrackingStatus | null;
  city?: string | null;
  state?: string | null;
  pickupLat?: number | null;
  pickupLng?: number | null;
  dropoffLat?: number | null;
  dropoffLng?: number | null;
  pickup?: {
    locality?: string | null;
    city?: string | null;
    state?: string | null;
    pincode?: string | null;
    latitude?: number | null;
    longitude?: number | null;
  } | null;
  dropoff?: {
    locality?: string | null;
    city?: string | null;
    state?: string | null;
    pincode?: string | null;
    latitude?: number | null;
    longitude?: number | null;
  } | null;
  currentLocation?: {
    latitude?: number | null;
    longitude?: number | null;
    updatedAt?: string | null;
  } | null;
};

const TRACKING_FLOW: TrackingStatus[] = ['PICKED_UP', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED'];

function formatLabel(value?: string | null) {
  if (!value) {
    return 'Not available';
  }
  return value.replace(/_/g, ' ');
}

function buildAddress(location?: OrderDetails['pickup']) {
  if (!location) {
    return 'Address unavailable';
  }

  const parts = [location.locality, location.city, location.state, location.pincode].filter(Boolean);
  return parts.length ? parts.join(', ') : 'Address unavailable';
}

export default function OrderTrackingScreen() {
  const navigation = useNavigation();
  const route = useRoute<any>();
  const dispatch = useDispatch<AppDispatch>();
  const { orderId } = route.params ?? {};
  const { acceptedOrders, pendingOrders } = useSelector((state: RootState) => state.orders);
  const cachedOrder = useMemo(
    () => [...pendingOrders, ...acceptedOrders].find((item) => item.id === orderId),
    [acceptedOrders, orderId, pendingOrders]
  );

  const [orderDetails, setOrderDetails] = useState<OrderDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [mapCenter, setMapCenter] = useState({ latitude: 28.6139, longitude: 77.209 });
  const [zoom, setZoom] = useState(13);

  useEffect(() => {
    const loadOrder = async () => {
      if (!orderId) {
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        const response = await apiClient.get(`/orderservice/api/order/v1/getOrderById/${orderId}`);
        setOrderDetails(response.data as OrderDetails);
      } catch (error: any) {
        const message = error.response?.data?.message || 'Failed to load order details';
        Burnt.toast({ title: message, preset: 'error' });
      } finally {
        setLoading(false);
      }
    };

    void loadOrder();
  }, [orderId]);

  const mergedOrder = orderDetails ?? (cachedOrder ? {
    ...cachedOrder,
    trackingStatus: cachedOrder.orderStatus as TrackingStatus | null,
  } : null);

  const pickupCoordinate = useMemo(
    () =>
      mergedOrder?.pickupLat != null && mergedOrder?.pickupLng != null
        ? { latitude: mergedOrder.pickupLat, longitude: mergedOrder.pickupLng }
        : null,
    [mergedOrder?.pickupLat, mergedOrder?.pickupLng]
  );
  const dropoffCoordinate = useMemo(
    () =>
      mergedOrder?.dropoffLat != null && mergedOrder?.dropoffLng != null
        ? { latitude: mergedOrder.dropoffLat, longitude: mergedOrder.dropoffLng }
        : null,
    [mergedOrder?.dropoffLat, mergedOrder?.dropoffLng]
  );
  const liveCoordinate = useMemo(
    () =>
      mergedOrder?.currentLocation?.latitude != null && mergedOrder?.currentLocation?.longitude != null
        ? {
            latitude: mergedOrder.currentLocation.latitude,
            longitude: mergedOrder.currentLocation.longitude,
          }
        : null,
    [mergedOrder?.currentLocation?.latitude, mergedOrder?.currentLocation?.longitude]
  );

  useEffect(() => {
    const preferredCenter = liveCoordinate ?? pickupCoordinate ?? dropoffCoordinate;
    if (!preferredCenter) {
      return;
    }

    setMapCenter((previous) => {
      if (
        Math.abs(previous.latitude - preferredCenter.latitude) < 0.000001 &&
        Math.abs(previous.longitude - preferredCenter.longitude) < 0.000001
      ) {
        return previous;
      }

      return preferredCenter;
    });
  }, [dropoffCoordinate, liveCoordinate, pickupCoordinate]);

  const isAssigned = mergedOrder?.orderStatus === 'ASSIGNED';
  const trackingStatus = (mergedOrder?.trackingStatus ?? null) as TrackingStatus | null;
  const currentStatusIndex = trackingStatus ? TRACKING_FLOW.findIndex((item) => item === trackingStatus) : -1;
  const nextStatus = currentStatusIndex >= 0 && currentStatusIndex < TRACKING_FLOW.length - 1
    ? TRACKING_FLOW[currentStatusIndex + 1]
    : currentStatusIndex === -1 && mergedOrder?.orderStatus === 'ACCEPTED'
      ? TRACKING_FLOW[0]
      : null;

  const handleRespond = async (accept: boolean) => {
    if (!orderId) {
      return;
    }

    try {
      setActionLoading(true);
      await dispatch(respondToAssignment({ orderId, accept })).unwrap();
      await Promise.all([
        dispatch(getPendingOrders()).unwrap(),
        dispatch(getAcceptedOrders()).unwrap(),
      ]);
      Burnt.toast({
        title: accept ? 'Order accepted' : 'Order rejected',
        preset: accept ? 'done' : 'none',
      });
      if (!accept) {
        navigation.goBack();
        return;
      }
      const response = await apiClient.get(`/orderservice/api/order/v1/getOrderById/${orderId}`);
      setOrderDetails(response.data as OrderDetails);
    } catch (error: any) {
      Burnt.toast({ title: error?.message || 'Failed', preset: 'error' });
    } finally {
      setActionLoading(false);
    }
  };

  const handleUpdateStatus = async () => {
    if (!orderId || !nextStatus) {
      return;
    }

    try {
      setActionLoading(true);
      await dispatch(updateOrderStatus({ orderId, status: nextStatus })).unwrap();
      await dispatch(getAcceptedOrders()).unwrap();
      const response = await apiClient.get(`/orderservice/api/order/v1/getOrderById/${orderId}`);
      setOrderDetails(response.data as OrderDetails);
      Burnt.toast({ title: formatLabel(nextStatus), preset: 'done' });
    } catch (error: any) {
      Burnt.toast({ title: error?.message || 'Failed to update status', preset: 'error' });
    } finally {
      setActionLoading(false);
    }
  };

  const openExternalMap = () => {
    if (!dropoffCoordinate && !pickupCoordinate) {
      return;
    }

    const target = dropoffCoordinate ?? pickupCoordinate;
    const url = `https://www.google.com/maps/search/?api=1&query=${target?.latitude},${target?.longitude}`;
    Linking.openURL(url).catch(() => {
      Burnt.toast({ title: 'Unable to open maps', preset: 'error' });
    });
  };

  if (loading) {
    return (
      <View style={styles.centerState}>
        <ActivityIndicator size="large" color={Colors.primary} />
        <Text style={styles.centerText}>Loading order details…</Text>
      </View>
    );
  }

  if (!mergedOrder) {
    return (
      <View style={styles.centerState}>
        <Text style={styles.centerText}>Order not found</Text>
        <TouchableOpacity style={styles.secondaryBtn} onPress={() => navigation.goBack()} activeOpacity={0.8}>
          <Text style={styles.secondaryBtnText}>Go Back</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.mapContainer}>
        <TouchableOpacity style={styles.backBtn} onPress={() => navigation.goBack()} activeOpacity={0.7}>
          <ArrowLeft color={Colors.textPrimary} size={22} />
        </TouchableOpacity>
        <OlaMapView
          center={mapCenter}
          zoom={zoom}
          onCenterChange={setMapCenter}
          onZoomChange={setZoom}
          markers={[
            ...(pickupCoordinate ? [{ ...pickupCoordinate, color: Colors.accentTeal, label: 'Pickup' }] : []),
            ...(dropoffCoordinate ? [{ ...dropoffCoordinate, color: Colors.accent, label: 'Drop' }] : []),
            ...(liveCoordinate ? [{ ...liveCoordinate, color: Colors.accentGreen, label: 'Live' }] : []),
          ]}
          polyline={pickupCoordinate && dropoffCoordinate ? [pickupCoordinate, dropoffCoordinate] : []}
        />
      </View>

      <ScrollView style={styles.details} contentContainerStyle={styles.detailsContent} showsVerticalScrollIndicator={false}>
        <View style={styles.dragIndicator} />

        <View style={styles.headerRow}>
          <View>
            <Text style={styles.orderId}>{mergedOrder.customerReferenceId || 'Order'}</Text>
            <Text style={styles.orderMeta}>
              {formatLabel(mergedOrder.orderStatus)}{trackingStatus ? ` • ${formatLabel(trackingStatus)}` : ''}
            </Text>
          </View>
          <TouchableOpacity style={styles.iconBtn} onPress={openExternalMap} activeOpacity={0.8}>
            <Navigation color={Colors.textPrimary} size={18} />
          </TouchableOpacity>
        </View>

        <View style={styles.summaryCard}>
          <View style={styles.summaryRow}>
            <MapPinned color={Colors.accentTeal} size={18} />
            <View style={styles.summaryCopy}>
              <Text style={styles.summaryLabel}>Pickup</Text>
              <Text style={styles.summaryValue}>{buildAddress(mergedOrder.pickup)}</Text>
            </View>
          </View>
          <View style={styles.summaryDivider} />
          <View style={styles.summaryRow}>
            <PackageCheck color={Colors.accent} size={18} />
            <View style={styles.summaryCopy}>
              <Text style={styles.summaryLabel}>Drop</Text>
              <Text style={styles.summaryValue}>{buildAddress(mergedOrder.dropoff)}</Text>
            </View>
          </View>
        </View>

        <View style={styles.infoGrid}>
          <View style={styles.infoCard}>
            <Text style={styles.infoLabel}>City</Text>
            <Text style={styles.infoValue}>{mergedOrder.city || 'Unknown'}</Text>
          </View>
          <View style={styles.infoCard}>
            <Text style={styles.infoLabel}>State</Text>
            <Text style={styles.infoValue}>{mergedOrder.state || 'Unknown'}</Text>
          </View>
        </View>

        <View style={styles.timeline}>
          <View style={styles.timelineHeader}>
            <Clock3 color={Colors.textPrimary} size={16} />
            <Text style={styles.timelineTitle}>Delivery Progress</Text>
          </View>
          {TRACKING_FLOW.map((status, index) => {
            const isCompleted = currentStatusIndex >= index;
            const isCurrent = currentStatusIndex === index;
            const isLast = index === TRACKING_FLOW.length - 1;

            return (
              <View key={status} style={styles.timelineItem}>
                <View style={styles.timelineLeft}>
                  <View
                    style={[
                      styles.timelineDot,
                      isCompleted && { backgroundColor: Colors.accentGreen },
                      isCurrent && styles.timelineDotCurrent,
                    ]}
                  />
                  {!isLast ? (
                    <View style={[styles.timelineLine, isCompleted && { backgroundColor: Colors.accentGreen }]} />
                  ) : null}
                </View>
                <Text style={[styles.timelineText, isCompleted && { color: Colors.textPrimary }]}>
                  {formatLabel(status)}
                </Text>
              </View>
            );
          })}
        </View>

        {isAssigned ? (
          <View style={styles.actionRow}>
            <TouchableOpacity
              style={[styles.rejectBtn, actionLoading && styles.disabled]}
              onPress={() => void handleRespond(false)}
              disabled={actionLoading}
              activeOpacity={0.8}
            >
              <XCircle color={Colors.accent} size={18} />
              <Text style={styles.rejectBtnText}>Reject</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.acceptBtn, actionLoading && styles.disabled]}
              onPress={() => void handleRespond(true)}
              disabled={actionLoading}
              activeOpacity={0.8}
            >
              <CheckCircle color="#FFFFFF" size={18} />
              <Text style={styles.acceptBtnText}>Accept Order</Text>
            </TouchableOpacity>
          </View>
        ) : null}

        {!isAssigned && nextStatus ? (
          <TouchableOpacity
            style={[styles.primaryBtn, actionLoading && styles.disabled]}
            onPress={() => void handleUpdateStatus()}
            disabled={actionLoading}
            activeOpacity={0.8}
          >
            <Text style={styles.primaryBtnText}>Update Status to {formatLabel(nextStatus)}</Text>
          </TouchableOpacity>
        ) : null}

        {!isAssigned && !nextStatus ? (
          <View style={styles.completeBanner}>
            <CheckCircle color={Colors.accentGreen} size={20} />
            <Text style={styles.completeText}>This shipment has no further driver actions pending.</Text>
          </View>
        ) : null}

        <TouchableOpacity style={styles.secondaryBtn} onPress={openExternalMap} activeOpacity={0.8}>
          <Navigation color={Colors.textPrimary} size={16} />
          <Text style={styles.secondaryBtnText}>Open Destination in Maps</Text>
        </TouchableOpacity>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.bgDark },
  centerState: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: Colors.bgDark,
    paddingHorizontal: 24,
    gap: 14,
  },
  centerText: {
    color: Colors.textSecondary,
    fontSize: 16,
    textAlign: 'center',
  },
  mapContainer: { height: '38%', width: '100%' },
  backBtn: {
    position: 'absolute',
    top: 56,
    left: 20,
    zIndex: 10,
    backgroundColor: Colors.bgCard,
    padding: 12,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: Colors.borderLight,
  },
  details: {
    flex: 1,
    backgroundColor: Colors.bgCard,
    marginTop: -24,
    borderTopLeftRadius: 28,
    borderTopRightRadius: 28,
  },
  detailsContent: {
    padding: 24,
    paddingBottom: 40,
  },
  dragIndicator: {
    width: 42,
    height: 4,
    borderRadius: 999,
    backgroundColor: Colors.textMuted,
    alignSelf: 'center',
    opacity: 0.35,
    marginBottom: 20,
  },
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 18,
    gap: 16,
  },
  orderId: {
    color: Colors.textPrimary,
    fontSize: 22,
    fontWeight: '800',
  },
  orderMeta: {
    color: Colors.textSecondary,
    fontSize: 14,
    marginTop: 4,
  },
  iconBtn: {
    width: 40,
    height: 40,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: Colors.bgDark,
    borderWidth: 1,
    borderColor: Colors.borderLight,
  },
  summaryCard: {
    backgroundColor: Colors.bgDark,
    borderRadius: 18,
    padding: 16,
    borderWidth: 1,
    borderColor: Colors.borderLight,
    marginBottom: 16,
  },
  summaryRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: 12,
  },
  summaryCopy: {
    flex: 1,
  },
  summaryLabel: {
    color: Colors.textMuted,
    fontSize: 12,
    fontWeight: '700',
    textTransform: 'uppercase',
    marginBottom: 4,
  },
  summaryValue: {
    color: Colors.textPrimary,
    fontSize: 14,
    lineHeight: 20,
    fontWeight: '600',
  },
  summaryDivider: {
    height: 1,
    backgroundColor: Colors.borderLight,
    marginVertical: 14,
  },
  infoGrid: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 16,
  },
  infoCard: {
    flex: 1,
    backgroundColor: Colors.bgDark,
    borderRadius: 16,
    padding: 14,
    borderWidth: 1,
    borderColor: Colors.borderLight,
  },
  infoLabel: {
    color: Colors.textMuted,
    fontSize: 12,
    marginBottom: 6,
    textTransform: 'uppercase',
    fontWeight: '700',
  },
  infoValue: {
    color: Colors.textPrimary,
    fontSize: 15,
    fontWeight: '700',
  },
  timeline: {
    backgroundColor: Colors.bgDark,
    borderRadius: 18,
    padding: 16,
    borderWidth: 1,
    borderColor: Colors.borderLight,
    marginBottom: 16,
  },
  timelineHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginBottom: 14,
  },
  timelineTitle: {
    color: Colors.textPrimary,
    fontSize: 16,
    fontWeight: '700',
  },
  timelineItem: {
    flexDirection: 'row',
    alignItems: 'flex-start',
  },
  timelineLeft: {
    width: 24,
    alignItems: 'center',
    marginRight: 12,
  },
  timelineDot: {
    width: 12,
    height: 12,
    borderRadius: 6,
    backgroundColor: Colors.textMuted,
    zIndex: 2,
  },
  timelineDotCurrent: {
    width: 16,
    height: 16,
    borderRadius: 8,
    borderWidth: 3,
    borderColor: Colors.primary,
    backgroundColor: Colors.bgCard,
  },
  timelineLine: {
    width: 2,
    height: 34,
    marginTop: -2,
    backgroundColor: Colors.textMuted,
    opacity: 0.35,
  },
  timelineText: {
    color: Colors.textMuted,
    fontSize: 15,
    fontWeight: '600',
    paddingBottom: 24,
  },
  actionRow: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 16,
  },
  rejectBtn: {
    flex: 1,
    height: 50,
    borderRadius: 14,
    backgroundColor: Colors.accent + '12',
    borderWidth: 1,
    borderColor: Colors.accent + '30',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
  },
  rejectBtnText: {
    color: Colors.accent,
    fontSize: 15,
    fontWeight: '700',
  },
  acceptBtn: {
    flex: 1.3,
    height: 50,
    borderRadius: 14,
    backgroundColor: Colors.accentGreen,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
  },
  acceptBtnText: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '700',
  },
  primaryBtn: {
    height: 52,
    borderRadius: 14,
    backgroundColor: Colors.primary,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 16,
  },
  primaryBtnText: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '700',
  },
  secondaryBtn: {
    height: 48,
    borderRadius: 14,
    backgroundColor: Colors.bgDark,
    borderWidth: 1,
    borderColor: Colors.borderLight,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    gap: 8,
  },
  secondaryBtnText: {
    color: Colors.textPrimary,
    fontSize: 14,
    fontWeight: '700',
  },
  completeBanner: {
    borderRadius: 14,
    padding: 16,
    marginBottom: 16,
    backgroundColor: Colors.accentGreen + '14',
    borderWidth: 1,
    borderColor: Colors.accentGreen + '30',
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  completeText: {
    color: Colors.accentGreen,
    fontSize: 14,
    fontWeight: '700',
    flex: 1,
  },
  disabled: {
    opacity: 0.5,
  },
});
