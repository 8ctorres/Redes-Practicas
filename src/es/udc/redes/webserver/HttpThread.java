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
 * 
 * @author carlo
 */
public class HttpThread extends Thread{
    private final Socket socket;
    private BufferedReader input;
    private OutputStream output;
    public HttpThread(Socket socket){
        this.socket = socket;
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
            HttpRequest handler = new HttpRequest(request);
            //The HttpRequest handler responds using the output stream
            handler.respond(output);
            output.flush();
            //Close streams
            input.close();
            output.close();
        }
        catch(Exception e){
            System.out.println("Exception!!");
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
