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
<img width="300" height="500" alt="image" src="https://github.com/user-attachments/assets/5762f0f4-8a8e-4657-8e5a-754acef865f7" />

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
<img width="1024" height="1536" alt="image" src="https://github.com/user-attachments/assets/1ac50db5-99b8-4c7b-b0c5-e24789a1d57b" />

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
<img width="1086" height="1448" alt="image" src="https://github.com/user-attachments/assets/1bfbfbc9-48ab-4c40-ad51-5cae3096ccfa" />

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
<img width="1086" height="1448" alt="image" src="https://github.com/user-attachments/assets/1d5fd0c0-aed5-44bb-98bd-baa2df0e1cdc" />

---

#### 📡 API & Socket Endpoints Summary

| Type | Endpoint / Destination | Method | Description |
| :--- | :--- | :---: | :--- |
| **REST API** | `/api/v1/user/updateBlockList` | `POST` | Update user block list (triggers pending notification if target is offline) |
| **REST API** | `/api/v1/user/notification/pending` | `GET` | Fetch all pending notifications generated while the authenticated recipient was offline |

### 📱 User Contact List & Active Chat Channel Retrieval

#### Use Case: Fetch Active User Chat Channels (Contact List)

Allows an authenticated user to retrieve a complete list of all active private chat channels (contacts) in which they are a participant via the REST API.

* **Primary Actors:** Authenticated Users
* **Protocols:** REST API

---

#### 🔄 Execution Flow
<img width="1024" height="1536" alt="image" src="https://github.com/user-attachments/assets/6ddb686a-9951-48da-a3ef-cb82421d7edf" />


#### 📡 API & Socket Endpoints Summary

| Type | Endpoint / Destination | Method | Description |
| :--- | :--- | :---: | :--- |
| **REST API** | `/api/v1/user/contacts` | `GET` | Retrieve all active private chat channels (contact list) associated with the authenticated user |

This project uses integration tests to validate end-to-end behavior of the real-time messaging and offline catch-up flows. See the integration test method `testSendMessage_andRead_bySubscribers()` in the test class `WebSocketEndpointIT` for a concrete example: it exercises sending a STOMP message, broadcasting to active subscribers, and verifying pending message storage and retrieval for offline users — see the test
