import React, { useMemo, useRef, useState } from 'react';
import { GestureResponderEvent, Image, LayoutChangeEvent, PanResponder, StyleSheet, Text, View } from 'react-native';
import Svg, { Polyline as SvgPolyline } from 'react-native-svg';
import { Colors } from '../theme/colors';

const TILE_SIZE = 256;
const MIN_ZOOM = 3;
const MAX_ZOOM = 18;

export type MapCoordinate = {
  latitude: number;
  longitude: number;
};

export type MapMarker = MapCoordinate & {
  color?: string;
  label?: string;
};

type Props = {
  center: MapCoordinate;
  zoom: number;
  markers?: MapMarker[];
  polyline?: MapCoordinate[];
  onCenterChange: (nextCenter: MapCoordinate) => void;
  onZoomChange?: (zoom: number) => void;
  onPressCoordinate?: (coordinate: MapCoordinate) => void;
  interactive?: boolean;
};

function clampLatitude(latitude: number) {
  return Math.max(-85.0511, Math.min(85.0511, latitude));
}

function normalizeLongitude(longitude: number) {
  let next = longitude;
  while (next < -180) next += 360;
  while (next > 180) next -= 360;
  return next;
}

function longitudeToTileX(longitude: number, zoom: number) {
  return ((normalizeLongitude(longitude) + 180) / 360) * Math.pow(2, zoom);
}

function latitudeToTileY(latitude: number, zoom: number) {
  const latRad = (clampLatitude(latitude) * Math.PI) / 180;
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

function clampZoom(zoom: number) {
  return Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, Math.round(zoom)));
}

export default function OpenStreetMapView({
  center,
  zoom,
  markers = [],
  polyline = [],
  onCenterChange,
  onZoomChange,
  onPressCoordinate,
  interactive = true,
}: Props) {
  const [surfaceSize, setSurfaceSize] = useState({ width: 0, height: 0 });
  const dragStartRef = useRef<{ tileX: number; tileY: number } | null>(null);

  const safeZoom = clampZoom(zoom);

  const tileState = useMemo(() => {
    const width = surfaceSize.width || TILE_SIZE * 2;
    const height = surfaceSize.height || TILE_SIZE * 2;
    const centerTileX = longitudeToTileX(center.longitude, safeZoom);
    const centerTileY = latitudeToTileY(center.latitude, safeZoom);
    const halfTilesX = width / TILE_SIZE / 2;
    const halfTilesY = height / TILE_SIZE / 2;
    const startX = Math.floor(centerTileX - halfTilesX) - 1;
    const endX = Math.floor(centerTileX + halfTilesX) + 1;
    const startY = Math.floor(centerTileY - halfTilesY) - 1;
    const endY = Math.floor(centerTileY + halfTilesY) + 1;
    const tileCount = Math.pow(2, safeZoom);

    const tiles: Array<{ key: string; uri: string; left: number; top: number }> = [];

    for (let tileX = startX; tileX <= endX; tileX += 1) {
      for (let tileY = startY; tileY <= endY; tileY += 1) {
        if (tileY < 0 || tileY >= tileCount) {
          continue;
        }

        const wrappedX = ((tileX % tileCount) + tileCount) % tileCount;
        const left = (tileX - centerTileX) * TILE_SIZE + width / 2;
        const top = (tileY - centerTileY) * TILE_SIZE + height / 2;

        tiles.push({
          key: `${safeZoom}-${tileX}-${tileY}`,
          uri: `https://tile.openstreetmap.org/${safeZoom}/${wrappedX}/${tileY}.png`,
          left,
          top,
        });
      }
    }

    const toScreenPoint = (coordinate: MapCoordinate) => {
      const tileX = longitudeToTileX(coordinate.longitude, safeZoom);
      const tileY = latitudeToTileY(coordinate.latitude, safeZoom);
      return {
        x: (tileX - centerTileX) * TILE_SIZE + width / 2,
        y: (tileY - centerTileY) * TILE_SIZE + height / 2,
      };
    };

    return {
      width,
      height,
      centerTileX,
      centerTileY,
      tiles,
      toScreenPoint,
    };
  }, [center.latitude, center.longitude, safeZoom, surfaceSize.height, surfaceSize.width]);

  const handleSurfaceLayout = (event: LayoutChangeEvent) => {
    const { width, height } = event.nativeEvent.layout;
    setSurfaceSize({ width, height });
  };

  const eventToCoordinate = (event: GestureResponderEvent) => {
    const { locationX, locationY } = event.nativeEvent;
    const tileX = tileState.centerTileX + (locationX - tileState.width / 2) / TILE_SIZE;
    const tileY = tileState.centerTileY + (locationY - tileState.height / 2) / TILE_SIZE;

    return {
      latitude: Number(tileYToLatitude(tileY, safeZoom).toFixed(6)),
      longitude: Number(tileXToLongitude(tileX, safeZoom).toFixed(6)),
    };
  };

  const panResponder = useMemo(
    () =>
      PanResponder.create({
        onStartShouldSetPanResponder: () => interactive,
        onMoveShouldSetPanResponder: (_, gestureState) =>
          interactive && (Math.abs(gestureState.dx) > 2 || Math.abs(gestureState.dy) > 2),
        onPanResponderGrant: () => {
          dragStartRef.current = {
            tileX: longitudeToTileX(center.longitude, safeZoom),
            tileY: latitudeToTileY(center.latitude, safeZoom),
          };
        },
        onPanResponderMove: (_, gestureState) => {
          if (!dragStartRef.current) {
            return;
          }

          const nextTileX = dragStartRef.current.tileX - gestureState.dx / TILE_SIZE;
          const nextTileY = dragStartRef.current.tileY - gestureState.dy / TILE_SIZE;

          onCenterChange({
            latitude: tileYToLatitude(nextTileY, safeZoom),
            longitude: tileXToLongitude(nextTileX, safeZoom),
          });
        },
        onPanResponderRelease: (event, gestureState) => {
          if (interactive && Math.abs(gestureState.dx) < 6 && Math.abs(gestureState.dy) < 6 && onPressCoordinate) {
            onPressCoordinate(eventToCoordinate(event));
          }
          dragStartRef.current = null;
        },
        onPanResponderTerminate: () => {
          dragStartRef.current = null;
        },
      }),
    [center.latitude, center.longitude, interactive, onCenterChange, onPressCoordinate, safeZoom, tileState.centerTileX, tileState.centerTileY]
  );

  return (
    <View style={styles.container} onLayout={handleSurfaceLayout} {...panResponder.panHandlers}>
      <View style={styles.tileSurface}>
        {tileState.tiles.map((tile) => (
          <Image
            key={tile.key}
            source={{ uri: tile.uri }}
            style={[
              styles.tile,
              {
                left: tile.left,
                top: tile.top,
              },
            ]}
          />
        ))}

        {polyline.length > 1 ? (
          <Svg width={tileState.width} height={tileState.height} style={StyleSheet.absoluteFillObject}>
            <SvgPolyline
              points={polyline
                .map((coordinate) => {
                  const point = tileState.toScreenPoint(coordinate);
                  return `${point.x},${point.y}`;
                })
                .join(' ')}
              fill="none"
              stroke={Colors.primary}
              strokeWidth={4}
              strokeLinejoin="round"
              strokeLinecap="round"
            />
          </Svg>
        ) : null}

        {markers.map((marker, index) => {
          const point = tileState.toScreenPoint(marker);
          const color = marker.color || Colors.primary;

          return (
            <View
              key={`${marker.latitude}-${marker.longitude}-${index}`}
              style={[
                styles.markerWrap,
                {
                  left: point.x - 10,
                  top: point.y - 26,
                },
              ]}
            >
              {marker.label ? <Text style={styles.markerLabel}>{marker.label}</Text> : null}
              <View style={[styles.markerDot, { backgroundColor: color, borderColor: `${color}55` }]} />
            </View>
          );
        })}
      </View>

      <View style={styles.attributionChip}>
        <Text style={styles.attributionText}>© OpenStreetMap</Text>
      </View>

      {onZoomChange ? (
        <View style={styles.zoomChip}>
          <Text style={styles.zoomText}>Zoom {safeZoom}</Text>
        </View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    overflow: 'hidden',
  },
  tileSurface: {
    flex: 1,
    backgroundColor: '#111827',
  },
  tile: {
    position: 'absolute',
    width: TILE_SIZE,
    height: TILE_SIZE,
  },
  markerWrap: {
    position: 'absolute',
    alignItems: 'center',
  },
  markerLabel: {
    color: '#FFFFFF',
    backgroundColor: 'rgba(15,23,42,0.92)',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 999,
    fontSize: 11,
    fontWeight: '700',
    marginBottom: 6,
    overflow: 'hidden',
  },
  markerDot: {
    width: 20,
    height: 20,
    borderRadius: 10,
    borderWidth: 4,
    backgroundColor: Colors.primary,
  },
  attributionChip: {
    position: 'absolute',
    left: 10,
    bottom: 10,
    backgroundColor: 'rgba(15,23,42,0.9)',
    borderRadius: 999,
    paddingHorizontal: 10,
    paddingVertical: 6,
  },
  attributionText: {
    color: '#FFFFFF',
    fontSize: 11,
    fontWeight: '600',
  },
  zoomChip: {
    position: 'absolute',
    left: 10,
    top: 10,
    backgroundColor: 'rgba(15,23,42,0.9)',
    borderRadius: 999,
    paddingHorizontal: 10,
    paddingVertical: 6,
  },
  zoomText: {
    color: '#FFFFFF',
    fontSize: 11,
    fontWeight: '700',
  },
});
