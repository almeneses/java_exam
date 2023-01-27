package com.almeneses.exam.client;

import java.io.IOException;
import java.net.Socket;

import com.almeneses.exam.client.controller.ClientController;
import com.almeneses.exam.client.ui.ClientUI;

public class Client {
    public static void main(String[] args) {
        try {

            Socket socket = new Socket("localhost", 1234);
            ClientUI clientUI = new ClientUI();
            ClientController controller = new ClientController(socket, clientUI);

            controller.init();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
