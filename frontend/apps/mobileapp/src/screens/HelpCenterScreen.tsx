import React from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, Linking } from 'react-native';
import { ArrowLeft, MessageCircle, Phone, Mail, HelpCircle, BookOpen, ExternalLink } from 'lucide-react-native';
import { useNavigation } from '@react-navigation/native';
import * as Burnt from 'burnt';
import { Colors } from '../theme/colors';

const faqItems = [
  {
    question: 'How do I go online to receive orders?',
    answer: 'From the Home screen, toggle the Online switch at the top. Make sure location permissions are enabled.',
  },
  {
    question: 'Why am I not receiving new orders?',
    answer: 'Ensure you are Online, location services are enabled, and you have a stable internet connection. Check if your account verification is complete.',
  },
  {
    question: 'How do I update the order delivery status?',
    answer: 'Open the active order from Orders tab, then tap the "Update Status" button to progress through: Picked Up → In Transit → Out for Delivery → Delivered.',
  },
  {
    question: 'When do I receive my earnings?',
    answer: 'Earnings are credited to your wallet after successful delivery. Withdrawals can be initiated from the Wallet tab.',
  },
  {
    question: 'How do I update my vehicle details?',
    answer: 'Contact support to update vehicle information as it requires document verification.',
  },
];

export default function HelpCenterScreen() {
  const navigation = useNavigation();

  const handleContact = (method: string) => {
    switch (method) {
      case 'call':
        Linking.openURL('tel:+919876543210').catch(() => Burnt.toast({ title: 'Cannot make calls on this device', preset: 'error' }));
        break;
      case 'email':
        Linking.openURL('mailto:support@swifttrack.in').catch(() => Burnt.toast({ title: 'Cannot open email', preset: 'error' }));
        break;
      case 'chat':
        Burnt.toast({ title: 'Live chat coming soon!', preset: 'none' });
        break;
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity style={styles.backBtn} onPress={() => navigation.goBack()} activeOpacity={0.7}>
          <ArrowLeft color={Colors.textPrimary} size={22} />
        </TouchableOpacity>
        <Text style={styles.title}>Help Center</Text>
      </View>

      <ScrollView style={styles.content} contentContainerStyle={{ paddingBottom: 40 }} showsVerticalScrollIndicator={false}>
        {/* Contact Options */}
        <Text style={styles.sectionLabel}>Get in Touch</Text>
        <View style={styles.contactRow}>
          <TouchableOpacity style={styles.contactCard} onPress={() => handleContact('call')} activeOpacity={0.7}>
            <View style={[styles.contactIcon, { backgroundColor: Colors.accentGreen + '15' }]}>
              <Phone color={Colors.accentGreen} size={22} />
            </View>
            <Text style={styles.contactText}>Call Us</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.contactCard} onPress={() => handleContact('email')} activeOpacity={0.7}>
            <View style={[styles.contactIcon, { backgroundColor: Colors.primary + '15' }]}>
              <Mail color={Colors.primaryLight} size={22} />
            </View>
            <Text style={styles.contactText}>Email</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.contactCard} onPress={() => handleContact('chat')} activeOpacity={0.7}>
            <View style={[styles.contactIcon, { backgroundColor: Colors.accentTeal + '15' }]}>
              <MessageCircle color={Colors.accentTeal} size={22} />
            </View>
            <Text style={styles.contactText}>Chat</Text>
          </TouchableOpacity>
        </View>

        {/* FAQ */}
        <Text style={styles.sectionLabel}>Frequently Asked Questions</Text>
        {faqItems.map((item, index) => (
          <View key={index} style={styles.faqCard}>
            <View style={styles.faqHeader}>
              <HelpCircle color={Colors.accentOrange} size={18} />
              <Text style={styles.faqQuestion}>{item.question}</Text>
            </View>
            <Text style={styles.faqAnswer}>{item.answer}</Text>
          </View>
        ))}
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
  sectionLabel: { fontSize: 16, fontWeight: '700', color: Colors.textPrimary, marginBottom: 12, marginTop: 8 },
  contactRow: { flexDirection: 'row', gap: 10, marginBottom: 24 },
  contactCard: {
    flex: 1, backgroundColor: Colors.bgCard, padding: 16, borderRadius: 16,
    alignItems: 'center', borderWidth: 1, borderColor: Colors.borderLight,
  },
  contactIcon: { width: 48, height: 48, borderRadius: 14, justifyContent: 'center', alignItems: 'center', marginBottom: 8 },
  contactText: { fontSize: 13, fontWeight: '600', color: Colors.textPrimary },
  faqCard: {
    backgroundColor: Colors.bgCard, borderRadius: 14, padding: 16, marginBottom: 10,
    borderWidth: 1, borderColor: Colors.borderLight,
  },
  faqHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: 8, gap: 8 },
  faqQuestion: { fontSize: 15, fontWeight: '600', color: Colors.textPrimary, flex: 1 },
  faqAnswer: { fontSize: 14, color: Colors.textSecondary, lineHeight: 21, paddingLeft: 26 },
});
