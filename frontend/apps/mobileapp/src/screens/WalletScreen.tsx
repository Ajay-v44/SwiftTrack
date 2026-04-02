import React, { useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store/store';
import { fetchWalletDetails, fetchTransactions } from '../store/walletSlice';
import { ArrowUpRight, ArrowDownLeft, Wallet as WalletIcon, ExternalLink } from 'lucide-react-native';

export default function WalletScreen() {
  const dispatch = useDispatch<AppDispatch>();
  const { balance, transactions, loading } = useSelector((state: RootState) => state.wallet);

  useEffect(() => {
    dispatch(fetchWalletDetails());
    dispatch(fetchTransactions());
  }, [dispatch]);

  const renderTransaction = ({ item }: { item: any }) => {
    const isCredit = item.type === 'CREDIT';

    return (
      <View style={styles.transactionCard}>
         <View style={styles.transactionLeft}>
            <View style={[styles.iconContainer, { backgroundColor: isCredit ? '#D1FAE5' : '#FEF2F2' }]}>
               {isCredit ? (
                  <ArrowDownLeft color="#059669" size={20} />
               ) : (
                  <ArrowUpRight color="#DC2626" size={20} />
               )}
            </View>
            <View>
               <Text style={styles.transactionTitle}>{item.description || (isCredit ? 'Earnings' : 'Withdrawal')}</Text>
               <Text style={styles.transactionDate}>{new Date(item.createdAt || Date.now()).toLocaleDateString()}</Text>
            </View>
         </View>
         <Text style={[styles.transactionAmount, { color: isCredit ? '#059669' : '#111827' }]}>
            {isCredit ? '+' : '-'}${Math.abs(item.amount || 0).toFixed(2)}
         </Text>
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Wallet</Text>
      </View>

      <View style={styles.balanceCard}>
         <View style={styles.balanceHeader}>
            <Text style={styles.balanceLabel}>Total Balance</Text>
            <WalletIcon color="#9CA3AF" size={24} />
         </View>
         <Text style={styles.balanceValue}>${(balance || 0).toFixed(2)}</Text>

         <View style={styles.balanceActions}>
            <TouchableOpacity style={styles.actionBtn}>
               <Text style={styles.actionBtnText}>Withdraw Funds</Text>
            </TouchableOpacity>
         </View>
      </View>

      <View style={styles.transactionsSection}>
         <View style={styles.sectionHeader}>
            <Text style={styles.sectionTitle}>Recent Transactions</Text>
         </View>

         {loading && transactions.length === 0 ? (
            <ActivityIndicator size="large" color="#2563EB" style={{marginTop: 40}} />
         ) : (
            <FlatList
              data={transactions}
              keyExtractor={(item: any, index) => item.id || index.toString()}
              renderItem={renderTransaction}
              contentContainerStyle={styles.listContent}
              showsVerticalScrollIndicator={false}
              ListEmptyComponent={
                <View style={styles.emptyState}>
                   <Text style={styles.emptyStateText}>No transactions yet.</Text>
                </View>
              }
            />
         )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F9FAFB',
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
  balanceCard: {
    backgroundColor: '#1E3A8A', // Dark Blue
    margin: 20,
    padding: 24,
    borderRadius: 24,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 8,
  },
  balanceHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  balanceLabel: {
    color: '#93C5FD',
    fontSize: 16,
    fontWeight: '500',
  },
  balanceValue: {
    color: '#FFFFFF',
    fontSize: 40,
    fontWeight: '700',
    marginBottom: 24,
  },
  balanceActions: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
  },
  actionBtn: {
    backgroundColor: '#3B82F6',
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 12,
  },
  actionBtnText: {
    color: '#FFFFFF',
    fontWeight: '600',
    fontSize: 14,
  },
  transactionsSection: {
    flex: 1,
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    paddingTop: 24,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: -2 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 4,
  },
  sectionHeader: {
    paddingHorizontal: 20,
    marginBottom: 16,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#111827',
  },
  listContent: {
    paddingHorizontal: 20,
    paddingBottom: 40,
  },
  transactionCard: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#F3F4F6',
  },
  transactionLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  iconContainer: {
    width: 48,
    height: 48,
    borderRadius: 24,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 16,
  },
  transactionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
    marginBottom: 4,
  },
  transactionDate: {
    fontSize: 14,
    color: '#6B7280',
  },
  transactionAmount: {
    fontSize: 16,
    fontWeight: '700',
  },
  emptyState: {
    padding: 40,
    alignItems: 'center',
  },
  emptyStateText: {
    color: '#6B7280',
    fontSize: 16,
  }
});
