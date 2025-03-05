package main;

import java.io.*;
import java.net.*;
import java.util.*;

public class MapServer {
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private boolean running = true;

    public MapServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        new Thread(this::acceptClients).start();
    }

    private void acceptClients() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);
                new Thread(handler).start();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void broadcast(String message) {
        synchronized(clients) {
            for (ClientHandler handler : clients) {
                handler.sendMessage(message);
            }
        }
    }

    public void stop() {
        running = false;
        try {
            serverSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final PrintWriter out;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            out = new PrintWriter(socket.getOutputStream(), true);
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String line;
                // In questa versione non elaboriamo messaggi in arrivo dal client
                while ((line = in.readLine()) != null) {
                    // eventuale logica futura per messaggi dal client
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException ex) {}
                clients.remove(this);
            }
        }
    }
}
