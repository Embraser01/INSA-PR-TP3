package http;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * WebServer is the core of the application.
 * This class start listening to incoming connection when
 * {@link #start(int)} is called.
 * Each request is traited in a different Thread, managed by a {@link ThreadPoolExecutor}
 *
 * @author Tristan Bourvon
 * @author Marc-Antoine FERNANDES
 * @version 1.0.0
 */
public class WebServer {

    /**
     * Number of Thread executed simultaneously
     */
    private static final int THREAD_POOL_CORE_SIZE = 10;

    /**
     * Number max of Thread in the ThreadPool
     */
    private static final int THREAD_POOL_MAX_SIZE = 30;

    /**
     * Time in seconds before a request timeout
     */
    private static final int REQUEST_TIMEOUT = 20;

    /**
     * Method to start the application
     *
     * @param port Port to listen to
     */
    public void start(int port) {
        ServerSocket s;

        // Thread Pool initialisation

        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(THREAD_POOL_CORE_SIZE,
                THREAD_POOL_MAX_SIZE,
                REQUEST_TIMEOUT,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(THREAD_POOL_MAX_SIZE - THREAD_POOL_CORE_SIZE),
                Executors.defaultThreadFactory(),
                (r, executor) -> {
                    System.out.println(r.toString() + " is rejected, sending message");
                    ((RequestHandler) r).sendOverloadMsg();
                }
        );


        // Starting the server

        System.out.printf("Webserver starting up on port %d", port);
        System.out.println("(Press Ctrl-C to exit)");

        try {
            // Create the main server socket
            s = new ServerSocket(port);
        } catch (Exception e) {
            System.out.println("Error on WebServer#start(): " + e);
            return;
        }

        System.out.printf("Listening HTTP Request on port %d", port);

        //noinspection InfiniteLoopStatement
        for (; ; ) {
            try {
                // wait for a connection
                Socket remote = s.accept();
                // remote is now the connected socket
                poolExecutor.execute(new RequestHandler(remote));

            } catch (IOException ignored) {
            }
        }
    }
}
