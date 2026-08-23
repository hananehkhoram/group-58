package com.workshop.server;

import com.workshop.net.GameClient;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;

public final class ServerMain {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : GameClient.DEFAULT_PORT;
        Path storeFile = Path.of("server-data", "users.txt");
        UserStore store = new UserStore(storeFile);
        SessionManager sessions = new SessionManager();

        System.out.println("PVZ server listening on port " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket client = serverSocket.accept();
                Thread thread = new Thread(new ClientHandler(client, store, sessions));
                thread.setDaemon(true);
                thread.start();
                System.out.println("Client connected: " + client.getRemoteSocketAddress());
            }
        }
    }
}
