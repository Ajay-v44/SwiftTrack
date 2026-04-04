import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, Linking, Platform } from 'react-native';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store/store';
import { updateOrderStatus } from '../store/ordersSlice';
import { ArrowLeft, Phone, MessageSquare, CheckCircle } from 'lucide-react-native';
import { useNavigation, useRoute } from '@react-navigation/native';
import MapView, { Marker } from 'react-native-maps';
import * as Burnt from 'burnt';
import { Colors } from '../theme/colors';

/**
 * Backend: TrackingStatus { PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, FAILED }
 */
const STATUSES = ['PICKED_UP', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED'] as const;

export default function OrderTrackingScreen() {
  const navigation = useNavigation();
  const route = useRoute<any>();
  const dispatch = useDispatch<AppDispatch>();
  const { orderId } = route.params;

  const { acceptedOrders } = useSelector((state: RootState) => state.orders);
  const order = acceptedOrders.find(o => o.id === orderId);

  const currentStatusIndex = order ? STATUSES.findIndex(s => s === order.orderStatus) : -1;

  const handleUpdateStatus = async () => {
    if (currentStatusIndex >= 0 && currentStatusIndex < STATUSES.length - 1) {
      const nextStatus = STATUSES[currentStatusIndex + 1];
      try {
        await dispatch(updateOrderStatus({ orderId, status: nextStatus })).unwrap();
        Burnt.toast({ title: `✅ ${nextStatus.replace(/_/g, ' ')}`, preset: 'done' });
      } catch (err: any) {
        Burnt.toast({ title: typeof err === 'string' ? err : 'Failed', preset: 'error' });
      }
    }
  };

  if (!order) {
    return (
      <View style={[styles.container, { justifyContent: 'center', alignItems: 'center' }]}>
        <Text style={{ color: Colors.textSecondary, fontSize: 16 }}>Order not found</Text>
        <TouchableOpacity style={{ marginTop: 16 }} onPress={() => navigation.goBack()}>
          <Text style={{ color: Colors.primaryLight, fontWeight: '600' }}>Go Back</Text>
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
        <MapView
          style={styles.map}
          initialRegion={{
            latitude: order.pickupLat || 37.78825, longitude: order.pickupLng || -122.4324,
            latitudeDelta: 0.0922, longitudeDelta: 0.0421,
          }}
        >
          {order.pickupLat && (
            <Marker coordinate={{ latitude: order.pickupLat, longitude: order.pickupLng }} title="Pickup" pinColor="blue" />
          )}
          {order.dropoffLat && (
            <Marker coordinate={{ latitude: order.dropoffLat, longitude: order.dropoffLng }} title="Dropoff" pinColor="red" />
          )}
        </MapView>
      </View>

      <ScrollView style={styles.details} contentContainerStyle={{ paddingBottom: 40 }} showsVerticalScrollIndicator={false}>
        <View style={styles.dragIndicator} />

        <View style={styles.orderHeader}>
          <Text style={styles.orderId}>{order.customerReferenceId || 'Order'}</Text>
          <Text style={styles.orderLocation}>{order.city}, {order.state}</Text>
        </View>

        <View style={styles.timeline}>
          <Text style={styles.timelineTitle}>Shipment Timeline</Text>
          {STATUSES.map((status, index) => {
            const isCompleted = index <= currentStatusIndex;
            const isCurrent = index === currentStatusIndex;
            const isLast = index === STATUSES.length - 1;
            return (
              <View key={status} style={styles.timelineItem}>
                <View style={styles.timelineLeft}>
                  <View style={[
                    styles.timelineDot,
                    isCompleted && { backgroundColor: Colors.accentGreen },
                    isCurrent && styles.dotCurrent,
                  ]} />
                  {!isLast && <View style={[styles.timelineLine, isCompleted && { backgroundColor: Colors.accentGreen }]} />}
                </View>
                <Text style={[styles.timelineText, isCompleted && { color: Colors.textPrimary }]}>
                  {status.replace(/_/g, ' ')}
                  {isCurrent ? '  ← Current' : ''}
                </Text>
              </View>
            );
          })}
        </View>

        {currentStatusIndex >= 0 && currentStatusIndex < STATUSES.length - 1 && (
          <TouchableOpacity style={styles.updateBtn} onPress={handleUpdateStatus} activeOpacity={0.8}>
            <Text style={styles.updateBtnText}>
              Update → {STATUSES[currentStatusIndex + 1].replace(/_/g, ' ')}
            </Text>
          </TouchableOpacity>
        )}

        {currentStatusIndex === STATUSES.length - 1 && (
          <View style={styles.deliveredBanner}>
            <CheckCircle color={Colors.accentGreen} size={24} />
            <Text style={styles.deliveredText}>Order Delivered!</Text>
          </View>
        )}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.bgDark },
  mapContainer: { height: '40%', width: '100%' },
  map: { ...StyleSheet.absoluteFillObject },
  backBtn: {
    position: 'absolute', top: 56, left: 20, zIndex: 10,
    backgroundColor: Colors.bgCard, padding: 12, borderRadius: 14,
    borderWidth: 1, borderColor: Colors.borderLight,
  },
  details: {
    flex: 1, backgroundColor: Colors.bgCard, marginTop: -24,
    borderTopLeftRadius: 28, borderTopRightRadius: 28, padding: 24,
  },
  dragIndicator: {
    width: 40, height: 4, backgroundColor: Colors.textMuted, borderRadius: 2,
    alignSelf: 'center', marginBottom: 24, opacity: 0.4,
  },
  orderHeader: { marginBottom: 24 },
  orderId: { fontSize: 22, fontWeight: '800', color: Colors.textPrimary },
  orderLocation: { fontSize: 14, color: Colors.textSecondary, marginTop: 4 },
  timeline: { marginBottom: 24 },
  timelineTitle: { fontSize: 16, fontWeight: '700', color: Colors.textPrimary, marginBottom: 16 },
  timelineItem: { flexDirection: 'row', alignItems: 'flex-start' },
  timelineLeft: { alignItems: 'center', width: 24, marginRight: 12 },
  timelineDot: {
    width: 12, height: 12, borderRadius: 6, backgroundColor: Colors.textMuted, zIndex: 2,
  },
  dotCurrent: {
    width: 16, height: 16, borderRadius: 8, borderWidth: 3,
    borderColor: Colors.accentTeal, backgroundColor: Colors.bgCard,
  },
  timelineLine: {
    width: 2, height: 36, backgroundColor: Colors.textMuted, marginTop: -2, opacity: 0.3,
  },
  timelineText: { fontSize: 15, fontWeight: '600', color: Colors.textMuted, paddingBottom: 28 },
  updateBtn: {
    backgroundColor: Colors.primary, padding: 16, borderRadius: 14, alignItems: 'center',
    shadowColor: Colors.primary, shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.3, shadowRadius: 12, elevation: 6,
  },
  updateBtnText: { color: '#FFFFFF', fontSize: 16, fontWeight: '700' },
  deliveredBanner: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'center',
    backgroundColor: Colors.accentGreen + '15', padding: 16, borderRadius: 14, gap: 8,
    borderWidth: 1, borderColor: Colors.accentGreen + '30',
  },
  deliveredText: { color: Colors.accentGreen, fontSize: 16, fontWeight: '700' },
});
