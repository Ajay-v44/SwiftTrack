// The dark style URL is inferred from Ola's documented style naming convention and
// commonly used official SDK examples.
const DEFAULT_OLA_MAPS_STYLE_URL =
  'https://api.olamaps.io/tiles/vector/v1/styles/default-dark-standard/style.json';

export const olaMapsConfig = {
  apiKey: process.env.EXPO_PUBLIC_OLA_MAPS_API_KEY || '',
  projectId: process.env.EXPO_PUBLIC_OLA_MAPS_PROJECT_ID || '',
  styleUrl: process.env.EXPO_PUBLIC_OLA_MAPS_STYLE_URL || DEFAULT_OLA_MAPS_STYLE_URL,
};
