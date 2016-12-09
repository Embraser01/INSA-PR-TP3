package http;


import java.io.BufferedReader;
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

    public static Request build(BufferedReader in) throws MalformedRequestException {

        try {
            // read the data sent. We basically ignore it,
            // stop reading once a blank line is hit. This
            // blank line signals the end of the client HTTP
            // headers.
            String str = ".";
            String[] requestLine = in.readLine().split(" ");


            // Headers

            String[] header;
            HashMap<String, String> headers = new HashMap<>();

            while (!str.equals("")) {
                str = in.readLine();

                if (!str.equals("")) {
                    header = str.split(":", 2);
                    headers.put(header[0].toLowerCase(), header[1].trim());
                }
            }

            // BODY

            int size = Integer.parseInt(headers.getOrDefault("content-size", "0"));

            char[] body = new char[size];

            if (in.read(body, 0, size) < size) throw new Exception();


            // Method, Path, Http version

            String method = requestLine[0].toUpperCase();
            String path;

            if (headers.containsKey("host")) {
                path = requestLine[1];
            } else {
                URL url = new URL(requestLine[1]);
                path = url.getPath() + "?" + url.getQuery();
            }

            // TODO Decode %0x..

            // PARAMS

            String[] parts = path.split("\\?", 2);

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

            return new Request(METHOD.valueOf(method), path, new String(body), headers, params);
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
