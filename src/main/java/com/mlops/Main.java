package com.mlops;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;

public class Main {

    private static final int DEFAULT_PORT = 8080;

    // base URI includes the versioned path - @ApplicationPath on the Application class
    // defines the intent, but with Grizzly we set it explicitly here
    public static String BASE_URI = "http://localhost:8080/api/v1/";

    public static void main(String[] args) throws Exception {
        int port = resolvePort(args);
        BASE_URI = "http://localhost:" + port + "/api/v1/";

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create(BASE_URI),
                new MLOpsApplication()
        );

        // add a shutdown hook so Ctrl+C or kill gracefully stops the server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down server...");
            server.shutdownNow();
        }));

        System.out.println("===========================================");
        System.out.println("MLOps API server started!");
        System.out.println("Base URL: " + BASE_URI);
        System.out.println("Press Ctrl+C to stop the server.");
        System.out.println("===========================================");

        // keep the main thread alive until the JVM is killed
        Thread.currentThread().join();
    }

    private static int resolvePort(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--port=")) {
                return parsePort(arg.substring("--port=".length()));
            }
        }

        return findAvailablePort(DEFAULT_PORT);
    }

    private static int parsePort(String value) {
        try {
            int port = Integer.parseInt(value.trim());
            return port > 0 ? port : DEFAULT_PORT;
        } catch (NumberFormatException ex) {
            return DEFAULT_PORT;
        }
    }

    private static int findAvailablePort(int preferredPort) {
        try (ServerSocket socket = new ServerSocket(preferredPort)) {
            return preferredPort;
        } catch (IOException ignored) {
            try (ServerSocket socket = new ServerSocket(0)) {
                return socket.getLocalPort();
            } catch (IOException ex) {
                return preferredPort;
            }
        }
    }
}
