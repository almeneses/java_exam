package com.almeneses.exam.server.controller;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.almeneses.exam.models.Message;
import com.almeneses.exam.models.MessageType;
import com.almeneses.exam.models.Question;
import com.almeneses.exam.server.ClientThread;
import com.almeneses.exam.server.ui.ServerUI;
import com.almeneses.exam.utils.TimeUtils;

public class ServerController {

    private ServerSocket serverSocket;

    private List<Question> questions;
    private List<ClientThread> clients;
    private int examTime;
    private boolean hasExamStarted;
    private ServerUI serverUI;

    public ServerController(ServerSocket serverSocket, ServerUI serverUI) {
        this.serverSocket = serverSocket;
        this.clients = new ArrayList<>();
        this.questions = new ArrayList<>();
        this.examTime = 45;
        this.hasExamStarted = false;
        this.serverUI = serverUI;
    }

    public void loadQuestions(File file) {
        this.questions.clear();
        try (
                FileReader fileReader = new FileReader(file);
                BufferedReader reader = new BufferedReader(fileReader)) {

            String line = reader.readLine();

            while (line != null) {
                String[] questionSplit = line.split(":");
                String[] statementSplit = questionSplit[0].split("\\)");
                String[] options = questionSplit[1].split("-");
                String answer = questionSplit[2];
                String questionNumber = statementSplit[0];
                String statement = statementSplit[1];

                questions.add(new Question(questionNumber, statement, options, answer));

                line = reader.readLine();
            }

            System.out.println(questions.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showQuestions() {
        serverUI.getGeneralInfoTextArea().setText(null);
        serverUI.getGeneralInfoTextArea().append("The following quiz questions have been loaded:\n\n");

        for (Question question : questions) {

            serverUI.getGeneralInfoTextArea().append("-- Statement: " + question.getStatement() + "\n");

            for (String option : question.getOptions()) {
                serverUI.getGeneralInfoTextArea().append(" - Option: " + option + "\n");
            }
        }
    }

    public void startTest() {
        try {
            this.hasExamStarted = true;
            Message message = new Message(MessageType.EXAM_START, questions);

            for (ClientThread client : clients) {
                client.sendMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        serverUI.getExamStatusValueLabel().setForeground(new Color(75, 139, 59));
        serverUI.getExamStatusValueLabel().setText("In Progress...");
    }

    public void startCounter() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    int examTimeSeconds = examTime * 60;

                    while (examTimeSeconds > 0) {
                        Message timeMessage = new Message(MessageType.TIME_UPDATE, examTimeSeconds);

                        for (ClientThread client : clients) {
                            client.sendMessage(timeMessage);
                        }

                        examTimeSeconds--;
                        serverUI.getRemainingTimeValueLabel().setText(TimeUtils.toClockFormat(examTimeSeconds));
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }

            }

        }).start();

    }

    public void sendInitialMessage(ClientThread client) {
        Message message = new Message();

        if (this.hasExamStarted) {
            message.setType(MessageType.EXAM_START);
            message.setContent(questions);
        } else {
            message.setType(MessageType.EXAM_WAIT);
        }

        client.sendMessage(message);
    }

    public void loadExam() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Plain Text files", "txt"));
        int result = fileChooser.showOpenDialog(serverUI);

        if (result != JFileChooser.CANCEL_OPTION) {
            File fileName = fileChooser.getSelectedFile();
            loadQuestions(fileName);
            showQuestions();

            serverUI.getExamStatusValueLabel().setForeground(Color.BLUE);
            serverUI.getExamStatusValueLabel().setText("Exam loaded");

        }
    }

    public void initHandlers() {
        serverUI.getStartExamBtn().addActionListener(event -> {
            startTest();
            startCounter();
        });
        serverUI.getLoadExamBtn().addActionListener(event -> loadExam());
    }

    public void initValues() {
        serverUI.getExamStatusValueLabel().setForeground(Color.RED);
        serverUI.getExamStatusValueLabel().setText("Exam not loaded");

        serverUI.getRemainingTimeValueLabel().setText(String.valueOf(examTime) + " mins.");
    }

    public void initServer() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    while (serverSocket != null && !serverSocket.isClosed()) {
                        Socket socket = serverSocket.accept();

                        ClientThread nuevoCliente = new ClientThread(socket, questions, clients);
                        clients.add(nuevoCliente);

                        Thread hilo = new Thread(nuevoCliente);
                        hilo.start();

                        serverUI.getConnectedTextArea().append("Nuevo cliente conectado\n");

                        sendInitialMessage(nuevoCliente);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();

    }

    public void init() {
        initValues();
        initHandlers();
        serverUI.setVisible(true);
        initServer();

    }

}
