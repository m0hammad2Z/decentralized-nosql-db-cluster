# Decentralized NoSQL Database Cluster

## Introduction
This repository outlines the design and implementation details of a decentralized NoSQL database cluster built using Java. The system simulates the interaction between users and nodes within the cluster, providing functionalities such as user authentication, database queries, and data consistency across multiple nodes.

## How It Works

### Cluster Initialization
- **Bootstrapping Node**: Serves as the central point during cluster startup.
- **Node Registration**: Nodes register with the Bootstrapping Node, providing their hostname and API port.
- **Registry Maintenance**: The Bootstrapping Node maintains a registry of these registered Nodes.

### User Registration and Login
- **New User Registration**: Users first communicate with the Bootstrapping Node.
- **Load Balancing**: The Bootstrapping Node uses a round-robin strategy to assign users to specific Nodes.
- **Login Information**: The Bootstrapping Node retrieves and relays login information (URL and JWT token) to the user.

### User Interaction with Assigned Node
- **Connection**: Users connect to their assigned Node using the provided URL and JWT token via a REST API.
- **Database Operations**: Users perform data queries and CRUD operations directly with their assigned Node.
- **Authentication**: JWT tokens are included in request headers for authentication.

### Data Consistency
- **Data Replication**: Data is replicated across all Nodes in the cluster, ensuring each Node has a complete copy of the database.
- **Node Affinity for Writes**: Designated Nodes handle writes for specific documents.
- **Write Propagation**: Updates are broadcasted to all other Nodes to maintain consistency, though a brief window for eventual consistency exists.

## The Custom Broadcasting Library

### Library Components
- **Communication**: Manages low-level socket connections, including opening/closing connections and sending/receiving data.
- **Message and Topic Classes**: Encapsulate data being transmitted, with the Message class representing the data and the Topic class categorizing it for routing and processing.
- **ListenerManager Class**: Manages listeners for processing incoming messages, routing them based on their topic.
- **Broadcast Mechanisms**: Includes classes for broadcasting messages to all nodes, a random subset of nodes, or specific nodes.

### Library Usage
- **Message Sending**: Nodes create Message objects, assign a Topic, and use broadcast mechanisms to send messages.
- **Automatic Handling**: The library handles socket communication and ensures proper message delivery and error handling.

## Node Communication

### Peer-to-Peer Network
- **Equal Roles**: All Nodes have equal roles and responsibilities, eliminating single points of failure and enabling horizontal scalability.
- **Benefits**:
  - **Resilience**: Cluster remains operational even if a Node fails.
  - **Scalability**: Can accommodate more Nodes without significant performance degradation.

### Communication Mechanisms
- **Gossip Protocol**:
  - **Discovery and Information Sharing**: Nodes periodically exchange information with randomly chosen peers.
  - **Efficiency and Fault Tolerance**: Lightweight and efficient, suitable for large-scale distributed systems, and inherently fault-tolerant.

### Node Registration and Communication Flow
1. **Node Registration**:
   - New Nodes send their information to the Bootstrapping Node for validation and registration.
2. **Gossip Protocol**:
   - Bootstrapping Node broadcasts new Node information to existing Nodes.
   - Nodes propagate this information within the network.
3. **Node-to-Node Communication**:
   - **Discovery**: New Nodes register with existing Nodes via the gossip protocol.
   - **Data Consistency**: Gossip protocol propagates write operations to maintain data consistency.

## Node Implementation

### User Interaction through REST API
- **Authentication**: Verified using JWT tokens included in request headers.
- **CRUD Operations**: Nodes handle Create, Read, Update, and Delete operations on documents.

### Data Consistency Mechanisms
- **Data Replication**: Full replication across all Nodes.
- **Node Affinity for Writes**: Ensures designated Nodes handle specific document writes.
- **Write Propagation and Eventual Consistency**: Write operations propagated using the gossip protocol, with eventual consistency maintained.

### Indexing
- **B-Tree Data Structure**: Used for efficient indexing of JSON properties within documents.
- **Efficient Operations**: Supports efficient insertion, deletion, and search operations.
- **Index Creation**: Example provided for creating an index on the `age` property using `IndexService`.

### Data Versioning
- **Version Numbers**: Each document maintains a version number.
- **Write Verification**: Ensures versions match before proceeding with writes.
- **Concurrent Modification Handling**: Aborts and retries writes if versions mismatch, helping maintain data consistency.

---

Feel free to explore the repository for more detailed information on the design and implementation of the decentralized NoSQL database cluster. Contributions and feedback are welcome!

## License
MIT License [Link to MIT License](https://opensource.org/licenses/MIT)
