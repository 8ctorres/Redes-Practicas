/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.udc.redes.webserver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * An HTTP Thread, given an existing, open socket, listens for an HTTP Request,
 * handles it and then sends back the adequate response into that socket
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
     * @param socket - The TCP Socket through which the HTTP Requests will come in
     * @param log_writer - A PrintWriter object that can be used to write logs
     * @param error_writer - A PrintWriter object that can be used to write error logs
     */
    public HttpThread(Socket socket, PrintWriter log_writer, PrintWriter error_writer){
        this.socket = socket;
        this.log_writer = log_writer;
        this.error_writer = error_writer;
    }
    @Override
    public void run(){
        try{
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
        }
        catch(IOException e){
            System.out.println("IO Exception while closing output streams!!");
            e.printStackTrace();
        }
        finally{
            try {
                socket.close();
            } catch (IOException ex) {
                System.out.println("I/O Exception while closing socket!!");
                ex.printStackTrace();
            }
        }
    }
}
