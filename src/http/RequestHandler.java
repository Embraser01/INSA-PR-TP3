package http;


import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;


/**
 * RequestHandler is the class that wait the {@link Request} from the client
 * and then build and send the corresponding {@link Response}
 *
 * @author Tristan Bourvon
 * @author Marc-Antoine FERNANDES
 * @version 1.0.0
 */
public class RequestHandler extends Thread {

    /**
     * Folder containing all the files to serve
     */
    private final static String WWW_FOLDER = "./www";

    /**
     * File to serve when the user ask for '/'
     */
    private final static String INDEX = "/index.html";

    /**
     * Canonical path to {@link #WWW_FOLDER}
     */
    private static String rootPath = RequestHandler.getRootPath();

    /**
     * Socket of the client
     */
    private Socket socket;

    /**
     * Input stream from the client
     */
    private BufferedReader in;

    /**
     * Output stream to the client (in bytes)
     */
    private BufferedOutputStream out;

    /**
     * Flag to check if we already treated the request
     */
    private boolean running = false;


    /**
     * Constructor of RequestHandler
     *
     * @param socket client Socket
     */
    RequestHandler(Socket socket) {
        this.socket = socket;

        try {
            this.in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));

            this.out = new BufferedOutputStream(
                    socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Thread start, the function build the request and then,
     * if the request is valid, handle it to finally send response to the user
     */
    @Override
    public void run() {
        running = true;

        try {

            Request req;
            Response res;

            try {

                // Bloc until full request
                req = Request.build(in);

                // Handle Request
                res = handleRequest(req);

            } catch (Request.MalformedRequestException ex) {
                res = new Response(400);
            }


            // Default HEADER

            res.appendHeader("Server", "Another Web Server");


            // Send Response

            out.write(res.getBytes());
            out.flush();

            // Close the connection
            socket.close();
        } catch (IOException e) {
            System.out.println("Client connection error : " + e.getMessage());
        }
    }

    /**
     * This function is called when a request is well formed.
     * It create a {@link Response} depending on the {@code req}
     *
     * @param req Request
     * @return complete Response (excepts server defaults headers)
     */
    private Response handleRequest(Request req) {

        Response res;
        Path path = getStaticFile(req.getPath());

        switch (req.getMethod()) {
            case GET:
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

            case HEAD:
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

            case POST:
                try {
                    if (path != null
                            && !Files.probeContentType(path).equals("text/html")) {

                        res = new Response();

                        res.appendHeader("Content-type", "text/html");
                        String body = String.join("", Files.readAllLines(path));

                        for (Map.Entry<String, String> entry : req.getParams().entrySet()) {
                            // {{ fieldName }}
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

            case PUT:
            case DELETE:
            case CONNECT:
            case OPTIONS:
            case TRACE:
            default:
                res = new Response(501, "Not implemented");
                break;
        }
        return res;
    }

    /**
     * Search the file and check if it's in {@link #WWW_FOLDER}. If it is, return the canonical path, Otherwise, return null
     *
     * @param path relative path to the resource
     * @return canonical path to the resource
     */
    private Path getStaticFile(String path) {
        File f = new File(WWW_FOLDER +
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

    /**
     * Helper function to send a short message to the client when the server is overloaded
     */
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

    /**
     * Return the canonical path of {@link #WWW_FOLDER}, if it doesn't exists, it stop the application
     *
     * @return canonical path
     */
    private static String getRootPath() {
        String path = "";
        try {
            File root = new File(WWW_FOLDER);
            path = root.getCanonicalPath();
            System.out.println("Content will be loaded from " + path);
        } catch (IOException e) {
            System.out.println("Unable to find WWW folder : " + e);
            System.exit(1);
        }
        return path;
    }
}
