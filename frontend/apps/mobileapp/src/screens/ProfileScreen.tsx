import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, Image } from 'react-native';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store/store';
import { logout } from '../store/authSlice';
import { LogOut, Settings, Bell, CircleHelp, Shield, ChevronRight } from 'lucide-react-native';

export default function ProfileScreen() {
  const { user } = useSelector((state: RootState) => state.auth);
  const dispatch = useDispatch<AppDispatch>();

  const handleLogout = () => {
    dispatch(logout());
  };

  const menuItems = [
    { title: 'Personal Information', icon: <Settings color="#4B5563" size={24} /> },
    { title: 'Notifications', icon: <Bell color="#4B5563" size={24} /> },
    { title: 'Privacy Policy', icon: <Shield color="#4B5563" size={24} /> },
    { title: 'Help Center', icon: <CircleHelp color="#4B5563" size={24} /> },
  ];

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Profile</Text>
      </View>

      <View style={styles.profileCard}>
        <Image
          source={{ uri: 'https://ui-avatars.com/api/?name=' + (user?.name || 'Driver') + '&background=random&size=128' }}
          style={styles.avatar}
        />
        <View style={styles.profileInfo}>
           <Text style={styles.name}>{user?.name || 'Driver Name'}</Text>
           <Text style={styles.contact}>{user?.email || 'driver@example.com'}</Text>
           <Text style={styles.contact}>{user?.mobileNumber || '+1 234 567 8900'}</Text>
        </View>
      </View>

      <View style={styles.menuContainer}>
         {menuItems.map((item, index) => (
           <TouchableOpacity key={index} style={styles.menuItem}>
              <View style={styles.menuLeft}>
                 <View style={styles.menuIcon}>{item.icon}</View>
                 <Text style={styles.menuText}>{item.title}</Text>
              </View>
              <ChevronRight color="#9CA3AF" size={20} />
           </TouchableOpacity>
         ))}
      </View>

      <TouchableOpacity style={styles.logoutBtn} onPress={handleLogout}>
         <LogOut color="#EF4444" size={20} style={{marginRight: 8}} />
         <Text style={styles.logoutText}>Log Out</Text>
      </TouchableOpacity>
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
  }
});
