package es.udc.redes.webserver;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents an HTTP v1.0 Request
 * @author Carlos Torres (carlos.torres@udc.es)
 */
public class HttpRequest {
    private final String[] request_lines;
    private String hostname;
    private PrintWriter output_writer;
    private File resource_file;
    /*Stores some of the response details,
    so they can be accessed later by log() and errorLog() methods*/
    private final String log_date_time;
    private final String client_ip;
    private int status_code = 100;
    private int bytes_sent = 0;
    
    /**
     * Creates a new HTTPRequest object, from a given request,
     * and the IP of the Client that sent the request
     * @param req The request, in form of a multi-line string
     * @param client_ip The InedAddress of the client
     */
    public HttpRequest(String req, InetAddress client_ip){
        //When constructing the object, the request is split in lines
        this.request_lines = req.split(System.lineSeparator());
        this.client_ip = client_ip.getHostAddress();
        this.log_date_time = getLogDate();
    }
    
    /**
     * Interprets the HTTP request and sends the appropiate response
     * using the given output stream
     * This method does not close the output stream after using it
     * @param output the outputstream it uses to send the response
     * @return status code of the response
     */
    public int respond(OutputStream output){
        
        //Builds a PrintWriter object to send characters, with AutoFlush enabled
        this.output_writer = new PrintWriter(output, true);
        
        //Reads the host name from the request
        this.hostname = readHostname();
        
        //Gets the request resource name and parameters
        String resource_name = getResourceName();
        
        //If the resource name wasn't parsed, it means that the request is not correct
        if(resource_name == null){
            output_writer.write(badRequest());
            output_writer.println();
            return status_code;
        }
        
        //Handling dynamic requests
        status_code = handleDynRequest(resource_name);
        
        //If if wasn't a dynamic request, continue
        if(status_code != 100)
            return status_code;
        
        //Builds a file that points to the resource in the system
        //Opens the file now because the name has been correctly parsed (in method getResourceName)
        this.resource_file = openFile(resource_name);
        
        /*Handles the requests that ask for a directory
        If the resource is not a directory, handleDirectories() returns "100 Continue"
        status code and the execution continues as normal. If it is a directory, it will
        check if the default file for that directory exists. In that case it will assign that
        to the object's resource_file field and return "100 Continue". In case it does not
        exist, it'll read the ALLOW directive from the properties file and act accordingly, returning
        either a 404 and a 403, in either case, execution will stop right when this method returns
        */
        status_code = handleDirectories();
        
        //If it wasn't a directory, continue
        if (status_code != 100)
            return status_code;
        
        /*If the request asked for a file and not a directory
        it is still necessary to check if that file exists*/
        if (!resource_file.exists()){
            //Checks that the file exists
            //If the requested resource does not exist, respond 404 Not Found and exit
            output_writer.write(fileNotFound());
            output_writer.println();
            return status_code;
        }
        
        /*Checks if the client send an If-Modified-Since line.
        If there is no such line, or if the line exists but the file has been
        modified since that date, the method returns "100 Continue" status code
        If the file was not modified, the method returns a "304 Not Modified" status code*/
        try{
            status_code = checkIfModified();
        }
        catch (DateTimeParseException ex){
            //If the If-Modified-Since line is wrong, then send a 400 Bad Request and return
            System.out.println("If Modified Since line could not be parsed");
            output_writer.write(badRequest());
            output_writer.println();
            return status_code;
        }
        
        //Creates an Http Resource from the file, to read header and body from it        
        HttpResource resource = new HttpResource(resource_file);
        
        //Status and header lines
        status_code = resource.writeHead(output_writer, (status_code != 304));
        
        //Writes blank line
        output_writer.println();
        //Output PrintWriter is flushed now to prevent problems when sending the file body
        output_writer.flush();
        
        //Only if it is a GET request AND the file was modified, then sends body
        if (("GET".equals(this.request_lines[0].split(" ")[0])) && (status_code == 200)){
            try {
                bytes_sent = resource.writeBody(output, output_writer); //File body
            } catch (FileNotFoundException ex) {
                //Should never happen because its already been checked that the file exists
                System.out.println("File not found exception");
                Logger.getLogger(HttpRequest.class.getName()).log(Level.SEVERE, null, ex);
                return 500;
            }
        }
        return status_code;
    }
    
    /**
     * Reads the Host line from the request, and returns the specified hostname
     * @return a String containing only the hostname.
     */
    private String readHostname(){
        //Reads hostname, used for redirection links
        for (int i = 1; i<request_lines.length; i++){
                if (request_lines[i].startsWith("Host: ")){
                    return request_lines[i].substring(6);
                }
        }
        return null;
    }
    
    /**
     * Gets the requested resource name from the request. This method also
     * validates if the petition line is a valid HTTP 1.0 Request. If there is an error,
     * uses the object's output_writer to send an adequate error response.
     * @return a String containing the resource name, or null if there is an error
     * in the request (in which case, the object's status_code will be changed)
     */
    private String getResourceName(){
        String petition_line = request_lines[0];
        String[] petition_words = petition_line.split(" ");
        try{
            if (!petition_words[2].startsWith("HTTP/")){
                //Bad request
                return null;
            }
        }
        catch(ArrayIndexOutOfBoundsException ex){
            //Bad request
            return null;
        }
        
        if ((!"GET".equals(petition_words[0])) && (!"HEAD".equals(petition_words[0]))){
            //If the method is not GET or HEAD, send 400 badRequest
            return null;
        }
        
        return petition_words[1].split("\\?")[0].substring(1);
    }
    
    /**
     * Gets the request parameters, if any.
     * @return a Map containing all the parameters in a Map as <Param_name, value>
     */
    private Map<String, String> getParameters(){
        HashMap<String, String> mapa = new HashMap<>();
        /*Takes the petition line (request_lines[0]), splits it in words (split (space))
        then takes the first word, which contains the resource name and the parameters.
        Splits that using the "?" and takes the second part (the parameters), and then
        splits all of the parameters in a String of the form (param=value) (splits by "&")
        */
        try{
            String[] params = this.request_lines[0].split(" ")[1].split("\\?")[1].split("&");
            for (String param : params) {
                String[] name_and_value = param.split("=");
                mapa.put(name_and_value[0], name_and_value[1]);
            }
        }
        catch(NullPointerException | ArrayIndexOutOfBoundsException ex){
            /*If any of those array positions doesn't exist, that means that there
            are no parameters, or they're incorrectly parsed*/
            return null;
        }
        return mapa;        
    }
    
    /**
     * Checks if the request is a dynamic request. If it's not, returns "100 Continue"
     * If it is, handles it accordingly and returns the corresponding status code (always different from 100)
     * @param resource_name the resource name from the request (used to determine if it is a dynamic 
     * request or not)
     * @return the status code
     */
    private int handleDynRequest(String resource_name){
        if (resource_name.endsWith(".do")){
            try {
                Map<String, String> parameters = getParameters();
                String dyn_res = "es.udc.redes.webserver." + resource_name.substring(0, resource_name.length() -3);            
                String body = ServerUtils.processDynRequest(dyn_res, parameters);
                //Writes head of the response
                output_writer.println("HTTP/1.0 200 OK");
                output_writer.write(getHttpDate());
                output_writer.println("Server: Redes/Carlos Torres");
                output_writer.println("Content-Type: text/html");
                output_writer.println("Content-Length: " + body.length()+ "\n".length());
                //Blank line
                output_writer.println();
                output_writer.flush();
                //Writes body
                output_writer.write(body);
                output_writer.println();
                output_writer.flush();
                return (status_code = 200);
            } catch (Exception ex) {
                Logger.getLogger(HttpRequest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return 100;
    }
    
    /**
     * Opens the file in the system that corresponds to the request resource
     * @param resource_name a String containing the resource name
     * @return a File pointer
     */
    private File openFile(String resource_name){
        //Reads resources path from properties file
        String directory = WebServer.PROPERTIES.getProperty("DIRECTORY");
        try {
            return new File(directory, resource_name);
        }catch (NullPointerException ex){
            return new File(directory, "");
        }
    }
    
    /**
     * Checks if the specified resource is a directory. If it's not, returns a 100 (Continue)
     * status code. If it is, applies ALLOW directive and sends the adequate response through the
     * object's output_writer
     * @return 100 if it's not a directory, the pertinent status code if it is
     */
    private int handleDirectories(){
        //Handling if it asks for a directory
        if (resource_file.isDirectory()){
            //If it is a directory
            //Gets default directory index
            String default_res_name = WebServer.PROPERTIES.getProperty("DIRECTORY_INDEX"); //Default index in directory
            File default_resource = new File(resource_file, default_res_name);
            //Checks if the default resource for the directory exists
            if (default_resource.exists()){
                //If the request asks for a directory and the default file exists in it
                //Sets the default resource file as the current resource file
                this.resource_file = default_resource;
                //Returns 100 Continue
                return 100;
            }else{
                //The requested resource is a directory AND
                //The default file for the directory does not exist
                //The action depends on the ALLOW directive
                //Retrieve ALLOW directive from properties file
                String allow = WebServer.PROPERTIES.getProperty("ALLOW");
                if (allow.equalsIgnoreCase("true")){
                    //ALLOW directive enabled
                    //Sets status code to 404 (because the file the client asked for does not exist)
                    this.status_code = 404;
                    //Generates an HTML document with the directory listing
                    String[] dir_page = generateDirectoryPage(resource_file);
                    int dir_page_size = 0;
                    for (String line : dir_page)
                        dir_page_size += (line.length() + System.lineSeparator().length());
                    //Stores bytes send in the object
                    this.bytes_sent = dir_page_size;
                    //Sends the 404 Header
                    output_writer.write(fileNotFound());
                    output_writer.println("Content-type: text/html");
                    output_writer.println("Content-size: " + dir_page_size);
                    output_writer.println();
                    //Sends the blank line
                    output_writer.println();
                    //Sends the page
                    for (String line : dir_page)
                        output_writer.println(line);
                    return status_code;
                }else{
                    //ALLOW directive disabled
                    //Can't access a directory, so returns accessForbidden
                    output_writer.write(accessForbidden());
                    output_writer.println();
                    return status_code;
                }
            }
        }
        return 100;
    }
    
    /**
     * Dynamically generate an HTML Document that links every item in the specified directory,
     * and returns it in an array of Strings, line by line.
     * @param dir a FILE object containing the directory to list
     * @return The HTML page represented as an array of Strings, with each element being a line of the file
     */
    private String[] generateDirectoryPage(File dir){
        if(dir.isDirectory()){
            String dirname = this.request_lines[0].split(" ")[1].substring(1);
            ArrayList<String> page = new ArrayList(50);
            //The header of this generic document is 116 bytes long
            page.add("<!DOCTYPE html>");
            page.add("<html>");
            page.add("<head>");
            page.add("<meta charset=\"utf-8\" />");
            page.add("<meta http-equiv=\"Content-type\" content=\"text/html; charset=utf-8\" />");
            page.add("<title>Directory index</title>");
            page.add("</head>");
            page.add("");
            page.add("<body>");
            page.add("<h1>Listing of the directory " + dir.getName() + "</h1>");
            page.add("<ul>");
            String[] dir_list = dir.list();
            for (String item : dir_list) {
                page.add("<li><a href=\"" + "http://" + hostname + "/" + dirname + "/" + item + "\">" + item + "</a></li>");
            }
            page.add("</ul>");
            page.add("</body>");
            page.add("</html>");
            return page.toArray(new String[0]);
        }else{
            //The specified directory is not a directory
            return null;
        }
    }
    
    /**
     * Checks if there is a If-Modified-Since line, and if there is one, checks if
     * the file has been modified since the indicated date
     * @return the status code 100 if there is no If-Mod-Since line, or if the file
     * was modified, 304 if the file was not modified
     * @throws DateTimeParseException if the date is not correctly formatted
     */
    private int checkIfModified() throws DateTimeParseException{
        /*Checks for the If-Modified-Since line.
        if the field exists, checks whether the file was or not modified*/
            for (int i = 1; i<request_lines.length; i++){
                if (request_lines[i].startsWith("If-Modified-Since: ")){
                    if (wasModified(resource_file, request_lines[i].substring(19)))
                        return 100;
                    else
                        return (this.status_code = 304);
                }
            }
            return 100;
    }
    
    /**
     * Writes information about the request and response in a log file. If the request
     * caused an error, writes it in the same file
     * @param log_writer a PrintWriter object to write to the log file
     */
    public void log(PrintWriter log_writer){
        log(log_writer,log_writer);
    }
    
    /**
     * Writes information about the request and response in a log file. If the request
     * caused an error, writes it in the error logs file.
     * @param log_writer a PrintWriter object to write to the log file
     * @param error_writer a PrintWriter object to write to the error logs file
     */
    public void log(PrintWriter log_writer, PrintWriter error_writer){
        String petition_line = this.request_lines[0];
        if (this.status_code < 400){
            //NO ERRORS
            log_writer.println(petition_line);
            log_writer.println(this.client_ip);
            log_writer.print(this.log_date_time);
            log_writer.println(statusCodeString());
            log_writer.println("Bytes sent: " + this.bytes_sent);
            log_writer.println();
        }else{
            //ERROR LOG
            error_writer.println(petition_line);
            error_writer.println(this.client_ip);
            error_writer.print(this.log_date_time);
            error_writer.println(statusCodeString());
            error_writer.println();
        }
    }
    
    /**
     * Gives the status of the object, based on the status_code
     * @return a String with the name of the status code of this object
     */
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
    
    private static String getLogDate(){
        return "Date : " +
                ZonedDateTime.now(ZoneOffset.UTC).format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH))
                + " GMT"
                + System.lineSeparator();
    }
    
    /**
     * This method generates a generic HTTP 400 Bad Request response
     * Includes the header and the Date line
     * @return a String containing the response
     */
    private String badRequest(){
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.0 400 Bad Request");
        response.append(System.lineSeparator());
        response.append(getHttpDate());
        this.status_code = 400;
        return response.toString();
    }
    
    /**
     * This method generates a generic HTTP 403 Access Forbidden response
     * Includes header and Date line
     * @return a String containing the response
     */
    private String accessForbidden() {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.0 403 Access Forbidden");
        response.append(System.lineSeparator());
        response.append(getHttpDate());
        this.status_code = 403;
        return response.toString();
    }
    
    /**
     * This method generates a generic HTTP 404 Not Found response
     * Includes header and Date line
     * @return a String containing the response
     */
    private String fileNotFound(){
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.0 404 Not Found");
        response.append(System.lineSeparator());
        response.append(getHttpDate());
        this.status_code = 404;
        return response.toString();
    }
    
    /**
     * This method generates a generic HTTP 500 Internal Server Error response
     * Includes header and Date line
     * @return a String containing the response
     */
    public static String serverError(){
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.0 500 Internal Server Error");
        response.append(System.lineSeparator());
        response.append(getHttpDate());
        return response.toString();
    }
    
    /**
     * This method checks if a file has been modified since the date indicated
     * @param file the file to check
     * @param since the date formatted in a String
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