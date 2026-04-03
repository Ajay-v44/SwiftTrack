import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator, KeyboardAvoidingView, Platform, ScrollView, Image } from 'react-native';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '../store/store';
import { login, getDriverDetails, register } from '../store/authSlice';
import { Mail, Phone, Lock, KeyRound } from 'lucide-react-native';
import * as Application from 'expo-application';
import * as Updates from 'expo-updates';

export default function LoginScreen() {
  const [loginMethod, setLoginMethod] = useState<'email' | 'mobile'>('mobile');
  const [isRegistering, setIsRegistering] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [mobileNumber, setMobileNumber] = useState('');
  const [otp, setOtp] = useState('');
  // Register fields
  const [name, setName] = useState('');

  const dispatch = useDispatch<AppDispatch>();
  const { loading, error } = useSelector((state: RootState) => state.auth);

  const handleLogin = async () => {
    try {
      const credentials = loginMethod === 'email'
        ? { email, password }
        : { mobileNumber, otp };

      await dispatch(login(credentials)).unwrap();
      await dispatch(getDriverDetails()).unwrap();
      // Navigation is handled automatically by RootNavigator listening to token changes
    } catch (err) {
      console.error('Login Error:', err);
    }
  };

  const handleRegister = async () => {
    try {
      await dispatch(register({
        name,
        email,
        mobileNumber,
        password
      })).unwrap();
      setIsRegistering(false);
      // Auto switch back to login with new credentials
    } catch (err) {
      console.error('Registration Error:', err);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <View style={styles.header}>
          <Image 
            source={require('../../assets/images/swifttrack_logo.png')} 
            style={styles.logo} 
            resizeMode="contain" 
          />
          <Text style={styles.title}>SwiftTrack Driver</Text>
          <Text style={styles.subtitle}>
            {isRegistering ? 'Create a new account' : 'Welcome back, please login'}
          </Text>
        </View>

        {error && (
          <View style={styles.errorContainer}>
            <Text style={styles.errorText}>{error}</Text>
          </View>
        )}

        <View style={styles.formContainer}>
          {!isRegistering && (
             <View style={styles.methodToggle}>
                <TouchableOpacity
                  style={[styles.toggleBtn, loginMethod === 'mobile' && styles.activeToggle]}
                  onPress={() => setLoginMethod('mobile')}
                >
                  <Text style={[styles.toggleText, loginMethod === 'mobile' && styles.activeToggleText]}>Mobile / OTP</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={[styles.toggleBtn, loginMethod === 'email' && styles.activeToggle]}
                  onPress={() => setLoginMethod('email')}
                >
                  <Text style={[styles.toggleText, loginMethod === 'email' && styles.activeToggleText]}>Email / Password</Text>
                </TouchableOpacity>
             </View>
          )}

          {isRegistering && (
             <View style={styles.inputContainer}>
              <User color="#6B7280" size={20} style={styles.icon} />
              <TextInput
                style={styles.input}
                placeholder="Full Name"
                value={name}
                onChangeText={setName}
              />
            </View>
          )}

          {(loginMethod === 'email' || isRegistering) && (
            <View style={styles.inputContainer}>
              <Mail color="#6B7280" size={20} style={styles.icon} />
              <TextInput
                style={styles.input}
                placeholder="Email Address"
                value={email}
                onChangeText={setEmail}
                keyboardType="email-address"
                autoCapitalize="none"
              />
            </View>
          )}

          {(loginMethod === 'mobile' || isRegistering) && (
            <View style={styles.inputContainer}>
              <Phone color="#6B7280" size={20} style={styles.icon} />
              <TextInput
                style={styles.input}
                placeholder="Mobile Number"
                value={mobileNumber}
                onChangeText={setMobileNumber}
                keyboardType="phone-pad"
              />
            </View>
          )}

          {(loginMethod === 'email' || isRegistering) && (
            <View style={styles.inputContainer}>
              <Lock color="#6B7280" size={20} style={styles.icon} />
              <TextInput
                style={styles.input}
                placeholder="Password"
                value={password}
                onChangeText={setPassword}
                secureTextEntry
              />
            </View>
          )}

          {loginMethod === 'mobile' && !isRegistering && (
            <View style={styles.inputContainer}>
              <KeyRound color="#6B7280" size={20} style={styles.icon} />
              <TextInput
                style={styles.input}
                placeholder="OTP"
                value={otp}
                onChangeText={setOtp}
                keyboardType="number-pad"
              />
            </View>
          )}

          <TouchableOpacity
            style={styles.button}
            onPress={isRegistering ? handleRegister : handleLogin}
            disabled={loading}
          >
            {loading ? (
              <ActivityIndicator color="#FFFFFF" />
            ) : (
              <Text style={styles.buttonText}>{isRegistering ? 'Register' : 'Login'}</Text>
            )}
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.switchModeBtn}
            onPress={() => setIsRegistering(!isRegistering)}
          >
             <Text style={styles.switchModeText}>
               {isRegistering ? 'Already have an account? Login' : "Don't have an account? Register"}
             </Text>
          </TouchableOpacity>
        </View>

        <Text style={styles.versionText}>
          v{Application.nativeApplicationVersion || '1.0.0'} {Updates.updateId ? `• OTA: ${Updates.updateId.substring(0, 8)}` : ''}
        </Text>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

// User Icon component not exported from lucide initially in some setups, fallback
const User = ({ color, size, style }: any) => (
  <View style={style}><Text style={{color, fontSize: size}}>👤</Text></View>
);

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F9FAFB', // Gray 50
  },
  scrollContent: {
    flexGrow: 1,
    justifyContent: 'center',
    padding: 24,
  },
  header: {
    alignItems: 'center',
    marginBottom: 48,
  },
  logo: {
    width: 100,
    height: 100,
    marginBottom: 16,
  },
  title: {
    fontSize: 28,
    fontWeight: '700',
    color: '#111827', // Gray 900
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: '#6B7280', // Gray 500
  },
  formContainer: {
    width: '100%',
  },
  methodToggle: {
    flexDirection: 'row',
    backgroundColor: '#E5E7EB', // Gray 200
    borderRadius: 12,
    padding: 4,
    marginBottom: 24,
  },
  toggleBtn: {
    flex: 1,
    paddingVertical: 10,
    alignItems: 'center',
    borderRadius: 8,
  },
  activeToggle: {
    backgroundColor: '#FFFFFF',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    elevation: 2,
  },
  toggleText: {
    fontWeight: '600',
    color: '#6B7280',
  },
  activeToggleText: {
    color: '#111827',
  },
  inputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#E5E7EB',
    paddingHorizontal: 16,
    height: 56,
    marginBottom: 16,
  },
  icon: {
    marginRight: 12,
  },
  input: {
    flex: 1,
    height: '100%',
    fontSize: 16,
    color: '#111827',
  },
  button: {
    backgroundColor: '#2563EB', // Blue 600
    borderRadius: 12,
    height: 56,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 8,
  },
  buttonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '600',
  },
  errorContainer: {
    backgroundColor: '#FEE2E2', // Red 100
    padding: 12,
    borderRadius: 8,
    marginBottom: 24,
  },
  errorText: {
    color: '#DC2626', // Red 600
    textAlign: 'center',
  },
  switchModeBtn: {
    marginTop: 20,
    alignItems: 'center',
  },
  switchModeText: {
    color: '#2563EB',
    fontWeight: '600'
  },
  versionText: {
    textAlign: 'center',
    color: '#9CA3AF',
    fontSize: 12,
    marginTop: 24,
  }
});
