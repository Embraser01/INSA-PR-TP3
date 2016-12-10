import http.WebServer;

public class Main {

    /**
     * Start the application.
     *
     * @param args Command line arguments,
     *             First is the port to listen (default : 80)
     */
    public static void main(String args[]) {
        WebServer ws = new WebServer();
        ws.start(args.length > 0 ? Integer.parseInt(args[0]) : 80);
    }
}