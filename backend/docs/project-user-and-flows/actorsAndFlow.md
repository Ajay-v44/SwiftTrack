# **SwiftTrack – Actors & Responsibilities Overview**

This document outlines all actors in the SwiftTrack ecosystem and their responsibilities. It provides a clear understanding of user roles, system behavior, and how each entity interacts with the platform. This acts as the foundation for feature design, access control (RBAC), and API requirements.

---

# ## **1. Tenants (Businesses Using SwiftTrack)**

Tenants are companies (e-commerce, hyperlocal, D2C, retail, grocery, pharmacy, etc.) integrating SwiftTrack to manage delivery operations.

### **Key Responsibilities:**

* **Register Business**: Onboard the company and set up account details.
* **Select Third-party Services**: Choose logistics partners like Dunzo, Porter, Rapido.
* **Request New Provider Integrations**: Submit documentation if a provider is not available.
* **Configure Customer Notification Channels**:

    * SMS
    * WhatsApp
    * Email
    * Firebase FCM (mobile apps)
* **Onboard Tenant Drivers**:

    * Add internal fleet drivers
    * Manage driver permissions
* **Manage Wallet Balance**:

    * Top-up wallet
    * Auto-pay for delivery costs
* **Use Agentic Dashboard**:

    * Voice or text-based operations
    * System insights and analytics
* **Interact With AI Chatbot**:

    * Delivery insights
    * Cost breakdowns
    * Recommendations
* **Create Tenant Users With Roles**:

    * Admin
    * Dispatcher
    * Support
    * Finance
* **Manage Orders**:

    * View all orders & statuses
    * Track order progress
    * Receive real-time updates
* **Create Orders (API / Dashboard)**:

    * Real-time booking
    * Scheduled deliveries
* **Cancel Orders**:

    * Trigger fallback assignment
* **Manual Order Booking**:

    * Book offline delivery jobs
* **Auto-Reassignment**:

    * Reassign orders on driver decline/cancel

---

# ## **2. Third-Party Delivery Providers (Porter, Dunzo, Shadowfax, etc.)**

These are logistics partners integrated into SwiftTrack via the Provider Integration Gateway.

### **Key Responsibilities:**

* **Register Their Service on SwiftTrack**:

    * Provide API credentials
    * Service coverage areas
    * Pricing models
* **Receive Orders From SwiftTrack**:

    * Accept bookings automatically
    * Provide updates
* **Transfer Revenue to Wallet**:

    * Move received payments into their SwiftTrack wallet
* **Interact With AI Chatbot**:

    * View service metrics
    * View delivery volumes
    * Troubleshoot issues

---

# ## **3. Drivers (SwiftTrack Marketplace Drivers & Tenant Drivers)**

Drivers may be:

* Independent marketplace drivers (SwiftTrack-owned)
* Tenant-owned drivers (internal fleet)

### **Key Responsibilities:**

### **Driver Account & Verification**

* **Register to System**
* **KYC & Identity Verification**
* **ML-Based Verification**:

    * Past performance
    * Customer feedback patterns
    * Risk assessment

### **Delivery Operations**

* **View Assigned Orders**
* **Accept/Reject Delivery Tasks**
* **Navigate to Pickup & Drop**
* **Provide Live Location Updates**
* **Provide Delivery Status Updates**

### **Financial Activities**

* **Track Earnings**

    * Job-wise revenue
    * Daily/weekly/monthly breakdown
* **View Order History & Completed Jobs**
* **Transfer Amount to Bank Account**

    * From driver wallet

### **Performance Insights**

* **Rating Summary**
* **Quality Score (ML-based)**
* **Behavioral Trends**

---

# ## **4. SwiftTrack Admin / System Actor**

SwiftTrack Admin is responsible for the overall platform operation, system health, integrations, and global configurations.

### **Key Responsibilities:**

### **Platform Management**

* **Approve/Reject Provider Integration Requests**
* **Approve Driver Onboarding (manual/ML assisted)**
* **Monitor System Health**

    * Microservices health
    * Kafka lag
    * Delivery performance

### **Order Assignment Engine**

* **Run Global Assignment Logic**:

    * Tenant Fleet
    * Marketplace Drivers
    * Third-party service providers
* **Fallback Handling**:

    * Try next best provider
    * Trigger retries
    * Auto-assignment & reassignments

### **Analytics & AI Ops**

* **Monitor AI Dispatch Models**
* **Tune ML Scoring Metrics**
* **Analyze Failure Patterns**

### **Financial Oversight**

* **Monitor Wallet Balances** (Tenant, Driver, Provider)
* **Fraud Detection**
* **Dispute Resolution**

### **System Integrations**

* **Add new delivery providers**
* **Test integration adapters**
* **Monitor API rate limits**

---

# ## **5. High-Level Actor Interactions**

### **Tenants → SwiftTrack**

* Create orders
* Receive real-time updates
* Configure delivery providers
* Manage internal driver fleet
* Use dashboards + AI chatbot

### **Drivers → SwiftTrack**

* Receive tasks
* Push location
* Update delivery status
* Track earnings

### **Third-Party Providers → SwiftTrack**

* Receive booked tasks
* Sync statuses
* Provide cost estimations

### **SwiftTrack Admin → System**

* Oversight
* Integrations
* Quality monitoring
* Global configuration

---

# ## **6. Summary**

This document clearly defines:

* All actors in the SwiftTrack ecosystem
* Their functions
* Their responsibilities
* Their interactions with the system

This will be used to design:

* User flows
* DB schema
* Access control models (RBAC)
* Microservice boundaries
* API specifications

It forms the foundation of the next step—**Feature Finalization + DB Design**.
