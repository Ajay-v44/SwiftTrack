import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, TextInput, TouchableOpacity, ScrollView, Alert, ActivityIndicator, KeyboardAvoidingView, Platform } from 'react-native';
import { Colors } from '../theme/colors';
import { useNavigation } from '@react-navigation/native';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store/store';
import apiClient from '../api/client';
import { ArrowLeft, Edit2, History, Landmark, Wallet } from 'lucide-react-native';
import { fetchWalletDetails } from '../store/walletSlice';

export default function WithdrawScreen() {
  const navigation = useNavigation();
  const dispatch = useDispatch<AppDispatch>();
  const { balance, accountId, currency } = useSelector((state: RootState) => state.wallet);
  
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [withdrawing, setWithdrawing] = useState(false);
  const [logsLoading, setLogsLoading] = useState(false);
  
  const [amount, setAmount] = useState('');
  
  const [bankDetails, setBankDetails] = useState({
    accountNumber: '',
    ifscCode: '',
    upiId: '',
    accountHolderName: '',
    bankName: ''
  });
  
  const [hasAccount, setHasAccount] = useState(false);
  const [isEditingBank, setIsEditingBank] = useState(false);
  const [settlementLogs, setSettlementLogs] = useState<any[]>([]);

  useEffect(() => {
    fetchBankDetails();
    if (accountId) {
      fetchSettlementLogs(accountId);
    }
  }, [accountId]);

  const fetchBankDetails = async () => {
    try {
      setLoading(true);
      const res = await apiClient.get('/billingandsettlementservice/api/billing/v1/driver-bank-details');
      if (res.data) {
        setBankDetails({
          accountNumber: res.data.accountNumber || '',
          ifscCode: res.data.ifscCode || '',
          upiId: res.data.upiId || '',
          accountHolderName: res.data.accountHolderName || '',
          bankName: res.data.bankName || ''
        });
        const hasData = !!res.data.accountNumber || !!res.data.upiId;
        setHasAccount(hasData);
        setIsEditingBank(!hasData);
      }
    } catch (e: any) {
      if (e.response?.status !== 404) {
        // Only set editing mode if it's 404
      }
      setIsEditingBank(true);
    } finally {
      setLoading(false);
    }
  };

  const fetchSettlementLogs = async (id: string) => {
    try {
      setLogsLoading(true);
      const res = await apiClient.get(`/billingandsettlementservice/api/settlements/account/${id}`);
      // Sort by latest first
      const sorted = (res.data || []).sort((a: any, b: any) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
      setSettlementLogs(sorted);
    } catch (e: any) {
      console.log('Failed to fetch settlement logs', e);
    } finally {
      setLogsLoading(false);
    }
  };

  const handleSaveBankDetails = async () => {
    if (!bankDetails.accountNumber && !bankDetails.upiId) {
      Alert.alert('Incomplete', 'Please provide at least an Account Number or UPI ID.');
      return;
    }
    
    try {
      setSaving(true);
      await apiClient.post('/billingandsettlementservice/api/billing/v1/driver-bank-details', bankDetails);
      setHasAccount(true);
      setIsEditingBank(false);
      Alert.alert('Success', 'Bank details saved successfully');
    } catch (e: any) {
      Alert.alert('Error', 'Failed to save bank details');
    } finally {
      setSaving(false);
    }
  };

  const handleWithdraw = async () => {
    if (!amount || isNaN(Number(amount)) || Number(amount) <= 0) {
      Alert.alert('Invalid Amount', 'Please enter a valid amount.');
      return;
    }
    if (Number(amount) > balance) {
      Alert.alert('Insufficient Balance', 'You cannot withdraw more than your current balance.');
      return;
    }
    if (!accountId) {
      Alert.alert('Error', 'Account details missing. Please try again.');
      return;
    }

    try {
      setWithdrawing(true);
      await apiClient.post(`/billingandsettlementservice/api/settlements/initiate?accountId=${accountId}&amount=${amount}`);
      Alert.alert('Success', 'Settlement initiated successfully');
      setAmount('');
      dispatch(fetchWalletDetails());
      fetchSettlementLogs(accountId);
    } catch (e: any) {
      Alert.alert('Error', e.response?.data?.message || 'Failed to initiate settlement');
    } finally {
      setWithdrawing(false);
    }
  };

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color={Colors.primary} />
      </View>
    );
  }

  return (
    <KeyboardAvoidingView 
      style={styles.container} 
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      keyboardVerticalOffset={Platform.OS === 'ios' ? 0 : 20}
    >
      <View style={styles.header}>
        <TouchableOpacity style={styles.backButton} onPress={() => navigation.goBack()}>
          <ArrowLeft color={Colors.textPrimary} size={24} />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Withdraw Funds</Text>
        <View style={{ width: 44 }} />
      </View>

      <ScrollView 
        style={styles.content} 
        contentContainerStyle={{ paddingBottom: 40 }}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
      >
        <View style={styles.balanceCard}>
          <Wallet color="#FFF" size={32} opacity={0.6} style={{ position: 'absolute', top: 20, right: 20 }} />
          <Text style={styles.balanceLabel}>Available Balance</Text>
          <Text style={styles.balanceValue}>{currency === 'INR' ? '₹' : currency}{balance.toFixed(2)}</Text>
        </View>

        {isEditingBank ? (
          <View style={styles.formCard}>
            <View style={styles.formHeader}>
              <Landmark color={Colors.primary} size={24} />
              <Text style={styles.formTitle}>{hasAccount ? 'Edit Bank Details' : 'Add Bank Details'}</Text>
            </View>
            <Text style={styles.formSubtitle}>We need these details to send your earnings.</Text>
            
            <View style={styles.inputGroup}>
              <Text style={styles.label}>Account Holder Name</Text>
              <TextInput style={styles.input} placeholder="e.g. John Doe" placeholderTextColor={Colors.textMuted} value={bankDetails.accountHolderName} onChangeText={t => setBankDetails({...bankDetails, accountHolderName: t})} />
            </View>

            <View style={styles.inputGroup}>
              <Text style={styles.label}>Bank Name</Text>
              <TextInput style={styles.input} placeholder="e.g. State Bank of India" placeholderTextColor={Colors.textMuted} value={bankDetails.bankName} onChangeText={t => setBankDetails({...bankDetails, bankName: t})} />
            </View>

            <View style={styles.inputGroup}>
              <Text style={styles.label}>Account Number</Text>
              <TextInput style={styles.input} placeholder="123456789" placeholderTextColor={Colors.textMuted} value={bankDetails.accountNumber} onChangeText={t => setBankDetails({...bankDetails, accountNumber: t})} keyboardType="number-pad" />
            </View>

            <View style={styles.inputGroup}>
              <Text style={styles.label}>IFSC Code</Text>
              <TextInput style={styles.input} placeholder="SBIN0000123" placeholderTextColor={Colors.textMuted} value={bankDetails.ifscCode} onChangeText={t => setBankDetails({...bankDetails, ifscCode: t})} autoCapitalize="characters" />
            </View>

            <View style={styles.inputGroup}>
              <Text style={styles.label}>UPI ID (optional)</Text>
              <TextInput style={styles.input} placeholder="user@bank" placeholderTextColor={Colors.textMuted} value={bankDetails.upiId} onChangeText={t => setBankDetails({...bankDetails, upiId: t})} />
            </View>

            <TouchableOpacity style={styles.submitBtn} onPress={handleSaveBankDetails} disabled={saving}>
              {saving ? <ActivityIndicator color="#fff" /> : <Text style={styles.submitBtnText}>{hasAccount ? 'Update Bank Details' : 'Save Bank Details'}</Text>}
            </TouchableOpacity>

            {hasAccount && (
              <TouchableOpacity style={styles.cancelBtn} onPress={() => setIsEditingBank(false)}>
                <Text style={styles.cancelBtnText}>Cancel</Text>
              </TouchableOpacity>
            )}
          </View>
        ) : (
          <>
            <View style={styles.withdrawSection}>
              <Text style={styles.sectionHeading}>Amount to Withdraw</Text>
              <View style={styles.amountInputWrapper}>
                <Text style={styles.currencySymbol}>{currency === 'INR' ? '₹' : currency}</Text>
                <TextInput
                  style={styles.amountInput}
                  placeholder="0.00"
                  placeholderTextColor={Colors.textMuted}
                  value={amount}
                  onChangeText={setAmount}
                  keyboardType="decimal-pad"
                  autoFocus={true}
                />
              </View>
              <TouchableOpacity style={styles.withdrawActionBtn} onPress={handleWithdraw} disabled={withdrawing}>
                {withdrawing ? <ActivityIndicator color="#fff" /> : <Text style={styles.withdrawActionText}>Confirm Withdrawal</Text>}
              </TouchableOpacity>
            </View>

            <View style={styles.infoCard}>
              <View style={styles.infoRow}>
                <Landmark color={Colors.textSecondary} size={20} />
                <View style={styles.infoContent}>
                  <Text style={styles.infoTitle}>Bank Account</Text>
                  <Text style={styles.infoText}>{bankDetails.bankName || 'Unknown Bank'} •••• {bankDetails.accountNumber ? bankDetails.accountNumber.slice(-4) : 'N/A'}</Text>
                </View>
                <TouchableOpacity onPress={() => setIsEditingBank(true)} style={styles.editIconBtn}>
                  <Edit2 color={Colors.primary} size={18} />
                </TouchableOpacity>
              </View>
            </View>

            <View style={styles.historySection}>
            <View style={styles.historyHeader}>
              <History color={Colors.textSecondary} size={20} />
              <Text style={[styles.sectionHeading, { marginBottom: 0, marginLeft: 10 }]}>Recent Withdrawals</Text>
            </View>
              
              {logsLoading ? (
                <ActivityIndicator color={Colors.primary} style={{ marginTop: 20 }} />
              ) : settlementLogs.length === 0 ? (
                <Text style={styles.emptyLogsText}>No recent settlements.</Text>
              ) : (
                settlementLogs.slice(0, 5).map((log: any, idx) => (
                  <View key={idx} style={styles.historyCard}>
                    <View style={styles.historyLeft}>
                      <Text style={styles.historyAmount}>₹{log.amount.toFixed(2)}</Text>
                      <Text style={styles.historyDate}>{new Date(log.createdAt).toLocaleString('en-IN', {
                        day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit'
                      })}</Text>
                    </View>
                    <View style={[styles.statusPill, 
                      log.status === 'SETTLED' ? { backgroundColor: Colors.accentGreen + '20' } 
                      : log.status === 'FAILED' ? { backgroundColor: Colors.accent + '20' } 
                      : { backgroundColor: Colors.accentYellow + '20' }]}>
                      <Text style={[styles.statusText,
                        log.status === 'SETTLED' ? { color: Colors.accentGreen } 
                        : log.status === 'FAILED' ? { color: Colors.accent } 
                        : { color: Colors.accentYellow }]}>
                        {log.status}
                      </Text>
                    </View>
                  </View>
                ))
              )}
            </View>
          </>
        )}
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.bgDark },
  loadingContainer: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: Colors.bgDark },
  header: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    paddingHorizontal: 20, paddingTop: 60, paddingBottom: 20, backgroundColor: Colors.bgCard,
  },
  backButton: { width: 44, height: 44, justifyContent: 'center', alignItems: 'flex-start' },
  headerTitle: { fontSize: 20, fontWeight: '700', color: Colors.textPrimary },
  content: { flex: 1 },
  balanceCard: {
    margin: 20, marginTop: 10, backgroundColor: Colors.primary, padding: 24, borderRadius: 20,
    shadowColor: Colors.primary, shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.3, shadowRadius: 10, elevation: 8, overflow: 'hidden'
  },
  balanceLabel: { color: 'rgba(255,255,255,0.8)', fontSize: 13, fontWeight: '600', marginBottom: 6 },
  balanceValue: { color: '#FFFFFF', fontSize: 36, fontWeight: '800', letterSpacing: -1 },
  
  formCard: {
    backgroundColor: Colors.bgCard, margin: 20, marginTop: 0, padding: 20, borderRadius: 16,
    borderWidth: 1, borderColor: Colors.borderLight,
  },
  formHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: 8 },
  formTitle: { fontSize: 18, fontWeight: '700', color: Colors.textPrimary, marginLeft: 10 },
  formSubtitle: { fontSize: 13, color: Colors.textMuted, marginBottom: 20 },
  inputGroup: { marginBottom: 16 },
  label: { fontSize: 13, fontWeight: '600', color: Colors.textSecondary, marginBottom: 8, textTransform: 'uppercase', letterSpacing: 0.5 },
  input: {
    backgroundColor: Colors.bgDark, borderWidth: 1, borderColor: Colors.borderLight,
    paddingHorizontal: 16, paddingVertical: 14, borderRadius: 12,
    color: Colors.textPrimary, fontSize: 15
  },
  submitBtn: {
    backgroundColor: Colors.primary, paddingVertical: 16, borderRadius: 12,
    alignItems: 'center', marginTop: 10
  },
  submitBtnText: { color: '#FFFFFF', fontWeight: '700', fontSize: 16 },
  cancelBtn: { alignItems: 'center', marginTop: 16, paddingVertical: 8 },
  cancelBtnText: { color: Colors.textSecondary, fontSize: 15, fontWeight: '600' },

  withdrawSection: { paddingHorizontal: 20, marginBottom: 24 },
  sectionHeading: { fontSize: 16, fontWeight: '700', color: Colors.textPrimary, marginBottom: 12 },
  amountInputWrapper: {
    flexDirection: 'row', alignItems: 'center', backgroundColor: Colors.bgCard,
    borderWidth: 1, borderColor: Colors.borderLight, borderRadius: 16, paddingHorizontal: 20, marginBottom: 16
  },
  currencySymbol: { fontSize: 24, fontWeight: '600', color: Colors.textMuted, marginRight: 8 },
  amountInput: {
    flex: 1, fontSize: 28, fontWeight: '700', color: Colors.textPrimary, paddingVertical: 20
  },
  withdrawActionBtn: {
    backgroundColor: Colors.accent, paddingVertical: 18, borderRadius: 16,
    alignItems: 'center', shadowColor: Colors.accent, shadowOpacity: 0.3, shadowRadius: 8, shadowOffset: { width: 0, height: 4 }, elevation: 4
  },
  withdrawActionText: { color: '#FFF', fontSize: 16, fontWeight: '700' },

  infoCard: {
    marginHorizontal: 20, backgroundColor: Colors.bgCard, padding: 16, borderRadius: 16,
    borderWidth: 1, borderColor: Colors.borderLight, marginBottom: 24
  },
  infoRow: { flexDirection: 'row', alignItems: 'center' },
  infoContent: { flex: 1, marginLeft: 12 },
  infoTitle: { fontSize: 12, color: Colors.textMuted, textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 2 },
  infoText: { fontSize: 15, fontWeight: '600', color: Colors.textPrimary },
  editIconBtn: { padding: 8 },

  historySection: { paddingHorizontal: 20 },
  historyHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: 4 },
  historyCard: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    paddingVertical: 16, borderBottomWidth: 1, borderBottomColor: Colors.borderLight
  },
  historyLeft: { flex: 1 },
  historyAmount: { fontSize: 16, fontWeight: '700', color: Colors.textPrimary, marginBottom: 4 },
  historyDate: { fontSize: 13, color: Colors.textMuted },
  statusPill: { paddingHorizontal: 12, paddingVertical: 6, borderRadius: 20 },
  statusText: { fontSize: 11, fontWeight: '800', textTransform: 'uppercase', letterSpacing: 0.5 },
  emptyLogsText: { color: Colors.textMuted, fontSize: 14, fontStyle: 'italic', marginTop: 12 },
});
