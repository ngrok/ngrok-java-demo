package com.ngrok.quickstart;

import com.ngrok.Http;
import com.ngrok.Session;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Quickstart {
    public static void main(String[] args) throws IOException {
        // Session.withAuthtokenFromEnv() will create a new session builder, pulling NGROK_AUTHTOKEN env variable.
        // You can get your authtoken by registering at https://dashboard.ngrok.com
        var sessionBuilder = Session.withAuthtokenFromEnv().metadata("my session");
        // Session.Builder lets you customize different aspects of the session, see docs for details.
        // After customizing the builder, you connect:
        try (var session = sessionBuilder.connect()) {
            // Creates and configures http listener that will be using oauth to secure it
            var listenerBuilder = session.httpEndpoint().metadata("my listener")
                    .oauthOptions(new Http.OAuth("google"));
            // Now start listening with the above configuration
            try (var listener = listenerBuilder.listen()) {
                // Print the url that was assigned for this listener
                System.out.println("ngrok url: " + listener.getUrl());
                var buf = ByteBuffer.allocateDirect(1024);

                while (true) {
                    // Wait for a new connection
                    var conn = listener.accept();
                    // Print the address of the client connecting to this listener
                    System.out.println("\nnew connection from: " + conn.getRemoteAddr());

                    // Read the request from the connection
                    buf.clear();
                    conn.read(buf);
                    // Print the incoming http request
                    System.out.println(StandardCharsets.UTF_8.decode(buf));

                    // And write back a canned response
                    conn.write(response);
                    conn.close();
                }
            }
        }
    }

    private static final ByteBuffer response = ByteBuffer.allocateDirect(1024);
    static {
        response.put("HTTP/1.0 200 OK\n\nHello from ngrok!".getBytes(StandardCharsets.UTF_8));
        response.flip();
    }
}
