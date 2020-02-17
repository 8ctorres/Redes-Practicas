package es.udc.redes.tutorial.tcp.server;

import java.net.*;
import java.io.*;

/**
 * Multithread TCP echo server.
 */
public class TcpServer {

    public static void main(String argv[]) {
        if (argv.length != 1) {
            System.err.println("Format: TcpServer <port>");
            System.exit(-1);
        }
        try{
            ServerSocket servidor = new ServerSocket(Integer.parseInt(argv[0]));
            while (true){
                (new ServerThread(servidor.accept())).start();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}

class ServerThread extends Thread {
    ServerThread(Socket socket){
        this.socket = socket;
    }
    private Socket socket = null;
    @Override
    public void run(){
        try{
            //Set input channel
            BufferedReader input = new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream()));
            //Set output channet
            PrintWriter output = new PrintWriter(socket.getOutputStream(),true);
            //Read message
            String entrante = input.readLine();
            System.out.println("Received message: " + entrante);
            //Send response
            String saliente = "This is the TCP Server. You said :" + entrante;
            output.print(saliente);
            output.flush();
            //Close streams
            input.close();
            output.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                socket.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
