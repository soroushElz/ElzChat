### 💬 Real-Time Messaging & Offline Catch-Up

#### Use Case: Real-Time Private Messaging, Threaded Replies, and Pending Message Retrieval

Allows authenticated users to engage in real-time private conversations over WebSockets/STOMP, reply directly to specific messages, and retrieve undelivered messages sent while offline.

* **Primary Actors:** Authenticated Users
* **Protocols:** STOMP over WebSocket (Real-Time), REST API (Catch-Up)

---
#### 🧪 Integration test
 implementation here: https://github.com/soroushElz/ElzChat/blob/main/src/test/java/com/example/ChatApplication/WebSocketEndpointIT.java#L108-L172

The integration tests run against the embedded server and messaging stack to ensure the full stack (authentication, WebSocket/STOMP endpoints, persistence, and REST catch-up) behaves correctly in realistic scenarios.

#### 🔄 Execution Flow
<img width="1024" height="1536" alt="image" src="https://github.com/user-attachments/assets/5762f0f4-8a8e-4657-8e5a-754acef865f7" />

#### 📋 Detailed Steps

1. **Session Setup & Connection**
   * Users establish JWT-authenticated STOMP sessions and subscribe to their personal destination: `/user/topic/chat`.

2. **Real-Time Exchange & Threading**
   * **Send Message:** Users post messages to `/app/chat/{channelId}`. Active subscribers receive the payload instantaneously.
   * **Threaded Reply:** Users post messages referencing a parent `messageId` (`replyTo`). The server links the payload to the original message before broadcasting.

3. **Offline Messaging & Synchronization**
   * **Disconnected State:** Messages sent to an offline user are automatically captured and saved in the database as pending.
   * **Pending Catch-Up:** Upon reconnecting, users issue a `GET` request to `/api/v1/chat/messages/pending` (optionally passing `?channelId={id}`) to fetch all missed messages sent while offline.

---

#### 📡 API & Socket Endpoints Summary

| Type | Endpoint / Destination | Method | Description |
| :--- | :--- | :---: | :--- |
| **STOMP Send** | `/app/chat/{channelId}` | `SEND` | Send a new message or reply to a private channel |
| **STOMP Sub** | `/user/topic/chat` | `SUB` | Subscribe to receive real-time incoming messages |
| **REST API** | `/api/v1/chat/messages/pending` | `GET` | Retrieve undelivered messages accumulated while offline |

### 👍 Message Reactions & Real-Time Event Updates

#### Use Case: Add/Remove Message Reactions and Real-Time Event Broadcast

Allows authenticated users to add or remove emoji reactions on channel messages via REST endpoints, while broadcasting real-time reaction updates to subscribed channel members over WebSocket queues and validating reaction state constraints.

* **Primary Actors:** Authenticated Users
* **Protocols:** REST API (Reaction Actions), STOMP over WebSocket (Event Notifications)

---
#### 🧪 Integration test
 implementation here: https://github.com/soroushElz/ElzChat/blob/main/src/test/java/com/example/ChatApplication/WebSocketEndpointIT.java#L175-L226


#### 🔄 Execution Flow

```
User A (Reactor)                  Server                     User B (Subscriber)
   │                                │                                │
   │                                │◄────── STOMP Subscribe ────────┤
   │                                │     (/user/queue/events)       │
   │                                │                                │
   ├─── POST /message/{id}/reaction►│                                │
   │    (Action: ADD, Type: LIKE)   ├────── STOMP Event Update ─────►│ (Received)
   │◄── 200 OK (ReactionAck) ───────┤   (ReactionAckResponseDto)     │
   │                                │                                │
   ├─── POST /message/{id}/reaction►│                                │
   │    (Action: REMOVE, Type: LIKE)│                                │
   │◄── 200 OK (ReactionAck) ───────┤                                │
   │                                │                                │
   ├─── POST /message/{id}/reaction►│                                │
   │    (REMOVE Non-Existent)       │                                │
   │◄── Error Response ─────────────┤                                │
   │    ("reaction does not exists")│                                │
   │                                │                                │
   ├─── POST /message/{id}/reaction►│                                │
   │    (ADD Duplicate Reaction)    │                                │
   │◄── Error Response ─────────────┤                                │
   │    ("user reaction already...")│                                │

```

#### 📋 Detailed Steps

1. **Subscription Setup**
   * Active channel members establish JWT-authenticated STOMP connections and subscribe to their personal event queue: `/user/queue/events`.

2. **Add Reaction & Event Dispatch**
   * **Submit Reaction:** User A issues a `POST` request to `/api/v1/message/{messageId}/reaction` with payload `{"reactionType": "LIKE", "action": "ADD"}`.
   * **Acknowledge Sender:** Server validates, persists the reaction, and returns `200 OK` with a `ReactionAckResponseDto`.
   * **Broadcast Update:** Server pushes a real-time STOMP event payload containing the reaction details to User B via `/user/queue/events`.

3. **Remove Reaction**
   * **Submit Removal:** User A sends a `POST` request with payload `{"reactionType": "LIKE", "action": "REMOVE"}`.
   * **Acknowledge & Delete:** Server removes the reaction entry and returns a successful `ReactionAckResponseDto`.

4. **Validation & Edge Case Handling**
   * **Non-Existent Reaction:** Attempting to remove a non-existent reaction returns an error response: `{"error": "reaction does not exists"}`.
   * **Duplicate Reaction:** Attempting to add a reaction when one already exists for that user returns an error response: `{"error": "user reaction already exists!"}`.

---

#### 📡 API & Socket Endpoints Summary

| Type | Endpoint / Destination | Method | Description |
| :--- | :--- | :---: | :--- |
| **REST API** | `/api/v1/message/{messageId}/reaction` | `POST` | Add or remove an emoji reaction on a target message |
| **STOMP Sub** | `/user/queue/events` | `SUB` | Receive real-time notification events for reaction changes |

### 🚫 User Blocking, Real-Time Notifications & Message Restriction

#### Use Case: User Block/Unblock Events and Blocked Message Enforcement

Allows authenticated users to manage their block lists via REST endpoints while automatically notifying blocked users in real-time over WebSocket queues and enforcing message restriction rules when a blocked user attempts to communicate.

* **Primary Actors:** Authenticated Users
* **Protocols:** REST API (Block List Management), STOMP over WebSocket (Block Notifications & Error Dispatch)

---
#### 🧪 Integration test
 implementation here: https://github.com/soroushElz/ElzChat/blob/main/src/test/java/com/example/ChatApplication/WebSocketEndpointIT.java#L231-L280

#### 🔄 Execution Flow

```
User A (Blocker)                  Server                     User B (Blocked)
   │                                │                                │
   │                                │◄── STOMP Subscribe Block Queue ┤
   │                                │    (/user/queue/notif/block)   │
   │                                │◄── STOMP Subscribe Error Queue ┤
   │                                │    (/user/queue/notif/error)   │
   │                                │                                │
   ├─── POST /user/updateBlockList ►│                                │
   │    (Block: User B)             ├────── STOMP Block Event ──────►│ (Received)
   │◄── 200 OK ─────────────────────┤    (BlockedBy: A, Blocked: B)  │
   │                                │                                │
   │                                │◄───── STOMP Send /app/chat ────┤
   │                                │       (Attempt message send)   │
   │                                ├────── STOMP Error Event ──────►│ (Received)
   │                                │       ("you are blocked!")     │
   │                                │                                │
   ├─── POST /user/updateBlockList ►│                                │
   │    (Unblock: User B)           ├────── STOMP Unblock Event ────►│ (Received)
   │◄── 200 OK ─────────────────────┤    (Action: UNBLOCK)           │
```
#### 📋 Detailed Steps

1. **Subscription Setup**
   * User B establishes a JWT-authenticated STOMP session and subscribes to two personal event destinations:
     * **Block Notifications:** `/user/queue/notification/block`
     * **Error Notifications:** `/user/queue/notification/error`

2. **Blocking a User & Real-Time Event Dispatch**
   * **Submit Block Request:** User A issues a `POST` request to `/api/v1/user/updateBlockList` with User B's ID in the block list payload.
   * **Broadcast Block Event:** The server processes the block request and immediately pushes a `BlockNotificationPayload` to User B's `/user/queue/notification/block` topic, identifying User A as the blocker and User B as the blocked user.

3. **Message Rejection & Error Dispatch**
   * **Attempt Message Send:** Blocked User B attempts to send a STOMP chat message to `/app/chat/{channelId}`.
   * **Block Enforcement:** The server identifies the active block restriction, intercepts the message, and pushes an `ErrorNotificationPayload` to User B's `/user/queue/notification/error` destination stating `"you are blocked!"`.

4. **Unblocking a User & Event Dispatch**
   * **Submit Unblock Request:** User A issues a `POST` request to `/api/v1/user/updateBlockList` specifying User B's ID to be removed from the block list.
   * **Broadcast Unblock Event:** The server updates the block status and dispatches an unblock notification (`Action: UNBLOCK`) to User B's `/user/queue/notification/block` topic.

---

#### 📡 API & Socket Endpoints Summary

| Type | Endpoint / Destination | Method | Description |
| :--- | :--- | :---: | :--- |
| **REST API** | `/api/v1/user/updateBlockList` | `POST` | Update user block list (add/remove users from block set) |
| **STOMP Sub** | `/user/queue/notification/block` | `SUB` | Receive real-time notifications for block and unblock events |
| **STOMP Sub** | `/user/queue/notification/error` | `SUB` | Receive real-time error messages (e.g., blocked message attempt failures) |
| **STOMP Send** | `/app/chat/{channelId}` | `SEND` | Attempt chat message delivery to a target channel |

### 🔔 Offline Notification Catch-Up & Pending Event Synchronization

#### Use Case: Offline Recipient Pending Notification Retrieval

Allows users who were offline when a notification-generating event occurred (such as being blocked by another user) to retrieve all queued pending notifications via REST API upon reconnecting.

* **Primary Actors:** Authenticated Users (Offline Recipient, Active Sender)
* **Protocols:** REST API

---
#### 🧪 Integration test
 implementation here: https://github.com/soroushElz/ElzChat/blob/main/src/test/java/com/example/ChatApplication/WebSocketEndpointIT.java#L283-L325


#### 🔄 Execution Flow

```
User A (Offline Recipient)           Server                     User B (Active Sender)
   │                                   │                                │
   │   [User A Offline for 1+ Day]     │◄── POST /user/updateBlockList ─┤
   │                                   │    (Block User A)              │
   │                                   ├─── Persist Pending Notification│
   │                                   │─────────── 200 OK ────────────►│
   │                                   │                                │
   │   [User A Logs In / Reconnects]   │                                │
   ├── GET /user/notification/pending ►│                                │
   │◄──  200 OK List<Notification> ────│                                │
   │    (BlockNotificationPayload)     │                                │
```
#### 📋 Detailed Steps

1. **Offline State Identification**
   * User A has been offline for a period of time (e.g., recorded last offline timestamp in database).

2. **Event Trigger & Pending Persistence**
   * **Trigger Event:** Active User B issues a `POST` request to `/api/v1/user/updateBlockList` adding User A to their block list.
   * **Store Pending Notification:** Detecting that User A is offline, the server encapsulates the block event into a `Notification` object with a `BlockNotificationPayload` and stores it with a pending status assigned to recipient User A.

3. **Pending Notification Retrieval**
   * **Fetch Missed Notifications:** Upon reconnecting, User A sends an authenticated `GET` request to `/api/v1/user/notification/pending`.
   * **Deliver Payloads:** The server queries and returns all unread pending notifications, allowing User A to inspect the notification details (`blockedUser`, `blockedBy`, and `recipientId`).

---

#### 📡 API & Socket Endpoints Summary

| Type | Endpoint / Destination | Method | Description |
| :--- | :--- | :---: | :--- |
| **REST API** | `/api/v1/user/updateBlockList` | `POST` | Update user block list (triggers pending notification if target is offline) |
| **REST API** | `/api/v1/user/notification/pending` | `GET` | Fetch all pending notifications generated while the authenticated recipient was offline |

This project uses integration tests to validate end-to-end behavior of the real-time messaging and offline catch-up flows. See the integration test method `testSendMessage_andRead_bySubscribers()` in the test class `WebSocketEndpointIT` for a concrete example: it exercises sending a STOMP message, broadcasting to active subscribers, and verifying pending message storage and retrieval for offline users — see the test
