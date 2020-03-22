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
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
    
/**
 * Represents a resource (html text, plain text, images...)
 * that can possibly be sent as response to an HTTP Request
 * @author carlos.torres
 */
public class HttpResource {
    private final File file;
    private final String content_type;
    /**
     * Creates a new HttpResource associated to the specified file in the system
     * @param file: the file in the system to use as resource
     */
    public HttpResource(File file){
        this.file = file;
        content_type = this.getHttpContentType();
    }
    /**
     * This method sends the head of the http response
     * If the file was not modified, it returns a 304 Not Mofidied status code
     * @param output: the PrintWriter object that will be used to send the output to
     * @param was_mod: a flag that indicates if the file was modified since last request
     * @return the Http status code
     */
    public int writeHead(PrintWriter output, boolean was_mod){
        int code;
        if (was_mod){
            output.println("HTTP/1.0 200 OK");
            code = 200;
        }
        else{
            output.println("HTTP/1.0 304 Not Modified");
            code = 304;
        }
        
        output.write(HttpRequest.getHttpDate());
        output.println("Server: Redes/Carlos Torres");
        output.println("Content-Type: " + content_type);
        try {
            output.println("Content-Length: " + Files.size(file.toPath()));
        } catch (IOException ex) {
            Logger.getLogger(HttpResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        output.write(getLastModified());
        return code;
    }
    /**
     * This method reads from the file and sends it to an output. It flushes the output at the end.
     * It always reads the file byte per byte, independently of it being a text file or other type of file
     * @param output: the OutputStream object that will be used to send the output to
     * @return the number of bytes read from the file sent
     * @throws java.io.FileNotFoundException if the file does not exist or can't be found
     */
    public int writeBody(OutputStream output) throws FileNotFoundException, IOException{
        return writeBinary(new PrintStream(output,true));
    }
    /**
     * This method reads from the file and sends it to an output. It flushes the output it used when it ends.
     * If the file is a text file, it uses the PrintWriter, and it uses the OutputStream otherwise
     * @param output: the OutputStream object that will be used to send the output to
     * @param output_writer: a PrintWriter object to send the output to in case the file is a text file
     * @return number of bytes read from the file and sent
     * @throws java.io.FileNotFoundException if the file does not exist or can't be found
     */
    public int writeBody(OutputStream output, PrintWriter output_writer) throws FileNotFoundException{
        switch(this.getExtension()){
            case("html"):
            case("txt"):
                //Uses an output_writer that is already opened to prevent problems with output buffers
                return writeText(output_writer);
            default:
                return writeBinary(new PrintStream(output, true));
        }
    }
    /**
     * Writes text files into a PrintWriter
     * @param output - the PrintWriter to send the file into
     * @return - the number of BYTES (not characters) read from the file
     * @throws FileNotFoundException 
     */
    private int writeText(PrintWriter output) throws FileNotFoundException{
        FileReader reader = new FileReader(this.file);
        int c;
        int counter = 0;
        try {
            while ((c = reader.read()) != -1){
                if (c>0xff)
                    counter+=2;
                else
                    counter++;
                output.write(c);
            }
        }
        catch (IOException ex) {
            Logger.getLogger(HttpResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        output.flush();
        return counter;
    }   
    /**
     * Writes raw data from a file into a PrintStream
     * @param output - The PrintStream to copy the file in
     * @return the number of bytes read and sent
     * @throws FileNotFoundException 
     */
    private int writeBinary(PrintStream output) throws FileNotFoundException{
        FileInputStream input = new FileInputStream(this.file);
        int c;
        int counter = 0;
        try {
            while ((c = input.read()) != -1){
                counter++;
                output.write(c);
            }
        } catch (IOException ex){
            Logger.getLogger(HttpResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        output.flush();
        return counter;
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
    /**
     * This method returns the last modification date of the file
     * @return a String formatted according to the HTML 1.1 standard
     */
    public String getLastModified(){
        StringBuilder sb = new StringBuilder("Last-Modified: ");
        sb.append(DateTimeFormatter.RFC_1123_DATE_TIME.format(
                ZonedDateTime.ofInstant(
                        Instant.ofEpochMilli(this.file.lastModified())
                        ,ZoneOffset.UTC
                )
            )
        );
        sb.append("\n");
        return sb.toString();
    }
}
