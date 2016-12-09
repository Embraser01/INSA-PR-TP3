package http;


import java.util.HashMap;
import java.net.URL;

public class Request {


    public enum METHOD {GET, POST, HEAD, PUT, DELETE}

    private METHOD method;
    private String path;
    private String body;

    private HashMap<String, String> params;
    private HashMap<String, String> headers;


    private Request(METHOD method, String path, String body, HashMap<String, String> headers, HashMap<String, String> params) {
        this.method = method;
        this.path = path;
        this.body = body;
        this.params = params;
    }

    public static Request build(String request) throws MalformedRequestException {

        try {
            String[] requestLine;
            String[] parts = request.split("\r\n\r\n");
            String[] lines = parts[0].split("\r\n");
            String body = parts[1];

            String[] header;

            // Headers

            HashMap<String, String> headers = new HashMap<>(lines.length - 1);

            for (int i = 1; i < lines.length; i++) {
                header = lines[i].split(":", 2);
                headers.put(header[0].toLowerCase(), header[1].trim());
            }

            // Method, Path, Http version

            requestLine = lines[0].split(" ");

            String method = requestLine[0].toUpperCase();
            String path;

            if (headers.containsKey("host")) {
                path = requestLine[1];
            } else {
                URL url = new URL(requestLine[1]);
                path = url.getPath() + "?" + url.getQuery();
            }

            // TODO Decode %0x..

            parts = path.split("\\?", 2);

            HashMap<String, String> params = null;

            if (parts.length > 1) {
                String[] paramList = parts[1].split("&");
                String[] param;

                params = new HashMap<>(paramList.length);

                for (String aParamList : paramList) {
                    param = aParamList.split("=", 2);
                    params.put(param[0], param[1]);
                }
            }

            return new Request(METHOD.valueOf(method), path, body, headers, params);

        } catch (Exception e) {
            throw new MalformedRequestException();
        }
    }

    public METHOD getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getBody() {
        return body;
    }

    public String getParams(String key) {
        return params.get(key);
    }

    public String getHeaders(String key) {
        return headers.get(key);
    }

    public static class MalformedRequestException extends Exception {

    }
}
