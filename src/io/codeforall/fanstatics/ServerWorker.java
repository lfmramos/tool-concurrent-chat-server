package io.codeforall.fanstatics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

/**
 * ServerWorker class handles individual client connections in a chat server.
 * It implements the Runnable interface to handle each client in a separate thread.
 */
public class ServerWorker implements Runnable {

    private final Socket clientSocket; // Socket connection to the client
    private PrintWriter out; // Output stream to send messages to client
    private String clientName; // Username of the connected client
    private final List<ServerWorker> clients; // Shared list of all connected clients
    private volatile boolean running = true; // Control the worker lifecycle

    /**
     * Constructor initializes the worker with client socket and shared client list
     */
    public ServerWorker(Socket clientSocket, List<ServerWorker> clients) {
        this.clientSocket = clientSocket;
        this.clients = clients;
    }

    @Override
    public void run() {
        try {
            // Create a reader to receive data from the client
            // Initialize input/output streams for communication
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Get client's username and send welcome message
            out.println("Please, insert your name: ");
            clientName = in.readLine();
            out.println("Welcome " + clientName + "! You are connected. \n Type /h to see a list of available commands. \n");

            // Add this client to the shared clients list (thread-safe)
            synchronized (clients) {
                clients.add(this);
            }

            // Main message processing loop
            String message;
            while ((message = in.readLine()) != null) {
                // Process different commands based on message prefix
                if (message.startsWith("/q")) { // Quit command
                    break;
                } else if (message.startsWith("/w")) { // Whisper (private message)
                    handleWhisper(message);
                } else if (message.startsWith("/l")) { // List users
                    listUsers();
                } else if (message.startsWith("/c")) { // Change username
                    changeName();
                } else if (message.startsWith("/h")) { // Help command
                    helpHandler();
                } else { // Regular broadcast message
                    broadcast(clientName + ": " + message, this);
                }
            }
        } catch (IOException e) {
            System.err.println("Error in worker thread: " + e.getMessage());
        } finally {
            try {
                closeConnection();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void closeConnection() throws IOException {
        running = false;
            // Clean up when client disconnects
                synchronized (clients) {
                    clients.remove(this);
                    broadcast(clientName + " has left the chat.", this);
                }
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            }

    /**
     * Displays available commands to the client
     */
    private void helpHandler() {
        out.println(" /w - Sends a private message for a specific user.");
        out.println(" /h - Show available commands.");
        out.println(" /c - Changes the username.");
        out.println(" /l - Lists all connected users.");
        out.println(" /q - Leaves the chat.");
    }

    /**
     * Allows client to change their username
     */
    private void changeName() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out.println("Type your name: ");
            clientName = in.readLine();
            out.println("New username: " + clientName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Broadcasts a message to all clients except the sender
     * @param message Message to broadcast
     * @param sender Client who sent the message
     */
    private void broadcast(String message, ServerWorker sender) {
        synchronized (clients) {
            for (ServerWorker client : clients) {
                if (client != sender) {
                    client.out.println(message);
                }
            }
        }
    }

    /**
     * Handles private messages between clients
     * Format: /w <targetUsername> <message>
     */
    private void handleWhisper(String message) {
        String[] tokens = message.split(" ", 3);
        if (tokens.length < 3) {
            out.println("Correct use: /w <name> <message>");
            return;
        }

        String targetName = tokens[1];
        String privateMessage = tokens[2];
        ServerWorker targetClient = null;

        // Find the target client by username
        synchronized (clients) {
            for (ServerWorker client : clients) {
                if (client.clientName.equals(targetName)) {
                    targetClient = client;
                    break;
                }
            }
        }

        // Send private message if target client exists
        if (targetClient != null) {
            targetClient.out.println("[Whisper from " + clientName + "]: " + privateMessage);
        } else {
            out.println("User " + targetName + "not found.");
        }
    }

    /**
     * Lists all currently connected users
     */
    private void listUsers() {
        out.println("Connected users: ");
        synchronized (clients) {
            for (ServerWorker client : clients) {
                out.println("- " + client.clientName);
            }
        }
    }
}