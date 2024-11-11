package org.os.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ExecutorService pool;

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    @Override
    public void run() {
        try {
            socket = new Socket("127.0.0.1", 8888);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            pool = Executors.newCachedThreadPool();
            pool.execute(new InputThread());
            while (true) {
                System.out.println(in.readLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void shutdown() {
        try {
            socket.close();
            in.close();
            out.close();
            pool.shutdown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    class InputThread implements Runnable {
        public void sendMessage(String msg) {
            out.println(msg);
        }

        @Override
        public void run() {
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    String message = input.readLine();
                    sendMessage(message);
                    if (message.equals("/quit")) {
                        shutdown();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
