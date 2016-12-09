///A Simple Web Server (WebServer.java)

package http;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

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

    private static String HTML_FOLDER = "./www";
    private static String INDEX = "/index.html";

    private String rootPath = null;

    /**
     * WebServer constructor.
     */
    public void start() {
        ServerSocket s;

        System.out.println("Webserver starting up on port 80");
        System.out.println("(press ctrl-c to exit)");
        try {
            // create the main server socket
            s = new ServerSocket(80);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return;
        }

        //
        File root = new File(HTML_FOLDER);
        try {
            rootPath = root.getCanonicalPath();
            System.out.println("Load content from " + rootPath);
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }


        System.out.println("Waiting for connection");
        for (; ; ) {
            try {
                // wait for a connection
                Socket remote = s.accept();
                // remote is now the connected socket
                System.out.println("Connection, sending data.");
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        remote.getInputStream()));

                BufferedOutputStream out = new BufferedOutputStream(remote.getOutputStream());

                Request req;
                Response res;

                try {
                    req = Request.build(in); // Bloc until ful request

                    // Handle Request

                    res = handleRequest(req);
                } catch (Request.MalformedRequestException ex) {
                    res = new Response(400);
                }

                if (res != null) {
                    res.appendHeader("Server", "BADASS");
                    out.write(res.getBytes());
                    out.flush();
                }
                remote.close();
            } catch (Exception e) {
                System.out.println("Error: " + e);
            }
        }
    }


    private Response handleRequest(Request req) {

        Response res = null;
        File f = null;


        switch (req.getMethod()) {

            case GET:

                // STATIC

                f = new File(HTML_FOLDER +
                        (req.getPath().equals("/") ?
                                INDEX :
                                req.getPath()));

                try {
                    if (f.exists() && f.isFile() && f.getCanonicalPath().startsWith(rootPath) && f.canRead()) {

                        Path path = Paths.get(f.getCanonicalPath());
                        res = new Response();

                        res.appendHeader("Content-type", Files.probeContentType(path));
                        res.appendBody(Files.readAllBytes(path));

                    } else {
                        res = new Response(404, "Not Found");
                    }
                } catch (IOException e) {
                    res = new Response(404, "Not Found");
                }
                break;
            case POST:
                // STATIC

                f = new File(HTML_FOLDER +
                        (req.getPath().equals("/") ?
                                INDEX :
                                req.getPath()));

                try {
                    if (f.exists() && f.isFile() && f.getCanonicalPath().startsWith(rootPath) && f.canRead()) {

                        Path path = Paths.get(f.getCanonicalPath());

                        if (!Files.probeContentType(path).equals("text/html")) throw new Exception();

                        res = new Response();

                        res.appendHeader("Content-type", "text/html");
                        String body = String.join("", Files.readAllLines(path));

                        for (Map.Entry<String, String> entry : req.getParams().entrySet()) {
                            // {{username}}
                            body = body.replace("{{" + entry.getKey() + "}}", entry.getValue());
                        }
                        res.appendBody(body);

                    } else {
                        res = new Response(404, "Not Found");
                    }
                } catch (Exception e) {
                    res = new Response(404, "Not Found");
                }
                break;
            case HEAD:
            case PUT:
            case DELETE:
                res = new Response(501, "Not yet implemented");
                break;
        }
        return res;
    }
}
