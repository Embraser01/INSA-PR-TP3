package http;


import java.io.BufferedReader;
import java.util.HashMap;
import java.net.URL;


/**
 * Representation of a Request.
 * Can also build a Request from an input stream
 *
 * @author Tristan Bourvon
 * @author Marc-Antoine FERNANDES
 * @version 1.0.0
 */
public class Request {


    /**
     * List of all methods allowed by HTTP/1.1
     */
    public enum METHOD {
        GET, HEAD, POST, PUT, DELETE, CONNECT, OPTIONS, TRACE
    }

    /**
     * method use by the request
     */
    private METHOD method;

    /**
     * Path of the request
     * (e.g. : "GET www.example.com/path/to/data/1.html", path will be : "/path/to/data/1.html")
     */
    private String path;

    /**
     * Body of the request
     */
    private String body;

    /**
     * List of all the params of the request (GET and POST mix)
     */
    private HashMap<String, String> params;

    /**
     * List of all the headers send by the client
     */
    private HashMap<String, String> headers;


    /**
     * Constructor of the request, it's private because a Request can only be build by {@link #build(BufferedReader)}.
     *
     * @param method  see {@link #method}
     * @param path    see {@link #path}
     * @param body    see {@link #body}
     * @param headers see {@link #headers}
     * @param params  see {@link #params}
     */
    private Request(METHOD method, String path, String body, HashMap<String, String> headers, HashMap<String, String> params) {
        this.method = method;
        this.path = path;
        this.body = body;
        this.headers = headers;
        this.params = params;
    }

    /**
     * Request builder, it builds the request from a InputStream.
     *
     * @param in Input stream
     * @return a well formed request
     * @throws MalformedRequestException when we cannot parse the input stream
     */
    public static Request build(BufferedReader in) throws MalformedRequestException {

        try {
            // First line of the request

            String[] requestLine = in.readLine().split(" ");

            // Headers

            String[] header;
            HashMap<String, String> headers = new HashMap<>();

            String str = ".";
            while (!str.equals("")) {
                str = in.readLine();

                // If it's not an empty line
                if (!str.equals("")) {
                    header = str.split(":", 2);
                    headers.put(header[0].toLowerCase(), header[1].trim());
                }
            }

            // Body

            int size = Integer.parseInt(headers.getOrDefault("content-length", "0"));

            char[] body = new char[size];

            if (in.read(body, 0, size) < size) throw new Exception();


            // Method, Path, Http version

            METHOD method = METHOD.valueOf(requestLine[0].toUpperCase());

            String path;

            if (headers.containsKey("host")) {
                path = requestLine[1];
            } else {
                URL url = new URL(requestLine[1]);
                path = url.getPath() + "?" + url.getQuery();
            }

            // Decoding path
            path = java.net.URLDecoder.decode(path, "UTF-8");

            // PARAMS

            String[] parts = path.split("\\?", 2);

            HashMap<String, String> params = null;

            String paramsStr = parts.length > 1 ? parts[1] + "&" : "";

            if (method == METHOD.POST) {
                paramsStr += new String(body);
            }

            if (!paramsStr.isEmpty()) {
                String[] paramList = paramsStr.split("&");
                String[] param;

                params = new HashMap<>(paramList.length);

                for (String aParamList : paramList) {
                    param = aParamList.split("=", 2);
                    params.put(param[0], param[1]);
                }
            }

            return new Request(method, path, new String(body), headers, params);
        } catch (Exception e) {
            // We convert any Exception to a MalformedRequestException
            throw new MalformedRequestException();
        }
    }

    /**
     * @return method used
     */
    public METHOD getMethod() {
        return method;
    }

    /**
     * @return path asked by the request
     */
    public String getPath() {
        return path;
    }

    /**
     * @return body of the request
     */
    public String getBody() {
        return body;
    }

    /**
     * @param key param name
     * @return param value or ""
     */
    public String getParam(String key) {
        return params.getOrDefault(key, "");
    }

    /**
     * @return return all parameters
     */
    public HashMap<String, String> getParams() {
        return params;
    }

    /**
     * @param key header name
     * @return header value or ""
     */
    public String getHeaders(String key) {
        return headers.getOrDefault(key, "");
    }

    /**
     * Custom Exception
     */
    public static class MalformedRequestException extends Exception {

    }
}
