import React from 'react';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { Provider } from 'react-redux';
import { NavigationContainer } from '@react-navigation/native';
import { store } from '../store/store';
import RootNavigator from '../navigation/RootNavigator';
import { StatusBar } from 'expo-status-bar';

export const App = () => {
  return (
    <Provider store={store}>
      <SafeAreaProvider>
        <NavigationContainer
          theme={{
            dark: true,
            colors: {
              primary: '#6C63FF',
              background: '#0F0F23',
              card: '#1A1A3E',
              text: '#F0F0FF',
              border: '#2A2A5C',
              notification: '#FF6B6B',
            },
            fonts: {
              regular: { fontFamily: 'System', fontWeight: '400' as const },
              medium: { fontFamily: 'System', fontWeight: '500' as const },
              bold: { fontFamily: 'System', fontWeight: '700' as const },
              heavy: { fontFamily: 'System', fontWeight: '800' as const },
            },
          }}
        >
          <StatusBar style="light" />
          <RootNavigator />
        </NavigationContainer>
      </SafeAreaProvider>
    </Provider>
  );
};

export default App;
