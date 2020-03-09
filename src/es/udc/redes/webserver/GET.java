/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.udc.redes.webserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public void respond(String request, PrintWriter output) throws HttpException{
        StringBuilder response = new StringBuilder();
        String[] lines = request.split("\n");
        
        //First line, request line
        String[] rq = lines[1].split(" ");
        
        if (!"GET".equals(rq[0]))
            throw new HttpException("Wrong handler");
        
        String resource_name = rq[1];
        
        if (!rq[2].startsWith("HTTP/")){
            //Bad requests handling
            output.print(badRequest());
        }
        
        if (resource_name.equals("/")){
            resource_name = "/index.html";
        }
        
        HttpResource resource = new HttpResource(resource_name);
        
        resource.getHead(output); //Status and header lines
        output.println(); //Blank line
        resource.getBody(output); //File body
    }
}