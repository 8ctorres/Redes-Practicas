package es.udc.redes.tutorial.udp.server;

import java.net.*;

/**
 * Implements an UDP Echo Server.
 */
public class UdpServer {

    public static void main(String argv[]) {
        if (argv.length != 1) {
            System.err.println("Format: UdpServer <port_number>");
            System.exit(-1);
        }
        DatagramSocket socket = null;
        try {
            // Create a server socket
            int portn = Integer.parseInt(argv[0]);
            socket = new DatagramSocket(portn);
            // Set max. timeout to 300 secs
            socket.setSoTimeout(300000);
            while (true) {
                // Prepare datagram for reception
                //Creates buffer for the datagrams
                byte buff[] = new byte[512];
                DatagramPacket recibido = new DatagramPacket(buff, buff.length);
                // Receive the message
                socket.receive(recibido);
                // Prepare datagram to send response
                String entrante = new String(buff);
                String saliente =
                        "This is the UDP Server. You said: " + entrante;
                byte send[] = saliente.getBytes();
                DatagramPacket respuesta = new DatagramPacket(
                        send, send.length,
                        recibido.getAddress(), recibido.getPort());
                // Send response
                socket.send(respuesta);
            }
          
        // Uncomment next catch clause after implementing the logic
        } catch (SocketTimeoutException e) {
            System.err.println("No requests received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}
