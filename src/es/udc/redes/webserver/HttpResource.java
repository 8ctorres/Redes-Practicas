/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.udc.redes.webserver;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

/**
 * HttpResource handlers. All specific handlers (text/html, text/plain...) are HttpResources
 * @author carlos.torres
 */
public class HttpResource {
    private final String filename;
    private final File file;
    public HttpResource(String filename){
        this.filename = filename;
        file = new File(filename);
    }
    /**
     * This method sends the head of the http response
     * @param output: the PrintWriter object that will be used to send the output to
     */
    public void getHead(PrintWriter output){
        output.println("HTTP/1.1 200 OK");
        output.print(HttpRequest.getHttpDate());
        output.println("Server: Práctica de Redes");
        output.println(HttpRequest.getHttpContentType(filename));
        try {
            output.println("Content-Length: " + Files.size(file.toPath()));
        } catch (IOException ex) {
            System.out.println("I/O Exception!");
        }
    }
    /**
     * This method reads from the file and sends it to an output
     * @param output: the PrintWriter object that will be used to send the output to
     */
    public void getBody(PrintWriter output){
        
    }
    /**
     * Gets extension of a file
     * @param filename: a String containing the name of the file, relative or absolute
     * @return the extension, or the empty String if there is no extension
     */
    public static String getExtension(String filename){
        String extension = "";
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            extension = filename.substring(i+1);
        }
        return extension;
    }   
}
