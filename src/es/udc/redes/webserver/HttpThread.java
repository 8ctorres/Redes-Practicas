package es.udc.redes.webserver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a Thread that will be used to handle an HTTP request
 * This class provides the run() method, that given an existing, open socket,
 * reads an incoming HTTP Request, handles it and then sends back the
 * adequate response into that socket. It also logs all the requests to a file.
 * @author Carlos Torres (carlos.torres@udc.es)
 */
public class HttpThread extends Thread{
    private final Socket socket;
    private final PrintWriter log_writer;
    private final PrintWriter error_writer;
    private BufferedReader input;
    private OutputStream output;
    /**
     * Builds an HttpThread from an open Socket, and specifies streams to write logs and error logs
     * @param socket The TCP Socket through which the HTTP Requests will come in
     * @param log_writer A PrintWriter object that can be used to write logs
     * @param error_writer A PrintWriter object that can be used to write error logs
     */
    public HttpThread(Socket socket, PrintWriter log_writer, PrintWriter error_writer){
        this.socket = socket;
        this.log_writer = log_writer;
        this.error_writer = error_writer;
    }
    /**
     * Reads the request from the socket, processes it and sends the response back into that socket.
     * It also adds the request to the log files.
     */
    @Override
    public void run(){
        try {
            //Set input channel
            input = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));
            //Set output channel
            output = socket.getOutputStream();
            //reads line by line then builds a string with the
            //request that will be passed to the handler class
            String line;
            StringBuilder rq_builder = new StringBuilder(); 
            //Read incoming request
            do {
                line = input.readLine();
                rq_builder.append(line);
                rq_builder.append(System.lineSeparator());
            } while (!"".equals(line));
            String request = rq_builder.toString();
            HttpRequest handler = new HttpRequest(request, socket.getInetAddress());
            //The HttpRequest handler responds using the output stream
            handler.respond(output);
            //Adds the corresponding log entry
            handler.log(log_writer, error_writer);
            output.flush();
            //Close streams
            input.close();
            output.close();
        } catch (IOException ex) {
            Logger.getLogger(HttpThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OutOfMemoryError err) {
            /*
            During testing, sometimes the rq_builder.append(line) call in line 55, inside
            the do-while loop would throw an "OutOfMemoryError: Java heap space" error.
            I could not find a way to replicate the error, as it is apparently random, and
            it was impossible for me to solve it. If and when that happens, that error is
            caught here and the server returns a "500 Internal Server Error" HTTP Status Code
            
            Here I add a snapshot of the Error, just so it is documented if anybody would want
            to try and solve it:
            *
            Exception in thread "Thread-7" java.lang.OutOfMemoryError: Java heap space
                at java.util.Arrays.copyOf(Arrays.java:3332)
                at java.lang.AbstractStringBuilder.ensureCapacityInternal(AbstractStringBuilder.java:124)
                at java.lang.AbstractStringBuilder.append(AbstractStringBuilder.java:448)
                at java.lang.StringBuilder.append(StringBuilder.java:136)
                at es.udc.redes.webserver.HttpThread.run(HttpThread.java:55)
            *
            */
            try (PrintWriter output_writer = new PrintWriter(this.output, true)) {
                output_writer.write(HttpRequest.serverError());
            }
            try{
                input.close();
                output.close();
            }catch(IOException ex){
                Logger.getLogger(HttpThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        finally{
            try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(HttpThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
