/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simplechat;

import simplechat.ChatFrame;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;

/**
 *
 * @author linroex
 */
public class SimpleChat {
    private ServerSocket server;
    private ArrayList<Socket> clients;
    private DataInputStream input;
    private DataOutputStream output;
    
    private final int port = 6543;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ChatFrame chatFrame = new ChatFrame();
    }
    
}
