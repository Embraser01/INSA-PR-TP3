package http;

import java.util.HashMap;
import java.util.Map;

public class Response {


    private int statusCode;
    private String body;
    private HashMap<String, String> headers;


    public Response() {
        this(200);
    }

    public Response(int statusCode) {
        this.statusCode = statusCode;
        body = "";
        headers = new HashMap<>();
    }

    public void appendHeader(String key, String value) {
        headers.put(key, value);
    }

    public void appendBody(String part) {
        body += part;
    }

    @Override
    public String toString() {
        String res = "HTTP/1.0 " + statusCode + " " + statusCode + "\r\n";

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            res += entry.getKey() + ": " + entry.getValue() + "\r\n";
        }

        res += "\r\n";
        res += body.isEmpty() ? "Default Web Page" : body;

        return res;
    }
}
