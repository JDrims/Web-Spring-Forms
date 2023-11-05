package web;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Request {
    private final String method;
    private final String path;
    private final List<NameValuePair> param;

    public Request(String method, String path) {
        this.method = method;
        this.path = path;
        try {
            this.param = URLEncodedUtils.parse(new URI(this.path), StandardCharsets.UTF_8);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
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

    public String getQureyParam(String name) {
        List<NameValuePair> params = getQueryParams();
        for (NameValuePair param : params) {
            if (param.getName().equals(name)) {
                return param.getValue();
            }
        }
        return null;
    }

    public List<NameValuePair> getQueryParams() {
        return this.param;
    }
}