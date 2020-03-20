/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.udc.redes.webserver;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

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
    public void respond(OutputStream output){
        //Split request in lines
        String [] lines = request.split(System.lineSeparator());
        //First line, request line
        String[] rq = lines[0].split(" ");
        
        //Builds a PrintWriter object to send characters, with AutoFlush enabled
        PrintWriter output_writer = new PrintWriter(output, true);
        
        if (!rq[2].startsWith("HTTP/")){
            //Bad requests handling
            output_writer.write(badRequest());
            return;
        }
        
        if ((!"GET".equals(rq[0])) && (!"HEAD".equals(rq[0]))){
            System.out.println("rq[0] = " + rq[0]);
            output_writer.write(badRequest());
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
        
        File resource_file = new File(resource_name);
        
        if (!Files.exists(resource_file.toPath())){
            //Checks that the file exists
            output_writer.write(fileNotFound());
            return;
        }
        
        boolean was_mod = true;
        
        /*Checks for the If-Modified-Since line.
        if there is a date, checks whether the file was or not modified*/
        try{
            for (int i = 1; i<lines.length; i++){
                if (lines[i].startsWith("If-Modified-Since: ")){
                    was_mod = wasModified(resource_file, lines[i].substring(19));
                    break;
                }
            }
        }catch(DateTimeParseException ex){
            //If the If-Modified-Since line is wrong, then send a 400 Bad Request and return
            System.out.println("If Modified Since line could not be parsed");
            output_writer.write(badRequest());
            return;
        }
        
        HttpResource resource = new HttpResource(resource_file);
        
        resource.writeHead(output_writer, was_mod); //Status and header lines
        //Output PrintWriter is flushed now to prevent problems when sending the file body
        output_writer.flush();
        //Writes blank line
        output_writer.println();
        //Head requests don't send body. If it was not modified, the body isn't sent either
        if ((rq[0].equals("GET")) || (!was_mod)){
            try {
                resource.writeBody(output, output_writer); //File body
            } catch (FileNotFoundException ex) {
                //Should never happen because its already been checked that the file exists
                System.out.println("File not found exception");
                ex.printStackTrace();
            }
        }
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
    /**
     * This method checks if a file has been modified since the date indicated
     * @param file: the file to check
     * @param since: the date formatted in a String
     * @return true if it was modified
     */
    public static boolean wasModified(File file, String since) throws DateTimeParseException {
        long sincedt = (
                ZonedDateTime.parse(since,(DateTimeFormatter.RFC_1123_DATE_TIME)).toEpochSecond());
        /*file.lastModified() returns a long containing the last modified date, expressed in
          MILLISECONDS since the epoch, so it is divided by 1000 (integer division)
        */
        long file_mod = file.lastModified()/1000;
        //Gives the modification date a millisecond of margin
        return (sincedt < file_mod);
    }
}
