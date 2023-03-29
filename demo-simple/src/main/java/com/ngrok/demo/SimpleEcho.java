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

        var session = Session.connect(Session.newBuilder());
        try (var tunnel = session.tcpTunnel()) {
            System.out.println(tunnel.id());
            System.out.println(tunnel.forwardsTo());
            System.out.println(tunnel.metadata());
            System.out.println(tunnel.url());
            System.out.println(tunnel.proto());

            while (true) {
                var conn = tunnel.accept();
                System.out.printf("[%s] Accepted\n", conn.remoteAddr());

                exec.submit(() -> {
                    try {
                        handle(conn);
                    } catch (IOException exc) {
                        exc.printStackTrace();
                    }
                    System.out.printf("[%s] Done\n", conn.remoteAddr());
                });
            }
        }
    }

    public static void handle(Connection conn) throws IOException {
        var buf = ByteBuffer.allocateDirect(1024);

        try (conn) {
            while (true) {
                var readSz = conn.read(buf);
                System.out.printf("[%s] Read: %d\n", conn.remoteAddr(), readSz);

                System.out.println(StandardCharsets.UTF_8.decode(buf));
                conn.write(buf);

                buf.clear();
            }
        }
    }
}