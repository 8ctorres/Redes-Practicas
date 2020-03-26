package es.udc.redes.webserver;
import java.util.Map;

/**
 * This class representes a Servlet that serves calculator web pages
 * @author carlo
 */
public class Calculate implements MiniServlet {
    
    /**
     * Constructs this servlet, takes no arguments
     */
    public Calculate(){}
    
    /**
     * Generates the page based on the parameters
     * @param parameters a Map(String, String) containing the parameters
     * @return a multi line String that represents the page
     */
    @Override
    public String doGet (Map<String, String> parameters){
        try{
            String operacion = parameters.get("op");
            int op1 = Integer.parseInt(parameters.get("op1"));
            int op2 = Integer.parseInt(parameters.get("op2"));
            return printHeader() + printBody(operacion, op1, op2) + printEnd();
        }catch(IllegalArgumentException | NullPointerException ex){
            return printHeader() + printErrorBody() + printEnd();
        }catch(ArithmeticException ex){
            return printHeader() + printMathErrorBody() + printEnd();
        }
    }    

    private String printHeader() {
        return "<html><head> <title>Calculate</title> </head>";
    }

    private String printBody(String operacion, int op1, int op2) throws IllegalArgumentException{
        return "<body><h1> Resultado: " + op1 + parseOp(operacion) + op2 + " = "+ + calculate(operacion, op1, op2) + "</h1></body>";
    }
    
    private String printErrorBody(){
        return "<body><h1> Se han introducido valores incorrectos </h1>";
    }
    
    private String printMathErrorBody(){
        return "<body><h1> Operación ilegal </h1>";
    }

    private String printEnd() {
        return "</body></html>";
    }
    
    private String parseOp(String op) throws IllegalArgumentException{
        switch(op){
            case("Sumar"):
                return "+";
            case("Restar"):
                return "-";
            case("Multiplicar"):
                return "*";
            case("Dividir"):
                return "/";
            default:
                throw new IllegalArgumentException("Unrecognised operation");
        }
    }

    private int calculate(String op, int op1, int op2){
        switch(op){
            case("Sumar"):
                return op1+op2;
            case("Restar"):
                return op1-op2;
            case("Multiplicar"):
                return op1*op2;
            case("Dividir"):
                return op1/op2;
            default:
                throw new IllegalArgumentException("Unrecognised operation");
        }
    }
}
