///A Simple Web Server (WebServer.java)

package http;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Example program from Chapter 1 Programming Spiders, Bots and Aggregators in
 * Java Copyright 2001 by Jeff Heaton
 * <p>
 * WebServer is a very simple web-server. Any request is responded with a very
 * simple web-page.
 *
 * @author Jeff Heaton
 * @version 1.0
 */
public class WebServer {

    private static final int THREAD_POOL_CORE_SIZE = 10;
    private static final int THREAD_POOL_MAX_SIZE = 30;
    private static final int REQUEST_TIMEOUT = 20;

    /**
     * WebServer constructor.
     */
    public void start() {
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

        System.out.println("Webserver starting up on port 80");
        System.out.println("(press ctrl-c to exit)");
        try {
            // create the main server socket
            s = new ServerSocket(80);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return;
        }

        System.out.println("Waiting for connection");

        for (; ; ) {
            try {
                // wait for a connection
                Socket remote = s.accept();
                // remote is now the connected socket
                poolExecutor.execute(new RequestHandler(remote));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
