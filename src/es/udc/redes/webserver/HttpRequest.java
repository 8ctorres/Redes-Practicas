/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.udc.redes.webserver;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * All Http request handlers (HEAD, GET, PUT, POST...) must inherit from this class
 * @author carlos.torres
 */
public abstract class HttpRequest {
    /**
     * Given an HTTP request, interprets it and sends the appropiate response
     * using the given output writer
     * @param request = request to handle
     * This method does not close the writer after using it
     * @param output : the writer it uses to send the response
     * @throws es.udc.redes.webserver.HttpException
     */
    public abstract void respond(String request, PrintWriter output) throws HttpException;
    /**
     * This method takes no arguments and returns the current system date and time
     * in Http v1 format
     * @return String with the current date-time
     */
    public static String getHttpDate(){
        return "Date: " + 
                LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME) + 
                "\n";
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
     * 
     * @return 
     */
    public static String getHttpContentType(){
        return "antonio";
    }
}
