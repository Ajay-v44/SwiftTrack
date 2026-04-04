import React, { useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, RefreshControl } from 'react-native';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store/store';
import { getMyOrders, respondToAssignment } from '../store/ordersSlice';
import { MapPin, Box, CheckCircle, XCircle, Package } from 'lucide-react-native';
import { useNavigation } from '@react-navigation/native';
import * as Burnt from 'burnt';

export default function OrdersScreen() {
  const dispatch = useDispatch<AppDispatch>();
  const navigation = useNavigation<any>();
  const { orders, loading } = useSelector((state: RootState) => state.orders);
  const [refreshing, setRefreshing] = React.useState(false);

  useEffect(() => {
    dispatch(getMyOrders());
  }, [dispatch]);

  const onRefresh = async () => {
    setRefreshing(true);
    try {
      await dispatch(getMyOrders()).unwrap();
    } catch {
      Burnt.toast({ title: 'Failed to refresh orders', preset: 'error' });
    }
    setRefreshing(false);
  };

  const handleResponse = async (assignmentId: string, response: 'ACCEPT' | 'REJECT') => {
    try {
      await dispatch(respondToAssignment({ assignmentId, response })).unwrap();
      Burnt.toast({
        title: response === 'ACCEPT' ? 'Order accepted!' : 'Order rejected',
        preset: response === 'ACCEPT' ? 'done' : 'none',
      });
    } catch (err: any) {
      Burnt.toast({
        title: typeof err === 'string' ? err : 'Failed to respond to assignment',
        preset: 'error',
      });
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING': return { bg: '#FEF3C7', text: '#D97706' };
      case 'ACCEPTED': return { bg: '#DBEAFE', text: '#2563EB' };
      case 'PICKED_UP': return { bg: '#E0E7FF', text: '#4F46E5' };
      case 'IN_TRANSIT': return { bg: '#FEF2F2', text: '#DC2626' };
      case 'DELIVERED': return { bg: '#D1FAE5', text: '#059669' };
      default: return { bg: '#F3F4F6', text: '#6B7280' };
    }
  };

  const renderItem = ({ item }: { item: any }) => {
    const isPending = item.status === 'PENDING' && item.assignmentId;
    const statusColor = getStatusColor(item.status);

    return (
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <View style={styles.cardIcon}>
            <Box color="#2563EB" size={24} />
          </View>
          <View style={styles.cardInfo}>
            <Text style={styles.orderId}>{item.orderNumber}</Text>
            <Text style={styles.customerName}>{item.customerDetails?.name || 'Customer'}</Text>
          </View>
          <View style={[styles.statusBadge, { backgroundColor: statusColor.bg }]}>
            <Text style={[styles.statusBadgeText, { color: statusColor.text }]}>
              {item.status.replace(/_/g, ' ')}
            </Text>
          </View>
        </View>

        <View style={styles.routeContainer}>
           <View style={styles.routePoint}>
             <MapPin color="#2563EB" size={16} />
             <Text style={styles.routeText} numberOfLines={1}>{item.pickupLocation?.address || 'Pickup Location'}</Text>
           </View>
           <View style={styles.routeLine} />
           <View style={styles.routePoint}>
             <MapPin color="#EF4444" size={16} />
             <Text style={styles.routeText} numberOfLines={1}>{item.dropoffLocation?.address || 'Dropoff Location'}</Text>
           </View>
        </View>

        {isPending ? (
          <View style={styles.actionsContainer}>
            <TouchableOpacity
              style={[styles.actionBtn, styles.rejectBtn]}
              onPress={() => handleResponse(item.assignmentId, 'REJECT')}
              activeOpacity={0.7}
            >
              <XCircle color="#DC2626" size={20} />
              <Text style={styles.rejectText}>Reject</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.actionBtn, styles.acceptBtn]}
              onPress={() => handleResponse(item.assignmentId, 'ACCEPT')}
              activeOpacity={0.7}
            >
              <CheckCircle color="#FFFFFF" size={20} />
              <Text style={styles.acceptText}>Accept</Text>
            </TouchableOpacity>
          </View>
        ) : (
          <TouchableOpacity
            style={styles.viewBtn}
            onPress={() => navigation.navigate('OrderTracking', { orderId: item.id })}
            activeOpacity={0.7}
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
        <Text style={styles.headerSubtitle}>{orders.length} total orders</Text>
      </View>
      <FlatList
        data={orders}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        contentContainerStyle={styles.listContent}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} colors={['#2563EB']} tintColor="#2563EB" />
        }
        ListEmptyComponent={
          <View style={styles.emptyState}>
             <Package color="#D1D5DB" size={48} />
             <Text style={styles.emptyTitle}>No Orders Yet</Text>
             <Text style={styles.emptyText}>When orders are assigned to you, they will appear here.</Text>
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
  headerSubtitle: {
    fontSize: 14,
    color: '#6B7280',
    marginTop: 4,
  },
  listContent: {
    padding: 16,
    paddingBottom: 40,
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
    padding: 60,
    alignItems: 'center',
  },
  emptyTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#374151',
    marginTop: 16,
  },
  emptyText: {
    color: '#6B7280',
    fontSize: 14,
    marginTop: 8,
    textAlign: 'center',
    lineHeight: 20,
  },
});
