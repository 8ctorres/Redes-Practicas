/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.udc.redes.webserver;

import java.io.Writer;

/**
 * The GET class implements handling of HTTP GET requests
 * @author carlos.torres
 */
public class GET implements HttpRequest{
    @Override
    /**
     * This method generates an HTTP response to a GET request, and sends it
     * through the given output
     */
    public void respond(String request, Writer output) throws HttpException{
        StringBuilder response = new StringBuilder();
        String[] lines = request.split("\n");
        //First line, request line
        String[] rq = lines[1].split(" ");
        if (!"GET".equals(rq[0]))
            throw new HttpException("Wrong handler");
        
        String resource = rq[1];
        if (!rq[2].startsWith("HTTP/"))
            response.append("HTTP/1.0 400 Bad Request\n");
        else
            response.append("HTTP/1.0 200 OK");
        
    }
    
}
