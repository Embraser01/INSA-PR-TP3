package http;


import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class RequestHandler extends Thread {

    private final static String HTML_FOLDER = "./www";
    private final static String INDEX = "/index.html";

    private static String rootPath = null;

    private Socket socket;

    private BufferedReader in;
    private BufferedOutputStream out;

    private boolean running = false;


    public RequestHandler(Socket socket) {
        this.socket = socket;
        try {
            this.in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));

            this.out = new BufferedOutputStream(
                    socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If this is the first request, we find the root path for www data
        if (rootPath == null) {

            File root = new File(HTML_FOLDER);
            try {
                rootPath = root.getCanonicalPath();
                System.out.println("Load content from " + rootPath);
            } catch (IOException e) {
                System.out.println("Error: " + e);
            }
        }
    }

    @Override
    public void run() {

        running = true;
        try {

            Request req;
            Response res;

            try {
                req = Request.build(in); // Bloc until full request

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
            socket.close();
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    private Response handleRequest(Request req) {

        Response res = null;
        Path path;

        switch (req.getMethod()) {

            case GET:

                // STATIC
                path = getStaticFile(req.getPath());
                try {
                    if (path != null) {

                        res = new Response();
                        res.appendHeader("Content-type", Files.probeContentType(path));
                        res.appendBody(Files.readAllBytes(path));


                    } else {
                        res = Response.notFound();
                    }
                } catch (IOException e) {
                    res = Response.notFound();
                }
                break;
            case POST:
                // STATIC

                path = getStaticFile(req.getPath());

                try {
                    if (path != null
                            && !Files.probeContentType(path).equals("text/html")) {


                        res = new Response();

                        res.appendHeader("Content-type", "text/html");
                        String body = String.join("", Files.readAllLines(path));

                        for (Map.Entry<String, String> entry : req.getParams().entrySet()) {
                            // {{ username }}
                            body = body.replaceAll("\\{\\{\\s?" + entry.getKey() + "\\s?}}", entry.getValue());
                        }
                        res.appendBody(body);

                    } else {
                        res = Response.notFound();
                    }
                } catch (IOException e) {
                    res = Response.notFound();
                }
                break;
            case HEAD:
                // STATIC
                path = getStaticFile(req.getPath());
                try {
                    if (path != null) {

                        res = new Response();
                        res.appendHeader("Content-type", Files.probeContentType(path));

                    } else {
                        res = Response.notFound();
                    }
                } catch (IOException e) {
                    res = Response.notFound();
                }
                break;

            case PUT:
            case DELETE:
                res = new Response(501, "Not yet implemented");
                break;
        }
        return res;
    }

    public void sendOverloadMsg() {
        if (running) {
            System.out.println("ERROR : Thread is running where it should not");
        } else {
            Response res = new Response(500, "The server is currently overload, try again later !");
            try {
                out.write(res.getBytes());
                out.flush();
            } catch (IOException e) {
                System.out.println("Unable to send data");
            }
        }
    }

    private Path getStaticFile(String path) {
        File f = new File(HTML_FOLDER +
                (path.equals("/") ?
                        INDEX :
                        path));

        try {
            if (f.exists() && f.isFile() && f.getCanonicalPath().startsWith(rootPath) && f.canRead()) {
                return Paths.get(f.getCanonicalPath());
            }
        } catch (IOException ignored) {
        }
        return null;
    }
}
