/**
 * SwiftTrack Driver App — Vibrant Theme Palette
 * A bold, premium color system with gradients and rich accents
 */

export const Colors = {
  // Primary Gradient
  primary: '#6C63FF',        // Vivid Purple
  primaryDark: '#4F46E5',    // Deep Indigo
  primaryLight: '#A78BFA',   // Soft Lavender

  // Accent colors
  accent: '#FF6B6B',         // Coral Red
  accentOrange: '#FF9F43',   // Warm Amber
  accentTeal: '#00D2D3',     // Electric Teal
  accentPink: '#FF6B81',     // Hot Pink
  accentGreen: '#1DD1A1',    // Mint Green
  accentYellow: '#FECA57',   // Sunshine Yellow

  // Backgrounds
  bgDark: '#0F0F23',         // Rich Midnight
  bgCard: '#1A1A3E',         // Deep Navy Card
  bgCardLight: '#242452',    // Lighter Navy Card
  bgLight: '#F0F0FF',        // Soft Lavender Tint
  bgGlass: 'rgba(255,255,255,0.08)',  // Glassmorphism

  // Text
  textPrimary: '#FFFFFF',
  textSecondary: '#A0A0CC',
  textMuted: '#6B6B99',
  textDark: '#1A1A2E',
  textDarkSub: '#4A4A6A',

  // Status
  online: '#1DD1A1',
  offline: '#FF6B6B',
  warning: '#FECA57',
  info: '#54A0FF',

  // Gradients (for LinearGradient if available, otherwise use solid)
  gradientStart: '#6C63FF',
  gradientEnd: '#4F46E5',
  gradientAccentStart: '#FF6B6B',
  gradientAccentEnd: '#FF9F43',
  gradientTealStart: '#00D2D3',
  gradientTealEnd: '#1DD1A1',

  // Surfaces
  surface: '#FFFFFF',
  surfaceElevated: '#F8F8FF',
  border: 'rgba(108, 99, 255, 0.15)',
  borderLight: 'rgba(255,255,255,0.1)',

  // Shadows
  shadowPrimary: '#6C63FF',
  shadowDark: '#000000',
} as const;

export const Gradients = {
  primary: ['#6C63FF', '#4F46E5'] as const,
  accent: ['#FF6B6B', '#FF9F43'] as const,
  teal: ['#00D2D3', '#1DD1A1'] as const,
  dark: ['#1A1A3E', '#0F0F23'] as const,
  card: ['#242452', '#1A1A3E'] as const,
  sunrise: ['#FF6B81', '#FECA57'] as const,
} as const;
