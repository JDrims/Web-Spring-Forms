package web;

public class Main {
    private final static int PORT = 8089;
    private final static int POOL_SIZE = 64;

    public static void main(String[] args) {
        Server server = new Server(PORT, POOL_SIZE);
        server.addHandler("GET", "/messages", (((request, responseStream) -> {
            server.responseWithoutContent(responseStream, "404", "Not found");
        })));
        server.addHandler("POST", "/messages", (((request, responseStream) -> {
            server.responseWithoutContent(responseStream, "404", "Not found");
        })));
        server.addHandler("GET", "/index.html", (((request, responseStream) -> {
            server.responceWithContent(responseStream, request.getPath());
        })));
        server.addHandler("GET", "/classic.html", (((request, responseStream) -> {
            server.responceWithContent(responseStream, request.getPath());
        })));
        server.start();
    }
}