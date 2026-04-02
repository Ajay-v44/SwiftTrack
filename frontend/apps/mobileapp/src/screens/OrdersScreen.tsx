import React, { useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store/store';
import { getMyOrders, respondToAssignment } from '../store/ordersSlice';
import { MapPin, Box, CheckCircle, XCircle } from 'lucide-react-native';
import { useNavigation } from '@react-navigation/native';

export default function OrdersScreen() {
  const dispatch = useDispatch<AppDispatch>();
  const navigation = useNavigation<any>();
  const { orders, loading } = useSelector((state: RootState) => state.orders);

  useEffect(() => {
    dispatch(getMyOrders());
  }, [dispatch]);

  const handleResponse = async (assignmentId: string, response: 'ACCEPT' | 'REJECT') => {
    await dispatch(respondToAssignment({ assignmentId, response }));
  };

  const renderItem = ({ item }: { item: any }) => {
    const isPending = item.status === 'PENDING' && item.assignmentId;

    return (
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <View style={styles.cardIcon}>
            <Box color="#2563EB" size={24} />
          </View>
          <View style={styles.cardInfo}>
            <Text style={styles.orderId}>{item.orderNumber}</Text>
            <Text style={styles.customerName}>{item.customerDetails?.name}</Text>
          </View>
          <View style={[styles.statusBadge, { backgroundColor: isPending ? '#FEF2F2' : '#EFF6FF' }]}>
            <Text style={[styles.statusBadgeText, { color: isPending ? '#DC2626' : '#2563EB' }]}>
              {item.status}
            </Text>
          </View>
        </View>

        <View style={styles.routeContainer}>
           <View style={styles.routePoint}>
             <MapPin color="#2563EB" size={16} />
             <Text style={styles.routeText} numberOfLines={1}>{item.pickupLocation?.address}</Text>
           </View>
           <View style={styles.routeLine} />
           <View style={styles.routePoint}>
             <MapPin color="#EF4444" size={16} />
             <Text style={styles.routeText} numberOfLines={1}>{item.dropoffLocation?.address}</Text>
           </View>
        </View>

        {isPending ? (
          <View style={styles.actionsContainer}>
            <TouchableOpacity
              style={[styles.actionBtn, styles.rejectBtn]}
              onPress={() => handleResponse(item.assignmentId, 'REJECT')}
            >
              <XCircle color="#DC2626" size={20} />
              <Text style={styles.rejectText}>Reject</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.actionBtn, styles.acceptBtn]}
              onPress={() => handleResponse(item.assignmentId, 'ACCEPT')}
            >
              <CheckCircle color="#FFFFFF" size={20} />
              <Text style={styles.acceptText}>Accept</Text>
            </TouchableOpacity>
          </View>
        ) : (
          <TouchableOpacity
            style={styles.viewBtn}
            onPress={() => navigation.navigate('OrderTracking', { orderId: item.id })}
          >
            <Text style={styles.viewBtnText}>View Details</Text>
          </TouchableOpacity>
        )}
      </View>
    );
  };

  if (loading && orders.length === 0) {
    return (
      <View style={styles.centerContainer}>
        <ActivityIndicator size="large" color="#2563EB" />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Orders & Assignments</Text>
      </View>
      <FlatList
        data={orders}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        contentContainerStyle={styles.listContent}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={
          <View style={styles.emptyState}>
             <Text style={styles.emptyText}>No orders assigned yet.</Text>
          </View>
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F9FAFB',
  },
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  header: {
    padding: 20,
    paddingTop: 60,
    backgroundColor: '#FFFFFF',
    borderBottomWidth: 1,
    borderBottomColor: '#E5E7EB',
  },
  title: {
    fontSize: 24,
    fontWeight: '700',
    color: '#111827',
  },
  listContent: {
    padding: 16,
  },
  card: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 16,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: '#E5E7EB',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 2,
  },
  cardHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 16,
  },
  cardIcon: {
    width: 48,
    height: 48,
    backgroundColor: '#EFF6FF',
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  cardInfo: {
    flex: 1,
  },
  orderId: {
    fontSize: 16,
    fontWeight: '700',
    color: '#111827',
  },
  customerName: {
    fontSize: 14,
    color: '#6B7280',
    marginTop: 2,
  },
  statusBadge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
  },
  statusBadgeText: {
    fontSize: 12,
    fontWeight: '600',
  },
  routeContainer: {
    backgroundColor: '#F9FAFB',
    padding: 12,
    borderRadius: 12,
    marginBottom: 16,
  },
  routePoint: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  routeLine: {
    width: 2,
    height: 16,
    backgroundColor: '#D1D5DB',
    marginLeft: 7,
    marginVertical: 4,
  },
  routeText: {
    marginLeft: 12,
    fontSize: 14,
    color: '#4B5563',
    flex: 1,
  },
  actionsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 12,
  },
  actionBtn: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    height: 48,
    borderRadius: 12,
  },
  rejectBtn: {
    backgroundColor: '#FEF2F2',
    borderWidth: 1,
    borderColor: '#FCA5A5',
  },
  acceptBtn: {
    backgroundColor: '#2563EB',
  },
  rejectText: {
    color: '#DC2626',
    fontWeight: '600',
    marginLeft: 8,
  },
  acceptText: {
    color: '#FFFFFF',
    fontWeight: '600',
    marginLeft: 8,
  },
  viewBtn: {
    height: 48,
    backgroundColor: '#F3F4F6',
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
  },
  viewBtnText: {
    color: '#374151',
    fontWeight: '600',
  },
  emptyState: {
    padding: 40,
    alignItems: 'center',
  },
  emptyText: {
    color: '#6B7280',
    fontSize: 16,
  }
});
