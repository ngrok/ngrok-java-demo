package com.ngrok.demo;

import com.ngrok.Session;
import com.ngrok.net.TunnelServerSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

public class SocketEcho {
    public static void main(String[] args) throws Exception {
        var exec = Executors.newFixedThreadPool(10);

        var session = Session.connect(Session.newBuilder().metadata("abc"));
        try (var tunnel = session.tcpTunnel()) {
            System.out.println(tunnel.getUrl());

            ServerSocket server = new TunnelServerSocket(tunnel);
            while (true) {
                Socket socket = server.accept();
                System.out.printf("[%s] Accepted\n", socket.getInetAddress());

                exec.submit(() -> {
                    try {
                        handle(socket);
                    } catch (IOException exc) {
                        exc.printStackTrace();
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                    System.out.printf("[%s] Done\n", socket.getInetAddress());
                });
            }
        }
    }

    public static void handle(Socket socket) throws IOException {
        var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        var writer = new OutputStreamWriter(socket.getOutputStream());

        while (true) {
            var l = reader.readLine();
            System.out.println(l);
            writer.write(l + "\n");
            writer.flush();
        }
    }
}
