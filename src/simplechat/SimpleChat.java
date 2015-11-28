/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simplechat;

import simplechat.ChatFrame;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;

/**
 *
 * @author linroex
 */
public class SimpleChat {
    private final ArrayList<Socket> clients;
    private DataInputStream input;
    private DataOutputStream output;
    
    private final int port = 6543;
    
    public SimpleChat() {
        this.clients = new ArrayList();
    }
    
    public void listen() {
        Thread ListenThread = new Thread(new ListenRunnable(this.port));
        ListenThread.start();
    }
    
    private class ListenRunnable implements Runnable {
        private ServerSocket server;
        
        public ListenRunnable(int port) {
            try {
                this.server = new ServerSocket(port);
            } catch (IOException e) {
                System.out.println("ServerSocket init failed:" + e.getMessage());
            }
        }
        
        @Override
        public void run() {
            while (true) {
                try {
                    Socket socket = this.server.accept();

                    System.out.println("Connect success:" + socket.getInetAddress());

                    clients.add(socket);

                    Thread ReceiverThread = new Thread(new ReceiverRunnable(socket, "name"));
                    ReceiverThread.start();

                } catch (IOException e) {
                    System.out.println("ServerSocket accept failed: " + e.getMessage());
                }
            }
        }
    }
    
    private class ReceiverRunnable implements Runnable {
        private final Socket socket;
        private final String name;
        
        public ReceiverRunnable(Socket socket, String name) {
            this.socket = socket;
            this.name = name;
        }
        
        @Override
        public void run() {
            System.out.println("listen..." + this.socket.getInetAddress());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ChatFrame chatFrame = new ChatFrame();
        chatFrame.setVisible(true);
    }
    
}
