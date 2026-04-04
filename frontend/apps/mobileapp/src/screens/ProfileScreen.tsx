import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, Image, Alert } from 'react-native';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store/store';
import { logout } from '../store/authSlice';
import { LogOut, Settings, Bell, CircleHelp, Shield, ChevronRight } from 'lucide-react-native';
import * as Application from 'expo-application';
import * as Updates from 'expo-updates';
import * as Burnt from 'burnt';

export default function ProfileScreen() {
  const { user } = useSelector((state: RootState) => state.auth);
  const dispatch = useDispatch<AppDispatch>();

  const handleLogout = () => {
    Alert.alert(
      'Logout',
      'Are you sure you want to logout?',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Logout',
          style: 'destructive',
          onPress: () => {
            dispatch(logout());
            Burnt.toast({ title: 'Logged out successfully', preset: 'done' });
          },
        },
      ]
    );
  };

  const menuItems = [
    { title: 'Personal Information', icon: <Settings color="#4B5563" size={24} />, onPress: () => Burnt.toast({ title: 'Coming soon', preset: 'none' }) },
    { title: 'Notifications', icon: <Bell color="#4B5563" size={24} />, onPress: () => Burnt.toast({ title: 'Coming soon', preset: 'none' }) },
    { title: 'Privacy Policy', icon: <Shield color="#4B5563" size={24} />, onPress: () => Burnt.toast({ title: 'Coming soon', preset: 'none' }) },
    { title: 'Help Center', icon: <CircleHelp color="#4B5563" size={24} />, onPress: () => Burnt.toast({ title: 'Coming soon', preset: 'none' }) },
  ];

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      <View style={styles.header}>
        <Text style={styles.title}>Profile</Text>
      </View>

      <View style={styles.profileCard}>
        <Image
          source={require('../../assets/images/Driver.jpg')}
          style={styles.avatar}
        />
        <View style={styles.profileInfo}>
           <Text style={styles.name}>{user?.name || 'Driver Name'}</Text>
           <Text style={styles.contact}>{user?.email || 'No email on file'}</Text>
           <Text style={styles.contact}>{user?.mobileNumber || 'No mobile on file'}</Text>
        </View>
      </View>

      <View style={styles.menuContainer}>
         {menuItems.map((item, index) => (
           <TouchableOpacity key={index} style={styles.menuItem} onPress={item.onPress} activeOpacity={0.7}>
              <View style={styles.menuLeft}>
                 <View style={styles.menuIcon}>{item.icon}</View>
                 <Text style={styles.menuText}>{item.title}</Text>
              </View>
              <ChevronRight color="#9CA3AF" size={20} />
           </TouchableOpacity>
         ))}
      </View>

      <TouchableOpacity style={styles.logoutBtn} onPress={handleLogout} activeOpacity={0.7}>
         <LogOut color="#EF4444" size={20} style={{marginRight: 8}} />
         <Text style={styles.logoutText}>Log Out</Text>
      </TouchableOpacity>

      <View style={styles.versionContainer}>
        <Text style={styles.versionText}>
          v{Application.nativeApplicationVersion || '1.0.0'} {Updates.updateId ? `• OTA: ${Updates.updateId.substring(0, 8)}` : ''}
        </Text>
      </View>
    </ScrollView>
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
  profileCard: {
    backgroundColor: '#FFFFFF',
    margin: 20,
    padding: 20,
    borderRadius: 16,
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#E5E7EB',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 2,
  },
  avatar: {
    width: 80,
    height: 80,
    borderRadius: 40,
    marginRight: 16,
  },
  profileInfo: {
    flex: 1,
  },
  name: {
    fontSize: 20,
    fontWeight: '700',
    color: '#111827',
    marginBottom: 4,
  },
  contact: {
    fontSize: 14,
    color: '#6B7280',
    marginBottom: 2,
  },
  menuContainer: {
    backgroundColor: '#FFFFFF',
    marginHorizontal: 20,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#E5E7EB',
    overflow: 'hidden',
  },
  menuItem: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#F3F4F6',
  },
  menuLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  menuIcon: {
    width: 40,
    height: 40,
    borderRadius: 8,
    backgroundColor: '#F3F4F6',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  menuText: {
    fontSize: 16,
    fontWeight: '500',
    color: '#374151',
  },
  logoutBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    margin: 20,
    padding: 16,
    backgroundColor: '#FEF2F2',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#FCA5A5',
  },
  logoutText: {
    color: '#DC2626',
    fontSize: 16,
    fontWeight: '600',
  },
  versionContainer: {
    alignItems: 'center',
    marginBottom: 40,
    marginTop: 10,
  },
  versionText: {
    color: '#9CA3AF',
    fontSize: 12,
  },
});
