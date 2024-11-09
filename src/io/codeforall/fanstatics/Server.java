package io.codeforall.fanstatics;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {

    // Define the port number to listen on (clients connect here)
    private static final int DEFAULT_PORT = 8080;
    private static final int THREAD_POOL_SIZE = 10; // Adjust based on your needs
    private final List<ServerWorker> clients;
    private final ExecutorService executorService;

    public Server(){
        this.clients = new ArrayList<>();
        // Create a fixed-size thread pool
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public void start() {
        // Create a server socket to listen for incoming connections on the specified port
        try {
            ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("Server is listening on port " + DEFAULT_PORT + "...");
            System.out.println("Waiting for connection...");

            while(true){
                // Wait for a client to connect
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Create new worker and submit to thread pool
                ServerWorker worker = new ServerWorker(clientSocket, clients);
                executorService.execute(worker);
            }
        } catch (IOException e) {
           System.err.println("Server error: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    public void shutdown(){
        // Proper shutdown of thread pool
        if (executorService != null && !executorService.isShutdown()){
            executorService.shutdown();

            // Wait for existing tasks to terminate
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)){
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e){
                executorService.shutdownNow();
            }
        }

        // Close all client connections
        synchronized (clients) {
            for (ServerWorker worker : clients) {
                try {
                    worker.closeConnection();
                } catch ( IOException e) {
                    System.err.println("Error closing client connection: " + e.getMessage());
                }
            }
            clients.clear();
        }
    }
}