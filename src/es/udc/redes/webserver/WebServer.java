package es.udc.redes.webserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;

/**
 * A Webserver listens for new TCP connections, estabilishes a socket and then
 * launches a new thread to generate and send the adequate HTTP response
 * @author Carlos Torres (carlos.torres@udc.es)
 */
public class WebServer {
    /**
     * Indicates where the webserver resources and logs are located in the File System.
     * This is only temporary until the configuration file is implemented
     */
    public static final String RESOURCES_PATH = "C:\\Users\\carlo\\FIC\\Redes\\resources";
    public static final String LOG_PATH = "C:\\Users\\carlo\\FIC\\Redes\\logs\\webserver.log";
    public static final String ERROR_LOG_PATH = "C:\\Users\\carlo\\FIC\\Redes\\logs\\error.log";
    /**
     * Opens the log file
     * @return a PrintWriter object that can be used to write to the log file
     */
    private static PrintWriter openLog() throws FileNotFoundException, IOException{
        File log = new File(LOG_PATH);
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
        File error_log = new File(ERROR_LOG_PATH);
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
        if (argv.length != 1) {
            System.err.println("Format: TcpServer <port>");
            System.exit(-1);
        }
        try{
            ServerSocket servidor = new ServerSocket(Integer.parseInt(argv[0]));
            while (true){
                (new HttpThread(servidor.accept(), openLog(), openErrorLog())).start();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }    
}
