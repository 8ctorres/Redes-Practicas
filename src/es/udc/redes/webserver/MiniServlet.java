package es.udc.redes.webserver;
import java.util.Map;

/**
 * An interface that represents simple servlets that generate webpages based on parameters
 * @author carlo
 */
public interface MiniServlet {
    /**
    * Generates the page based on the parameters
    * @param parameters a Map(String, String) containing the parameters
    * @return a multi line String that represents the page
     * @throws java.lang.Exception
    */
    public String doGet (Map<String, String> parameters) throws Exception;

}
