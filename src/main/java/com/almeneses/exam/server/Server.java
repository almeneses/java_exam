package com.almeneses.exam.server;

import java.io.IOException;
import java.net.ServerSocket;

import com.almeneses.exam.server.controller.ServerController;
import com.almeneses.exam.server.ui.ServerUI;

public class Server {
    public static void main(String[] args) {
        try {

            ServerSocket serverSocket = new ServerSocket(1234);

            try {
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(ServerUI.class.getName()).log(java.util.logging.Level.SEVERE, null,
                        ex);
            }

            ServerUI serverUI = new ServerUI();
            ServerController serverController = new ServerController(serverSocket, serverUI);

            serverController.init();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
