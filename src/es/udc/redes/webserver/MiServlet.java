package es.udc.redes.webserver;
import java.util.Map;

/**
 * This class represents a servlet that generates a page that greets the user
 * @author Carlos Torres (carlos.torres@udc.es)
 */
public class MiServlet implements MiniServlet {
	

        /**
         * Constructs this servlet, takes no parameters
         */
	public MiServlet(){
		
	}
	
        @Override
        /**
        * Generates the page based on the parameters
        * @param parameters a Map(String, String) containing the parameters
        * @return a multi line String that represents the page
        */
	public String doGet (Map<String, String> parameters){
		String nombre = parameters.get("nombre");
		String primerApellido = parameters.get("primerApellido");
		String segundoApellido = parameters.get("segundoApellido");
		String nombreCompleto = nombre + " " + primerApellido + " " + segundoApellido;
		
		return printHeader() + printBody(nombreCompleto) + printEnd();
	}	

	private String printHeader() {
		return "<html><head> <title>Greetings</title> </head> ";
	}

	private String printBody(String nombreCompleto) {
		return "<body><h1> Hola " + nombreCompleto + "</h1></body>";
	}

	private String printEnd() {
		return "</html>";
	}
}
