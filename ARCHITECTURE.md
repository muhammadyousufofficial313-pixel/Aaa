# Official WhatsApp Business Platform Unified Inbox Architecture

This document defines the complete end-to-end production architecture, security model, database schema, API design, webhook lifecycle, and multi-OEM notification strategy for the WhatsApp Business Unified Inbox Android application.

---

## A. Complete Architecture

```
                                  [ WhatsApp User (Customer) ]
                                                │
                                                ▼
                                [ WhatsApp Business Platform / Cloud API ]
                                   (graph.facebook.com/v21.0)
                                                │
                                                ▼  HTTPS POST (Signature: X-Hub-Signature-256)
                                      [ Webhook Receiver ]
                                                │
                                                ▼
                                      [ Secure Backend Server ]
                         (Spring Boot / Node.js / Ktor / Python FastAPI)
                         ┌──────────────────────┴──────────────────────┐
                         ▼                                             ▼
                 [ Cloud Database ]                         [ Dual Push Gateway ]
          (PostgreSQL / Cloud SQL / Mongo)                 ┌───────────┴───────────┐
                         │                                 ▼                       ▼
                         │                        [ Firebase FCM ]        [ Huawei Push Kit ]
                         │                                 │                       │
                         └─────────────────┬───────────────┴───────────────────────┘
                                           │ HTTPS REST / WebSocket / SSE
                                           ▼
                           [ Unified Inbox Android Application ]
                 ┌───────────────────────────────────────────────────────────┐
                 │  UI Layer (Jetpack Compose, M3, RTL Urdu / LTR English)  │
                 ├───────────────────────────────────────────────────────────┤
                 │  ViewModel Layer (StateFlow, Lifecycle-aware MVVM)        │
                 ├───────────────────────────────────────────────────────────┤
                 │  Repository Layer (Single Source of Truth, Offline Cache) │
                 ├─────────────────────────────┬─────────────────────────────┤
                 │  Local Cache: Room Database │ Network: Retrofit + OkHttp  │
                 │  (Indexed, Reactive Flows)  │ (JWT Auth, Backoff Retry)   │
                 └─────────────────────────────┴─────────────────────────────┘
```

### Outgoing Reply Flow (Business Owner to Customer):
```
1. Business Owner sends reply in Android App (Text / Media / Template)
2. App writes message locally in Room as PENDING (instant UI response)
3. App dispatches POST /messages/send to Backend Server with JWT
4. Backend Server verifies 24-hour service window:
   - If within 24h: invokes WhatsApp Cloud API `POST /{phone-number-id}/messages` with standard text/media payload.
   - If outside 24h: requires pre-approved Meta Template payload.
5. WhatsApp Cloud API returns message ID (`wamid.HBg...`)
6. Backend updates message state to SENT and returns response to App
7. Customer receives message on WhatsApp; WhatsApp sends webhook statuses (DELIVERED -> READ)
8. Backend updates database and dispatches push update to Android App
```

---

## B. Folder Structure

```
app/src/main/java/com/example/
├── data/
│   ├── local/
│   │   ├── Converters.kt                   # Room type converters (Enums, Timestamps, Lists)
│   │   ├── AppDatabase.kt                  # Room Database configuration
│   │   ├── dao/
│   │   │   └── WhatsAppDao.kt              # Reactive DAO queries for inbox, chat & stats
│   │   └── entities/
│   │       ├── BusinessEntity.kt           # Business profile and config metadata
│   │       ├── CustomerEntity.kt           # Customer profile, tags, notes, contact status
│   │       ├── ConversationEntity.kt       # Conversation state, 24h window, unread count
│   │       ├── MessageEntity.kt            # Messages with status (PENDING/SENT/DELIVERED/READ/FAILED)
│   │       ├── TemplateEntity.kt           # WhatsApp message templates (Utility, Marketing)
│   │       ├── StaffEntity.kt              # Multi-user staff directory (Owner, Admin, Agent)
│   │       └── AuditLogEntity.kt           # Security audit trail for compliance
│   ├── notification/
│   │   └── UnifiedNotificationManager.kt   # Dual-channel notification handling (FCM / HMS / Fallback)
│   ├── remote/
│   │   ├── api/
│   │   │   └── WhatsAppBackendApi.kt       # Retrofit HTTP interface for backend endpoints
│   │   ├── engine/
│   │   │   └── MockBackendEngine.kt        # Realistic Meta Cloud API v21.0 engine for instant sandbox testing
│   │   └── models/
│   │       ├── AuthModels.kt               # Login, session, password reset payloads
│   │       ├── ConversationDto.kt          # DTOs for server communication
│   │       ├── WebhookModels.kt            # Official Meta Webhook event schema
│   │       └── WhatsAppCloudModels.kt      # WhatsApp Cloud API request/response structures
│   └── repository/
│       └── WhatsAppRepository.kt           # Central repository mediating Room & Remote API
├── domain/
│   └── model/
│       ├── FilterModels.kt                 # Search filters and conversation tags
│       ├── MessageModels.kt                # UI domain models (MessageStatus, SenderType)
│       └── UserSession.kt                  # Authentication state & staff privileges
└── ui/
    ├── MainActivity.kt                     # Single-activity container with Edge-to-Edge & RTL support
    ├── auth/
    │   └── AuthScreen.kt                   # Login, reset password & staff session management
    ├── chat/
    │   ├── ChatScreen.kt                   # Dynamic chat view with 24-hour service window warning
    │   ├── CustomerDetailsSheet.kt         # Customer tags, notes, staff assignment
    │   ├── MessageBubble.kt                # Custom message bubble (delivery ticks, media preview)
    │   └── TemplatePickerDialog.kt         # Pre-approved WhatsApp template picker
    ├── components/
    │   ├── SearchBarComponent.kt           # Real-time search with chips & debounce
    │   ├── ServiceWindowBanner.kt          # 24-hour customer service window visual indicator
    │   └── WebhookSimulatorDialog.kt       # Direct Meta Webhook event simulator for QA
    ├── connection/
    │   └── WhatsAppConnectionScreen.kt     # WABA ID, Phone Number ID, Webhook verify token setup
    ├── dashboard/
    │   └── DashboardScreen.kt              # Business KPI analytics: volume, response rate, SLAs
    ├── inbox/
    │   ├── ConversationItem.kt             # Inbox list tile with unread badge & status
    │   └── InboxScreen.kt                  # Modern unified inbox list view
    ├── navigation/
    │   └── NavigationDestination.kt        # Navigation routes and screens
    ├── settings/
    │   └── SettingsScreen.kt               # Server URL config, language (Urdu/English), dark mode
    ├── staff/
    │   └── StaffManagementScreen.kt        # Multi-user staff accounts & role permissions
    ├── templates/
    │   └── TemplatesScreen.kt              # Meta template catalog & manual sync
    ├── theme/
    │   ├── Color.kt                        # SaaS enterprise teal & slate palette
    │   ├── Theme.kt                        # Dynamic M3 theme with Dark/Light support
    │   └── Type.kt                         # Clean accessible typography
    └── viewmodel/
        ├── AuthViewModel.kt                # Session management & credential validation
        ├── ChatViewModel.kt                # Chat messaging, template sending, window monitoring
        ├── DashboardViewModel.kt           # Real-time aggregated business statistics
        ├── InboxViewModel.kt               # Conversation list, search, filter, fast updates
        └── SettingsViewModel.kt            # App preferences, language, engine mode
```

---

## C. Database Schema

### 1. `businesses`
- `id` (TEXT, PK): Unique business ID.
- `name` (TEXT): Legal business name.
- `metaBusinessAccountId` (TEXT): Meta Business Manager Account ID.
- `wabaId` (TEXT): WhatsApp Business Account ID.
- `phoneNumberId` (TEXT): Meta Phone Number ID.
- `displayPhoneNumber` (TEXT): E.164 business phone number (e.g., `+15551234567`).
- `webhookVerifyToken` (TEXT): Shared secret for Meta Webhook verification handshake.
- `createdAt` (INTEGER): Epoch timestamp.

### 2. `business_users` (Staff)
- `id` (TEXT, PK): Staff ID.
- `businessId` (TEXT, FK): Business ID reference.
- `name` (TEXT): Staff member display name.
- `email` (TEXT, UNIQUE): Staff login email.
- `role` (TEXT): Role (`OWNER`, `ADMIN`, `AGENT`).
- `avatarUrl` (TEXT): Profile photo.
- `isActive` (INTEGER): Active status boolean.

### 3. `customers`
- `id` (TEXT, PK): E.164 phone number without spaces (e.g., `15559876543`).
- `businessId` (TEXT, FK): Business ID reference.
- `name` (TEXT): Contact name (from WhatsApp profile or manual override).
- `phoneNumber` (TEXT): E.164 phone number.
- `avatarUrl` (TEXT): Customer profile avatar if provided by Meta.
- `notes` (TEXT): Internal business notes regarding customer requirements.
- `tags` (TEXT): JSON array of tags (e.g., `["VIP", "Lead", "Urgent", "Wholesale"]`).
- `isBlocked` (INTEGER): Blocking flag.
- `createdAt` (INTEGER): Timestamp of first contact.

### 4. `conversations`
- `id` (TEXT, PK): Conversation ID (typically `conv_{customerId}`).
- `businessId` (TEXT, FK): Business ID reference.
- `customerId` (TEXT, FK): Customer ID reference.
- `lastMessageText` (TEXT): Snippet of the most recent message.
- `lastMessageTimestamp` (INTEGER): Timestamp of the most recent message.
- `lastCustomerMessageTimestamp` (INTEGER): Timestamp of the last incoming message (drives 24h window).
- `unreadCount` (INTEGER): Number of unread customer messages.
- `assignedStaffId` (TEXT, Nullable): ID of staff member handling this thread.
- `isImportant` (INTEGER): Starred/important conversation flag.
- `isArchived` (INTEGER): Archived state flag.
- `windowExpiresAt` (INTEGER): Timestamp when the 24-hour free-form customer window closes (`lastCustomerMessageTimestamp + 86,400,000`).

### 5. `messages`
- `id` (TEXT, PK): UUID or server message ID.
- `conversationId` (TEXT, FK): Associated conversation ID.
- `waMessageId` (TEXT, UNIQUE, Nullable): Meta `wamid.HBg...` identifier.
- `senderType` (TEXT): `CUSTOMER` or `BUSINESS`.
- `senderStaffId` (TEXT, Nullable): Staff member who sent the reply.
- `messageType` (TEXT): `TEXT`, `IMAGE`, `DOCUMENT`, `AUDIO`, `TEMPLATE`.
- `content` (TEXT): Message body text or template summary.
- `mediaUrl` (TEXT, Nullable): Public URL or cached local file path for attachments.
- `mediaMimeType` (TEXT, Nullable): MIME type (e.g. `image/jpeg`, `application/pdf`).
- `mediaFileName` (TEXT, Nullable): File name for document attachments.
- `mediaSize` (INTEGER, Nullable): Size in bytes.
- `status` (TEXT): `PENDING`, `SENT`, `DELIVERED`, `READ`, `FAILED`.
- `failureReason` (TEXT, Nullable): Meta error message or code (e.g., "Outside 24-hour window", "Invalid phone number").
- `timestamp` (INTEGER): Creation epoch timestamp.
- `replyToMessageId` (TEXT, Nullable): Parent message ID for contextual replies.

### 6. `templates`
- `id` (TEXT, PK): Meta template identifier.
- `name` (TEXT): Template programmatic name (e.g., `order_confirmation_v2`).
- `language` (TEXT): ISO language code (e.g., `en_US`, `ur`).
- `category` (TEXT): `MARKETING`, `UTILITY`, `AUTHENTICATION`.
- `status` (TEXT): `APPROVED`, `PENDING`, `REJECTED`.
- `bodyText` (TEXT): Template message body with variable placeholders `{{1}}`, `{{2}}`.
- `headerText` (TEXT, Nullable): Optional header text.
- `buttonsJson` (TEXT): JSON representation of quick-reply or CTA buttons.

### 7. `audit_logs`
- `id` (TEXT, PK): UUID.
- `action` (TEXT): Action performed (e.g., `LOGIN`, `SEND_TEMPLATE`, `ASSIGN_STAFF`, `UPDATE_CONFIG`).
- `staffEmail` (TEXT): Performed by staff user.
- `timestamp` (INTEGER): Epoch timestamp.
- `details` (TEXT): Additional context.

---

## D. Backend API Design

All endpoints require `Authorization: Bearer <JWT>` except authentication and webhook callbacks.

### 1. `POST /auth/login`
- **Request**: `{ "email": "owner@business.com", "password": "secure_password" }`
- **Response**: `{ "token": "jwt...", "refreshToken": "...", "user": { "id": "u1", "role": "OWNER" } }`

### 2. `POST /auth/reset-password`
- **Request**: `{ "email": "owner@business.com" }`
- **Response**: `{ "status": "success", "message": "Password reset email sent." }`

### 3. `GET /whatsapp/account/status`
- **Response**: `{ "connected": true, "wabaId": "1009823...", "phoneNumberId": "10564...", "displayPhoneNumber": "+15551234567", "qualityRating": "GREEN" }`

### 4. `POST /whatsapp/account/connect`
- **Request**: `{ "metaBusinessAccountId": "...", "wabaId": "...", "phoneNumberId": "...", "webhookVerifyToken": "..." }`
- **Response**: `{ "success": true, "webhookCallbackUrl": "https://api.yourdomain.com/webhooks/whatsapp" }`

### 5. `GET /conversations?limit=50&cursor=...&filter=...`
- **Response**: List of conversations with unread count, 24h window expiration, and customer summary.

### 6. `GET /conversations/{id}/messages?limit=50&before=...`
- **Response**: Paginated messages ordered by timestamp descending.

### 7. `POST /messages/send`
- **Request**:
```json
{
  "conversationId": "conv_15559876543",
  "recipientPhone": "15559876543",
  "messageType": "TEXT",
  "content": "Thank you for reaching out! We are preparing your order.",
  "replyToMessageId": "wamid.HBg..."
}
```
- **Response**:
```json
{
  "messageId": "msg_98124",
  "waMessageId": "wamid.HBgLMTU1NTk4NzY1NDM...",
  "status": "SENT",
  "timestamp": 1726532400000
}
```

### 8. `POST /messages/send-template`
- **Request**:
```json
{
  "conversationId": "conv_15559876543",
  "recipientPhone": "15559876543",
  "templateName": "order_confirmation_v2",
  "languageCode": "en_US",
  "components": [
    {
      "type": "body",
      "parameters": [
        { "type": "text", "text": "Customer Name" },
        { "type": "text", "text": "#ORD-9021" }
      ]
    }
  ]
}
```

### 9. `POST /media/upload`
- Multipart form: `file`, `type` (`image`, `document`, `audio`).
- Returns: `{ "mediaId": "meta_media_id_123", "mediaUrl": "https://storage..." }`.

### 10. `POST /webhooks/whatsapp` (Meta Webhook Endpoint)
- Handshake `GET`: checks `hub.mode == "subscribe"` and `hub.verify_token == webhookVerifyToken`, returns `hub.challenge`.
- Ingestion `POST`: validates `X-Hub-Signature-256`, processes incoming messages & delivery status updates.

---

## E. WhatsApp Webhook Flow

```
1. Customer sends message on WhatsApp (e.g. "Is my order ready?")
2. WhatsApp Cloud API signs payload using App Secret into SHA-256 HMAC
3. Cloud API posts payload to Backend Server `POST /webhooks/whatsapp`
4. Backend Server verifies `X-Hub-Signature-256` matching `sha256=HMAC_SHA256(body, app_secret)`
5. Backend parses `entry[].changes[].value`:
   - If `messages[]`:
     * Extracts `from`, `id`, `timestamp`, `type` (text, image, audio, interactive).
     * Creates or updates `customer` record.
     * Creates or updates `conversation` record (`lastCustomerMessageTimestamp = now`, `windowExpiresAt = now + 24h`).
     * Inserts into `messages` table with status `DELIVERED`.
     * Emits push notification to Android app with customer name and preview.
   - If `statuses[]`:
     * Extracts `id` (matching `waMessageId`), `status` (`sent`, `delivered`, `read`, `failed`).
     * Updates matching record in `messages` table.
     * If failed, captures `errors[].title` and `errors[].code`.
     * Emits status update event to Android app.
6. Backend returns HTTP 200 OK immediately to Meta (to prevent delivery timeouts).
```

---

## F. Authentication Flow

```
1. Business Owner opens App.
2. If no valid refresh token stored in encrypted storage: displays AuthScreen.
3. User enters email & password.
4. App dispatches `POST /auth/login`.
5. Backend verifies password hash (Argon2 / BCrypt) and issues:
   - Access Token (short-lived JWT, 15 min expiration)
   - Refresh Token (long-lived cryptographically random token, 30 days)
6. App stores Refresh Token securely in Android Keystore / EncryptedSharedPreferences.
7. OkHttp AuthInterceptor attaches Access Token to all backend requests.
8. Authenticator automatically handles HTTP 401: invokes `/auth/refresh` silently and retries request.
9. If Refresh Token is expired or revoked, session clears and returns to AuthScreen.
```

---

## G. Security Model

1. **Zero Access Tokens on Client**: Meta System User Permanent Access Tokens and App Secrets NEVER touch the Android device. They reside exclusively on the secure backend server in encrypted environment variables or Secret Manager.
2. **Encrypted Client Credentials**: Android user JWTs and session tokens are encrypted using `EncryptedSharedPreferences` backed by the hardware Android Keystore.
3. **Transport Layer Security**: All API traffic is strictly HTTPS with TLS 1.3. Certificate pinning can be enabled in production OkHttp configuration.
4. **Meta Webhook Signature Verification**: The backend server validates `X-Hub-Signature-256` on every incoming Meta webhook.
5. **Role-Based Access Control (RBAC)**:
   - `OWNER`: Full control, manage connection credentials, billing, staff, and conversations.
   - `ADMIN`: Manage staff, templates, analytics, and all conversations.
   - `AGENT`: View and reply to assigned or unassigned customer conversations only.
6. **Audit Trail**: Every administrative action (staff invite, role alteration, WhatsApp config change) creates an immutable entry in `audit_logs`.

---

## H. Huawei Notification Strategy (Dual-Channel Architecture)

To ensure push notifications deliver reliably on Huawei devices without Google Play Services (HMS Core devices) as well as standard Android devices (GMS Core):

1. **OEM Environment Detection**:
   - `UnifiedNotificationManager` checks at runtime:
     * `GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS` -> GMS available.
     * `HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(context) == ConnectionResult.SUCCESS` -> HMS available.
2. **Push Token Registration**:
   - On GMS devices: fetches Firebase Cloud Messaging (FCM) token.
   - On HMS devices: fetches Huawei Push Kit (HMS Push) token via `HmsInstanceId`.
   - On devices with neither: initializes a lightweight Foreground Service / WebSocket background sync channel with periodic JobScheduler keep-alive.
3. **Dual Token Registration Endpoint**:
   - App registers token with backend: `POST /devices/register-push` specifying `{ "token": "...", "provider": "FCM" | "HMS" | "FALLBACK" }`.
4. **Server-Side Dispatch**:
   - When new message arrives, backend checks staff device provider:
     * If `FCM`: calls Firebase Admin SDK.
     * If `HMS`: calls Huawei Push Kit REST API (`https://push-api.cloud.huawei.com/v1/{appId}/messages:send`).
5. **Local High-Priority Channels**:
   - App configures high-importance Android Notification Channels (`channel_messages`, `channel_alerts`) with custom sound and vibration pattern to wake the device screen on incoming customer chats.
