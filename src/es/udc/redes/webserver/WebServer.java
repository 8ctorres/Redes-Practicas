package es.udc.redes.webserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Webserver listens for new TCP connections, estabilishes a socket and then
 * launches a new thread to generate and send the adequate HTTP response
 * @author Carlos Torres (carlos.torres@udc.es)
 */
public class WebServer {
    /**
     * Loads the webserver properties file
     */
    public static Properties PROPERTIES;
    
    /**
     * This method loads the webserver properties from the file
     * @param path the Path to the properties file
     * @return the Properties object
     */
    public static Properties loadProperties(String path){
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(new File(path)));
        } catch (FileNotFoundException ex) {
            System.err.println("ERROR: Properties file not found");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println("ERROR: Properties file could not be opened");
            System.exit(-1);
        }
        System.out.println("Properties file loaded successfully from " + path);
        return prop;
    }
    
    /**
     * Opens the log file
     * @return a PrintWriter object that can be used to write to the log file
     */
    private static PrintWriter openLog() throws FileNotFoundException, IOException{
        String path = PROPERTIES.getProperty("LOG_FILE");
        File log = new File(path);
        if (!log.isFile()){
            /*The FileNotFoundException exception should never occur because it is checked that
            the file exists BEFORE trying to open a PrintWriter to it*/
            log.createNewFile();
        }
        //Opens the file in append mode, and enables autoflush
        return new PrintWriter(new FileOutputStream(log, true), true);
    }
    
    /**
     * Opens the error log file
     * @return a PrintWriter object that can be used to write to the log file
     */
    private static PrintWriter openErrorLog() throws FileNotFoundException, IOException{
        String path = PROPERTIES.getProperty("ERROR_LOG_FILE");
        File error_log = new File(path);
        if(!error_log.isFile()){
            error_log.createNewFile();
        }
        return new PrintWriter(new FileOutputStream(error_log, true), true);
    }
    
    /**
     * Main method, the webserver runs this method when it is launched
     * It continuosly listens for new TCP connections and when a socket is
     * estabilished, launches a new thread and passes it as an argument.
     * @param argv = arguments given in the command line. None are needed
     */
    public static void main(String argv[]) {
        if (argv.length != 1) {
            System.err.println("ERROR: Argument required (path to server.properties file)");
            System.exit(-1);
        }else{
            WebServer.PROPERTIES = WebServer.loadProperties(argv[0]);
        }
        try{
            ServerSocket servidor = new ServerSocket(Integer.parseInt(PROPERTIES.getProperty("PORT")));
            while (true){
                (new HttpThread(servidor.accept(), openLog(), openErrorLog())).start();
            }
        } catch (IOException ex) {
            Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
}
