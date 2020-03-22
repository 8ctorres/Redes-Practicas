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
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An Http request object represents an HTTP v1.0 Request, given as a multi-line String
 * @author carlos.torres
 */
public class HttpRequest {
    private final String request;
    private final String client_ip;
    private String hostname;
    /*Stores some of the response details,
    so they can be accessed later by log() and errorLog() methods*/
    private String petition_line;
    private final String date_time;
    private int status_code = 500;
    private int bytes_sent = 0;
    /**
     * Creates a new HTTPRequest object, from a given request,
     * and the IP of the Client that sent the request
     * @param req - The request, in form of a multi-line string
     * @param client_ip - The InedAddress of the client
     */
    public HttpRequest(String req, InetAddress client_ip){
        this.request = req;
        this.client_ip = client_ip.getHostAddress();
        this.date_time = getHttpDate();
    }
    /**
     * Interprets the HTTP request and sends the appropiate response
     * using the given output stream
     * This method does not close the output stream after using it
     * @param output : the outputstream it uses to send the response
     * @return status code of the response
     */
    public int respond(OutputStream output){
        //Split request in lines
        String [] lines = request.split(System.lineSeparator());
        //Stores petition line
        this.petition_line = lines[0];
        //Splits petition line in words
        String[] split_petition = lines[0].split(" ");
        
        //Reads hostname, used for redirection links
        for (int i = 1; i<lines.length; i++){
                if (lines[i].startsWith("Host: ")){
                    this.hostname = lines[i].substring(6);
                    break;
                }
            }
        
        //Builds a PrintWriter object to send characters, with AutoFlush enabled
        PrintWriter output_writer = new PrintWriter(output, true);
        
        if (!split_petition[2].startsWith("HTTP/")){
            //Bad requests handling
            output_writer.write(badRequest());
            return status_code;
        }
        
        if ((!"GET".equals(split_petition[0])) && (!"HEAD".equals(split_petition[0]))){
            //If the method is not GET or HEAD, send 400 badRequest
            System.out.println("split_petition[0] = " + split_petition[0]);
            output_writer.write(badRequest());
            return status_code;
        }
        
        //Gets the wanted resource name
        String resource_name = split_petition[1];
        
        if (resource_name.equals("/")){
            String directory = WebServer.PROPERTIES.getProperty("DIRECTORY");
            resource_name = WebServer.PROPERTIES.getProperty("DIRECTORY_INDEX"); //Default in that directory
            if (!Files.exists(Paths.get(directory + resource_name))){
                //If the default file for the directory does not exist
                //The action depends on the ALLOW directive
                if (Boolean.getBoolean(WebServer.PROPERTIES.getProperty("ALLOW"))){
                    //ALLOW directive enabled
                    this.status_code = 404;
                    String[] dir_page = generateDirectoryPage(new File(WebServer.PROPERTIES.getProperty("DIRECTORY")));
                    output_writer.write(fileNotFound());
                    output_writer.println();
                    for (String line : dir_page)
                        output_writer.println(line);
                    return status_code;
                }else{
                    //ALLOW directive disabled
                    output_writer.write(accessForbidden());
                    return status_code;
                }
            }
        }
        
        /*Ensures that it works under Windows or any other OS where the path separator
        is the backwards slash "\" instead of the forward "/"
        On UNIX systems, the resource_name String remains unchanged
        */
        resource_name = resource_name.replace('/', File.separatorChar);
        //Converts relative to absolute pathname
        resource_name = WebServer.PROPERTIES.getProperty("DIRECTORY") + resource_name;
        
        //Creates a File object associated to the requested resource
        File resource_file = new File(resource_name);
        
        if (!Files.exists(resource_file.toPath())){
            //Checks that the file exists
            //If the requested resource does not exist, respond 404 Not Found
            output_writer.write(fileNotFound());
            return status_code;
        }
        
        boolean was_mod = true;        
        /*Checks for the If-Modified-Since line.
        if the field exists, checks whether the file was or not modified*/
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
            return status_code;
        }
        
        HttpResource resource = new HttpResource(resource_file);
        
        status_code = resource.writeHead(output_writer, was_mod); //Status and header lines
        //Output PrintWriter is flushed now to prevent problems when sending the file body
        output_writer.flush();
        //Writes blank line
        output_writer.println();
        //Head requests don't send body. If it was not modified, the body isn't sent either
        if ((split_petition[0].equals("GET")) && (was_mod)){
            try {
                bytes_sent = resource.writeBody(output, output_writer); //File body
            } catch (FileNotFoundException ex) {
                //Should never happen because its already been checked that the file exists
                System.out.println("File not found exception");
                Logger.getLogger(HttpRequest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return status_code;
    }
    /**
     * Dynamically generate an HTML Document that links every item in the specified directory,
     * and returns it in an array of Strings, line by line.
     * @param dir - a FILE object containing the directory to list
     * @return - The HTML page represented as an array of Strings, with each element being a line of the file
     */
    public String[] generateDirectoryPage(File dir){
        if(dir.isDirectory()){
            ArrayList<String> page = new ArrayList(50);
            page.add("<!DOCTYPE html>");
            page.add("<html>");
            page.add("<head>");
            page.add("<title>Directory index</title>");
            page.add("</head>");
            page.add("");
            page.add("<h1>Listing of the directory</h1>");
            page.add("<ul>");
            String[] dir_list = dir.list();
            for (String item : dir_list) {
                page.add("<il href=\"" + "http://" + hostname + "/" + item + "\">" + item + "</il>");
            }
            page.add("</ul>");
            return page.toArray(new String[0]);
        }else{
            //The specified directory is not a directory
            return null;
        }
    }
    /**
     * Writes information about the request and response in a log file. If the request
     * caused an error, writes it in the same file
     * @param log_writer - a PrintWriter object to write to the log file
     */
    public void log(PrintWriter log_writer){
        log(log_writer,log_writer);
    }
    /**
     * Writes information about the request and response in a log file. If the request
     * caused an error, writes it in the error logs file.
     * @param log_writer - a PrintWriter object to write to the log file
     * @param error_writer - a PrintWriter object to write to the error logs file
     */
    public void log(PrintWriter log_writer, PrintWriter error_writer){
        if (this.status_code < 400){
            //NO ERRORS
            log_writer.println(this.petition_line);
            log_writer.println(this.client_ip);
            log_writer.print(this.date_time);
            log_writer.println(statusCodeString());
            log_writer.println("Bytes sent: " + this.bytes_sent);
            log_writer.println();
        }else{
            //ERROR LOG
            error_writer.println(this.petition_line);
            error_writer.println(this.client_ip);
            error_writer.print(this.date_time);
            error_writer.println(statusCodeString());
            error_writer.println();
        }
    }
    public String statusCodeString(){
        StringBuilder sb = new StringBuilder("Status code: ");
        switch (this.status_code){
            case(200):
                sb.append("200 OK");
                break;
            case(304):
                sb.append("304 Not Modified");
                break;
            case(400):
                sb.append("400 Bad Request");
                break;
            case(403):
                sb.append("403 Access Forbidden");
                break;
            case(404):
                sb.append("404 Not Found");
                break;
            default:
                sb.append(this.status_code);
        }
        return sb.toString();
    }
    /**
     * This method takes no arguments and returns the current system date and time
     * in Http v1 format
     * @return String with the current date-time
     */
    public static String getHttpDate(){
        return "Date: " + 
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME) + 
                System.lineSeparator();
    }
    
    /**
     * This method generates a generic HTTP 400 Bad Request response
     * Includes the header and the Date line
     * @return a String containing the response
     */
    public String badRequest(){
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.0 400 Bad Request");
        response.append(System.lineSeparator());
        response.append(getHttpDate());
        response.append(System.lineSeparator());
        this.status_code = 400;
        return response.toString();
    }
    /**
     * This method generates a generic HTTP 403 Access Forbidden response
     * Includes header and Date line
     * @return a String containing the response
     */
    public String accessForbidden() {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.0 403 Access Forbidden");
        response.append(System.lineSeparator());
        response.append(getHttpDate());
        response.append(System.lineSeparator());
        this.status_code = 403;
        return response.toString();
    }
    
    /**
     * This method generates a generic HTTP 404 Not Found response
     * Includes header and Date line
     * @return a String containing the response
     */
    public String fileNotFound(){
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.0 404 Not Found");
        response.append(System.lineSeparator());
        response.append(getHttpDate());
        response.append(System.lineSeparator());
        this.status_code = 404;
        return response.toString();
    }
    /**
     * This method checks if a file has been modified since the date indicated
     * @param file: the file to check
     * @param since: the date formatted in a String
     * @return true if it was modified
     */
    public boolean wasModified(File file, String since) throws DateTimeParseException {
        long sincedt = (
                ZonedDateTime.parse(since,(DateTimeFormatter.RFC_1123_DATE_TIME)).toEpochSecond());
        /*file.lastModified() returns a long containing the last modified date, expressed in
          MILLISECONDS since the epoch, so it is divided by 1000 (integer division)
        */
        long file_mod = file.lastModified()/1000;
        boolean wasmod = (sincedt < file_mod);
        if (!wasmod)
            this.status_code = 304;
        return wasmod;
    }
}