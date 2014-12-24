package org.bitxbit;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/myapp/";

//    /**
//     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
//     * @return Grizzly HTTP server.
//     */
//    public static HttpServer startServer() {
//        // create a resource config that scans for JAX-RS resources and providers
//        // in org.bitxbit package
//        final ResourceConfig rc = new ResourceConfig().packages("org.bitxbit");
//
//        // create and start a new instance of grizzly http server
//        // exposing the Jersey application at BASE_URI
//        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
//    }
//
//    /**
//     * Main method.
//     * @param args
//     * @throws IOException
//     */
//    public static void main(String[] args) throws IOException {
//        final HttpServer server = startServer();
//        System.out.println(String.format("Jersey app started with WADL available at "
//                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
//        System.in.read();
//        server.stop();
//    }

    public static void main(String[] args) throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        Server jettyServer = new Server(8080);
        jettyServer.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(
                org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        // Tells the Jersey Servlet which REST service/class to load.
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("jersey.config.server.provider.classnames", MyResource.class.getCanonicalName());
        initParams.put("javax.ws.rs.Application", JerseyApplication.class.getCanonicalName());
//        jerseyServlet.setInitParameter(
//                "jersey.config.server.provider.classnames",
//                MyResource.class.getCanonicalName());
        jerseyServlet.setInitParameters(initParams);

        try {
            jettyServer.start();
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }
    }
}

