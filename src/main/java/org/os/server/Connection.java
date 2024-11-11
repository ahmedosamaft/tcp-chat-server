package org.os.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;

public class Connection implements Runnable {
    private Socket client;

    public String getName() {
        return name;
    }

    private String name;
    private BufferedReader in;
    private PrintWriter out;
    private Server server;

    public Connection(Server server, Socket client) {
        this.client = client;
        this.server = server;
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Client " + client + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void close() {
        try {
            if (!client.isClosed()) {
                in.close();
                out.close();
                client.close();
                server.removeConnection(this);
                server.broadcast("Client " + name + " quited.");
                System.out.println("Client " + name + " quited.");
            }
        } catch (IOException e) {
            System.out.println("Client " + client + ": Error while closing connection.\n" + e.getMessage());
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        try {
            String message;
            out.println("Please enter your name: ");
            out.flush();
            name = in.readLine();
            server.broadcast("Client " + name + " connected");
            System.out.printf("Client %s connected%n", name);
            while ((message = in.readLine()) != null) {
                if (message.equals("/quit")) {
                    this.close();
                    return;
                } else if (message.startsWith("/rename ")) {
                    String[] parts = message.split(" ", 2);
                    server.broadcast("Client " + name + " renamed to " + parts[1] + ".");
                    setName(parts[1]);
                }
                server.broadcast("Client " + name + ": " + message);
            }
        } catch (IOException e) {
            close();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return Objects.equals(client, that.client);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(client);
    }
}