package com.ngrok.demo;

import com.ngrok.Session;
import org.eclipse.jetty.server.Server;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.LogManager;

public class JettyForward {
    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(
                JettyHello.LOG_CONFIG.getBytes(StandardCharsets.UTF_8)));

        var server = new Server(8080);
        server.setHandler(new JettyHello());

        new Thread(() -> {
            try {
                tunnel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        server.start();
        server.join();
    }

    private static void tunnel() throws Exception {
        var sb = Session.withAuthtokenFromEnv()
                .addClientInfo("jetty-forward", "0.1.0");

        try (var session = Session.connect(sb);
             var tunnel = JettyHello.agentTunnel(session).forward(new URL("http://127.0.0.1:8000"))) {
            tunnel.join();
        }
    }
}
