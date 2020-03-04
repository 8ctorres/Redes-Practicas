/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.udc.redes.webserver;

/**
 * The GET class implements handling of HTTP GET requests
 * @author carlos.torres
 */
public class GET extends HttpRequest{
    @Override
    /**
     * This method generates an HTTP response to a GET request, and sends it
     * through the given output
     */
    public String respond(String request) throws HttpException{
        StringBuilder response = new StringBuilder();
        String[] lines = request.split("\n");
        //First line, request line
        String[] rq = lines[1].split(" ");

        if (!"GET".equals(rq[0]))
            throw new HttpException("Wrong handler");
        
        String resource = rq[1];
        if (!rq[2].startsWith("HTTP/")){
            //Bad requests handling
            return badRequest();
        }
        
    }
    
}
