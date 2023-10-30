package web;

import org.apache.http.NameValuePair;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final List<String> validPaths = List.of("/index.html", "/spring.svg",
            "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html",
            "/forms.html", "/classic.html", "/events.html", "/events.js", "/default-get.html");
    private int port;
    private ExecutorService executorService;
    private final ConcurrentHashMap<String, Map<String, Handler>> handlers;

    public Server(int port, int poolSize) {
        this.port = port;
        this.executorService = Executors.newFixedThreadPool(poolSize);
        this.handlers = new ConcurrentHashMap<>();
    }

    public void start() {
        while (true) {
            try {
                final var serverSocket = new ServerSocket(port);
                System.out.println("Server started");
                while (!serverSocket.isClosed()) {
                    final var socket = serverSocket.accept();
                    executorService.execute(() -> {
                        processing(socket);
                    });
                    System.out.println("Connected");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                executorService.shutdown();
            }
        }
    }

    public void processing(Socket socket) {
        try (final var out = new BufferedOutputStream(socket.getOutputStream());
             final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                socket.close();
                return;
            }

            final var method = parts[0];
            final var path = parts[1];
            Request request = new Request(method, path);

            if (request == null || !handlers.containsKey(request.getMethod())) {
                responseWithoutContent(out, "404", "Not Found");
                return;
            }

            Map<String, Handler> handlerMap = handlers.get(request.getMethod());
            String requestPath = request.getPath();
            if (handlerMap.containsKey(requestPath)) {
                Handler handler = handlerMap.get(requestPath);
                handler.handle(request, out);
            } else {
                if (!validPaths.contains(request.getPath())) {
                    responseWithoutContent(out, "404", "Not found");
                } else {
                    responceWithContent(out, path);
                }
            }
        } catch (NullPointerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void responseWithoutContent(BufferedOutputStream out, String responseCode, String responceStatus) {
        try {
            out.write((
                    "HTTP/1.1 " + responseCode + " " + responceStatus + "\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void responceWithContent(BufferedOutputStream out, String path) {
        try {
            final var filePath = Path.of(".", "public", path);
            final String mimeType = Files.probeContentType(filePath);

            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();
                return;
            }

            final var length = Files.size(filePath);
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        if (!handlers.containsKey(method)) {
            handlers.put(method, new HashMap<>());
        }
        handlers.get(method).put(path, handler);
    }
}
