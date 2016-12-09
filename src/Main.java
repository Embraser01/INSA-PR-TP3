import http.WebServer;

public class Main {

    /**
     * Start the application.
     *
     * @param args
     *            Command line parameters are not used.
     */
    public static void main(String args[]) {
        WebServer ws = new WebServer();
        ws.start();
    }
}