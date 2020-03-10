/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.udc.redes.webserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
    
/**
 * HttpResource handlers. All specific handlers (text/html, text/plain...) are HttpResources
 * @author carlos.torres
 */
public class HttpResource {
    private final File file;
    private final String content_type;
    /**
     * Creates a new HttpResource with the specified file
     * @param file: the file in the system to use as resource
     */
    public HttpResource(File file){
        this.file = file;
        content_type = this.getHttpContentType();
    }
    /**
     * This method sends the head of the http response
     * @param output: the PrintWriter object that will be used to send the output to
     */
    public void writeHead(PrintWriter output){
        output.println("HTTP/1.0 200 OK");
        output.write(HttpRequest.getHttpDate());
        output.println("Server: Práctica de Redes/Carlos Torres");
        output.println("Content-Type: " + content_type);
        try {
            output.println("Content-Length: " + Files.size(file.toPath()));
        } catch (IOException ex) {
            System.out.println("I/O Exception!");
            ex.printStackTrace();
        }
    }
    /**
     * This method reads from the file and sends it to an output
     * @param output: the PrintWriter object that will be used to send the output to
     * @throws java.io.FileNotFoundException if the file does not exist or can't be found
     */
    public void writeBody(PrintWriter output) throws FileNotFoundException{
        switch(this.getExtension()){
            case("html"):
            case("txt"):
                writeText(output); return;
            default:
                writeBinary(output);
        }
    }
    /**
     * Writes text files into a PrintWriter
     * @param output: the PrintWriter to copy the file in
     */
    private void writeText(PrintWriter output) throws FileNotFoundException{
        FileReader reader = new FileReader(this.file);
        int c;
        try {
            while ((c = reader.read()) == -1){
                output.write(c);
            }
        } catch (IOException ex) {
            System.out.println("IO Exception while reading the file");
            ex.printStackTrace();
        }
        
    }
    /**
     * Writes raw data from a file into a PrintWriter
     * @param output: the PrintWriter to copy the file in
     */
    private void writeBinary(PrintWriter output) throws FileNotFoundException{
        FileInputStream input = new FileInputStream(this.file);
        int c;
        try {
            while ((c = input.read()) == -1){
                output.write(c);
            }
        } catch (IOException ex){
            System.out.println("IO Exception while reading the file");
            ex.printStackTrace();
        }
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
    private String getExtension(){
        return getExtension(this.file.getPath());
    }
    /**
     * Returns the MIME type that corresponds to the resource
     * @return a String containing the type
     */
    private String getHttpContentType(){
        switch (this.getExtension()){
            case("html"):
                return ("text/html");
            case("txt"):
                return ("text/plain");
            case("gif"):
                return ("image/gif");
            case("png"):
                return ("image/png");
            default:
                return ("application/octet-stream");
        }
    }
}
