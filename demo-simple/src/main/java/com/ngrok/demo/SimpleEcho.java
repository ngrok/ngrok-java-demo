package com.ngrok.demo;

import com.ngrok.Connection;
import com.ngrok.Session;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class SimpleEcho {
    public static void main(String[] args) throws Exception {
        var exec = Executors.newFixedThreadPool(10);

        try (var session = Session.withAuthtokenFromEnv().connect();
             var tunnel = session.tcpEndpoint().listen()) {
            System.out.println(tunnel.getId());
            System.out.println(tunnel.getForwardsTo());
            System.out.println(tunnel.getMetadata());
            System.out.println(tunnel.getUrl());
            System.out.println(tunnel.getProto());

            while (true) {
                var conn = tunnel.accept();
                System.out.printf("[%s] Accepted\n", conn.getRemoteAddr());

                exec.submit(() -> {
                    try {
                        handle(conn);
                    } catch (IOException exc) {
                        exc.printStackTrace();
                    }
                    System.out.printf("[%s] Done\n", conn.getRemoteAddr());
                });
            }
        }
    }

    public static void handle(Connection conn) throws IOException {
        var buf = ByteBuffer.allocateDirect(1024);

        try (conn) {
            while (true) {
                var readSz = conn.read(buf);
                System.out.printf("[%s] Read: %d\n", conn.getRemoteAddr(), readSz);

                System.out.println(StandardCharsets.UTF_8.decode(buf));
                conn.write(buf);

                buf.clear();
            }
        }
    }
}