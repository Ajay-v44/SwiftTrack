import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, Dimensions } from 'react-native';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store/store';
import { updateOrderStatus } from '../store/ordersSlice';
import { ArrowLeft, Phone, MessageSquare } from 'lucide-react-native';
import { useNavigation, useRoute } from '@react-navigation/native';
import MapView, { Marker, Polyline } from 'react-native-maps';

const { width } = Dimensions.get('window');

export default function OrderTrackingScreen() {
  const navigation = useNavigation();
  const route = useRoute<any>();
  const dispatch = useDispatch<AppDispatch>();
  const { orderId } = route.params;

  const { orders } = useSelector((state: RootState) => state.orders);
  const order = orders.find(o => o.id === orderId);

  // Status mapping for simple timeline
  const statuses = ['ACCEPTED', 'IN_TRANSIT', 'DELIVERED'];
  const currentStatusIndex = statuses.indexOf(order?.status || 'ACCEPTED');

  const handleUpdateStatus = async () => {
     if (currentStatusIndex < statuses.length - 1) {
       const nextStatus = statuses[currentStatusIndex + 1];
       await dispatch(updateOrderStatus({ orderId, status: nextStatus }));
     }
  };

  if (!order) {
    return (
       <View style={styles.container}>
         <Text>Order not found</Text>
       </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.mapContainer}>
        <TouchableOpacity
          style={styles.backBtn}
          onPress={() => navigation.goBack()}
        >
           <ArrowLeft color="#111827" size={24} />
        </TouchableOpacity>

        <MapView
          style={styles.map}
          initialRegion={{
            latitude: order.pickupLocation?.lat || 37.78825,
            longitude: order.pickupLocation?.lng || -122.4324,
            latitudeDelta: 0.0922,
            longitudeDelta: 0.0421,
          }}
        >
           {order.pickupLocation && (
             <Marker
               coordinate={{latitude: order.pickupLocation.lat, longitude: order.pickupLocation.lng}}
               title="Pickup"
               pinColor="blue"
             />
           )}
           {order.dropoffLocation && (
             <Marker
               coordinate={{latitude: order.dropoffLocation.lat, longitude: order.dropoffLocation.lng}}
               title="Dropoff"
               pinColor="red"
             />
           )}
           {order.pickupLocation && order.dropoffLocation && (
             <Polyline
               coordinates={[
                 {latitude: order.pickupLocation.lat, longitude: order.pickupLocation.lng},
                 {latitude: order.dropoffLocation.lat, longitude: order.dropoffLocation.lng}
               ]}
               strokeColor="#2563EB"
               strokeWidth={3}
             />
           )}
        </MapView>
      </View>

      <ScrollView style={styles.detailsContainer} contentContainerStyle={{paddingBottom: 40}}>
         <View style={styles.dragIndicator} />

         <View style={styles.customerCard}>
            <View style={styles.customerInfo}>
               <View style={styles.customerAvatar}>
                  <Text style={styles.avatarText}>{order.customerDetails?.name?.charAt(0) || 'C'}</Text>
               </View>
               <View>
                 <Text style={styles.customerName}>{order.customerDetails?.name}</Text>
                 <Text style={styles.customerRole}>Customer</Text>
               </View>
            </View>
            <View style={styles.contactActions}>
               <TouchableOpacity style={styles.iconBtn}>
                  <MessageSquare color="#2563EB" size={20} />
               </TouchableOpacity>
               <TouchableOpacity style={[styles.iconBtn, {backgroundColor: '#2563EB'}]}>
                  <Phone color="#FFFFFF" size={20} />
               </TouchableOpacity>
            </View>
         </View>

         <View style={styles.timelineSection}>
            <Text style={styles.timelineTitle}>Shipment Timeline</Text>

            {statuses.map((status, index) => {
               const isCompleted = index <= currentStatusIndex;
               const isLast = index === statuses.length - 1;

               return (
                 <View key={status} style={styles.timelineItem}>
                    <View style={styles.timelineLeft}>
                       <View style={[styles.timelineDot, isCompleted ? styles.dotCompleted : styles.dotPending]} />
                       {!isLast && <View style={[styles.timelineLine, isCompleted ? styles.lineCompleted : styles.linePending]} />}
                    </View>
                    <View style={styles.timelineContent}>
                       <Text style={[styles.timelineStatusText, isCompleted ? styles.textCompleted : styles.textPending]}>
                         {status.replace('_', ' ')}
                       </Text>
                    </View>
                 </View>
               );
            })}
         </View>

         {currentStatusIndex < statuses.length - 1 && (
           <TouchableOpacity
             style={styles.updateBtn}
             onPress={handleUpdateStatus}
           >
             <Text style={styles.updateBtnText}>
               Update Status to {statuses[currentStatusIndex + 1].replace('_', ' ')}
             </Text>
           </TouchableOpacity>
         )}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  mapContainer: {
    height: '45%',
    width: '100%',
    position: 'relative',
  },
  map: {
    ...StyleSheet.absoluteFillObject,
  },
  backBtn: {
    position: 'absolute',
    top: 60,
    left: 20,
    zIndex: 10,
    backgroundColor: '#FFFFFF',
    padding: 12,
    borderRadius: 24,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 4,
  },
  detailsContainer: {
    flex: 1,
    backgroundColor: '#FFFFFF',
    marginTop: -24,
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    padding: 24,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: -2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 8,
  },
  dragIndicator: {
    width: 40,
    height: 4,
    backgroundColor: '#E5E7EB',
    borderRadius: 2,
    alignSelf: 'center',
    marginBottom: 24,
  },
  customerCard: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingBottom: 24,
    borderBottomWidth: 1,
    borderBottomColor: '#E5E7EB',
    marginBottom: 24,
  },
  customerInfo: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  customerAvatar: {
    width: 48,
    height: 48,
    borderRadius: 24,
    backgroundColor: '#F3F4F6',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  avatarText: {
    fontSize: 18,
    fontWeight: '600',
    color: '#4B5563',
  },
  customerName: {
    fontSize: 16,
    fontWeight: '700',
    color: '#111827',
  },
  customerRole: {
    fontSize: 14,
    color: '#6B7280',
    marginTop: 2,
  },
  contactActions: {
    flexDirection: 'row',
    gap: 12,
  },
  iconBtn: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#EFF6FF',
    justifyContent: 'center',
    alignItems: 'center',
  },
  timelineSection: {
    marginBottom: 32,
  },
  timelineTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#111827',
    marginBottom: 16,
  },
  timelineItem: {
    flexDirection: 'row',
  },
  timelineLeft: {
    alignItems: 'center',
    width: 24,
    marginRight: 16,
  },
  timelineDot: {
    width: 12,
    height: 12,
    borderRadius: 6,
    zIndex: 2,
  },
  dotCompleted: {
    backgroundColor: '#2563EB',
  },
  dotPending: {
    backgroundColor: '#D1D5DB',
  },
  timelineLine: {
    width: 2,
    height: 40,
    marginTop: -2,
    marginBottom: -2,
  },
  lineCompleted: {
    backgroundColor: '#2563EB',
  },
  linePending: {
    backgroundColor: '#E5E7EB',
  },
  timelineContent: {
    paddingBottom: 24,
  },
  timelineStatusText: {
    fontSize: 16,
    fontWeight: '600',
  },
  textCompleted: {
    color: '#111827',
  },
  textPending: {
    color: '#9CA3AF',
  },
  updateBtn: {
    backgroundColor: '#2563EB',
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
  updateBtnText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '600',
  }
});
