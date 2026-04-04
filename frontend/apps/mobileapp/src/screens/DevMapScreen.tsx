import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { useDispatch } from 'react-redux';
import { AppDispatch } from '../store/store';
import { updateLocation } from '../store/driverSlice';
import MapView, { Marker } from 'react-native-maps';
import { useNavigation } from '@react-navigation/native';
import { ArrowLeft } from 'lucide-react-native';
import * as Burnt from 'burnt';

export default function DevMapScreen() {
  const [selectedLocation, setSelectedLocation] = useState<{lat: number, lng: number} | null>(null);
  const dispatch = useDispatch<AppDispatch>();
  const navigation = useNavigation();

  const handleMapPress = (e: any) => {
    setSelectedLocation({
      lat: e.nativeEvent.coordinate.latitude,
      lng: e.nativeEvent.coordinate.longitude
    });
  };

  const handleUpdateLocation = async () => {
    if (selectedLocation) {
      try {
        await dispatch(updateLocation(selectedLocation)).unwrap();
        Burnt.toast({ title: 'Location updated successfully', preset: 'done' });
      } catch (err: any) {
        Burnt.toast({
          title: typeof err === 'string' ? err : 'Failed to update location',
          preset: 'error',
        });
      }
    }
  };

  return (
    <View style={styles.container}>
      <TouchableOpacity
          style={styles.backBtn}
          onPress={() => navigation.goBack()}
          activeOpacity={0.7}
      >
         <ArrowLeft color="#111827" size={24} />
      </TouchableOpacity>

      <MapView
        style={styles.map}
        initialRegion={{
          latitude: 37.78825,
          longitude: -122.4324,
          latitudeDelta: 0.0922,
          longitudeDelta: 0.0421,
        }}
        onPress={handleMapPress}
      >
        {selectedLocation && (
          <Marker
            coordinate={{latitude: selectedLocation.lat, longitude: selectedLocation.lng}}
            title="Selected Location"
          />
        )}
      </MapView>

      <View style={styles.overlayPanel}>
         <Text style={styles.title}>Dev Location Simulator</Text>
         <Text style={styles.desc}>Tap anywhere on the map to select a location, then press the button below to simulate sending this location to the backend.</Text>

         <View style={styles.coordBox}>
            <Text style={styles.coordText}>
              Lat: {selectedLocation?.lat?.toFixed(5) || '---'}
            </Text>
            <Text style={styles.coordText}>
              Lng: {selectedLocation?.lng?.toFixed(5) || '---'}
            </Text>
         </View>

         <TouchableOpacity
           style={[styles.btn, !selectedLocation && styles.btnDisabled]}
           disabled={!selectedLocation}
           onPress={handleUpdateLocation}
           activeOpacity={0.8}
         >
            <Text style={styles.btnText}>Simulate Location Update</Text>
         </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  map: {
    flex: 1,
  },
  backBtn: {
    position: 'absolute',
    top: 60,
    left: 20,
    zIndex: 10,
    backgroundColor: '#FFFFFF',
    padding: 12,
    borderRadius: 24,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 4,
  },
  overlayPanel: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: '#FFFFFF',
    padding: 24,
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: -2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 8,
  },
  title: {
    fontSize: 20,
    fontWeight: '700',
    color: '#111827',
    marginBottom: 8,
  },
  desc: {
    fontSize: 14,
    color: '#6B7280',
    marginBottom: 16,
    lineHeight: 20,
  },
  coordBox: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    backgroundColor: '#F3F4F6',
    padding: 12,
    borderRadius: 8,
    marginBottom: 16,
  },
  coordText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#374151',
  },
  btn: {
    backgroundColor: '#2563EB',
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
  btnDisabled: {
    backgroundColor: '#9CA3AF',
  },
  btnText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '600',
  },
});
