import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, Switch, ScrollView, TouchableOpacity, Image } from 'react-native';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store/store';
import { updateStatus } from '../store/driverSlice';
import { getMyOrders } from '../store/ordersSlice';
import { startLocationTracking, stopLocationTracking } from '../utils/locationTracking';
import { MapPin, Box, ChevronRight, Bell } from 'lucide-react-native';
import { useNavigation } from '@react-navigation/native';

export default function HomeScreen() {
  const dispatch = useDispatch<AppDispatch>();
  const navigation = useNavigation<any>();
  const { user } = useSelector((state: RootState) => state.auth);
  const { isActive, loading: statusLoading } = useSelector((state: RootState) => state.driver);
  const { orders } = useSelector((state: RootState) => state.orders);

  useEffect(() => {
    dispatch(getMyOrders());
  }, [dispatch]);

  const handleStatusToggle = async (value: boolean) => {
    const newStatus = value ? 'ACTIVE' : 'INACTIVE';
    try {
      await dispatch(updateStatus(newStatus)).unwrap();
      if (newStatus === 'ACTIVE') {
        startLocationTracking();
      } else {
        stopLocationTracking();
      }
    } catch (err) {
      console.error('Failed to change status:', err);
    }
  };

  const ongoingOrder = orders.find(o => o.status === 'ACCEPTED' || o.status === 'IN_TRANSIT' || o.status === 'PICKED_UP');
  const recentOrders = orders.filter(o => o.status === 'DELIVERED').slice(0, 3);

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <View style={styles.userInfo}>
           <Image
             source={require('../../assets/images/Driver.jpg')}
             style={styles.avatar}
           />
           <View>
             <Text style={styles.greeting}>Good Morning,</Text>
             <Text style={styles.userName}>{user?.name || 'Driver'}</Text>
           </View>
        </View>
        <TouchableOpacity style={styles.notificationBtn}>
           <Bell color="#111827" size={24} />
        </TouchableOpacity>
      </View>

      <View style={styles.statusCard}>
         <View>
           <Text style={styles.statusTitle}>Driver Status</Text>
           <Text style={styles.statusDesc}>
             {isActive ? 'You are online and visible' : 'You are offline'}
           </Text>
         </View>
         <Switch
           value={isActive}
           onValueChange={handleStatusToggle}
           disabled={statusLoading}
           trackColor={{ false: '#D1D5DB', true: '#BFDBFE' }}
           thumbColor={isActive ? '#2563EB' : '#9CA3AF'}
         />
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Current Shipment</Text>
        {ongoingOrder ? (
          <TouchableOpacity
            style={styles.shipmentCard}
            onPress={() => navigation.navigate('OrderTracking', { orderId: ongoingOrder.id })}
          >
            <View style={styles.shipmentHeader}>
              <View style={styles.shipmentIcon}>
                <Box color="#2563EB" size={24} />
              </View>
              <View style={styles.shipmentInfo}>
                <Text style={styles.shipmentId}>{ongoingOrder.orderNumber}</Text>
                <View style={styles.statusBadge}>
                   <Text style={styles.statusBadgeText}>{ongoingOrder.status}</Text>
                </View>
              </View>
              <ChevronRight color="#9CA3AF" size={20} />
            </View>
            <View style={styles.shipmentRoute}>
               <View style={styles.routePoint}>
                 <MapPin color="#2563EB" size={16} />
                 <Text style={styles.routeText} numberOfLines={1}>{ongoingOrder.pickupLocation?.address || 'Pickup'}</Text>
               </View>
               <View style={styles.routeLine} />
               <View style={styles.routePoint}>
                 <MapPin color="#EF4444" size={16} />
                 <Text style={styles.routeText} numberOfLines={1}>{ongoingOrder.dropoffLocation?.address || 'Dropoff'}</Text>
               </View>
            </View>
          </TouchableOpacity>
        ) : (
          <View style={styles.emptyState}>
             <Text style={styles.emptyStateText}>No ongoing shipments</Text>
          </View>
        )}
      </View>

      <View style={styles.section}>
        <View style={styles.sectionHeader}>
           <Text style={styles.sectionTitle}>Recent Shipments</Text>
           <TouchableOpacity onPress={() => navigation.navigate('Orders')}>
             <Text style={styles.seeAllBtn}>See All</Text>
           </TouchableOpacity>
        </View>
        {recentOrders.map((order, idx) => (
           <View key={idx} style={styles.recentItem}>
             <View style={styles.recentIcon}>
               <Box color="#4B5563" size={20} />
             </View>
             <View style={styles.recentInfo}>
               <Text style={styles.recentTitle}>{order.customerDetails?.name || 'Customer'}</Text>
               <Text style={styles.recentId}>{order.orderNumber}</Text>
             </View>
             <View style={[styles.statusBadge, { backgroundColor: '#D1FAE5' }]}>
               <Text style={[styles.statusBadgeText, { color: '#059669' }]}>Delivered</Text>
             </View>
           </View>
        ))}
        {recentOrders.length === 0 && (
          <Text style={styles.emptyStateText}>No recent shipments</Text>
        )}
      </View>

      {/* Dev Only Button */}
      <TouchableOpacity
         style={{marginTop: 40, padding: 16, backgroundColor: '#FEF3C7', alignItems: 'center', borderRadius: 8}}
         onPress={() => navigation.navigate('DevMap')}
      >
         <Text style={{color: '#D97706', fontWeight: 'bold'}}>DEV: Open Map Location Simulator</Text>
      </TouchableOpacity>

    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F9FAFB',
    padding: 20,
    paddingTop: 60,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 24,
  },
  userInfo: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  avatar: {
    width: 48,
    height: 48,
    borderRadius: 24,
    marginRight: 12,
  },
  greeting: {
    fontSize: 14,
    color: '#6B7280',
  },
  userName: {
    fontSize: 18,
    fontWeight: '700',
    color: '#111827',
  },
  notificationBtn: {
    padding: 8,
    backgroundColor: '#FFFFFF',
    borderRadius: 20,
    borderWidth: 1,
    borderColor: '#E5E7EB',
  },
  statusCard: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    padding: 16,
    borderRadius: 16,
    marginBottom: 24,
    borderWidth: 1,
    borderColor: '#E5E7EB',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 2,
  },
  statusTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
  },
  statusDesc: {
    fontSize: 14,
    color: '#6B7280',
    marginTop: 4,
  },
  section: {
    marginBottom: 24,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#111827',
    marginBottom: 12,
  },
  seeAllBtn: {
    color: '#2563EB',
    fontWeight: '600',
  },
  shipmentCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: '#E5E7EB',
  },
  shipmentHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 16,
  },
  shipmentIcon: {
    width: 40,
    height: 40,
    backgroundColor: '#EFF6FF',
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  shipmentInfo: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingRight: 8,
  },
  shipmentId: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
  },
  statusBadge: {
    backgroundColor: '#FEF2F2',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
  },
  statusBadgeText: {
    color: '#DC2626',
    fontSize: 12,
    fontWeight: '600',
  },
  shipmentRoute: {
    marginTop: 8,
  },
  routePoint: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  routeLine: {
    width: 2,
    height: 20,
    backgroundColor: '#E5E7EB',
    marginLeft: 7,
    marginVertical: 4,
  },
  routeText: {
    marginLeft: 12,
    fontSize: 14,
    color: '#4B5563',
    flex: 1,
  },
  emptyState: {
    backgroundColor: '#FFFFFF',
    padding: 24,
    borderRadius: 16,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#E5E7EB',
  },
  emptyStateText: {
    color: '#6B7280',
  },
  recentItem: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    padding: 16,
    borderRadius: 12,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#E5E7EB',
  },
  recentIcon: {
    width: 40,
    height: 40,
    backgroundColor: '#F3F4F6',
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  recentInfo: {
    flex: 1,
  },
  recentTitle: {
    fontSize: 15,
    fontWeight: '600',
    color: '#111827',
  },
  recentId: {
    fontSize: 13,
    color: '#6B7280',
    marginTop: 2,
  }
});
