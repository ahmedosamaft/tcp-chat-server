package org.os.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private ServerSocket socket;
    private Set<Connection> connections;
    private ExecutorService pool;

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }

    public void removeConnection(Connection connection) {
        connections.remove(connection);
    }

    public void broadcast(String message) {
        for (Connection connection : connections) {
            if (connection != null) {
                connection.sendMessage(message);
            }
        }
    }

    public void shutdown() {
        for (Connection connection : connections) {
            if (connection != null) {
                connection.close();
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        pool.shutdown();
    }


    @Override
    public void run() {
        try {
            socket = new ServerSocket(8888);
            System.out.println("Server listening on port 8888");
            pool = Executors.newCachedThreadPool();
            connections = new HashSet<>();
            while (true) {
                Socket client = socket.accept();
                Connection connection = new Connection(this, client);
                connections.add(connection);
                pool.execute(connection);
            }
        } catch (Exception e) {
            e.printStackTrace();
            shutdown();
        }
    }
}