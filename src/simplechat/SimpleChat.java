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
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;

import javax.swing.JTextArea;


/**
 *
 * @author linroex
 */
public class SimpleChat {
    private final ArrayList<Socket> clients;
    private final int port = 6543;
    private final JTextArea textArea;
    private DataOutputStream output;
    private Socket socket;
    
    public SimpleChat(JTextArea textArea) {
        this.clients = new ArrayList();
        this.textArea = textArea;
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

                    Thread ReceiverThread = new Thread(new ReceiverRunnable(socket));
                    ReceiverThread.start();

                } catch (IOException e) {
                    System.out.println("ServerSocket accept failed: " + e.getMessage());
                }
            }
        }
    }
    
    private class ReceiverRunnable implements Runnable {
        private final Socket socket;
        private String name;
        private DataInputStream input;
                
        public ReceiverRunnable(Socket socket) {
            this.socket = socket;
            this.name = "None";
            
            try {
                this.input = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                
                System.out.println("Receiver socket input stream failed:" + e.getMessage());
            }
        }
        
        @Override
        public synchronized void run() {
            boolean flag = true;
            
            System.out.println("listen..." + this.socket.getInetAddress());
            
            while(flag) {
                try {
                    String[] data = this.input.readUTF().split(" ");
                    
                    if(data[0].equals("/cmd")) {
                        
                        switch(data[1]) {
                            case "setname":
                                this.name = data[2];
                                break;
                        }
                        
                    }else if(data[0].equals("/msg")) {
                        textArea.append(this.name + ": " + data[1] + "\n");
                        broadcast(this.name + ": " + data[1] + "\n");
                    }
                    
                } catch (IOException e) {
                    System.out.println("client break");
                    flag = false;
                }
            }
        }
    }
    
    public void connect(String address) {
        try {
            this.socket = new Socket(address, this.port);

            System.out.println("Connection success");
            
            this.output = new DataOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Connect server error:" + e.getMessage());
        }
    }
    
    public void sendData(String str) {
        try {
            this.output.writeUTF(str);
            this.output.flush();
        } catch (IOException e) {
            System.out.println("send data failed:" + e.getMessage());
        }
    }
    
    public void sendCommand(String command) {
        this.sendData("/cmd " + command);
    }
    
    public void sendMessage(String message) {
        this.sendData("/msg " + message);
    }
    
    public void broadcast(String message) {
        try {
            for (Socket client : this.clients) {
                DataOutputStream outputTemp = new DataOutputStream(client.getOutputStream());
                outputTemp.writeUTF("/msg " + message);
                outputTemp.flush();
            }
        } catch (IOException e) {
            System.out.println("Broadcast failed:" + e.getMessage());
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
