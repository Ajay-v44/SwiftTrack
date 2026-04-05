import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, Alert } from 'react-native';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store/store';
import { logout } from '../store/authSlice';
import { LogOut, Settings, Bell, CircleHelp, Shield, ChevronRight, Truck, Star, MapPin } from 'lucide-react-native';
import { useNavigation } from '@react-navigation/native';
import * as Application from 'expo-application';
import * as Burnt from 'burnt';
import { Colors } from '../theme/colors';
import DiceBearAvatar from '../components/DiceBearAvatar';



export default function ProfileScreen() {
  const { driver } = useSelector((state: RootState) => state.auth);
  const { currentLocation, isOnline } = useSelector((state: RootState) => state.driver);
  const dispatch = useDispatch<AppDispatch>();
  const navigation = useNavigation<any>();

  const handleLogout = () => {
    Alert.alert('Logout', 'Are you sure you want to logout?', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Logout', style: 'destructive',
        onPress: () => { dispatch(logout()); Burnt.toast({ title: 'Logged out', preset: 'done' }); },
      },
    ]);
  };

  const driverName = driver?.user?.name || 'Driver Name';
  const driverMobile = driver?.user?.mobile || 'No mobile on file';
  const vehicleType = driver?.vehicleType || '—';
  const vehicleNumber = driver?.vehicleNumber || '—';
  const licenseNumber = driver?.driverLicenseNumber || '—';
  const driverStatus = isOnline ? 'ONLINE' : (driver?.status || 'OFFLINE');

  const menuItems = [
    { title: 'Personal Information', icon: <Settings color={Colors.primaryLight} size={22} />, color: Colors.primary + '15', action: () => Burnt.toast({ title: 'Coming soon', preset: 'none' }) },
    { title: 'Notifications', icon: <Bell color={Colors.accentOrange} size={22} />, color: Colors.accentOrange + '15', action: () => Burnt.toast({ title: 'Coming soon', preset: 'none' }) },
    { title: 'Privacy Policy', icon: <Shield color={Colors.accentTeal} size={22} />, color: Colors.accentTeal + '15', action: () => navigation.navigate('PrivacyPolicy') },
    { title: 'Help Center', icon: <CircleHelp color={Colors.accentPink} size={22} />, color: Colors.accentPink + '15', action: () => navigation.navigate('HelpCenter') },
  ];

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      <View style={styles.headerBg}>
        <View style={styles.decorCircle} />

        <Text style={styles.headerTitle}>Profile</Text>

        <View style={styles.profileCard}>
          <DiceBearAvatar
            seed={driverName}
            size={80}
            radius={24}
            style={styles.avatar}
          />
          <Text style={styles.name}>{driverName}</Text>
          <Text style={styles.mobile}>{driverMobile}</Text>
          <View style={[styles.statusPill, {
            backgroundColor: driverStatus === 'ONLINE' ? Colors.accentGreen + '20' : Colors.accent + '20'
          }]}>
            <View style={[styles.statusDot, {
              backgroundColor: driverStatus === 'ONLINE' ? Colors.accentGreen : Colors.accent
            }]} />
            <Text style={[styles.statusPillText, {
              color: driverStatus === 'ONLINE' ? Colors.accentGreen : Colors.accent
            }]}>{driverStatus}</Text>
          </View>
        </View>
      </View>

      {/* Current Location */}
      {currentLocation && (
        <View style={styles.locationSection}>
          <View style={styles.locationCard}>
            <MapPin color={Colors.accentTeal} size={18} />
            <View style={styles.locationTextContainer}>
              <Text style={styles.locationLabel}>Current Location</Text>
              <Text style={styles.locationCoords}>
                {currentLocation.latitude.toFixed(5)}, {currentLocation.longitude.toFixed(5)}
              </Text>
            </View>
          </View>
        </View>
      )}

      {/* Vehicle Info */}
      <View style={styles.infoSection}>
        <Text style={styles.infoSectionTitle}>Vehicle Details</Text>
        <View style={styles.infoCard}>
          <View style={styles.infoRow}>
            <Truck color={Colors.primaryLight} size={20} />
            <View style={styles.infoTextContainer}>
              <Text style={styles.infoLabel}>Vehicle Type</Text>
              <Text style={styles.infoValue}>{vehicleType}</Text>
            </View>
          </View>
          <View style={styles.infoDivider} />
          <View style={styles.infoRow}>
            <Star color={Colors.accentOrange} size={20} />
            <View style={styles.infoTextContainer}>
              <Text style={styles.infoLabel}>Vehicle Number</Text>
              <Text style={styles.infoValue}>{vehicleNumber}</Text>
            </View>
          </View>
          <View style={styles.infoDivider} />
          <View style={styles.infoRow}>
            <Shield color={Colors.accentTeal} size={20} />
            <View style={styles.infoTextContainer}>
              <Text style={styles.infoLabel}>License Number</Text>
              <Text style={styles.infoValue}>{licenseNumber}</Text>
            </View>
          </View>
        </View>
      </View>

      {/* Menu */}
      <View style={styles.menuContainer}>
        {menuItems.map((item, index) => (
          <TouchableOpacity
            key={index} style={styles.menuItem}
            onPress={item.action}
            activeOpacity={0.7}
          >
            <View style={[styles.menuIcon, { backgroundColor: item.color }]}>{item.icon}</View>
            <Text style={styles.menuText}>{item.title}</Text>
            <ChevronRight color={Colors.textMuted} size={18} />
          </TouchableOpacity>
        ))}
      </View>

      <TouchableOpacity style={styles.logoutBtn} onPress={handleLogout} activeOpacity={0.7}>
        <LogOut color={Colors.accent} size={20} />
        <Text style={styles.logoutText}>Log Out</Text>
      </TouchableOpacity>

      <Text style={styles.versionText}>v{Application.nativeApplicationVersion || '1.0.0'}</Text>
      <View style={{ height: 40 }} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.bgDark },
  headerBg: {
    backgroundColor: Colors.bgCard, paddingTop: 60, paddingBottom: 30,
    borderBottomLeftRadius: 28, borderBottomRightRadius: 28,
    alignItems: 'center', overflow: 'hidden',
  },
  decorCircle: {
    position: 'absolute', top: -50, right: -50, width: 180, height: 180,
    borderRadius: 90, backgroundColor: Colors.primary, opacity: 0.1,
  },
  headerTitle: { fontSize: 20, fontWeight: '700', color: Colors.textPrimary, marginBottom: 20 },
  profileCard: { alignItems: 'center' },
  avatar: {
    width: 80, height: 80, borderRadius: 24,
    justifyContent: 'center', alignItems: 'center', marginBottom: 12,
    shadowColor: '#000', shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.3, shadowRadius: 12, elevation: 8,
  },
  name: { fontSize: 22, fontWeight: '700', color: Colors.textPrimary },
  mobile: { fontSize: 14, color: Colors.textSecondary, marginTop: 4 },
  statusPill: {
    flexDirection: 'row', alignItems: 'center', paddingHorizontal: 12,
    paddingVertical: 6, borderRadius: 20, marginTop: 10, gap: 6,
  },
  statusDot: { width: 8, height: 8, borderRadius: 4 },
  statusPillText: { fontSize: 13, fontWeight: '600' },
  locationSection: { paddingHorizontal: 20, marginTop: 16 },
  locationCard: {
    flexDirection: 'row', alignItems: 'center', backgroundColor: Colors.bgCard,
    padding: 14, borderRadius: 14, gap: 12,
    borderWidth: 1, borderColor: Colors.borderLight,
  },
  locationTextContainer: { flex: 1 },
  locationLabel: { fontSize: 13, color: Colors.textMuted },
  locationCoords: { fontSize: 14, fontWeight: '600', color: Colors.textPrimary, marginTop: 2 },
  infoSection: { paddingHorizontal: 20, marginTop: 20 },
  infoSectionTitle: { fontSize: 16, fontWeight: '700', color: Colors.textPrimary, marginBottom: 12 },
  infoCard: {
    backgroundColor: Colors.bgCard, borderRadius: 18, padding: 16,
    borderWidth: 1, borderColor: Colors.borderLight,
  },
  infoRow: { flexDirection: 'row', alignItems: 'center', paddingVertical: 10 },
  infoTextContainer: { marginLeft: 14, flex: 1 },
  infoLabel: { fontSize: 13, color: Colors.textMuted },
  infoValue: { fontSize: 15, fontWeight: '600', color: Colors.textPrimary, marginTop: 2 },
  infoDivider: { height: 1, backgroundColor: Colors.borderLight, marginVertical: 2 },
  menuContainer: { paddingHorizontal: 20, marginTop: 24 },
  menuItem: {
    flexDirection: 'row', alignItems: 'center', backgroundColor: Colors.bgCard,
    padding: 14, borderRadius: 14, marginBottom: 8,
    borderWidth: 1, borderColor: Colors.borderLight,
  },
  menuIcon: { width: 40, height: 40, borderRadius: 12, justifyContent: 'center', alignItems: 'center', marginRight: 12 },
  menuText: { flex: 1, fontSize: 15, fontWeight: '500', color: Colors.textPrimary },
  logoutBtn: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'center',
    marginHorizontal: 20, marginTop: 24, padding: 16, gap: 8,
    backgroundColor: Colors.accent + '12', borderRadius: 14,
    borderWidth: 1, borderColor: Colors.accent + '30',
  },
  logoutText: { color: Colors.accent, fontSize: 16, fontWeight: '600' },
  versionText: { color: Colors.textMuted, fontSize: 12, textAlign: 'center', marginTop: 20 },
});
