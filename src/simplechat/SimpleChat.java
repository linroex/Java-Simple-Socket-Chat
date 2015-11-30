/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simplechat;

import java.awt.Frame;
import simplechat.ChatFrame;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;

import javax.swing.JTextArea;
import javax.swing.JOptionPane;


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
    
    public void listenFromClients() {
        Thread ListenFromClientsThread = new Thread(new ListenFromClientsRunnable(this.port));
        ListenFromClientsThread.start();
    }
    
    public void listenFromServer() {
        Thread listenFromServerThread = new Thread(new ListenFromServerRunnable());
        listenFromServerThread.start();
    }
    
    public String getAttender() {
        return this.clients.toString();
    }
    
    private class ListenFromServerRunnable implements Runnable {
        @Override
        public synchronized void run() {
            try {
                DataInputStream inputFromServer = new DataInputStream(socket.getInputStream());
                
                while(true) {
                    String data = inputFromServer.readUTF();
                    String[] columns = data.split(" ", 2);
                    
                    switch(columns[0]) {
                        case "/msg":
                            textArea.append(columns[1]);
                            break;
                        case "/attender":
                            JOptionPane.showMessageDialog(new Frame(), columns[1]);
                            break;
                    }
                    
                }
                
            } catch (IOException e) {
                System.out.println("Listen from server failed:" + e.getMessage());
            }
        }
    }
    
    private class ListenFromClientsRunnable implements Runnable {
        private ServerSocket server;
        
        public ListenFromClientsRunnable(int port) {
            
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

                    Thread ReceiverThread = new Thread(new ReceiverRunnable(socket, clients.size() - 1));
                    ReceiverThread.start();

                } catch (IOException e) {
                    System.out.println("ServerSocket accept failed: " + e.getMessage());
                }
            }
        }
    }
    
    private class ReceiverRunnable implements Runnable {
        private final Socket socket;
        private DataInputStream input;
        private DataOutputStream output;
        private String name;
        private int index;
        
        public ReceiverRunnable(Socket socket, int index) {
            this.socket = socket;
            this.name = "None";
            this.index = index;
            
            try {
                this.input = new DataInputStream(socket.getInputStream());
                this.output = new DataOutputStream(socket.getOutputStream());
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
                    String data = this.input.readUTF();
                    String[] columns = data.split(" ", 2);
                    
                    switch(columns[0]) {
                        case "/setname":
                            this.name = columns[1];
                            break;
                        case "/attender":
                            this.output.writeUTF("/attender " + getAttender());
                            break;
                        case "/msg":
                            textArea.append(this.name + ": " + columns[1] + "\n");
                            broadcast("/msg " + this.name + ": " + columns[1] + "\n");
                            break;
                            
                    }
                    
                } catch (IOException e) {
                    System.out.println("client break");
                    flag = false;
                    
                    clients.remove(this.index);
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
    
    public void sendDataToServer(String str) {
        try {
            this.output.writeUTF(str);
            this.output.flush();
        } catch (IOException e) {
            System.out.println("send data failed:" + e.getMessage());
        }
    }
    
    public void sendCommandToServer(String command, String data) {
        this.sendDataToServer(command + data);
    }
    
    public void sendMessageToServer(String message) {
        this.sendDataToServer("/msg " + message);
    }
    
    public void broadcast(String message) {
        try {
            for (Socket client : this.clients) {
                DataOutputStream outputTemp = new DataOutputStream(client.getOutputStream());
                outputTemp.writeUTF(message);
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
