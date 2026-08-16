## Table of Contents

- [Real-Time Messaging & Offline Catch-Up](#real-time-messaging-offline-catch-up)
- [Private Chat Channel Initialization](#private-chat-channel-initialization)
- [Message Reactions & Real-Time Event Updates](#message-reactions--real-time-event-updates)
- [User Blocking, Real-Time Notifications & Message Restriction](#user-blocking-real-time-notifications--message-restriction)
- [Offline Notification Catch-Up & Pending Event Synchronization](#offline-notification-catch-up--pending-event-synchronization)
- [User Contact List & Active Chat Channel Retrieval](#user-contact-list--active-chat-channel-retrieval)
- [Message Forwarding Across Channels](#message-forwarding-across-channels)
- [Filtered Message Search](#filtered-message-search)
- [Group Lifecycle Management (Creation, Leave, & Deletion)](#group-lifecycle-management-creation-leave--deletion)


<a id="real-time-messaging-offline-catch-up"></a>
### 💬 Real-Time Messaging & Offline Catch-Up

<details>
<summary>Summary & Details</summary>

#### Use Case: Real-Time Private Messaging, Threaded Replies, and Pending Message Retrieval

Allows authenticated users to engage in real-time private conversations over WebSockets/STOMP, reply directly to specific messages, and retrieve undelivered messages sent while offline.

* **Primary Actors:** Authenticated Users
* **Protocols:** STOMP over WebSocket (Real-Time), REST API (Catch-Up)

---
#### 🧪 Integration test
 implementation here: https://github.com/soroushElz/ElzChat/blob/main/src/test/java/com/example/ChatApplication/WebSocketEndpointIT.java#L108-L172

The integration tests run against the embedded server and messaging stack to ensure the full stack (authentication, WebSocket/STOMP endpoints, persistence, and REST catch-up) behaves correctly in real[...] 

#### 🔄 Execution Flow

 <img width="600" height="750" alt="image" src="https://github.com/user-attachments/assets/5762f0f4-8a8e-4657-8e5a-754acef865f7" />

---

#### 📡 API & Socket Endpoints Summary

| Type | Endpoint / Destination | Method | Description |
| :--- | :--- | :---: | :--- |
| **STOMP Send** | `/app/chat/{channelId}` | `SEND` | Send a new message or reply to a private channel |
| **STOMP Sub** | `/user/topic/chat` | `SUB` | Subscribe to receive real-time incoming messages |
| **REST API** | `/api/v1/chat/messages/pending` | `GET` | Retrieve undelivered messages accumulated while offline |

</details>

<a id="private-chat-channel-initialization"></a>
### 💬 Private Chat Channel Initialization

<details>
<summary>Summary & Details</summary>

#### Use Case: Establish Direct Private Chat Channel

Allows an authenticated user to initialize or establish a new 1-on-1 private chat channel with another registered user via the REST API.

* **Primary Actors:** Authenticated User (Initiator), Target User (Recipient)
* **Protocols:** REST API

#### 🧪 Integration test
 implementation here: https://github.com/soroushElz/ElzChat/blob/main/src/test/java/com/example/ChatApplication/WebSocketEndpointIT.java#L531-L548
 
#### 📋 Detailed Steps

1. **Initialization Request**
   * **Submit Request:** User 1 sends an authenticated `POST` request to `/api/v1/chat/newChannel` containing a `PrivateChatInitializationDto` payload specifying both `user1.getId()` and `user2.ge[...] 

2. **Channel Creation & Resolution**
   * **Process Channel:** The server validates the user IDs and authentication token, then creates or resolves the 1-on-1 private chat channel between the two specified participants.

3. **Response Delivery**
   * **Deliver Payload:** The server returns `200 OK` with an `EstablishedPrivateChannelDto` containing the initialized channel details.

---

#### 📡 API & Socket Endpoints Summary

| Type | Endpoint / Destination | Method | Description |
| :--- | :--- | :---: | :--- |
| **REST API** | `/api/v1/chat/newChannel` | `POST` | Initialize or retrieve a 1-on-1 private chat channel between two users |

</details>

### 👍 Message Reactions & Real-Time Event Updates
<a id="message-reactions--real-time-event-updates"></a>

<details>
<summary>Summary & Details</summary>

#### Use Case: Add/Remove Message Reactions and Real-Time Event Broadcast

Allows authenticated users to add or remove emoji reactions on channel messages via REST endpoints, while broadcasting real-time reaction updates to subscribed channel members over WebSocket queues and event channels.

* **Primary Actors:** Authenticated Users
* **Protocols:** REST API (Reaction Actions), STOMP over WebSocket (Event Notifications)

---
#### 🧪 Integration test
 implementation here: https://github.com/soroushElz/ElzChat/blob/main/src/test/java/com/example/ChatApplication/WebSocketEndpointIT.java#L175-L226


#### 🔄 Execution Flow
<img width="600" height="700" alt="image" src="https://github.com/user-attachments/assets/1ac50db5-99b8-4c7b-b0c5-e24789a1d57b" />

---

#### 📡 API & Socket Endpoints Summary

| Type | Endpoint / Destination | Method | Description |
| :--- | :--- | :---: | :--- |
| **REST API** | `/api/v1/message/{messageId}/reaction` | `POST` | Add or remove an emoji reaction on a target message |
| **STOMP Sub** | `/user/queue/events` | `SUB` | Receive real-time notification events for reaction changes |

</details>

<a id="user-blocking-real-time-notifications--message-restriction"></a>
### 🚫 User Blocking, Real-Time Notifications & Message Restriction

<details>
<summary>Summary & Details</summary>

#### Use Case: User Block/Unblock Events and Blocked Message Enforcement

Allows authenticated users to manage their block lists via REST endpoints while automatically notifying blocked users in real-time over WebSocket queues and enforcing message restriction rules when a block is in effect.

* **Primary Actors:** Authenticated Users
* **Protocols:** REST API (Block List Management), STOMP over WebSocket (Block Notifications & Error Dispatch)

---
#### 🧪 Integration test
 implementation here: https://github.com/soroushElz/ElzChat/blob/main/src/test/java/com/example/ChatApplication/WebSocketEndpointIT.java#L231-L280

#### 🔄 Execution Flow
<img width="600" height="750" alt="image" src="https://github.com/user-attachments/assets/1bfbfbc9-48ab-4c40-ad51-5cae3096ccfa" />

---

#### 📡 API & Socket Endpoints Summary

| Type | Endpoint / Destination | Method | Description |
| :--- | :--- | :---: | :--- |
| **REST API** | `/api/v1/user/updateBlockList` | `POST` | Update user block list (add/remove users from block set) |
| **STOMP Sub** | `/user/queue/notification/block` | `SUB` | Receive real-time notifications for block and unblock events |
| **STOMP Sub** | `/user/queue/notification/error` | `SUB` | Receive real-time error messages (e.g., blocked message attempt failures) |
| **STOMP Send** | `/app/chat/{channelId}` | `SEND` | Attempt chat message delivery to a target channel |

</details>

<a id="offline-notification-catch-up--pending-event-synchronization"></a>
### 🔔 Offline Notification Catch-Up & Pending Event Synchronization

<details>
<summary>Summary & Details</summary>

#### Use Case: Offline Recipient Pending Notification Retrieval

Allows users who were offline when a notification-generating event occurred (such as being blocked by another user) to retrieve all queued pending notifications via REST API upon reconnecting.

* **Primary Actors:** Authenticated Users (Offline Recipient, Active Sender)
* **Protocols:** REST API

---
#### 🧪 Integration test
 implementation here: https://github.com/soroushElz/ElzChat/blob/main/src/test/java/com/example/ChatApplication/WebSocketEndpointIT.java#L283-L325


#### 🔄 Execution Flow
<img width="600" height="750" alt="image" src="https://github.com/user-attachments/assets/1d5fd0c0-aed5-44bb-98bd-baa2df0e1cdc" />

---

#### 📡 API & Socket Endpoints Summary

| Type | Endpoint / Destination | Method | Description |
| :--- | :--- | :---: | :--- |
| **REST API** | `/api/v1/user/updateBlockList` | `POST` | Update user block list (triggers pending notification if target is offline) |
| **REST API** | `/api/v1/user/notification/pending` | `GET` | Fetch all pending notifications generated while the authenticated recipient was offline |

</details>

<a id="user-contact-list--active-chat-channel-retrieval"></a>
### 📱 User Contact List & Active Chat Channel Retrieval

<details>
<summary>Summary & Details</summary>

#### Use Case: Fetch Active User Chat Channels (Contact List)

Allows an authenticated user to retrieve a complete list of all active private chat channels (contacts) in which they are a participant via the REST API.

* **Primary Actors:** Authenticated Users
* **Protocols:** REST API
---
#### 🧪 Integration test
 implementation here: https://github.com/soroushElz/ElzChat/blob/main/src/test/java/com/example/ChatApplication/WebSocketEndpointIT.java#L330-L356

---

#### 🔄 Execution Flow
<img width="600" height="750" alt="image" src="https://github.com/user-attachments/assets/6ddb686a-9951-48da-a3ef-cb82421d7edf" />


#### 📡 API & Socket Endpoints Summary

| Type | Endpoint / Destination | Method | Description |
| :--- | :--- | :---: | :--- |
| **REST API** | `/api/v1/user/contacts` | `GET` | Retrieve all active private chat channels (contact list) associated with the authenticated user |

</details>

<a id="message-forwarding-across-channels"></a>
### ⏩ Message Forwarding Across Channels

<details>
<summary>Summary & Details</summary>

#### Use Case: Forward Messages Between Private Channels

Allows an authenticated user to forward existing messages from a source chat channel to a destination chat channel. The recipient in the destination channel receives the forwarded message in real-time.

* **Primary Actors:** Authenticated Users (Original Sender, Forwarder, Destination Recipient)
* **Protocols:** STOMP over WebSocket
---
#### 🧪 Integration test
 implementation here: https://github.com/soroushElz/ElzChat/blob/main/src/test/java/com/example/ChatApplication/WebSocketEndpointIT.java#L360-L399

#### 🔄 Execution Flow
<img width="600" height="750" alt="image" src="https://github.com/user-attachments/assets/e0b52283-198f-4536-9535-3953cec1b1f3" />


#### 📡 API & Socket Endpoints Summary

| Type | Endpoint / Destination | Method | Description |
| :--- | :--- | :---: | :--- |
| **STOMP Send** | `/app/chat/{destinationChannelId}/forward` | `SEND` | Forward existing messages from a source channel to a destination channel |
| **STOMP Sub** | `/user/topic/chat` | `SUB` | Receive real-time incoming messages, including forwarded messages |

</details>

<a id="filtered-message-search"></a>
### 🔍 Filtered Message Search

<details>
<summary>Summary & Details</summary>

#### Use Case: Search Channel Messages by Dynamic Criteria Filters

Allows an authorized channel member to search and filter chat history within a specific channel using dynamic criteria (keyword content, message writer_id, and date ). Access is restricted so that non-members cannot query channel history.

* **Primary Actors:** Authenticated Channel Members, Non-Members (Unauthorized)
* **Protocols:** REST API
----
#### 🧪 Integration test
 implementation here: https://github.com/soroushElz/ElzChat/blob/main/src/test/java/com/example/ChatApplication/WebSocketEndpointIT.java#L401-L463
 #### 📋 Detailed Steps

1. **Search Request Dispatch (Channel Member)**
   * **Submit Search:** User A sends a `POST` request to `/api/v1/chat/{channelId}/search` containing a payload array of `SearchFilters`.
   * **Authorization & Query Execution:** The server confirms User A is a member of `{channelId}`, builds a dynamic query using the provided filter criteria, and queries the channel message repos[...]
   * **Deliver Results:** The server returns `200 OK` with a `List<ChatMessageDto>` matching all requested filter constraints simultaneously (AND logic).

2. **Channel Authorization Enforcement (Non-Member)**
   * **Unauthorized Access Attempt:** User C (who is not a participant in `{channelId}`) sends a `POST` request to `/api/v1/chat/{channelId}/search`.
   * **Access Rejection:** The server detects that User C lacks channel membership and immediately returns a `403 Forbidden` response without exposing channel data.

---

#### 🛠️ Search Filter Mechanics Explained

The search endpoint accepts a list of filter objects (`List<SearchFilters>`) in the request body to construct dynamic JPA queries. Each filter object consists of three key properties:

* **`field` (`FieldFilter`):** Identifies the target database field to evaluate.
* **`value` (`String`):** The value or string criteria to match against.
* **`operation` (`Operation`):** The comparison operator used for filtering.

##### Filters 

* **Content Search (`CONTENT` | `LIKE`):**
  * Performs a partial text match/keyword search on the body of the message.
  * *Example:* Matches any message containing the substring `"text1..."`.
* **Sender Filtering (`WRITER_ID` | `EQ`):**
  * Performs an exact equality match (`EQ`) on the sender's user ID.
  * *Example:* Restricts search results exclusively to messages authored by a specific user.
* **Date Range Filtering (`TIME_SENT` | `DURING`,`LT`,`GT`):**
  * Evaluates timestamps against a date window (`DURING`).
  * *Example:* Matches messages sent within a specific 24-hour day window (between `00:00:00` and `23:59:59` of the target date).

</details>

<a id="group-lifecycle-management-creation-leave--deletion"></a>
### 👥 Group Lifecycle Management (Creation, Leave, & Deletion)

<details>
<summary>Summary & Details</summary>

#### Use Case: Create Group, Manage Member Departures, and Disband Group

Allows authenticated users to create multi-member group channels, allows group members to voluntarily leave an active group, and grants group administrators the authority to retrieve group details and manage membership.

* **Primary Actors:** Group Admin (Creator), Group Members
* **Protocols:** REST API

---
#### 🧪 Integration test
 implementation here: https://github.com/soroushElz/ElzChat/blob/main/src/test/java/com/example/ChatApplication/WebSocketEndpointIT.java#L466-L518

#### 🔄 Execution Flow
<img width="700" height="900" alt="image" src="https://github.com/user-attachments/assets/462eac4a-b5e8-493e-95f3-cc0a46c0a8c2" />

#### 📡 API & Socket Endpoints Summary

| Type | Endpoint / Destination | Method | Description |
| :--- | :--- | :---: | :--- |
| **REST API** | `/api/v1/group/create` | `POST` | Create a new group channel with designated initial members |
| **REST API** | `/api/v1/group/{groupId}/leave` | `PUT` | Leave an active group channel as a regular member |
| **REST API** | `/api/v1/group/{groupId}` | `GET` | Retrieve summary details and active member list of a group |
| **REST API** | `/api/v1/group/{groupId}` | `DELETE` | Permanently disband and delete a group channel (Admin only) |

</details>

This project uses integration tests to validate end-to-end behavior of the real-time messaging and offline catch-up flows. See the integration test method `testSendMessage_andRead_bySubscribers()` in [...]
