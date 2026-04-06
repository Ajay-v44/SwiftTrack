import React, { useEffect, useMemo, useRef } from 'react';
import { UIManager, StyleSheet, Text, View } from 'react-native';
import { olaMapsConfig } from '../config/olaMaps';
import OpenStreetMapView from './OpenStreetMapView';

export type OlaMapCoordinate = {
  latitude: number;
  longitude: number;
};

export type OlaMapMarker = OlaMapCoordinate & {
  color?: string;
  label?: string;
};

type Props = {
  center: OlaMapCoordinate;
  zoom: number;
  markers?: OlaMapMarker[];
  polyline?: OlaMapCoordinate[];
  interactive?: boolean;
  onPressCoordinate?: (coordinate: OlaMapCoordinate) => void;
  onCenterChange?: (coordinate: OlaMapCoordinate) => void;
  onZoomChange?: (zoom: number) => void;
};

const SDK_SCRIPT_URL = 'https://unpkg.com/olamaps-web-sdk@latest/dist/olamaps-web-sdk.umd.js';

function escapeJson(value: unknown) {
  return JSON.stringify(value).replace(/</g, '\\u003c');
}

export default function OlaMapView({
  center,
  zoom,
  markers = [],
  polyline = [],
  interactive = true,
  onPressCoordinate,
  onCenterChange,
  onZoomChange,
}: Props) {
  const webViewRef = useRef<any>(null);

  let WebViewComponent: any = null;
  try {
    const hasViewManager = UIManager.hasViewManagerConfig('RNCWebView');
    if (hasViewManager) {
      WebViewComponent = require('react-native-webview').WebView;
    }
  } catch (error) {}

  const initialConfigJson = useMemo(
    () =>
      escapeJson({
        apiKey: olaMapsConfig.apiKey,
        styleUrl: olaMapsConfig.styleUrl,
        center: [center.longitude, center.latitude],
        zoom,
        markers,
        polyline,
        interactive,
      }),
    []
  );

  const html = useMemo(() => {
    return `<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" />
    <style>
      html, body, #map { margin: 0; padding: 0; width: 100%; height: 100%; background: #0f172a; overflow: hidden; }
      .olamaps-control-container { display: ${interactive ? 'block' : 'none'}; }
    </style>
  </head>
  <body>
    <div id="map"></div>
    <script src="${SDK_SCRIPT_URL}"></script>
    <script>
      window.config = ${initialConfigJson};
      const post = (payload) => window.ReactNativeWebView && window.ReactNativeWebView.postMessage(JSON.stringify(payload));
      
      window.applyData = function() {
        if (!window.map) return;

        const features = window.config.markers.map((marker, index) => ({
          type: 'Feature',
          geometry: {
            type: 'Point',
            coordinates: [marker.longitude, marker.latitude],
          },
          properties: {
            id: index,
            color: marker.color || '#5B8DEF',
            label: marker.label || '',
          },
        }));

        const markerSource = {
          type: 'FeatureCollection',
          features,
        };

        if (window.map.getSource('swifttrack-markers')) {
          window.map.getSource('swifttrack-markers').setData(markerSource);
        } else {
          window.map.addSource('swifttrack-markers', {
            type: 'geojson',
            data: markerSource,
          });
          window.map.addLayer({
            id: 'swifttrack-markers-layer',
            type: 'circle',
            source: 'swifttrack-markers',
            paint: {
              'circle-radius': 7,
              'circle-color': ['get', 'color'],
              'circle-stroke-color': '#ffffff',
              'circle-stroke-width': 2,
            },
          });
          window.map.addLayer({
            id: 'swifttrack-marker-labels',
            type: 'symbol',
            source: 'swifttrack-markers',
            layout: {
              'text-field': ['get', 'label'],
              'text-size': 11,
              'text-offset': [0, -1.5],
              'text-anchor': 'top',
            },
            paint: {
              'text-color': '#ffffff',
              'text-halo-color': '#0f172a',
              'text-halo-width': 1.2,
            },
          });
        }

        const routeSource = {
          type: 'FeatureCollection',
          features: window.config.polyline.length > 1 ? [{
            type: 'Feature',
            geometry: {
              type: 'LineString',
              coordinates: window.config.polyline.map((point) => [point.longitude, point.latitude]),
            },
            properties: {},
          }] : [],
        };

        if (window.map.getSource('swifttrack-route')) {
          window.map.getSource('swifttrack-route').setData(routeSource);
        } else {
          window.map.addSource('swifttrack-route', {
            type: 'geojson',
            data: routeSource,
          });
          window.map.addLayer({
            id: 'swifttrack-route-layer',
            type: 'line',
            source: 'swifttrack-route',
            layout: {
              'line-cap': 'round',
              'line-join': 'round',
            },
            paint: {
              'line-color': '#5B8DEF',
              'line-width': 4,
            },
          });
        }
      };

      window.updateMapState = function(data) {
        if (!window.map) return;
        window.config.markers = data.markers || window.config.markers;
        window.config.polyline = data.polyline || window.config.polyline;
        window.applyData();
        
        if (data.zoom !== undefined && data.zoom !== null) {
          const currentZoom = Number(window.map.getZoom().toFixed(2));
          if (Math.abs(currentZoom - data.zoom) > 0.1) {
            window.map.setZoom(data.zoom);
          }
        }
        if (data.center) {
          const currentCenter = window.map.getCenter();
          const l1 = Number(currentCenter.lat.toFixed(6));
          const l2 = Number(currentCenter.lng.toFixed(6));
          if (Math.abs(l1 - data.center.latitude) > 0.0001 || Math.abs(l2 - data.center.longitude) > 0.0001) {
            window.map.setCenter([data.center.longitude, data.center.latitude]);
          }
        }
      };

      function hideNoiseLayers() {
        try {
          const layers = window.map.getStyle().layers || [];
          layers.forEach((layer) => {
            const id = String(layer.id || '').toLowerCase();
            if (
              id.includes('poi') ||
              id.includes('place') ||
              id.includes('transit') ||
              id.includes('airport') ||
              id.includes('aerodrome') ||
              id.includes('rail') ||
              id.includes('bus') ||
              id.includes('mountain_peak')
            ) {
              try {
                window.map.setLayoutProperty(layer.id, 'visibility', 'none');
              } catch (error) {}
            }
          });
        } catch (error) {}
      }

      function boot() {
        if (!window.OlaMaps) {
          post({ type: 'error', message: 'Ola Maps SDK failed to load' });
          return;
        }

        const olaMaps = new window.OlaMaps({ apiKey: window.config.apiKey });
        window.map = olaMaps.init({
          container: 'map',
          style: window.config.styleUrl,
          center: window.config.center,
          zoom: window.config.zoom,
          dragRotate: false,
          pitchWithRotate: false,
          doubleClickZoom: window.config.interactive,
          scrollZoom: window.config.interactive,
          dragPan: window.config.interactive,
          touchZoomRotate: window.config.interactive,
          keyboard: false,
          attributionControl: false,
        });

        window.map.on('load', () => {
          hideNoiseLayers();
          window.applyData();
          
          if (window.config.interactive && window.OlaMapsNavigationControl) {
            window.map.addControl(new window.OlaMapsNavigationControl({
                showCompass: true,
                showZoom: true,
                visualizePitch: false
            }), 'top-left');
          }

          post({ type: 'loaded' });
        });

        window.map.on('click', (event) => {
          if (!window.config.interactive) return;
          post({
            type: 'click',
            coordinate: {
              latitude: Number(event.lngLat.lat.toFixed(6)),
              longitude: Number(event.lngLat.lng.toFixed(6)),
            },
          });
        });

        window.map.on('moveend', () => {
          const nextCenter = window.map.getCenter();
          post({
            type: 'moveend',
            center: {
              latitude: Number(nextCenter.lat.toFixed(6)),
              longitude: Number(nextCenter.lng.toFixed(6)),
            },
            zoom: Number(window.map.getZoom().toFixed(2)),
          });
        });
      }

      window.addEventListener('load', boot);
    </script>
  </body>
</html>`;
  }, [interactive, initialConfigJson]);

  useEffect(() => {
    if (!webViewRef.current) return;
    const data = escapeJson({ center, zoom, markers, polyline });
    const updateJS = `
      if (window.updateMapState) {
        window.updateMapState(${data});
      }
      true;
    `;
    webViewRef.current.injectJavaScript(updateJS);
  }, [center, zoom, markers, polyline]);

  if (!WebViewComponent) {
    return (
      <View style={styles.container}>
        <OpenStreetMapView
          center={center}
          zoom={zoom}
          markers={markers}
          polyline={polyline}
          interactive={interactive}
          onCenterChange={onCenterChange || (() => {})}
          onZoomChange={onZoomChange}
          onPressCoordinate={onPressCoordinate}
        />
        <View style={styles.fallbackBanner}>
          <Text style={styles.fallbackText}>Ola map needs a rebuilt app binary with WebView. Using fallback map for now.</Text>
        </View>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <WebViewComponent
        ref={webViewRef}
        originWhitelist={['*']}
        source={{ html }}
        javaScriptEnabled
        domStorageEnabled
        scrollEnabled={false}
        bounces={false}
        showsHorizontalScrollIndicator={false}
        showsVerticalScrollIndicator={false}
        onMessage={(event: any) => {
          try {
            const payload = JSON.parse(event.nativeEvent.data);
            if (payload.type === 'click' && onPressCoordinate) {
              onPressCoordinate(payload.coordinate);
            }
            if (payload.type === 'moveend') {
              onCenterChange?.(payload.center);
              onZoomChange?.(Math.round(payload.zoom));
            }
          } catch (error) {}
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    overflow: 'hidden',
  },
  fallbackBanner: {
    position: 'absolute',
    left: 10,
    right: 10,
    top: 10,
    backgroundColor: 'rgba(15,23,42,0.92)',
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  fallbackText: {
    color: '#FFFFFF',
    fontSize: 12,
    fontWeight: '600',
    textAlign: 'center',
  },
});

