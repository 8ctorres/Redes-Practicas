package es.udc.redes.webserver;
import java.util.Map;

/**
 * This class contains utility methods for the use of servlets
 * @author Carlos Torres (carlos.torres@udc.es)
 */
public class ServerUtils {
        /**
         * Processes a dynamic request, using the specified servlet and parameters
         * @param nombreclase the name of the Servlet to use to generate the page
         * @param parameters a Map containing the request parameters
         * @return a multi line String containing the webpage
         * @throws Exception
         */
	public static String processDynRequest(String nombreclase,
			Map<String, String> parameters) throws Exception {

		MiniServlet servlet;
		Class<?> instancia;

		instancia = Class.forName(nombreclase);
		servlet = (MiniServlet) instancia.newInstance();

		return servlet.doGet(parameters);
	}
}
