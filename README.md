# Java Multi-threaded Chat Server

A robust, multi-threaded chat server implementation in Java that allows multiple clients to connect and communicate in real-time. The server supports both broadcast and private messaging capabilities, with a thread pool architecture for efficient client handling.

## Features

- Multi-client support using thread pool architecture
- Real-time message broadcasting
- Private messaging between users
- User management (username changes, listing connected users)
- Command-based interface
- Graceful shutdown handling

## Technical Architecture

The application consists of three main components:

1. **Main**: Entry point of the application that initializes and starts the server
2. **Server**: Manages incoming connections and maintains the thread pool
3. **ServerWorker**: Handles individual client connections and message processing

### Server Configuration

- Default Port: 8080
- Thread Pool Size: 10 concurrent connections
- Connection handling: Non-blocking using Java NIO

## Available Commands

Users can interact with the server using the following commands:

- `/w <username> <message>` - Send a private message to a specific user
- `/h` - Display help menu with available commands
- `/c` - Change username
- `/l` - List all connected users
- `/q` - Quit the chat server

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Java IDE (recommended: IntelliJ IDEA or Eclipse)

### Running the Server

1. Compile the Java files:
```bash
javac io/codeforall/fanstatics/*.java
```

2. Run the server:
```bash
java io.codeforall.fanstatics.Main
```

The server will start and listen for connections on port 8080.

### Connecting to the Server

Clients can connect to the server using any TCP client (such as Telnet or a custom client application):

```bash
telnet localhost 8080
```

## Implementation Details

### Thread Safety

The server implements several thread-safety measures:

- Synchronized access to the shared client list
- Thread pool management for controlled resource usage
- Proper connection cleanup on client disconnect

### Error Handling

The implementation includes:

- Graceful shutdown procedures
- Connection error handling
- Runtime exception management
- Resource cleanup in case of failures

## Contributing

Feel free to submit issues and enhancement requests!

## Future Enhancements

Potential improvements that could be added:

1. SSL/TLS support for secure communications
2. Persistent chat history
3. User authentication
4. Custom room creation
5. File sharing capabilities
