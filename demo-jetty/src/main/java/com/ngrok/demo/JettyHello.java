package com.ngrok.demo;

import com.ngrok.EdgeBuilder;
import com.ngrok.HttpBuilder;
import com.ngrok.Http;
import com.ngrok.Session;
import com.ngrok.jetty.NgrokConnector;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import java.util.logging.LogManager;

public class JettyHello extends AbstractHandler {
    static String LOG_CONFIG = """
                        .level=ALL
                        handlers=java.util.logging.ConsoleHandler
                        java.util.logging.ConsoleHandler.level=INFO
                        java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter
                        java.util.logging.SimpleFormatter.format=[%1$tc] %4$s: {%2$s} %5$s%n
                """;

    @Override
    public void handle(String s, Request baseReq, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=utf-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        baseReq.setHandled(true);
        resp.getWriter().println("<h1>Hello Ngrok Java</h1>");
    }

    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(
                JettyHello.LOG_CONFIG.getBytes(StandardCharsets.UTF_8)));

        var server = new Server(8080);
        server.setHandler(new JettyHello());

        var sb = Session.withAuthtokenFromEnv()
                .addClientInfo("jetty-hello", "0.1.0")
                .stopCallback(() -> {
                    System.out.println("server stop");
                }).restartCallback(() -> {
                    System.out.println("server restart");
                }).updateCallback(() -> {
                    System.out.println("server update");
                }).heartbeatHandler((latency) -> {
                    System.out.println("heartbeat: " + latency + "ms");
                });

        try (var session = Session.connect(sb)) {
            Supplier<Session> sessionFunc = () -> session;

            server.addConnector(new NgrokConnector(server, sessionFunc, (s) ->
            {
                try {
                    return agentTunnel(session).listen();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
            server.addConnector(new NgrokConnector(server, sessionFunc, (s) ->
            {
                try {
                    var l = edgeTunnel(session).listen();
                    System.out.println("labels: " + l.getLabels());
                    return l;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));

            server.start();
            server.join();
        }
    }

    static HttpBuilder agentTunnel(Session session) {
        return session.httpEndpoint()
                .domain("ngrok-java-test.ngrok.io")
                .forwardsTo("jetty")
                .metadata("hello from agent jetty")
                .oauthOptions(new Http.OAuth("google"));
    }

    static EdgeBuilder edgeTunnel(Session session) {
        return session.edge()
                .metadata("hello from edge jetty")
                .label("edge", "edghts_2VwaZ0b1ef23gpfCMyrdDvHIlEj");
    }
}
