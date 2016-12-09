package http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Response {


    private int statusCode;
    private ByteArrayOutputStream body;
    private HashMap<String, String> headers;


    public Response() {
        this(200);
    }

    public Response(String message) {
        this(200, message);
    }

    public Response(int statusCode) {
        this(statusCode, "");
    }

    public Response(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = new ByteArrayOutputStream();
        this.headers = new HashMap<>();

        try {
            this.body.write(body.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void appendHeader(String key, String value) {
        headers.put(key, value);
    }

    public void appendBody(String part) {
        try {
            body.write(part.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void appendBody(byte[] bytes) {
        try {
            this.body.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getBytes() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        String res = "HTTP/1.0 " + statusCode + " " + statusCode + "\r\n";

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            res += entry.getKey() + ": " + entry.getValue() + "\r\n";
        }

        res += "\r\n";

        try {
            byteArrayOutputStream.write(res.getBytes());
            byteArrayOutputStream.write(body.size() == 0 ? "Default Web Page".getBytes() : body.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return byteArrayOutputStream.toByteArray();
    }
}
