/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.udc.redes.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 
 * @author carlo
 */
public class HttpThread extends Thread{
    private final Socket socket;
    private BufferedReader input;
    private PrintWriter output;
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
            output = new PrintWriter(socket.getOutputStream());
            //Read incoming request
            String request = input.readLine();
            //Prints the request in command line (only for development purposes)
            System.out.println("Received request:");
            System.out.println(request);
            
            
            //Close streams
            input.close();
            output.close();
        }
        catch(IOException e){
            System.out.println("I/O Exception!!");
            e.printStackTrace();
        }
        finally{
            try {
                socket.close();
            } catch (IOException ex) {
                System.out.println("I/O Exception while closing socket!!");
            }
        }
    }
}
