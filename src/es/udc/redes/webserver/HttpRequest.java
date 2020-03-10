/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.udc.redes.webserver;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * All Http request handlers (HEAD, GET, PUT, POST...) must inherit from this class
 * @author carlos.torres
 */
public class HttpRequest {
    private final String request;
    public HttpRequest(String s){
        this.request = s;
    }
    /**
     * Given an HTTP request, interprets it and sends the appropiate response
     * using the given output writer
     * This method does not close the writer after using it
     * @param output : the writer it uses to send the response
     */
    public void respond(PrintWriter output){
        //First line, request line
        String[] rq = request.split("\n")[0].split(" ");
        
        if (!rq[2].startsWith("HTTP/")){
            //Bad requests handling
            output.write(badRequest());
            return;
        }
        
        if ((!"GET".equals(rq[0])) && (!"HEAD".equals(rq[0]))){
            System.out.println("rq[0] = " + rq[0]);
            output.write(badRequest());
            return;
        }
        
        String resource_name = rq[1];
        
        if (resource_name.equals("/")){
            resource_name = "/index.html"; //Default resource is index.html
        }
        
        /*Ensures that it works under Windows or any other OS where the path separator
        is the backwards slash "\" instead of the forward "/" */
        resource_name = resource_name.replace('/', File.separator.charAt(0));
        resource_name = WebServer.RESOURCES_PATH + resource_name;
        
        if (!Files.exists(new File(resource_name).toPath())){
            //Checks that the file exists
            output.write(fileNotFound());
            return;
        }
        
        HttpResource resource = new HttpResource(new File(resource_name));
        
        resource.writeHead(output); //Status and header lines
        output.println(); //Blank line
        //if ("GET".equals(rq[0])){ //Head requests don't send body
            try {
                resource.writeBody(output); //File body
            } catch (FileNotFoundException ex) {
                //Should never happen because its already been checked that the file exists
                System.out.println("File not found exception");
                ex.printStackTrace();
            }
        //}
    }
    
    /**
     * This method takes no arguments and returns the current system date and time
     * in Http v1 format
     * @return String with the current date-time
     */
    public static String getHttpDate(){
        try {
        return "Date: " + 
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME) + 
                "\n";
        }
        catch (Exception ex){
            System.out.println("Date formatting exception");
            ex.printStackTrace();
        }
        return null;
    }
    
    /**
     * This method generates a generic HTTP 400 Bad Request response
     * Includes the header and the Date line
     * @return a String containing the response
     */
    public static String badRequest(){
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.0 400 Bad Request\n");
        response.append(getHttpDate());
        response.append("\n");
        return response.toString();
    }
    
    /**
     * This method generates a generic HTTP 404 Not Found response
     * Includes header and Date line
     * @return a String containing the response
     */
    public static String fileNotFound(){
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.0 404 Not Found\n");
        response.append(getHttpDate());
        response.append("\n");
        return response.toString();
    }
}
