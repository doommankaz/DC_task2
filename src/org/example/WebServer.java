package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Simple web server.
 */
public class WebServer {
    public static void main(String[] args) throws Exception {
        // Port number for http request
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8080;
        // The maximum queue length for incoming connection
        int queueLength = args.length > 2 ? Integer.parseInt(args[2]) : 50;

        ThreadPool threadPool = new ThreadPool(1, 50);

        try (ServerSocket serverSocket = new ServerSocket(port, queueLength)) {
            System.out.println("Web Server is starting up, listening at port " + port + ".");
            System.out.println("You can access http://localhost:" + port + " now.");

            while (true) {
                // Make the server socket wait for the next client request
                Socket socket = serverSocket.accept();
                System.out.println("Got connection!");

                // To read input from the client
                BufferedReader input = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                // Get request
                HttpRequest request = HttpRequest.parse(input);

                Processor proc = new Processor(socket, request);
                threadPool.execute( () -> {
                    try {
                        proc.process();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    String message = Thread.currentThread().getName() + ": Task " + request ;
                    System.out.println(message);
                });
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            System.out.println("Server has been shutdown!");
            threadPool.waitUntilAllTasksFinished();
            threadPool.stop();
        }
    }
}