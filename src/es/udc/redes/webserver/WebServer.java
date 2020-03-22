package es.udc.redes.webserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
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
     * Indicates where the webserver properties file is located in the system
     */
    public static final String PROPERTIES_PATH = "C:\\Users\\carlo\\FIC\\Redes\\src\\server.properties";
    public static final Properties PROPERTIES = WebServer.loadProperties();
    /**
     * This method loads the webserver properties from the file
     * @return 
     */
    public static Properties loadProperties(){
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(PROPERTIES_PATH));
        } catch (FileNotFoundException ex) {
            System.out.println("Properties file does not exist");
            Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            System.out.println("Properties file can't be opened");
            Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return prop;
    }
    /**
     * Opens the log file
     * @return a PrintWriter object that can be used to write to the log file
     */
    private static PrintWriter openLog() throws FileNotFoundException, IOException{
        File log = new File(PROPERTIES.getProperty("LOG_FILE"));
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
        File error_log = new File(PROPERTIES.getProperty("ERROR_LOG_FILE"));
        if(!error_log.isFile()){
            error_log.createNewFile();
        }
        return new PrintWriter(new FileOutputStream(error_log, true), true);
    }
    /**
     * Main method, the webserver runs this method when it is launched
     * It continuosly listens for new TCP connections and when a socket is
     * estabilished, launches a new thread and passes it as an argument.
     * @param argv = arguments given in the command line
     */
    public static void main(String argv[]) {
        if (argv.length != 0) {
            System.err.println("No arguments required");
            System.exit(-1);
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
