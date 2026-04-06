import React from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, RefreshControl } from 'react-native';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store/store';
import { fetchWalletDetails, fetchTransactions } from '../store/walletSlice';
import { ArrowUpRight, ArrowDownLeft, Wallet as WalletIcon, TrendingUp } from 'lucide-react-native';
import { Colors } from '../theme/colors';
import { useFocusEffect } from '@react-navigation/native';

/**
 * Backend LedgerTransaction fields:
 * { id, accountId, transactionType: CREDIT|DEBIT, amount, referenceType, referenceId, orderId, description, createdAt }
 */

export default function WalletScreen() {
  const dispatch = useDispatch<AppDispatch>();
  const { balance, currency, transactions, loading, error } = useSelector((state: RootState) => state.wallet);
  const [refreshing, setRefreshing] = React.useState(false);

  const loadWallet = React.useCallback(async () => {
    await Promise.all([
      dispatch(fetchWalletDetails()).unwrap(),
      dispatch(fetchTransactions()).unwrap(),
    ]);
  }, [dispatch]);

  useFocusEffect(
    React.useCallback(() => {
      void loadWallet();
    }, [loadWallet])
  );

  const onRefresh = async () => {
    setRefreshing(true);
    try {
      await loadWallet();
    } catch {}
    setRefreshing(false);
  };

  const renderTransaction = ({ item }: { item: any }) => {
    // Backend uses transactionType: CREDIT|DEBIT (LedgerTransaction model)
    const isCredit = item.transactionType === 'CREDIT';
    const amount = parseFloat(item.amount) || 0;

    return (
      <View style={styles.transactionCard}>
        <View style={styles.transactionLeft}>
          <View style={[styles.iconContainer, {
            backgroundColor: isCredit ? Colors.accentGreen + '15' : Colors.accent + '15'
          }]}>
            {isCredit ? (
              <ArrowDownLeft color={Colors.accentGreen} size={20} />
            ) : (
              <ArrowUpRight color={Colors.accent} size={20} />
            )}
          </View>
          <View style={{ flex: 1 }}>
            <Text style={styles.transactionTitle} numberOfLines={1}>
              {item.description || (isCredit ? 'Earnings' : 'Debit')}
            </Text>
            <Text style={styles.transactionDate}>
              {item.createdAt ? new Date(item.createdAt).toLocaleDateString('en-IN', {
                day: 'numeric', month: 'short', year: 'numeric'
              }) : '—'}
            </Text>
          </View>
        </View>
        <Text style={[styles.transactionAmount, { color: isCredit ? Colors.accentGreen : Colors.accent }]}>
          {isCredit ? '+' : '-'}{currency === 'INR' ? '₹' : currency}{Math.abs(amount).toFixed(2)}
        </Text>
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <View style={styles.headerBg}>
        <View style={styles.decorCircle1} />
        <View style={styles.decorCircle2} />

        <Text style={styles.title}>Wallet</Text>

        <View style={styles.balanceCard}>
          <View style={styles.balanceHeader}>
            <Text style={styles.balanceLabel}>Total Balance</Text>
            <WalletIcon color={Colors.accentYellow} size={24} />
          </View>
          <Text style={styles.balanceValue}>
            {currency === 'INR' ? '₹' : currency}{balance.toFixed(2)}
          </Text>
          <View style={styles.balanceActions}>
            <TouchableOpacity style={styles.withdrawBtn}>
              <Text style={styles.withdrawBtnText}>Withdraw Funds</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>

      <View style={styles.transactionsSection}>
        <Text style={styles.sectionTitle}>Recent Transactions</Text>
        {error ? <Text style={styles.errorText}>{error}</Text> : null}

        {loading && transactions.length === 0 ? (
          <ActivityIndicator size="large" color={Colors.primary} style={{ marginTop: 40 }} />
        ) : (
          <FlatList
            data={transactions}
            keyExtractor={(item: any, index) => item.id?.toString() || index.toString()}
            renderItem={renderTransaction}
            contentContainerStyle={styles.listContent}
            showsVerticalScrollIndicator={false}
            refreshControl={
              <RefreshControl refreshing={refreshing} onRefresh={onRefresh} colors={[Colors.primary]} tintColor={Colors.primary} />
            }
            ListEmptyComponent={
              <View style={styles.emptyState}>
                <TrendingUp color={Colors.textMuted} size={40} />
                <Text style={styles.emptyText}>No transactions yet</Text>
                <Text style={styles.emptySubText}>Your earnings will show up here</Text>
              </View>
            }
          />
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.bgDark },
  headerBg: {
    backgroundColor: Colors.bgCard, paddingTop: 60, paddingHorizontal: 20,
    paddingBottom: 24, borderBottomLeftRadius: 28, borderBottomRightRadius: 28,
    overflow: 'hidden',
  },
  decorCircle1: {
    position: 'absolute', top: -30, right: -40, width: 140, height: 140,
    borderRadius: 70, backgroundColor: Colors.accentYellow, opacity: 0.08,
  },
  decorCircle2: {
    position: 'absolute', bottom: -20, left: -30, width: 100, height: 100,
    borderRadius: 50, backgroundColor: Colors.primary, opacity: 0.1,
  },
  title: { fontSize: 24, fontWeight: '800', color: Colors.textPrimary, marginBottom: 20 },
  balanceCard: {
    backgroundColor: Colors.primary, padding: 24, borderRadius: 20,
    shadowColor: Colors.primary, shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.4, shadowRadius: 16, elevation: 10,
  },
  balanceHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 },
  balanceLabel: { color: '#FFFFFF', fontSize: 14, opacity: 0.8 },
  balanceValue: { color: '#FFFFFF', fontSize: 40, fontWeight: '800', marginBottom: 20, letterSpacing: -1 },
  balanceActions: { flexDirection: 'row' },
  withdrawBtn: {
    backgroundColor: 'rgba(255,255,255,0.2)', paddingVertical: 12, paddingHorizontal: 24,
    borderRadius: 12, borderWidth: 1, borderColor: 'rgba(255,255,255,0.3)',
  },
  withdrawBtnText: { color: '#FFFFFF', fontWeight: '600', fontSize: 14 },
  transactionsSection: { flex: 1, paddingTop: 20, paddingHorizontal: 20 },
  sectionTitle: { fontSize: 18, fontWeight: '700', color: Colors.textPrimary, marginBottom: 16 },
  listContent: { paddingBottom: 40 },
  transactionCard: {
    flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center',
    backgroundColor: Colors.bgCard, padding: 14, borderRadius: 14, marginBottom: 8,
    borderWidth: 1, borderColor: Colors.borderLight,
  },
  transactionLeft: { flexDirection: 'row', alignItems: 'center', flex: 1 },
  iconContainer: { width: 44, height: 44, borderRadius: 12, justifyContent: 'center', alignItems: 'center', marginRight: 14 },
  transactionTitle: { fontSize: 15, fontWeight: '600', color: Colors.textPrimary, marginBottom: 2 },
  transactionDate: { fontSize: 13, color: Colors.textMuted },
  transactionAmount: { fontSize: 16, fontWeight: '700', marginLeft: 8 },
  emptyState: { padding: 50, alignItems: 'center' },
  emptyText: { color: Colors.textSecondary, fontSize: 16, marginTop: 12, fontWeight: '600' },
  emptySubText: { color: Colors.textMuted, fontSize: 14, marginTop: 4 },
  errorText: { color: Colors.accent, fontSize: 13, marginBottom: 10 },
});
