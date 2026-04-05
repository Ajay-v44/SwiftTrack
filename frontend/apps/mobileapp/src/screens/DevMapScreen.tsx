import React, { useMemo, useState } from 'react';
import {
  GestureResponderEvent,
  Image,
  LayoutChangeEvent,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import { useDispatch } from 'react-redux';
import { AppDispatch } from '../store/store';
import { updateLocation } from '../store/driverSlice';
import { useNavigation } from '@react-navigation/native';
import { ArrowLeft, MapPin } from 'lucide-react-native';
import * as Burnt from 'burnt';
import { Colors } from '../theme/colors';

const ZOOM = 13;
const TILE_SIZE = 256;
const GRID_SIZE = 3;
const DEFAULT_CENTER = {
  latitude: 28.6139,
  longitude: 77.209,
};

function longitudeToTileX(longitude: number, zoom: number) {
  return ((longitude + 180) / 360) * Math.pow(2, zoom);
}

function latitudeToTileY(latitude: number, zoom: number) {
  const latRad = (latitude * Math.PI) / 180;
  return (
    ((1 - Math.log(Math.tan(latRad) + 1 / Math.cos(latRad)) / Math.PI) / 2) *
    Math.pow(2, zoom)
  );
}

function tileXToLongitude(tileX: number, zoom: number) {
  return (tileX / Math.pow(2, zoom)) * 360 - 180;
}

function tileYToLatitude(tileY: number, zoom: number) {
  const n = Math.PI - (2 * Math.PI * tileY) / Math.pow(2, zoom);
  return (180 / Math.PI) * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
}

export default function DevMapScreen() {
  const [selectedLocation, setSelectedLocation] = useState<{ latitude: number; longitude: number } | null>(null);
  const [surfaceSize, setSurfaceSize] = useState({ width: 0, height: 0 });
  const dispatch = useDispatch<AppDispatch>();
  const navigation = useNavigation();

  const tileData = useMemo(() => {
    const centerTileX = longitudeToTileX(DEFAULT_CENTER.longitude, ZOOM);
    const centerTileY = latitudeToTileY(DEFAULT_CENTER.latitude, ZOOM);
    const baseTileX = Math.floor(centerTileX) - 1;
    const baseTileY = Math.floor(centerTileY) - 1;

    const tiles = [];
    for (let row = 0; row < GRID_SIZE; row += 1) {
      for (let col = 0; col < GRID_SIZE; col += 1) {
        const x = baseTileX + col;
        const y = baseTileY + row;
        const subdomain = ['a', 'b', 'c', 'd'][(row + col) % 4];
        tiles.push({
          key: `${x}-${y}`,
          row,
          col,
          uri: `https://${subdomain}.basemaps.cartocdn.com/dark_all/${ZOOM}/${x}/${y}.png`,
        });
      }
    }

    return {
      tiles,
      baseTileX,
      baseTileY,
      minLongitude: tileXToLongitude(baseTileX, ZOOM),
      maxLongitude: tileXToLongitude(baseTileX + GRID_SIZE, ZOOM),
      maxLatitude: tileYToLatitude(baseTileY, ZOOM),
      minLatitude: tileYToLatitude(baseTileY + GRID_SIZE, ZOOM),
    };
  }, []);

  const handleSurfaceLayout = (event: LayoutChangeEvent) => {
    const { width, height } = event.nativeEvent.layout;
    setSurfaceSize({ width, height });
  };

  const handleSurfacePress = (event: GestureResponderEvent) => {
    if (!surfaceSize.width || !surfaceSize.height) {
      return;
    }

    const { locationX, locationY } = event.nativeEvent;
    const longitude =
      tileData.minLongitude +
      (locationX / surfaceSize.width) * (tileData.maxLongitude - tileData.minLongitude);
    const latitude =
      tileData.maxLatitude -
      (locationY / surfaceSize.height) * (tileData.maxLatitude - tileData.minLatitude);

    setSelectedLocation({
      latitude: Number(latitude.toFixed(6)),
      longitude: Number(longitude.toFixed(6)),
    });
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

  const pinPosition = useMemo(() => {
    if (!selectedLocation || !surfaceSize.width || !surfaceSize.height) {
      return null;
    }

    const left =
      ((selectedLocation.longitude - tileData.minLongitude) /
        (tileData.maxLongitude - tileData.minLongitude)) *
      surfaceSize.width;
    const top =
      ((tileData.maxLatitude - selectedLocation.latitude) /
        (tileData.maxLatitude - tileData.minLatitude)) *
      surfaceSize.height;

    return {
      left: Math.max(0, Math.min(surfaceSize.width - 24, left - 12)),
      top: Math.max(0, Math.min(surfaceSize.height - 24, top - 24)),
    };
  }, [selectedLocation, surfaceSize, tileData]);

  return (
    <View style={styles.container}>
      <TouchableOpacity style={styles.backBtn} onPress={() => navigation.goBack()} activeOpacity={0.7}>
        <ArrowLeft color={Colors.textPrimary} size={22} />
      </TouchableOpacity>

      <View style={styles.header}>
        <Text style={styles.headerTitle}>Dev Map</Text>
        <Text style={styles.headerText}>Dark OpenStreetMap tiles without any API key. Tap to select a location.</Text>
      </View>

      <TouchableOpacity
        activeOpacity={1}
        style={styles.mapShell}
        onLayout={handleSurfaceLayout}
        onPress={handleSurfacePress}
      >
        <View style={styles.tileGrid} pointerEvents="none">
          {tileData.tiles.map((tile) => (
            <Image
              key={tile.key}
              source={{ uri: tile.uri }}
              style={styles.tile}
              resizeMode="cover"
            />
          ))}
        </View>
        {pinPosition && (
          <View
            pointerEvents="none"
            style={[
              styles.pinWrapper,
              {
                left: pinPosition.left,
                top: pinPosition.top,
              },
            ]}
          >
            <MapPin color="#FFFFFF" size={18} />
          </View>
        )}
        <View pointerEvents="none" style={styles.crosshair} />
        <View pointerEvents="none" style={styles.attributionChip}>
          <Text style={styles.attributionText}>Tiles © OpenStreetMap / CARTO</Text>
        </View>
      </TouchableOpacity>

      <View style={styles.overlayPanel}>
        <View style={styles.dragIndicator} />
        <Text style={styles.title}>Selected Coordinates</Text>
        <Text style={styles.desc}>Using direct OSM dark tiles. No Google Maps key is required.</Text>
        <View style={styles.coordBox}>
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
    height: 340,
    borderRadius: 28,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: Colors.borderLight,
    backgroundColor: '#101626',
    position: 'relative',
  },
  tileGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    width: '100%',
    height: '100%',
  },
  tile: {
    width: '33.3333%',
    height: '33.3333%',
  },
  pinWrapper: {
    position: 'absolute',
    width: 24,
    height: 24,
    borderRadius: 12,
    backgroundColor: Colors.accent,
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.25,
    shadowRadius: 8,
    elevation: 6,
  },
  crosshair: {
    position: 'absolute',
    left: '50%',
    top: '50%',
    width: 14,
    height: 14,
    marginLeft: -7,
    marginTop: -7,
    borderRadius: 7,
    borderWidth: 2,
    borderColor: Colors.accentTeal,
    backgroundColor: 'rgba(15,15,35,0.5)',
  },
  attributionChip: {
    position: 'absolute',
    right: 10,
    bottom: 10,
    backgroundColor: 'rgba(15, 15, 35, 0.8)',
    borderRadius: 999,
    paddingHorizontal: 8,
    paddingVertical: 5,
  },
  attributionText: {
    color: 'rgba(255,255,255,0.8)',
    fontSize: 10,
  },
  overlayPanel: {
    flex: 1,
    marginTop: 20,
    backgroundColor: Colors.bgCard,
    padding: 24,
    borderTopLeftRadius: 28,
    borderTopRightRadius: 28,
  },
  dragIndicator: {
    width: 40,
    height: 4,
    backgroundColor: Colors.textMuted,
    borderRadius: 2,
    alignSelf: 'center',
    marginBottom: 16,
    opacity: 0.4,
  },
  title: { fontSize: 20, fontWeight: '700', color: Colors.textPrimary, marginBottom: 6 },
  desc: { fontSize: 14, color: Colors.textSecondary, marginBottom: 16 },
  coordBox: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    backgroundColor: Colors.bgGlass,
    padding: 12,
    borderRadius: 10,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: Colors.borderLight,
  },
  coordText: { fontSize: 14, fontWeight: '600', color: Colors.textPrimary },
  btn: {
    backgroundColor: Colors.primary,
    padding: 16,
    borderRadius: 14,
    alignItems: 'center',
    shadowColor: Colors.primary,
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.3,
    shadowRadius: 12,
    elevation: 6,
  },
  btnDisabled: { backgroundColor: Colors.bgCardLight, shadowOpacity: 0, elevation: 0 },
  btnText: { color: '#FFFFFF', fontSize: 16, fontWeight: '700' },
});
