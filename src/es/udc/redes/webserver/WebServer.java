package es.udc.redes.webserver;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * A Webserver listens for new TCP connections, estabilishes a socket and then
 * launches a new thread to generate and send the adequate HTTP response
 * @author Carlos Torres (carlos.torres@udc.es)
 */
public class WebServer {
    /**
     * Indicates where the webserver resources are located in the File System
     */
    public static final String RESOURCES_PATH = "C:\\Users\\carlo\\FIC\\Redes\\resources";
    /**
     * Main method, the webserver runs this method when it is launched
     * It continuosly listens for new TCP connections and when a socket is
     * estabilished, launches a new thread and passes it as an argument.
     * @param argv = arguments given in the command line
     * @returns void
     */
    public static void main(String argv[]) {
        if (argv.length != 1) {
            System.err.println("Format: TcpServer <port>");
            System.exit(-1);
        }
        try{
            ServerSocket servidor = new ServerSocket(Integer.parseInt(argv[0]));
            while (true){
                (new HttpThread(servidor.accept())).start();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }    
}
