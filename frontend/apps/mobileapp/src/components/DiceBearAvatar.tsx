import React, { useMemo } from 'react';
import { View, StyleSheet, ViewStyle } from 'react-native';
import { createAvatar } from '@dicebear/core';
import { toonHead } from '@dicebear/collection';
import { SvgXml } from 'react-native-svg';

/**
 * DiceBear Avatar component for React Native
 * Style: toon-head (human cartoon avatar)
 * Version: 9.x
 *
 * Performance Tip:
 * - randomizeIds: true ensures unique IDs for gradients/masks, avoiding collisions on screens with multiple icons.
 * - This requires 'react-native-get-random-values' polyfill (added to index.js).
 */

interface DiceBearAvatarProps {
  seed: string;
  size?: number;
  radius?: number;
  backgroundColor?: string;
  style?: ViewStyle;
}

export default function DiceBearAvatar({
  seed,
  size = 64,
  radius = 16,
  backgroundColor,
  style,
}: DiceBearAvatarProps) {
  const svgXml = useMemo(() => {
    try {
      const avatar = createAvatar(toonHead, {
        seed: seed || 'driver',
        size,
        radius: Math.min(50, Math.round((radius / (size || 64)) * 100)),
        backgroundColor: backgroundColor ? [backgroundColor] : undefined,
        randomizeIds: true,
      });

      return avatar
        .toString()
        .replace(/<metadata[\s\S]*?<\/metadata>/, '')
        .trim();
    } catch (error) {
      console.warn('DiceBear Avatar generation failed:', error);
      return '';
    }
  }, [seed, size, radius, backgroundColor]);

  if (!svgXml) {
    return (
      <View
        style={[
          styles.container,
          { width: size, height: size, borderRadius: radius, backgroundColor: '#212145' },
          style,
        ]}
      />
    );
  }

  return (
    <View style={[
      styles.container,
      { width: size, height: size, borderRadius: radius, backgroundColor: '#212145' },
      style,
    ]}>
      <SvgXml xml={svgXml} width={size} height={size} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    overflow: 'hidden',
    justifyContent: 'center',
    alignItems: 'center',
  },
});
