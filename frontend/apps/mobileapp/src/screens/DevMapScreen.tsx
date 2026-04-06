import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { ActivityIndicator, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import * as Location from 'expo-location';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigation } from '@react-navigation/native';
import { ArrowLeft, LocateFixed, Minus, Plus } from 'lucide-react-native';
import * as Burnt from 'burnt';
import { AppDispatch, RootState } from '../store/store';
import { updateLocation } from '../store/driverSlice';
import { Colors } from '../theme/colors';
import OlaMapView from '../components/OlaMapView';

const FALLBACK_CENTER = {
  latitude: 28.6139,
  longitude: 77.209,
};

export default function DevMapScreen() {
  const dispatch = useDispatch<AppDispatch>();
  const navigation = useNavigation();
  const currentLocation = useSelector((state: RootState) => state.driver.currentLocation);
  const [selectedLocation, setSelectedLocation] = useState<{ latitude: number; longitude: number } | null>(null);
  const [mapCenter, setMapCenter] = useState(FALLBACK_CENTER);
  const [zoom, setZoom] = useState(14);
  const [isLocating, setIsLocating] = useState(true);
  const [selectedPlace, setSelectedPlace] = useState<string | null>(null);

  const currentMarker = useMemo(
    () =>
      currentLocation
        ? {
            latitude: currentLocation.latitude,
            longitude: currentLocation.longitude,
          }
        : null,
    [currentLocation]
  );

  const syncToDeviceLocation = useCallback(async () => {
    try {
      setIsLocating(true);
      const { status } = await Location.requestForegroundPermissionsAsync();
      if (status !== 'granted') {
        setIsLocating(false);
        return;
      }

      const location = await Location.getCurrentPositionAsync({
        accuracy: Location.Accuracy.Balanced,
      });

      const nextLocation = {
        latitude: Number(location.coords.latitude.toFixed(6)),
        longitude: Number(location.coords.longitude.toFixed(6)),
      };

      setMapCenter({
        latitude: nextLocation.latitude,
        longitude: nextLocation.longitude,
      });
      setSelectedLocation(nextLocation);
    } catch (error) {
      console.log('Failed to fetch current map location', error);
    } finally {
      setIsLocating(false);
    }
  }, []);

  useEffect(() => {
    void syncToDeviceLocation();
  }, [syncToDeviceLocation]);

  useEffect(() => {
    if (!currentMarker) {
      return;
    }

    setMapCenter({
      latitude: currentMarker.latitude,
      longitude: currentMarker.longitude,
    });
  }, [currentMarker]);

  useEffect(() => {
    const resolvePlace = async () => {
      if (!selectedLocation) {
        setSelectedPlace(null);
        return;
      }

      try {
        const results = await Location.reverseGeocodeAsync(selectedLocation);
        const place = results[0];
        if (!place) {
          setSelectedPlace('Place unavailable');
          return;
        }

        const parts = [place.name, place.street, place.city, place.region].filter(Boolean);
        setSelectedPlace(parts.length ? parts.join(', ') : 'Place unavailable');
      } catch (error) {
        console.log('Failed to reverse geocode selected location', error);
        setSelectedPlace('Place unavailable');
      }
    };

    void resolvePlace();
  }, [selectedLocation]);

  const updateZoom = (direction: 'in' | 'out') => {
    setZoom((prev) => Math.max(3, Math.min(18, direction === 'in' ? prev + 1 : prev - 1)));
  };

  const handleUpdateLocation = async () => {
    if (!selectedLocation) {
      return;
    }

    try {
      await dispatch(updateLocation(selectedLocation)).unwrap();
      Burnt.toast({ title: 'Location updated', preset: 'done' });
    } catch (err: any) {
      Burnt.toast({ title: typeof err === 'string' ? err : 'Failed', preset: 'error' });
    }
  };

  return (
    <View style={styles.container}>
      <TouchableOpacity style={styles.backBtn} onPress={() => navigation.goBack()} activeOpacity={0.7}>
        <ArrowLeft color={Colors.textPrimary} size={22} />
      </TouchableOpacity>

      <View style={styles.header}>
        <Text style={styles.headerTitle}>Dev Map</Text>
        <Text style={styles.headerText}>
          Your current location is marked automatically. Pinch or use the controls to zoom, then tap anywhere to select a place.
        </Text>
      </View>

      <View style={styles.mapShell}>
        <OlaMapView
          center={mapCenter}
          zoom={zoom}
          onCenterChange={setMapCenter}
          onZoomChange={setZoom}
          onPressCoordinate={(coordinate) => {
            setSelectedLocation(coordinate);
          }}
          markers={[
            ...(currentMarker
              ? [{ ...currentMarker, color: Colors.accentTeal, label: 'You' }]
              : []),
            ...(selectedLocation
              ? [{ ...selectedLocation, color: Colors.primary, label: 'Selected' }]
              : []),
          ]}
        />

        <View style={styles.mapControls}>
          <TouchableOpacity style={styles.controlBtn} onPress={() => updateZoom('in')} activeOpacity={0.8}>
            <Plus color={Colors.textPrimary} size={18} />
          </TouchableOpacity>
          <TouchableOpacity style={styles.controlBtn} onPress={() => updateZoom('out')} activeOpacity={0.8}>
            <Minus color={Colors.textPrimary} size={18} />
          </TouchableOpacity>
          <TouchableOpacity style={styles.controlBtn} onPress={() => void syncToDeviceLocation()} activeOpacity={0.8}>
            <LocateFixed color={Colors.textPrimary} size={18} />
          </TouchableOpacity>
        </View>

        {isLocating ? (
          <View style={styles.locatingChip}>
            <ActivityIndicator size="small" color={Colors.textPrimary} />
            <Text style={styles.locatingText}>Locating device…</Text>
          </View>
        ) : null}
      </View>

      <View style={styles.overlayPanel}>
        <View style={styles.dragIndicator} />
        <Text style={styles.title}>Selected Place</Text>
        <Text style={styles.desc}>
          {selectedPlace || 'Tap anywhere on the map to select a place, then push that point to the driver location API.'}
        </Text>
        <View style={styles.coordBox}>
          <Text style={styles.coordLabel}>Resolved location</Text>
          <Text style={styles.placeText}>{selectedPlace || '---'}</Text>
          <Text style={styles.coordText}>Lat: {selectedLocation?.latitude?.toFixed(5) || '---'}</Text>
          <Text style={styles.coordText}>Lng: {selectedLocation?.longitude?.toFixed(5) || '---'}</Text>
        </View>
        <TouchableOpacity
          style={[styles.btn, !selectedLocation && styles.btnDisabled]}
          disabled={!selectedLocation}
          onPress={handleUpdateLocation}
          activeOpacity={0.8}
        >
          <Text style={styles.btnText}>Update Driver Location</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.bgDark },
  backBtn: {
    position: 'absolute',
    top: 56,
    left: 20,
    zIndex: 10,
    backgroundColor: Colors.bgCard,
    padding: 12,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: Colors.borderLight,
  },
  header: {
    paddingTop: 120,
    paddingHorizontal: 20,
    paddingBottom: 18,
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: '800',
    color: Colors.textPrimary,
    marginBottom: 6,
  },
  headerText: {
    fontSize: 14,
    color: Colors.textSecondary,
  },
  mapShell: {
    marginHorizontal: 20,
    height: 380,
    borderRadius: 26,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: Colors.borderLight,
    backgroundColor: Colors.bgCard,
  },
  mapControls: {
    position: 'absolute',
    right: 14,
    top: 14,
    gap: 10,
  },
  controlBtn: {
    width: 42,
    height: 42,
    borderRadius: 14,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(15,15,35,0.88)',
    borderWidth: 1,
    borderColor: Colors.borderLight,
  },
  locatingChip: {
    position: 'absolute',
    left: 14,
    top: 14,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 999,
    backgroundColor: 'rgba(15,15,35,0.88)',
    borderWidth: 1,
    borderColor: Colors.borderLight,
  },
  locatingText: {
    color: Colors.textPrimary,
    fontSize: 12,
    fontWeight: '600',
  },
  overlayPanel: {
    marginTop: 18,
    marginHorizontal: 20,
    backgroundColor: Colors.bgCard,
    borderRadius: 28,
    padding: 20,
    paddingBottom: 28,
    borderWidth: 1,
    borderColor: Colors.borderLight,
  },
  dragIndicator: {
    alignSelf: 'center',
    width: 54,
    height: 5,
    borderRadius: 999,
    backgroundColor: Colors.borderLight,
    marginBottom: 18,
  },
  title: {
    color: Colors.textPrimary,
    fontSize: 20,
    fontWeight: '800',
    marginBottom: 8,
  },
  desc: {
    color: Colors.textSecondary,
    fontSize: 14,
    lineHeight: 20,
    marginBottom: 18,
  },
  coordBox: {
    backgroundColor: Colors.bgDark,
    borderRadius: 18,
    padding: 16,
    marginBottom: 18,
    gap: 6,
  },
  coordLabel: {
    color: Colors.textMuted,
    fontSize: 12,
    textTransform: 'uppercase',
    fontWeight: '700',
    marginBottom: 4,
  },
  placeText: {
    color: Colors.textPrimary,
    fontSize: 14,
    lineHeight: 20,
    fontWeight: '600',
    marginBottom: 8,
  },
  coordText: {
    color: Colors.textPrimary,
    fontSize: 15,
    fontWeight: '600',
  },
  btn: {
    backgroundColor: Colors.primary,
    borderRadius: 18,
    paddingVertical: 16,
    alignItems: 'center',
  },
  btnDisabled: {
    opacity: 0.45,
  },
  btnText: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '700',
  },
});
