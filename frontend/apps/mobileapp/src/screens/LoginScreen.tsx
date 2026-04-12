import React, { useState, useRef } from 'react';
import {
  View, Text, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator,
  KeyboardAvoidingView, Platform, ScrollView, Image, Keyboard,
  TouchableWithoutFeedback, Animated, Dimensions,
} from 'react-native';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '../store/store';
import { login, getDriverDetails, register, clearError } from '../store/authSlice';
import { Mail, Phone, Lock, KeyRound, Eye, EyeOff, User, Truck } from 'lucide-react-native';
import * as Application from 'expo-application';
import * as Burnt from 'burnt';
import { Colors } from '../theme/colors';

const { width } = Dimensions.get('window');
const VEHICLE_TYPES = ['BIKE', 'CAR', 'VAN', 'TRUCK'] as const;

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const MOBILE_REGEX = /^[0-9]{10,15}$/;
const VEHICLE_NUMBER_REGEX = /^[A-Z0-9 -]{6,15}$/;
const LICENSE_REGEX = /^[A-Z0-9-]{6,20}$/;

export default function LoginScreen() {
  const [loginMethod, setLoginMethod] = useState<'email' | 'mobile'>('mobile');
  const [isRegistering, setIsRegistering] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [mobileNumber, setMobileNumber] = useState('');
  const [otp, setOtp] = useState('');
  const [name, setName] = useState('');
  const [vehicleType, setVehicleType] = useState<(typeof VEHICLE_TYPES)[number] | null>(null);
  const [vehicleNumber, setVehicleNumber] = useState('');
  const [driverLicenseNumber, setDriverLicenseNumber] = useState('');

  const emailRef = useRef<TextInput>(null);
  const passwordRef = useRef<TextInput>(null);
  const mobileRef = useRef<TextInput>(null);
  const otpRef = useRef<TextInput>(null);
  const nameRef = useRef<TextInput>(null);
  const vehicleNumberRef = useRef<TextInput>(null);
  const driverLicenseRef = useRef<TextInput>(null);
  const scrollRef = useRef<ScrollView>(null);

  const dispatch = useDispatch<AppDispatch>();
  const { loading } = useSelector((state: RootState) => state.auth);

  const handleLogin = async () => {
    Keyboard.dismiss();
    dispatch(clearError());

    if (loginMethod === 'email') {
      if (!email.trim()) { Burnt.toast({ title: 'Please enter your email', preset: 'error' }); return; }
      if (!password) { Burnt.toast({ title: 'Please enter your password', preset: 'error' }); return; }
    } else {
      if (!mobileNumber.trim()) { Burnt.toast({ title: 'Please enter your mobile number', preset: 'error' }); return; }
      if (!otp.trim()) { Burnt.toast({ title: 'Please enter the OTP', preset: 'error' }); return; }
    }

    try {
      const credentials = loginMethod === 'email'
        ? { email: email.trim(), password }
        : { mobileNumber: mobileNumber.trim(), otp: otp.trim() };

      await dispatch(login(credentials)).unwrap();
      await dispatch(getDriverDetails()).unwrap();
      Burnt.toast({ title: 'Welcome back! 🎉', preset: 'done' });
    } catch (err: any) {
      Burnt.toast({ title: typeof err === 'string' ? err : 'Login failed', preset: 'error' });
    }
  };

  const handleRegister = async () => {
    Keyboard.dismiss();
    dispatch(clearError());

    const trimmedName = name.trim();
    const trimmedEmail = email.trim().toLowerCase();
    const trimmedMobile = mobileNumber.trim();
    const normalizedVehicleNumber = vehicleNumber.trim().toUpperCase();
    const normalizedLicenseNumber = driverLicenseNumber.trim().toUpperCase();

    if (!trimmedName || trimmedName.length < 3) { Burnt.toast({ title: 'Enter a valid full name', preset: 'error' }); return; }
    if (!trimmedEmail) { Burnt.toast({ title: 'Please enter your email', preset: 'error' }); return; }
    if (!EMAIL_REGEX.test(trimmedEmail)) { Burnt.toast({ title: 'Enter a valid email address', preset: 'error' }); return; }
    if (!trimmedMobile) { Burnt.toast({ title: 'Please enter mobile number', preset: 'error' }); return; }
    if (!MOBILE_REGEX.test(trimmedMobile)) { Burnt.toast({ title: 'Enter a valid mobile number', preset: 'error' }); return; }
    if (!password || password.length < 6) { Burnt.toast({ title: 'Password must be at least 6 chars', preset: 'error' }); return; }
    if (!vehicleType) { Burnt.toast({ title: 'Please select vehicle type', preset: 'error' }); return; }
    if (!normalizedVehicleNumber) { Burnt.toast({ title: 'Please enter vehicle number', preset: 'error' }); return; }
    if (!VEHICLE_NUMBER_REGEX.test(normalizedVehicleNumber)) { Burnt.toast({ title: 'Enter a valid vehicle number', preset: 'error' }); return; }
    if (!normalizedLicenseNumber) { Burnt.toast({ title: 'Please enter driving license number', preset: 'error' }); return; }
    if (!LICENSE_REGEX.test(normalizedLicenseNumber)) { Burnt.toast({ title: 'Enter a valid driving license number', preset: 'error' }); return; }

    try {
      await dispatch(register({
        name: trimmedName,
        email: trimmedEmail,
        mobileNumber: trimmedMobile,
        password,
        vehicleType,
        vehicleNumber: normalizedVehicleNumber,
        driverLicensNumber: normalizedLicenseNumber,
      })).unwrap();
      Burnt.toast({ title: 'Account created. Please wait for verification by admin, it may take some time.', preset: 'done' });
      setIsRegistering(false);
      setVehicleType(null);
      setVehicleNumber('');
      setDriverLicenseNumber('');
    } catch (err: any) {
      Burnt.toast({ title: typeof err === 'string' ? err : 'Registration failed', preset: 'error' });
    }
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
          {/* Decorative top circles */}
          <View style={styles.decorCircle1} />
          <View style={styles.decorCircle2} />
          <View style={styles.decorCircle3} />

          <View style={styles.header}>
            <View style={styles.logoContainer}>
              <Truck color="#FFFFFF" size={36} />
            </View>
            <Text style={styles.title}>SwiftTrack</Text>
            <Text style={styles.subtitle}>
              {isRegistering ? 'Create your driver account' : 'Welcome back, Captain! 🚀'}
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
                  <Phone color={loginMethod === 'mobile' ? '#FFFFFF' : Colors.textMuted} size={16} />
                  <Text style={[styles.toggleText, loginMethod === 'mobile' && styles.activeToggleText]}>Mobile</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={[styles.toggleBtn, loginMethod === 'email' && styles.activeToggle]}
                  onPress={() => { setLoginMethod('email'); dispatch(clearError()); }}
                  activeOpacity={0.7}
                >
                  <Mail color={loginMethod === 'email' ? '#FFFFFF' : Colors.textMuted} size={16} />
                  <Text style={[styles.toggleText, loginMethod === 'email' && styles.activeToggleText]}>Email</Text>
                </TouchableOpacity>
              </View>
            )}

            {isRegistering && (
              <View style={styles.inputContainer}>
                <View style={styles.inputIcon}><User color={Colors.primaryLight} size={20} /></View>
                <TextInput
                  ref={nameRef} style={styles.input} placeholder="Full Name"
                  placeholderTextColor={Colors.textMuted} value={name} onChangeText={setName}
                  autoCapitalize="words" returnKeyType="next"
                  onSubmitEditing={() => emailRef.current?.focus()} blurOnSubmit={false}
                />
              </View>
            )}

            {(loginMethod === 'email' || isRegistering) && (
              <View style={styles.inputContainer}>
                <View style={styles.inputIcon}><Mail color={Colors.primaryLight} size={20} /></View>
                <TextInput
                  ref={emailRef} style={styles.input} placeholder="Email Address"
                  placeholderTextColor={Colors.textMuted} value={email} onChangeText={setEmail}
                  keyboardType="email-address" autoCapitalize="none" autoCorrect={false}
                  autoComplete="email" textContentType="emailAddress" returnKeyType="next"
                  onSubmitEditing={() => isRegistering ? mobileRef.current?.focus() : passwordRef.current?.focus()}
                  blurOnSubmit={false}
                />
              </View>
            )}

            {(loginMethod === 'mobile' || isRegistering) && (
              <View style={styles.inputContainer}>
                <View style={styles.inputIcon}><Phone color={Colors.accentTeal} size={20} /></View>
                <TextInput
                  ref={mobileRef} style={styles.input} placeholder="Mobile Number"
                  placeholderTextColor={Colors.textMuted} value={mobileNumber} onChangeText={setMobileNumber}
                  keyboardType="phone-pad" autoComplete="tel" textContentType="telephoneNumber"
                  returnKeyType={isRegistering ? 'next' : 'done'}
                  onSubmitEditing={() => isRegistering ? vehicleNumberRef.current?.focus() : otpRef.current?.focus()}
                  blurOnSubmit={false}
                />
              </View>
            )}

            {isRegistering && (
              <>
                <View style={styles.registerSection}>
                  <Text style={styles.sectionLabel}>Vehicle Type</Text>
                  <View style={styles.vehicleTypeGrid}>
                    {VEHICLE_TYPES.map((type) => (
                      <TouchableOpacity
                        key={type}
                        style={[styles.vehicleTypeChip, vehicleType === type && styles.vehicleTypeChipActive]}
                        onPress={() => setVehicleType(type)}
                        activeOpacity={0.8}
                      >
                        <Text style={[styles.vehicleTypeText, vehicleType === type && styles.vehicleTypeTextActive]}>
                          {type}
                        </Text>
                      </TouchableOpacity>
                    ))}
                  </View>
                </View>

                <View style={styles.inputContainer}>
                  <View style={styles.inputIcon}><Truck color={Colors.accentGreen} size={20} /></View>
                  <TextInput
                    ref={vehicleNumberRef}
                    style={styles.input}
                    placeholder="Vehicle Number"
                    placeholderTextColor={Colors.textMuted}
                    value={vehicleNumber}
                    onChangeText={setVehicleNumber}
                    autoCapitalize="characters"
                    autoCorrect={false}
                    returnKeyType="next"
                    onSubmitEditing={() => driverLicenseRef.current?.focus()}
                    blurOnSubmit={false}
                  />
                </View>

                <View style={styles.inputContainer}>
                  <View style={styles.inputIcon}><KeyRound color={Colors.accentYellow} size={20} /></View>
                  <TextInput
                    ref={driverLicenseRef}
                    style={styles.input}
                    placeholder="Driving License Number"
                    placeholderTextColor={Colors.textMuted}
                    value={driverLicenseNumber}
                    onChangeText={setDriverLicenseNumber}
                    autoCapitalize="characters"
                    autoCorrect={false}
                    returnKeyType="next"
                    onSubmitEditing={() => passwordRef.current?.focus()}
                    blurOnSubmit={false}
                  />
                </View>
              </>
            )}

            {(loginMethod === 'email' || isRegistering) && (
              <View style={styles.inputContainer}>
                <View style={styles.inputIcon}><Lock color={Colors.accentOrange} size={20} /></View>
                <TextInput
                  ref={passwordRef} style={[styles.input, { paddingRight: 48 }]} placeholder="Password"
                  placeholderTextColor={Colors.textMuted} value={password} onChangeText={setPassword}
                  secureTextEntry={!showPassword} autoCapitalize="none" autoCorrect={false}
                  autoComplete="password" textContentType="password" returnKeyType="go"
                  onSubmitEditing={isRegistering ? handleRegister : handleLogin}
                  onFocus={() => setTimeout(() => scrollRef.current?.scrollToEnd({ animated: true }), 300)}
                />
                <TouchableOpacity
                  style={styles.eyeButton} onPress={() => setShowPassword(!showPassword)}
                  hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }} activeOpacity={0.6}
                >
                  {showPassword ? <EyeOff color={Colors.textMuted} size={20} /> : <Eye color={Colors.textMuted} size={20} />}
                </TouchableOpacity>
              </View>
            )}

            {loginMethod === 'mobile' && !isRegistering && (
              <View style={styles.inputContainer}>
                <View style={styles.inputIcon}><KeyRound color={Colors.accentPink} size={20} /></View>
                <TextInput
                  ref={otpRef} style={styles.input} placeholder="Enter OTP"
                  placeholderTextColor={Colors.textMuted} value={otp} onChangeText={setOtp}
                  keyboardType="number-pad" autoComplete="sms-otp" textContentType="oneTimeCode"
                  maxLength={6} returnKeyType="go" onSubmitEditing={handleLogin}
                  onFocus={() => setTimeout(() => scrollRef.current?.scrollToEnd({ animated: true }), 300)}
                />
              </View>
            )}

            <TouchableOpacity
              style={[styles.button, loading && styles.buttonDisabled]}
              onPress={isRegistering ? handleRegister : handleLogin}
              disabled={loading} activeOpacity={0.8}
            >
              {loading ? (
                <ActivityIndicator color="#FFFFFF" />
              ) : (
                <Text style={styles.buttonText}>{isRegistering ? 'Create Account ✨' : 'Let\'s Go! 🚀'}</Text>
              )}
            </TouchableOpacity>

            <TouchableOpacity style={styles.switchModeBtn} onPress={() => { dispatch(clearError()); setIsRegistering(!isRegistering); }} activeOpacity={0.7}>
              <Text style={styles.switchModeText}>
                {isRegistering ? 'Already have an account? Login' : "Don't have an account? Register"}
              </Text>
            </TouchableOpacity>
          </View>

          <Text style={styles.versionText}>v{Application.nativeApplicationVersion || '1.0.0'}</Text>
        </ScrollView>
      </KeyboardAvoidingView>
    </TouchableWithoutFeedback>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.bgDark },
  scrollContent: { flexGrow: 1, justifyContent: 'center', padding: 24, paddingBottom: 40 },
  decorCircle1: {
    position: 'absolute', top: -80, right: -60, width: 200, height: 200,
    borderRadius: 100, backgroundColor: Colors.primary, opacity: 0.15,
  },
  decorCircle2: {
    position: 'absolute', top: 60, left: -40, width: 120, height: 120,
    borderRadius: 60, backgroundColor: Colors.accentTeal, opacity: 0.1,
  },
  decorCircle3: {
    position: 'absolute', bottom: 100, right: -30, width: 150, height: 150,
    borderRadius: 75, backgroundColor: Colors.accentOrange, opacity: 0.08,
  },
  header: { alignItems: 'center', marginBottom: 40 },
  logoContainer: {
    width: 72, height: 72, borderRadius: 20, backgroundColor: Colors.primary,
    justifyContent: 'center', alignItems: 'center', marginBottom: 16,
    shadowColor: Colors.primary, shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.4, shadowRadius: 16, elevation: 12,
  },
  title: { fontSize: 34, fontWeight: '800', color: Colors.textPrimary, marginBottom: 8, letterSpacing: -0.5 },
  subtitle: { fontSize: 16, color: Colors.textSecondary, textAlign: 'center' },
  formContainer: { width: '100%' },
  registerSection: { marginBottom: 14 },
  sectionLabel: { color: Colors.textSecondary, fontSize: 13, fontWeight: '700', marginBottom: 10, paddingHorizontal: 4 },
  vehicleTypeGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
  vehicleTypeChip: {
    minWidth: '47%',
    backgroundColor: Colors.bgCard,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: Colors.borderLight,
    paddingVertical: 14,
    alignItems: 'center',
  },
  vehicleTypeChipActive: {
    backgroundColor: Colors.primary,
    borderColor: Colors.primaryLight,
    shadowColor: Colors.primary,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.25,
    shadowRadius: 10,
    elevation: 4,
  },
  vehicleTypeText: { color: Colors.textSecondary, fontSize: 14, fontWeight: '700' },
  vehicleTypeTextActive: { color: Colors.textPrimary },
  methodToggle: {
    flexDirection: 'row', backgroundColor: Colors.bgCard, borderRadius: 16, padding: 4, marginBottom: 24,
    borderWidth: 1, borderColor: Colors.borderLight,
  },
  toggleBtn: {
    flex: 1, flexDirection: 'row', paddingVertical: 12, alignItems: 'center',
    justifyContent: 'center', borderRadius: 12, gap: 6,
  },
  activeToggle: {
    backgroundColor: Colors.primary,
    shadowColor: Colors.primary, shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3, shadowRadius: 8, elevation: 4,
  },
  toggleText: { fontWeight: '600', color: Colors.textMuted, fontSize: 14 },
  activeToggleText: { color: '#FFFFFF' },
  inputContainer: {
    flexDirection: 'row', alignItems: 'center', backgroundColor: Colors.bgCard,
    borderRadius: 16, borderWidth: 1, borderColor: Colors.borderLight,
    paddingHorizontal: 4, height: 56, marginBottom: 14,
  },
  inputIcon: {
    width: 44, height: 44, borderRadius: 12, backgroundColor: Colors.bgGlass,
    justifyContent: 'center', alignItems: 'center', marginRight: 4,
  },
  input: { flex: 1, height: '100%', fontSize: 16, color: Colors.textPrimary },
  eyeButton: {
    position: 'absolute', right: 16, height: '100%', justifyContent: 'center', paddingLeft: 8,
  },
  button: {
    backgroundColor: Colors.primary, borderRadius: 16, height: 56,
    justifyContent: 'center', alignItems: 'center', marginTop: 8,
    shadowColor: Colors.primary, shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.4, shadowRadius: 16, elevation: 8,
  },
  buttonDisabled: { backgroundColor: Colors.bgCardLight, shadowOpacity: 0, elevation: 0 },
  buttonText: { color: '#FFFFFF', fontSize: 17, fontWeight: '700' },
  switchModeBtn: { marginTop: 20, alignItems: 'center', paddingVertical: 8 },
  switchModeText: { color: Colors.primaryLight, fontWeight: '600', fontSize: 14 },
  versionText: { textAlign: 'center', color: Colors.textMuted, fontSize: 12, marginTop: 24 },
});
