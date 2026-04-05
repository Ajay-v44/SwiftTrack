import React from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity } from 'react-native';
import { ArrowLeft, Shield, Lock, Eye, Database, UserCheck } from 'lucide-react-native';
import { useNavigation } from '@react-navigation/native';
import { Colors } from '../theme/colors';

const sections = [
  {
    icon: <Database color={Colors.primaryLight} size={22} />,
    title: 'Data Collection',
    content: 'SwiftTrack collects your name, email, phone number, and real-time location data to provide delivery services. Location data is only collected while you are on duty (online status).',
  },
  {
    icon: <Lock color={Colors.accentTeal} size={22} />,
    title: 'Data Security',
    content: 'All data is encrypted in transit using TLS 1.3 and at rest using AES-256 encryption. We follow industry-standard security practices and conduct regular security audits.',
  },
  {
    icon: <Eye color={Colors.accentOrange} size={22} />,
    title: 'Data Usage',
    content: 'Your personal data is used solely for order assignment, delivery tracking, and payment processing. We do not sell your data to third parties or use it for advertising.',
  },
  {
    icon: <UserCheck color={Colors.accentGreen} size={22} />,
    title: 'Your Rights',
    content: 'You have the right to access, modify, or request deletion of your personal data at any time. Contact our support team to exercise these rights.',
  },
  {
    icon: <Shield color={Colors.accentPink} size={22} />,
    title: 'Third-Party Services',
    content: 'We use Google Maps for navigation, Firebase for notifications, and secure payment gateways. Each third-party service has its own privacy policy that governs their use of data.',
  },
];

export default function PrivacyPolicyScreen() {
  const navigation = useNavigation();

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity style={styles.backBtn} onPress={() => navigation.goBack()} activeOpacity={0.7}>
          <ArrowLeft color={Colors.textPrimary} size={22} />
        </TouchableOpacity>
        <Text style={styles.title}>Privacy Policy</Text>
      </View>

      <ScrollView style={styles.content} contentContainerStyle={{ paddingBottom: 40 }} showsVerticalScrollIndicator={false}>
        <Text style={styles.lastUpdated}>Last updated: April 2026</Text>

        <View style={styles.introCard}>
          <Shield color={Colors.primary} size={32} />
          <Text style={styles.introText}>
            Your privacy matters to us. This policy explains how SwiftTrack handles your personal information.
          </Text>
        </View>

        {sections.map((section, index) => (
          <View key={index} style={styles.sectionCard}>
            <View style={styles.sectionHeader}>
              <View style={styles.sectionIcon}>{section.icon}</View>
              <Text style={styles.sectionTitle}>{section.title}</Text>
            </View>
            <Text style={styles.sectionContent}>{section.content}</Text>
          </View>
        ))}

        <View style={styles.contactCard}>
          <Text style={styles.contactTitle}>Questions about your data?</Text>
          <Text style={styles.contactText}>Contact us at privacy@swifttrack.in</Text>
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.bgDark },
  header: {
    flexDirection: 'row', alignItems: 'center', paddingHorizontal: 20,
    paddingTop: 56, paddingBottom: 16, backgroundColor: Colors.bgCard,
    borderBottomLeftRadius: 24, borderBottomRightRadius: 24,
  },
  backBtn: { padding: 8, marginRight: 12, backgroundColor: Colors.bgGlass, borderRadius: 12 },
  title: { fontSize: 22, fontWeight: '700', color: Colors.textPrimary },
  content: { flex: 1, paddingHorizontal: 20, paddingTop: 16 },
  lastUpdated: { fontSize: 13, color: Colors.textMuted, marginBottom: 16 },
  introCard: {
    flexDirection: 'row', alignItems: 'center', backgroundColor: Colors.primary + '12',
    padding: 16, borderRadius: 16, marginBottom: 20, gap: 14,
    borderWidth: 1, borderColor: Colors.primary + '25',
  },
  introText: { flex: 1, fontSize: 14, color: Colors.textSecondary, lineHeight: 20 },
  sectionCard: {
    backgroundColor: Colors.bgCard, borderRadius: 16, padding: 16, marginBottom: 12,
    borderWidth: 1, borderColor: Colors.borderLight,
  },
  sectionHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: 10 },
  sectionIcon: {
    width: 38, height: 38, borderRadius: 10, backgroundColor: Colors.bgGlass,
    justifyContent: 'center', alignItems: 'center', marginRight: 12,
  },
  sectionTitle: { fontSize: 16, fontWeight: '700', color: Colors.textPrimary },
  sectionContent: { fontSize: 14, color: Colors.textSecondary, lineHeight: 22 },
  contactCard: {
    backgroundColor: Colors.accentTeal + '10', borderRadius: 16, padding: 20,
    alignItems: 'center', marginTop: 8, borderWidth: 1, borderColor: Colors.accentTeal + '25',
  },
  contactTitle: { fontSize: 15, fontWeight: '600', color: Colors.textPrimary, marginBottom: 4 },
  contactText: { fontSize: 14, color: Colors.accentTeal },
});
