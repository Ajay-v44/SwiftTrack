import React, { useEffect, useState, useCallback } from 'react';
import {
  View, Text, StyleSheet, Switch, ScrollView, TouchableOpacity, Image,
  RefreshControl, Dimensions, Platform,
} from 'react-native';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store/store';
import { updateStatus, setCurrentLocation } from '../store/driverSlice';
import { getPendingOrders, getAcceptedOrders } from '../store/ordersSlice';
import { getDriverDetails } from '../store/authSlice';
import { fetchWalletDetails } from '../store/walletSlice';
import { startLocationTracking, stopLocationTracking } from '../utils/locationTracking';
import { MapPin, Box, ChevronRight, Bell, Zap, TrendingUp, Navigation, Map as MapIcon } from 'lucide-react-native';
import { useNavigation } from '@react-navigation/native';
import * as Location from 'expo-location';
import * as Burnt from 'burnt';
import { Colors } from '../theme/colors';
import DiceBearAvatar from '../components/DiceBearAvatar';

const { width } = Dimensions.get('window');

export default function HomeScreen() {
  const dispatch = useDispatch<AppDispatch>();
  const navigation = useNavigation<any>();
  const { driver } = useSelector((state: RootState) => state.auth);
  const { isOnline, loading: statusLoading, currentLocation } = useSelector((state: RootState) => state.driver);
  const { pendingOrders, acceptedOrders } = useSelector((state: RootState) => state.orders);
  const { balance: walletBalance, currency: walletCurrency } = useSelector((state: RootState) => state.wallet);
  const [refreshing, setRefreshing] = useState(false);
  const [locationAddress, setLocationAddress] = useState<string | null>(null);

  useEffect(() => {
    dispatch(getDriverDetails());
    dispatch(getPendingOrders());
    dispatch(getAcceptedOrders());
    dispatch(fetchWalletDetails());
    fetchCurrentLocation();
  }, [dispatch]);

  useEffect(() => {
    if (isOnline) {
      void startLocationTracking();
      return;
    }

    void stopLocationTracking();
  }, [isOnline]);

  const fetchCurrentLocation = useCallback(async () => {
    try {
      const { status } = await Location.requestForegroundPermissionsAsync();
      if (status !== 'granted') return;
      const loc = await Location.getCurrentPositionAsync({ accuracy: Location.Accuracy.Balanced });
      const coords = { latitude: loc.coords.latitude, longitude: loc.coords.longitude };
      dispatch(setCurrentLocation(coords));

      // Reverse geocode
      const addresses = await Location.reverseGeocodeAsync(coords);
      if (addresses.length > 0) {
        const a = addresses[0];
        const parts = [a.name, a.street, a.city, a.region].filter(Boolean);
        setLocationAddress(parts.join(', '));
      }
    } catch (err) {
      console.log('Location error:', err);
    }
  }, [dispatch]);

  const onRefresh = async () => {
    setRefreshing(true);
    try {
      await Promise.all([
        dispatch(getDriverDetails()).unwrap(),
        dispatch(getPendingOrders()).unwrap(),
        dispatch(getAcceptedOrders()).unwrap(),
        dispatch(fetchWalletDetails()).unwrap(),
      ]);
      fetchCurrentLocation();
    } catch {}
    setRefreshing(false);
  };

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good Morning';
    if (hour < 17) return 'Good Afternoon';
    return 'Good Evening';
  };

  const handleStatusToggle = async (value: boolean) => {
    const newStatus = value ? 'ONLINE' : 'OFFLINE';
    try {
      await dispatch(updateStatus(newStatus)).unwrap();
      if (newStatus === 'ONLINE') {
        await startLocationTracking();
        Burnt.toast({ title: '🟢 You are now online!', preset: 'done' });
      } else {
        await stopLocationTracking();
        Burnt.toast({ title: '🔴 You are now offline', preset: 'done' });
      }
    } catch (err: any) {
      Burnt.toast({ title: typeof err === 'string' ? err : 'Failed to change status', preset: 'error' });
    }
  };

  const driverName = driver?.user?.name || 'Driver';
  const activeOrder = acceptedOrders[0];

  return (
    <ScrollView
      style={styles.container}
      refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} colors={[Colors.primary]} tintColor={Colors.primary} />}
      showsVerticalScrollIndicator={false}
    >
      {/* Header */}
      <View style={styles.headerBg}>
        <View style={styles.decorCircle1} />
        <View style={styles.decorCircle2} />

        {/* Brand Row */}
        <View style={styles.brandRow}>
          <View style={styles.brandContainer}>
            <Image
              source={require('../../assets/images/swifttrack_logo.png')}
              style={styles.brandLogo}
              resizeMode="contain"
            />
            <Text style={styles.brandName}>SwiftTrack</Text>
          </View>
          <TouchableOpacity style={styles.devMapBtn} onPress={() => navigation.navigate('DevMap')} activeOpacity={0.7}>
            <MapIcon color={Colors.accentTeal} size={18} />
          </TouchableOpacity>
        </View>

        {/* User Row */}
        <View style={styles.headerRow}>
          <TouchableOpacity
            style={styles.userInfo}
            onPress={() => navigation.navigate('Profile')}
            activeOpacity={0.7}
          >
            <DiceBearAvatar
              seed={driverName}
              size={50}
              radius={16}
              style={styles.avatarContainer}
            />
            <View>
              <Text style={styles.greeting}>{getGreeting()} 👋</Text>
              <Text style={styles.userName}>{driverName}</Text>
            </View>
          </TouchableOpacity>
          <TouchableOpacity style={styles.notificationBtn} activeOpacity={0.7}>
            <Bell color={Colors.textPrimary} size={22} />
            {pendingOrders.length > 0 && <View style={styles.notifDot} />}
          </TouchableOpacity>
        </View>

        {/* Location Row */}
        {locationAddress && (
          <View style={styles.locationRow}>
            <MapPin color={Colors.accentTeal} size={14} />
            <Text style={styles.locationText} numberOfLines={1}>{locationAddress}</Text>
          </View>
        )}

        {/* Status Card */}
        <View style={styles.statusCard}>
          <View style={styles.statusLeft}>
            <View style={[styles.statusDot, { backgroundColor: isOnline ? Colors.online : Colors.offline }]} />
            <View>
              <Text style={styles.statusTitle}>{isOnline ? 'Online & Active' : 'Currently Offline'}</Text>
              <Text style={styles.statusSub}>{isOnline ? 'Receiving new orders' : 'Go online to receive orders'}</Text>
            </View>
          </View>
          <Switch
            value={isOnline}
            onValueChange={handleStatusToggle}
            disabled={statusLoading}
            trackColor={{ false: '#3A3A5C', true: Colors.online + '60' }}
            thumbColor={isOnline ? Colors.online : '#6B6B99'}
          />
        </View>
      </View>

      {/* Quick Stats */}
      <View style={styles.statsRow}>
        <TouchableOpacity
          style={[styles.statCard, { backgroundColor: Colors.bgCard }]}
          onPress={() => navigation.navigate('Orders')}
          activeOpacity={0.7}
        >
          <View style={[styles.statIcon, { backgroundColor: Colors.accentOrange + '20' }]}>
            <Zap color={Colors.accentOrange} size={20} />
          </View>
          <Text style={styles.statValue}>{pendingOrders.length}</Text>
          <Text style={styles.statLabel}>Pending</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.statCard, { backgroundColor: Colors.bgCard }]}
          onPress={() => navigation.navigate('Orders')}
          activeOpacity={0.7}
        >
          <View style={[styles.statIcon, { backgroundColor: Colors.accentTeal + '20' }]}>
            <Navigation color={Colors.accentTeal} size={20} />
          </View>
          <Text style={styles.statValue}>{acceptedOrders.length}</Text>
          <Text style={styles.statLabel}>Active</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.statCard, { backgroundColor: Colors.bgCard }]}
          onPress={() => navigation.navigate('Wallet')}
          activeOpacity={0.7}
        >
          <View style={[styles.statIcon, { backgroundColor: Colors.primary + '20' }]}>
            <TrendingUp color={Colors.primaryLight} size={20} />
          </View>
          <Text style={styles.statValue}>
            {walletCurrency === 'INR' ? '₹' : walletCurrency}{walletBalance.toFixed(0)}
          </Text>
          <Text style={styles.statLabel}>Earnings</Text>
        </TouchableOpacity>
      </View>

      {/* Active Order */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Current Shipment</Text>
        {activeOrder ? (
          <TouchableOpacity
            style={styles.shipmentCard}
            onPress={() => navigation.navigate('OrderTracking', { orderId: activeOrder.id })}
            activeOpacity={0.7}
          >
            <View style={styles.shipmentHeader}>
              <View style={styles.shipmentIconContainer}>
                <Box color={Colors.accentTeal} size={22} />
              </View>
              <View style={styles.shipmentInfo}>
                <Text style={styles.shipmentId}>{activeOrder.customerReferenceId || 'Order'}</Text>
                <View style={[styles.statusBadge, { backgroundColor: Colors.accentTeal + '20' }]}>
                  <Text style={[styles.statusBadgeText, { color: Colors.accentTeal }]}>
                    {activeOrder.orderStatus?.replace(/_/g, ' ') || 'Active'}
                  </Text>
                </View>
              </View>
              <ChevronRight color={Colors.textMuted} size={20} />
            </View>
            <View style={styles.shipmentRoute}>
              <View style={styles.routePoint}>
                <View style={[styles.routeDot, { backgroundColor: Colors.primary }]} />
                <Text style={styles.routeText} numberOfLines={1}>{activeOrder.city || 'Pickup'}</Text>
              </View>
              <View style={styles.routeLine} />
              <View style={styles.routePoint}>
                <View style={[styles.routeDot, { backgroundColor: Colors.accent }]} />
                <Text style={styles.routeText} numberOfLines={1}>{activeOrder.state || 'Dropoff'}</Text>
              </View>
            </View>
          </TouchableOpacity>
        ) : (
          <View style={styles.emptyState}>
            <View style={styles.emptyIcon}><Box color={Colors.textMuted} size={36} /></View>
            <Text style={styles.emptyTitle}>No Active Shipments</Text>
            <Text style={styles.emptyText}>New orders will appear here when assigned</Text>
          </View>
        )}
      </View>

      {/* Pending Orders */}
      {pendingOrders.length > 0 && (
        <View style={styles.section}>
          <View style={styles.sectionHeader}>
            <Text style={styles.sectionTitle}>New Assignments 🔔</Text>
            <TouchableOpacity onPress={() => navigation.navigate('Orders')} activeOpacity={0.7}>
              <Text style={styles.seeAllBtn}>See All</Text>
            </TouchableOpacity>
          </View>
          {pendingOrders.slice(0, 3).map((order, idx) => (
            <TouchableOpacity
              key={order.id || idx}
              style={styles.pendingItem}
              onPress={() => navigation.navigate('Orders')}
              activeOpacity={0.7}
            >
              <View style={styles.pendingDot} />
              <View style={styles.pendingInfo}>
                <Text style={styles.pendingTitle}>{order.customerReferenceId || 'New Order'}</Text>
                <Text style={styles.pendingCity}>{order.city || 'City'}, {order.state || 'State'}</Text>
              </View>
              <View style={[styles.statusBadge, { backgroundColor: Colors.accentOrange + '20' }]}>
                <Text style={[styles.statusBadgeText, { color: Colors.accentOrange }]}>Assigned</Text>
              </View>
            </TouchableOpacity>
          ))}
        </View>
      )}

      <View style={{ height: 30 }} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.bgDark },
  headerBg: {
    backgroundColor: Colors.bgCard, paddingTop: 52, paddingHorizontal: 20,
    paddingBottom: 24, borderBottomLeftRadius: 28, borderBottomRightRadius: 28,
    overflow: 'hidden',
  },
  decorCircle1: {
    position: 'absolute', top: -40, right: -40, width: 160, height: 160,
    borderRadius: 80, backgroundColor: Colors.primary, opacity: 0.1,
  },
  decorCircle2: {
    position: 'absolute', bottom: -20, left: -30, width: 100, height: 100,
    borderRadius: 50, backgroundColor: Colors.accentTeal, opacity: 0.08,
  },
  brandRow: {
    flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16,
  },
  brandContainer: { flexDirection: 'row', alignItems: 'center' },
  brandLogo: { width: 32, height: 32, borderRadius: 8, marginRight: 8 },
  brandName: { fontSize: 18, fontWeight: '800', color: Colors.primaryLight, letterSpacing: -0.3 },
  devMapBtn: {
    padding: 10, backgroundColor: Colors.bgGlass, borderRadius: 12,
    borderWidth: 1, borderColor: Colors.borderLight,
  },
  headerRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 },
  userInfo: { flexDirection: 'row', alignItems: 'center' },
  avatarContainer: {
    marginRight: 12,
    shadowColor: '#000', shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.2, shadowRadius: 8, elevation: 6,
  },
  greeting: { fontSize: 14, color: Colors.textSecondary },
  userName: { fontSize: 20, fontWeight: '700', color: Colors.textPrimary },
  notificationBtn: {
    padding: 10, backgroundColor: Colors.bgGlass, borderRadius: 14,
    borderWidth: 1, borderColor: Colors.borderLight, position: 'relative',
  },
  notifDot: {
    position: 'absolute', top: 8, right: 8, width: 8, height: 8,
    borderRadius: 4, backgroundColor: Colors.accent,
  },
  locationRow: {
    flexDirection: 'row', alignItems: 'center', marginBottom: 14, gap: 6,
    backgroundColor: Colors.bgGlass, paddingHorizontal: 12, paddingVertical: 8,
    borderRadius: 10, borderWidth: 1, borderColor: Colors.borderLight,
  },
  locationText: { fontSize: 13, color: Colors.textSecondary, flex: 1 },
  statusCard: {
    flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center',
    backgroundColor: Colors.bgGlass, padding: 16, borderRadius: 16,
    borderWidth: 1, borderColor: Colors.borderLight,
  },
  statusLeft: { flexDirection: 'row', alignItems: 'center', flex: 1 },
  statusDot: { width: 12, height: 12, borderRadius: 6, marginRight: 12 },
  statusTitle: { fontSize: 15, fontWeight: '600', color: Colors.textPrimary },
  statusSub: { fontSize: 13, color: Colors.textSecondary, marginTop: 2 },
  statsRow: { flexDirection: 'row', paddingHorizontal: 16, marginTop: 20, gap: 10 },
  statCard: {
    flex: 1, padding: 16, borderRadius: 16, alignItems: 'center',
    borderWidth: 1, borderColor: Colors.borderLight,
  },
  statIcon: { width: 40, height: 40, borderRadius: 12, justifyContent: 'center', alignItems: 'center', marginBottom: 8 },
  statValue: { fontSize: 22, fontWeight: '800', color: Colors.textPrimary },
  statLabel: { fontSize: 12, color: Colors.textSecondary, marginTop: 2 },
  section: { paddingHorizontal: 20, marginTop: 24 },
  sectionHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 },
  sectionTitle: { fontSize: 18, fontWeight: '700', color: Colors.textPrimary, marginBottom: 12 },
  seeAllBtn: { color: Colors.primaryLight, fontWeight: '600' },
  shipmentCard: {
    backgroundColor: Colors.bgCard, borderRadius: 20, padding: 16,
    borderWidth: 1, borderColor: Colors.borderLight,
  },
  shipmentHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: 16 },
  shipmentIconContainer: {
    width: 44, height: 44, backgroundColor: Colors.accentTeal + '15',
    borderRadius: 12, justifyContent: 'center', alignItems: 'center', marginRight: 12,
  },
  shipmentInfo: { flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingRight: 8 },
  shipmentId: { fontSize: 16, fontWeight: '700', color: Colors.textPrimary },
  statusBadge: { paddingHorizontal: 10, paddingVertical: 4, borderRadius: 8 },
  statusBadgeText: { fontSize: 12, fontWeight: '600' },
  shipmentRoute: { marginLeft: 4 },
  routePoint: { flexDirection: 'row', alignItems: 'center' },
  routeDot: { width: 10, height: 10, borderRadius: 5 },
  routeLine: { width: 2, height: 20, backgroundColor: Colors.textMuted, marginLeft: 4, marginVertical: 4, opacity: 0.3 },
  routeText: { marginLeft: 12, fontSize: 14, color: Colors.textSecondary, flex: 1 },
  emptyState: {
    backgroundColor: Colors.bgCard, padding: 40, borderRadius: 20,
    alignItems: 'center', borderWidth: 1, borderColor: Colors.borderLight,
  },
  emptyIcon: {
    width: 64, height: 64, borderRadius: 20, backgroundColor: Colors.bgGlass,
    justifyContent: 'center', alignItems: 'center', marginBottom: 12,
  },
  emptyTitle: { fontSize: 16, fontWeight: '600', color: Colors.textSecondary, marginTop: 4 },
  emptyText: { color: Colors.textMuted, marginTop: 4, textAlign: 'center', fontSize: 14 },
  pendingItem: {
    flexDirection: 'row', alignItems: 'center', backgroundColor: Colors.bgCard,
    padding: 16, borderRadius: 14, marginBottom: 10,
    borderWidth: 1, borderColor: Colors.borderLight,
  },
  pendingDot: {
    width: 10, height: 10, borderRadius: 5, backgroundColor: Colors.accentOrange, marginRight: 12,
  },
  pendingInfo: { flex: 1 },
  pendingTitle: { fontSize: 15, fontWeight: '600', color: Colors.textPrimary },
  pendingCity: { fontSize: 13, color: Colors.textMuted, marginTop: 2 },
});
