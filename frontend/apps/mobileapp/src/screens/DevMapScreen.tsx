import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { useDispatch } from 'react-redux';
import { AppDispatch } from '../store/store';
import { updateLocation } from '../store/driverSlice';
import MapView, { Marker } from 'react-native-maps';
import { useNavigation } from '@react-navigation/native';
import { ArrowLeft } from 'lucide-react-native';
import * as Burnt from 'burnt';
import { Colors } from '../theme/colors';

export default function DevMapScreen() {
  const [selectedLocation, setSelectedLocation] = useState<{ latitude: number; longitude: number } | null>(null);
  const dispatch = useDispatch<AppDispatch>();
  const navigation = useNavigation();

  const handleMapPress = (e: any) => {
    setSelectedLocation({
      latitude: e.nativeEvent.coordinate.latitude,
      longitude: e.nativeEvent.coordinate.longitude,
    });
  };

  const handleUpdateLocation = async () => {
    if (selectedLocation) {
      try {
        await dispatch(updateLocation(selectedLocation)).unwrap();
        Burnt.toast({ title: '📍 Location updated!', preset: 'done' });
      } catch (err: any) {
        Burnt.toast({ title: typeof err === 'string' ? err : 'Failed', preset: 'error' });
      }
    }
  };

  return (
    <View style={styles.container}>
      <TouchableOpacity style={styles.backBtn} onPress={() => navigation.goBack()} activeOpacity={0.7}>
        <ArrowLeft color={Colors.textPrimary} size={22} />
      </TouchableOpacity>

      <MapView
        style={styles.map}
        initialRegion={{ latitude: 37.78825, longitude: -122.4324, latitudeDelta: 0.0922, longitudeDelta: 0.0421 }}
        onPress={handleMapPress}
      >
        {selectedLocation && (
          <Marker coordinate={selectedLocation} title="Selected Location" />
        )}
      </MapView>

      <View style={styles.overlayPanel}>
        <View style={styles.dragIndicator} />
        <Text style={styles.title}>Dev Location Simulator</Text>
        <Text style={styles.desc}>Tap map to select, then simulate the update.</Text>
        <View style={styles.coordBox}>
          <Text style={styles.coordText}>Lat: {selectedLocation?.latitude?.toFixed(5) || '---'}</Text>
          <Text style={styles.coordText}>Lng: {selectedLocation?.longitude?.toFixed(5) || '---'}</Text>
        </View>
        <TouchableOpacity
          style={[styles.btn, !selectedLocation && styles.btnDisabled]}
          disabled={!selectedLocation} onPress={handleUpdateLocation} activeOpacity={0.8}
        >
          <Text style={styles.btnText}>Simulate Update 📍</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.bgDark },
  map: { flex: 1 },
  backBtn: {
    position: 'absolute', top: 56, left: 20, zIndex: 10,
    backgroundColor: Colors.bgCard, padding: 12, borderRadius: 14,
    borderWidth: 1, borderColor: Colors.borderLight,
  },
  overlayPanel: {
    position: 'absolute', bottom: 0, left: 0, right: 0,
    backgroundColor: Colors.bgCard, padding: 24,
    borderTopLeftRadius: 28, borderTopRightRadius: 28,
  },
  dragIndicator: {
    width: 40, height: 4, backgroundColor: Colors.textMuted, borderRadius: 2,
    alignSelf: 'center', marginBottom: 16, opacity: 0.4,
  },
  title: { fontSize: 20, fontWeight: '700', color: Colors.textPrimary, marginBottom: 6 },
  desc: { fontSize: 14, color: Colors.textSecondary, marginBottom: 16 },
  coordBox: {
    flexDirection: 'row', justifyContent: 'space-between',
    backgroundColor: Colors.bgGlass, padding: 12, borderRadius: 10, marginBottom: 16,
    borderWidth: 1, borderColor: Colors.borderLight,
  },
  coordText: { fontSize: 14, fontWeight: '600', color: Colors.textPrimary },
  btn: {
    backgroundColor: Colors.primary, padding: 16, borderRadius: 14, alignItems: 'center',
    shadowColor: Colors.primary, shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.3, shadowRadius: 12, elevation: 6,
  },
  btnDisabled: { backgroundColor: Colors.bgCardLight, shadowOpacity: 0, elevation: 0 },
  btnText: { color: '#FFFFFF', fontSize: 16, fontWeight: '700' },
});
