import React, { useEffect, useState } from 'react';
import {
  View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, RefreshControl,
} from 'react-native';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store/store';
import { getPendingOrders, getAcceptedOrders, getCompletedOrders, DriverOrder } from '../store/ordersSlice';
import { Box, Package } from 'lucide-react-native';
import { useNavigation } from '@react-navigation/native';
import * as Burnt from 'burnt';
import { Colors } from '../theme/colors';

type TabKey = 'pending' | 'active' | 'completed';

export default function OrdersScreen() {
  const dispatch = useDispatch<AppDispatch>();
  const navigation = useNavigation<any>();
  const { pendingOrders, acceptedOrders, completedOrders, loading } = useSelector((state: RootState) => state.orders);
  const [refreshing, setRefreshing] = useState(false);
  const [activeTab, setActiveTab] = useState<TabKey>('pending');

  useEffect(() => {
    dispatch(getPendingOrders());
    dispatch(getAcceptedOrders());
    dispatch(getCompletedOrders());
  }, [dispatch]);

  const onRefresh = async () => {
    setRefreshing(true);
    try {
      await Promise.all([
        dispatch(getPendingOrders()).unwrap(),
        dispatch(getAcceptedOrders()).unwrap(),
        dispatch(getCompletedOrders()).unwrap(),
      ]);
    } catch {
      Burnt.toast({ title: 'Failed to refresh', preset: 'error' });
    }
    setRefreshing(false);
  };

  const getOrders = (): DriverOrder[] => {
    switch (activeTab) {
      case 'pending': return pendingOrders;
      case 'active': return acceptedOrders;
      case 'completed': return completedOrders;
    }
  };

  const tabs: { key: TabKey; label: string; count: number; color: string }[] = [
    { key: 'pending', label: 'Pending', count: pendingOrders.length, color: Colors.accentOrange },
    { key: 'active', label: 'Active', count: acceptedOrders.length, color: Colors.accentTeal },
    { key: 'completed', label: 'Done', count: completedOrders.length, color: Colors.accentGreen },
  ];

  const renderItem = ({ item }: { item: DriverOrder }) => {
    return (
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <View style={[styles.cardIcon, {
            backgroundColor: activeTab === 'pending' ? Colors.accentOrange + '15' :
              activeTab === 'active' ? Colors.accentTeal + '15' : Colors.accentGreen + '15'
          }]}>
            <Box color={
              activeTab === 'pending' ? Colors.accentOrange :
              activeTab === 'active' ? Colors.accentTeal : Colors.accentGreen
            } size={22} />
          </View>
          <View style={styles.cardInfo}>
            <Text style={styles.orderId}>{item.customerReferenceId || 'Order'}</Text>
            <Text style={styles.orderCity}>{item.city || 'City'}, {item.state || 'State'}</Text>
          </View>
          <View style={[styles.statusBadge, {
            backgroundColor: activeTab === 'pending' ? Colors.accentOrange + '20' :
              activeTab === 'active' ? Colors.accentTeal + '20' : Colors.accentGreen + '20'
          }]}>
            <Text style={[styles.statusBadgeText, {
              color: activeTab === 'pending' ? Colors.accentOrange :
                activeTab === 'active' ? Colors.accentTeal : Colors.accentGreen
            }]}>
              {item.orderStatus?.replace(/_/g, ' ') || activeTab.toUpperCase()}
            </Text>
          </View>
        </View>

        {activeTab === 'pending' && (
            <TouchableOpacity
              style={styles.viewBtn}
              onPress={() => navigation.navigate('OrderTracking', { orderId: item.id })}
              activeOpacity={0.7}
            >
              <Text style={styles.viewBtnText}>View Details →</Text>
            </TouchableOpacity>
        )}

        {activeTab === 'active' && (
          <TouchableOpacity
            style={styles.viewBtn}
            onPress={() => navigation.navigate('OrderTracking', { orderId: item.id })}
            activeOpacity={0.7}
          >
            <Text style={styles.viewBtnText}>Track Order →</Text>
          </TouchableOpacity>
        )}
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Orders</Text>
        <Text style={styles.headerSub}>Manage your shipments</Text>
      </View>

      {/* Tabs */}
      <View style={styles.tabsContainer}>
        {tabs.map(tab => (
          <TouchableOpacity
            key={tab.key}
            style={[styles.tab, activeTab === tab.key && { backgroundColor: tab.color + '20', borderColor: tab.color }]}
            onPress={() => setActiveTab(tab.key)}
            activeOpacity={0.7}
          >
            <Text style={[styles.tabText, activeTab === tab.key && { color: tab.color }]}>
              {tab.label}
            </Text>
            {tab.count > 0 && (
              <View style={[styles.tabBadge, { backgroundColor: activeTab === tab.key ? tab.color : Colors.textMuted }]}>
                <Text style={styles.tabBadgeText}>{tab.count}</Text>
              </View>
            )}
          </TouchableOpacity>
        ))}
      </View>

      <FlatList
        data={getOrders()}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        contentContainerStyle={styles.listContent}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} colors={[Colors.primary]} tintColor={Colors.primary} />
        }
        ListEmptyComponent={
          <View style={styles.emptyState}>
            <View style={styles.emptyIcon}>
              <Package color={Colors.textMuted} size={40} />
            </View>
            <Text style={styles.emptyTitle}>No {activeTab} orders</Text>
            <Text style={styles.emptyText}>Pull down to refresh</Text>
          </View>
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.bgDark },
  header: {
    paddingHorizontal: 20, paddingTop: 60, paddingBottom: 16,
    backgroundColor: Colors.bgCard, borderBottomLeftRadius: 24,
    borderBottomRightRadius: 24,
  },
  title: { fontSize: 28, fontWeight: '800', color: Colors.textPrimary },
  headerSub: { fontSize: 14, color: Colors.textSecondary, marginTop: 4 },
  tabsContainer: { flexDirection: 'row', paddingHorizontal: 16, marginTop: 16, gap: 8 },
  tab: {
    flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center',
    paddingVertical: 10, borderRadius: 12, gap: 6,
    backgroundColor: Colors.bgCard, borderWidth: 1, borderColor: Colors.borderLight,
  },
  tabText: { fontWeight: '600', fontSize: 14, color: Colors.textMuted },
  tabBadge: {
    minWidth: 20, height: 20, borderRadius: 10, justifyContent: 'center',
    alignItems: 'center', paddingHorizontal: 6,
  },
  tabBadgeText: { color: '#FFFFFF', fontSize: 11, fontWeight: '700' },
  listContent: { padding: 16, paddingBottom: 40 },
  card: {
    backgroundColor: Colors.bgCard, borderRadius: 18, padding: 16, marginBottom: 12,
    borderWidth: 1, borderColor: Colors.borderLight,
  },
  cardHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: 12 },
  cardIcon: {
    width: 44, height: 44, borderRadius: 12, justifyContent: 'center',
    alignItems: 'center', marginRight: 12,
  },
  cardInfo: { flex: 1 },
  orderId: { fontSize: 15, fontWeight: '700', color: Colors.textPrimary },
  orderCity: { fontSize: 13, color: Colors.textMuted, marginTop: 2 },
  statusBadge: { paddingHorizontal: 10, paddingVertical: 4, borderRadius: 8 },
  statusBadgeText: { fontSize: 11, fontWeight: '700' },
  actionsContainer: { flexDirection: 'row', gap: 10 },
  actionBtn: {
    flex: 1, flexDirection: 'row', justifyContent: 'center',
    alignItems: 'center', height: 44, borderRadius: 12, gap: 6,
  },
  rejectBtn: { backgroundColor: Colors.accent + '15', borderWidth: 1, borderColor: Colors.accent + '30' },
  acceptBtn: { backgroundColor: Colors.accentGreen },
  actionText: { fontWeight: '600', fontSize: 14 },
  viewBtn: {
    height: 44, backgroundColor: Colors.primary + '15', borderRadius: 12,
    justifyContent: 'center', alignItems: 'center', borderWidth: 1, borderColor: Colors.primary + '30',
  },
  viewBtnText: { color: Colors.primaryLight, fontWeight: '600' },
  emptyState: { padding: 60, alignItems: 'center' },
  emptyIcon: {
    width: 72, height: 72, borderRadius: 24, backgroundColor: Colors.bgCard,
    justifyContent: 'center', alignItems: 'center', marginBottom: 16,
    borderWidth: 1, borderColor: Colors.borderLight,
  },
  emptyTitle: { fontSize: 16, fontWeight: '600', color: Colors.textSecondary, marginTop: 4 },
  emptyText: { color: Colors.textMuted, fontSize: 14, marginTop: 4 },
});
