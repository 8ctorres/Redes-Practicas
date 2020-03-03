/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.udc.redes.webserver;
import java.io.Writer;

/**
 * All Http request handlers (HEAD, GET, PUT, POST...) must implement this interface
 * @author carlo
 */
public interface HttpRequest {
    /**
     * Given an HTTP request, interprets it and sends the appropiate response
     * using the given output writer
     * @param request = request to handle
     * @param output = open writer that will be used to send the response
     * This method does not close the writer after using it
     * @throws es.udc.redes.webserver.HttpException
     */
    public void respond(String request, Writer output) throws HttpException;
}
