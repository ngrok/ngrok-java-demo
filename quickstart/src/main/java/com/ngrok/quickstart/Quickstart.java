package com.ngrok.quickstart;

import com.ngrok.Session;
import com.ngrok.Http;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Quickstart {
    public static void main(String[] args) throws IOException {
        // Session.withAuthtokenFromEnv() will create a new session builder, pulling NGROK_AUTHTOKEN env variable.
        // You can get your authtoken by registering at https://dashboard.ngrok.com
        var sessionBuilder = Session.withAuthtokenFromEnv().metadata("my session");
        // Session.Builder let you customize different aspects of the session, see docs for details.
        // After customizing the builder, you connect:
        try (var session = sessionBuilder.connect()) {
            // Creates and configures http listener that will be using oauth to secure it
            var listenerBuilder = session.httpEndpoint().metadata("my listener")
                    .oauthOptions(new Http.OAuth("google"));
            // Now start listening with the above configuration
            try (var listener = listenerBuilder.listen()) {
                System.out.println("ngrok url: " + listener.getUrl());
                var buf = ByteBuffer.allocateDirect(1024);

                while (true) {
                    // Accept a new connection
                    var conn = listener.accept();

                    // Read from the connection
                    conn.read(buf);

                    System.out.println(buf.asCharBuffer());

                    // Or write to it
                    conn.write(buf);
                }
            }
        }
    }
}
