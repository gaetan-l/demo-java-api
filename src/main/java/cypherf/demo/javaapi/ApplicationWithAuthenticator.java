package cypherf.demo.javaapi;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * API with method and parameters mapping plus authentication
 * @author Marcin Piczkowski
 * @see <a href="https://medium.com/consulner/framework-less-rest-api-in-java-dd22d4d642fa">
 *     Framework-less REST API in Java</a>
 * @see <a href="https://github.com/piczmar/pure-java-rest-api"/>
 */
class ApplicationWithAuthenticator {

    public static void main(String[] args) throws IOException {
        System.out.println("Application.main()...");

        // CREATING SERVER
        final int serverPort = 8000;
        final HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);

        final HttpContext context = server.createContext("/api/hello", (exchange -> {

            // CREATING MAPPINGS
            if ("GET".equals(exchange.getRequestMethod())) {
                final Map<String, List<String>> params = splitQuery(exchange.getRequestURI().getRawQuery());
                final String noNameText = "Anonymous";
                final String name = params.getOrDefault("name", List.of(noNameText)).stream().findFirst().orElse(noNameText);
                final String respText = String.format("Hello %s!", name);
                exchange.sendResponseHeaders(200, respText.getBytes().length);
                final OutputStream output = exchange.getResponseBody();
                output.write(respText.getBytes());
                output.flush();
            } else {
                exchange.sendResponseHeaders(405, -1);// 405 Method Not Allowed
            }
            exchange.close();
        }));

        // ADDING AUTHENTICATOR
        // "myrealm" is a realm name, a virtual name which can be used to
        // separate different authentication spaces.
        context.setAuthenticator(new BasicAuthenticator("myrealm") {
            @Override
            public boolean checkCredentials(String user, String pwd) {
                return user.equals("admin") && pwd.equals("admin");
            }
        });

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
        System.out.println("    ↳ Request without authentication, should return 401 Unauthorized");
        System.out.println("  curl -v http://localhost:8000/api/hello -H 'Authorization: Basic YWRtaW46YWRtaW4='");
        System.out.println("  curl.exe -v http://localhost:8000/api/hello -H 'Authorization: Basic YWRtaW46YWRtaW4=' (if not in PowerShell)");
        System.out.println("    ↳ Request with authentication, (\"YWRtaW46YWRtaW4=\" is \"admin:admin\" encoded in Base64), should return 200 OK ");
    }

    public static Map<String, List<String>> splitQuery(String query) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
        }

        final Pattern pattern = Pattern.compile("&");
        final Stream<String> queryAsStream = pattern.splitAsStream(query);
        final Stream<String[]> mapped = queryAsStream.map(s -> Arrays.copyOf(s.split("="), 2));
        return mapped.collect(groupingBy(s -> decode(s[0]), mapping(s -> decode(s[1]), toList())));

        // In more concise form
        // return Pattern.compile("&")
        //        .splitAsStream(query)
        //        .map(s -> Arrays.copyOf(s.split("="), 2))
        //        .collect(groupingBy(s -> decode(s[0]), mapping(s -> decode(s[1]), toList())));
    }

    private static String decode(final String encoded) {
        return encoded == null ? "" : URLDecoder.decode(encoded, StandardCharsets.UTF_8);
    }
}