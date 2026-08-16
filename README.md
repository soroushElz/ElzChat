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

```
User A (Online)              Server                  User B (online)
      │                        │                            │
      ├─── STOMP Message ─────►│                            │
      │  (/app/chat/{id})      ├── Broadcast to Active ────►│ (Received)
      │                        │                            │
      │                        │   [User B Disconnects]     │
      │                        │                            │
      ├─── STOMP Message ─────►│                            │
      │ (With replyTo ref)     ├── Store as Pending ───────►│ (Offline)
      │                        │                            │
      │                        │◄── GET /messages/pending ──┤
      │                        │    (Fetch missed messages)  │
```

---

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

This project uses integration tests to validate end-to-end behavior of the real-time messaging and offline catch-up flows. See the integration test method `testSendMessage_andRead_bySubscribers()` in the test class `WebSocketEndpointIT` for a concrete example: it exercises sending a STOMP message, broadcasting to active subscribers, and verifying pending message storage and retrieval for offline users — see the test
