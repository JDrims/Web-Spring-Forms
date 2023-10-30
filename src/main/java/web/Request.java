package web;

import java.net.URI;
import java.net.URISyntaxException;

public class Request {
    private final String method;
    private final String path;

    public Request(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        try {
            URI uri = new URI(this.path);
            return uri.getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
