package com.ngrok.demo;

import com.ngrok.HttpTunnel;
import com.ngrok.LabeledTunnel;
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
                        java.util.logging.ConsoleHandler.level=FINE
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

        var sb = Session.newBuilder()
                .addUserAgent("jetty-demo", "0.1.0")
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
                    return s.httpTunnel(agentTunnel());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
            server.addConnector(new NgrokConnector(server, sessionFunc, (s) ->
            {
                try {
                    return s.labeledTunnel(edgeTunnel());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));

            server.start();
            server.join();
        }
    }

    static HttpTunnel.Builder agentTunnel() {
        return new HttpTunnel.Builder()
                .domain("ngrok-java-test.ngrok.io")
                .forwardsTo("jetty")
                .metadata("hello from agent jetty")
                .oauthOptions(new HttpTunnel.OAuthOptions("google"));
    }

    static LabeledTunnel.Builder edgeTunnel() {
        return new LabeledTunnel.Builder()
                .metadata("hello from edge jetty")
                .label("edge", "edghts_2LkMiSRTOuR4rnYck8PFZ9kYYYZ");
    }
}
