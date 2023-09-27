package cypherf.demo.javaapi;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * <a href="https://medium.com/consulner/framework-less-rest-api-in-java-dd22d4d642fa">...</a>
 */
public class Application {
    public static void main(String[] args) throws IOException {
        System.out.println("Application.main()...");

        // CREATING SERVER
        int serverPort = 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);

        // CREATING MAPPINGS
        server.createContext("/api/hello", (exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                String respText = "Hello!\n";
                exchange.sendResponseHeaders(200, respText.getBytes().length);
                OutputStream output = exchange.getResponseBody();
                output.write(respText.getBytes());
                output.flush();
            } else {
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            }
            exchange.close();
        }));

        // STARTING SERVER
        server.setExecutor(null); // creates a default executor
        System.out.println("STARTING SERVER...");
        server.start();
        System.out.println("SERVER STARTED");
        System.out.println();
        System.out.println("curl refresher:");
        System.out.println("- Use \"curl.exe\" instead of \"curl\" while in PowerShell Desktop");
        System.out.println("- Option -v to get verbose response");
        System.out.println("- Option -X to specify request method (GET, POST, etc.)");
        System.out.println();
        System.out.println("Try the following requests:");
        System.out.println("  curl -v http://localhost:8000/api/hello");
        System.out.println("    ↳ Implicit GET request, should return 200 OK + content \"Hello!\"");
        System.out.println("  curl -v -X POST http://localhost:8000/api/hello");
        System.out.println("    ↳ POST request, should return 405 Method Not Allowed");
    }
}
