package http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Representation of a Response.
 * Also contains some defaults responses
 *
 * @author Tristan Bourvon
 * @author Marc-Antoine FERNANDES
 * @version 1.0.0
 */
public class Response {

    /**
     * HTTP Status code
     */
    private int statusCode;

    /**
     * Response body (bytes to be able to send raw data)
     */
    private ByteArrayOutputStream body;

    /**
     * Headers of the response
     */
    private HashMap<String, String> headers;


    /**
     * Constructor by default, set a empty OK reponse (statusCode 200)
     */
    public Response() {
        this(200);
    }

    /**
     * Constructor with a specific {@code statusCode} and an empty {@link #body}
     *
     * @param statusCode see {@link #statusCode}
     */
    public Response(int statusCode) {
        this(statusCode, "");
    }

    /**
     * Constructor with specific {@code statusCode} and {@code body}
     *
     * @param statusCode see {@link #statusCode}
     * @param body       see {@link #body}
     */
    public Response(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = new ByteArrayOutputStream();
        this.headers = new HashMap<>();

        try {
            // Transform the string in bytes
            this.body.write(body.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add new header to the response
     *
     * @param key   header name
     * @param value header value
     */
    public void appendHeader(String key, String value) {
        headers.put(key, value);
    }

    /**
     * Add a string to the body
     *
     * @param part data
     */
    public void appendBody(String part) {
        try {
            body.write(part.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add raw data to the body
     *
     * @param bytes data
     */
    public void appendBody(byte[] bytes) {
        try {
            this.body.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return a fully formed response
     *
     * @return response
     */
    public byte[] getBytes() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // First raw
        String res = "HTTP/1.1 " + statusCode + " \r\n";

        // Headers
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            res += entry.getKey() + ": " + entry.getValue() + "\r\n";
        }

        // End of headers
        res += "\r\n";

        // Body
        try {
            byteArrayOutputStream.write(res.getBytes());
            byteArrayOutputStream.write(body.size() == 0 ? "Default Web Page".getBytes() : body.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // EOF
        return byteArrayOutputStream.toByteArray();
    }


    /**
     * Default Not found Response
     *
     * @return Not Found Response
     */
    public static Response notFound() {
        return new Response(404, "Not Found");
    }
}
