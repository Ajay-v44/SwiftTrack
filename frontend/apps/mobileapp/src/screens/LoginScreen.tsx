import React, { useState, useRef } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  Image,
  Keyboard,
  TouchableWithoutFeedback,
} from 'react-native';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '../store/store';
import { login, getDriverDetails, register, clearError } from '../store/authSlice';
import { Mail, Phone, Lock, KeyRound, Eye, EyeOff, User } from 'lucide-react-native';
import * as Application from 'expo-application';
import * as Updates from 'expo-updates';
import * as Burnt from 'burnt';

export default function LoginScreen() {
  const [loginMethod, setLoginMethod] = useState<'email' | 'mobile'>('mobile');
  const [isRegistering, setIsRegistering] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [mobileNumber, setMobileNumber] = useState('');
  const [otp, setOtp] = useState('');
  const [name, setName] = useState('');

  // Input refs for focus management
  const emailRef = useRef<TextInput>(null);
  const passwordRef = useRef<TextInput>(null);
  const mobileRef = useRef<TextInput>(null);
  const otpRef = useRef<TextInput>(null);
  const nameRef = useRef<TextInput>(null);
  const scrollRef = useRef<ScrollView>(null);

  const dispatch = useDispatch<AppDispatch>();
  const { loading, error } = useSelector((state: RootState) => state.auth);

  const handleLogin = async () => {
    Keyboard.dismiss();
    dispatch(clearError());

    // Validation
    if (loginMethod === 'email') {
      if (!email.trim()) {
        Burnt.toast({ title: 'Please enter your email address', preset: 'error' });
        return;
      }
      if (!password) {
        Burnt.toast({ title: 'Please enter your password', preset: 'error' });
        return;
      }
    } else {
      if (!mobileNumber.trim()) {
        Burnt.toast({ title: 'Please enter your mobile number', preset: 'error' });
        return;
      }
      if (!otp.trim()) {
        Burnt.toast({ title: 'Please enter the OTP', preset: 'error' });
        return;
      }
    }

    try {
      const credentials = loginMethod === 'email'
        ? { email: email.trim(), password }
        : { mobileNumber: mobileNumber.trim(), otp: otp.trim() };

      await dispatch(login(credentials)).unwrap();
      Burnt.toast({ title: 'Login successful!', preset: 'done' });
      // Fetch driver details after successful login
      dispatch(getDriverDetails());
    } catch (err: any) {
      Burnt.toast({
        title: typeof err === 'string' ? err : 'Login failed. Please check your credentials.',
        preset: 'error',
      });
    }
  };

  const handleRegister = async () => {
    Keyboard.dismiss();
    dispatch(clearError());

    // Validation
    if (!name.trim()) {
      Burnt.toast({ title: 'Please enter your full name', preset: 'error' });
      return;
    }
    if (!email.trim()) {
      Burnt.toast({ title: 'Please enter your email address', preset: 'error' });
      return;
    }
    if (!mobileNumber.trim()) {
      Burnt.toast({ title: 'Please enter your mobile number', preset: 'error' });
      return;
    }
    if (!password) {
      Burnt.toast({ title: 'Please enter a password', preset: 'error' });
      return;
    }
    if (password.length < 6) {
      Burnt.toast({ title: 'Password must be at least 6 characters', preset: 'error' });
      return;
    }

    try {
      await dispatch(register({
        name: name.trim(),
        email: email.trim(),
        mobileNumber: mobileNumber.trim(),
        password,
      })).unwrap();
      Burnt.toast({ title: 'Registration successful! Please login.', preset: 'done' });
      setIsRegistering(false);
    } catch (err: any) {
      Burnt.toast({
        title: typeof err === 'string' ? err : 'Registration failed. Please try again.',
        preset: 'error',
      });
    }
  };

  const switchMode = () => {
    dispatch(clearError());
    setIsRegistering(!isRegistering);
  };

  return (
    <TouchableWithoutFeedback onPress={Keyboard.dismiss}>
      <KeyboardAvoidingView
        style={styles.container}
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        keyboardVerticalOffset={Platform.OS === 'ios' ? 0 : 20}
      >
        <ScrollView
          ref={scrollRef}
          contentContainerStyle={styles.scrollContent}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}
          bounces={false}
        >
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

          <View style={styles.formContainer}>
            {!isRegistering && (
              <View style={styles.methodToggle}>
                <TouchableOpacity
                  style={[styles.toggleBtn, loginMethod === 'mobile' && styles.activeToggle]}
                  onPress={() => { setLoginMethod('mobile'); dispatch(clearError()); }}
                  activeOpacity={0.7}
                >
                  <Text style={[styles.toggleText, loginMethod === 'mobile' && styles.activeToggleText]}>Mobile / OTP</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={[styles.toggleBtn, loginMethod === 'email' && styles.activeToggle]}
                  onPress={() => { setLoginMethod('email'); dispatch(clearError()); }}
                  activeOpacity={0.7}
                >
                  <Text style={[styles.toggleText, loginMethod === 'email' && styles.activeToggleText]}>Email / Password</Text>
                </TouchableOpacity>
              </View>
            )}

            {/* Registration: Name field */}
            {isRegistering && (
              <View style={styles.inputContainer}>
                <User color="#6B7280" size={20} style={styles.icon} />
                <TextInput
                  ref={nameRef}
                  style={styles.input}
                  placeholder="Full Name"
                  placeholderTextColor="#9CA3AF"
                  value={name}
                  onChangeText={setName}
                  autoCapitalize="words"
                  returnKeyType="next"
                  onSubmitEditing={() => emailRef.current?.focus()}
                  blurOnSubmit={false}
                />
              </View>
            )}

            {/* Email field */}
            {(loginMethod === 'email' || isRegistering) && (
              <View style={styles.inputContainer}>
                <Mail color="#6B7280" size={20} style={styles.icon} />
                <TextInput
                  ref={emailRef}
                  style={styles.input}
                  placeholder="Email Address"
                  placeholderTextColor="#9CA3AF"
                  value={email}
                  onChangeText={setEmail}
                  keyboardType="email-address"
                  autoCapitalize="none"
                  autoCorrect={false}
                  autoComplete="email"
                  textContentType="emailAddress"
                  returnKeyType="next"
                  onSubmitEditing={() => {
                    if (isRegistering) {
                      mobileRef.current?.focus();
                    } else {
                      passwordRef.current?.focus();
                    }
                  }}
                  blurOnSubmit={false}
                />
              </View>
            )}

            {/* Mobile Number field */}
            {(loginMethod === 'mobile' || isRegistering) && (
              <View style={styles.inputContainer}>
                <Phone color="#6B7280" size={20} style={styles.icon} />
                <TextInput
                  ref={mobileRef}
                  style={styles.input}
                  placeholder="Mobile Number (e.g. +91 9876543210)"
                  placeholderTextColor="#9CA3AF"
                  value={mobileNumber}
                  onChangeText={setMobileNumber}
                  keyboardType="phone-pad"
                  autoComplete="tel"
                  textContentType="telephoneNumber"
                  returnKeyType={isRegistering ? 'next' : 'done'}
                  onSubmitEditing={() => {
                    if (isRegistering) {
                      passwordRef.current?.focus();
                    } else {
                      otpRef.current?.focus();
                    }
                  }}
                  blurOnSubmit={false}
                />
              </View>
            )}

            {/* Password field */}
            {(loginMethod === 'email' || isRegistering) && (
              <View style={styles.inputContainer}>
                <Lock color="#6B7280" size={20} style={styles.icon} />
                <TextInput
                  ref={passwordRef}
                  style={[styles.input, { paddingRight: 44 }]}
                  placeholder="Password"
                  placeholderTextColor="#9CA3AF"
                  value={password}
                  onChangeText={setPassword}
                  secureTextEntry={!showPassword}
                  autoCapitalize="none"
                  autoCorrect={false}
                  autoComplete="password"
                  textContentType="password"
                  returnKeyType="go"
                  onSubmitEditing={isRegistering ? handleRegister : handleLogin}
                  onFocus={() => {
                    // Scroll to ensure password field is visible above keyboard
                    setTimeout(() => scrollRef.current?.scrollToEnd({ animated: true }), 300);
                  }}
                />
                <TouchableOpacity
                  style={styles.eyeButton}
                  onPress={() => setShowPassword(!showPassword)}
                  hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
                  activeOpacity={0.6}
                >
                  {showPassword ? (
                    <EyeOff color="#6B7280" size={20} />
                  ) : (
                    <Eye color="#6B7280" size={20} />
                  )}
                </TouchableOpacity>
              </View>
            )}

            {/* OTP field */}
            {loginMethod === 'mobile' && !isRegistering && (
              <View style={styles.inputContainer}>
                <KeyRound color="#6B7280" size={20} style={styles.icon} />
                <TextInput
                  ref={otpRef}
                  style={styles.input}
                  placeholder="Enter OTP"
                  placeholderTextColor="#9CA3AF"
                  value={otp}
                  onChangeText={setOtp}
                  keyboardType="number-pad"
                  autoComplete="sms-otp"
                  textContentType="oneTimeCode"
                  maxLength={6}
                  returnKeyType="go"
                  onSubmitEditing={handleLogin}
                  onFocus={() => {
                    setTimeout(() => scrollRef.current?.scrollToEnd({ animated: true }), 300);
                  }}
                />
              </View>
            )}

            <TouchableOpacity
              style={[styles.button, loading && styles.buttonDisabled]}
              onPress={isRegistering ? handleRegister : handleLogin}
              disabled={loading}
              activeOpacity={0.8}
            >
              {loading ? (
                <ActivityIndicator color="#FFFFFF" />
              ) : (
                <Text style={styles.buttonText}>{isRegistering ? 'Create Account' : 'Login'}</Text>
              )}
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.switchModeBtn}
              onPress={switchMode}
              activeOpacity={0.7}
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
    </TouchableWithoutFeedback>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F9FAFB',
  },
  scrollContent: {
    flexGrow: 1,
    justifyContent: 'center',
    padding: 24,
    paddingBottom: 40,
  },
  header: {
    alignItems: 'center',
    marginBottom: 40,
  },
  logo: {
    width: 100,
    height: 100,
    marginBottom: 16,
  },
  title: {
    fontSize: 28,
    fontWeight: '700',
    color: '#111827',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: '#6B7280',
    textAlign: 'center',
  },
  formContainer: {
    width: '100%',
  },
  methodToggle: {
    flexDirection: 'row',
    backgroundColor: '#E5E7EB',
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
    fontSize: 14,
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
  eyeButton: {
    position: 'absolute',
    right: 16,
    height: '100%',
    justifyContent: 'center',
    paddingLeft: 8,
  },
  button: {
    backgroundColor: '#2563EB',
    borderRadius: 12,
    height: 56,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 8,
    shadowColor: '#2563EB',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.2,
    shadowRadius: 8,
    elevation: 4,
  },
  buttonDisabled: {
    backgroundColor: '#93C5FD',
    shadowOpacity: 0,
    elevation: 0,
  },
  buttonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '700',
  },
  switchModeBtn: {
    marginTop: 20,
    alignItems: 'center',
    paddingVertical: 8,
  },
  switchModeText: {
    color: '#2563EB',
    fontWeight: '600',
    fontSize: 14,
  },
  versionText: {
    textAlign: 'center',
    color: '#9CA3AF',
    fontSize: 12,
    marginTop: 24,
  },
});
