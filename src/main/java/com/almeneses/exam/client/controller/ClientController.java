package com.almeneses.exam.client.controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import com.almeneses.exam.client.ui.ClientUI;
import com.almeneses.exam.models.Message;
import com.almeneses.exam.models.MessageType;
import com.almeneses.exam.models.Question;
import com.almeneses.exam.models.QuestionStatus;
import com.almeneses.exam.utils.TimeUtils;

public class ClientController {

    private Socket socket;
    private ObjectOutputStream outStream;
    private ObjectInputStream inStream;
    private String clientName;
    private ClientUI clientUI;
    private String currentQuestion;

    public ClientController(Socket socket, ClientUI clientUI) {
        try {
            this.socket = socket;
            this.clientUI = clientUI;
            this.clientName = "Client - " + LocalDateTime.now();
            this.outStream = new ObjectOutputStream(socket.getOutputStream());
            this.inStream = new ObjectInputStream(socket.getInputStream());
            this.clientUI.getGeneralEditorPane().setContentType("text/html");
            this.currentQuestion = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Message mensaje) {
        try {
            this.outStream.reset();
            this.outStream.writeObject(mensaje);
            this.outStream.flush();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public void receiveMessages() {
        new Thread(() -> {
            try {

                while (socket.isConnected()) {

                    Message mensajeDelServer = (Message) inStream.readObject();
                    processMessage(mensajeDelServer);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void startExam(Message mensaje) {
        List<String> questions = (ArrayList) mensaje.getContent();

        for (String questionNumber : questions) {
            clientUI.getQuestionsComboBox().addItem(questionNumber);
        }

        this.clientUI.getGeneralEditorPane().setText(null);
        this.clientUI.getGeneralEditorPane().setText("<h2><strong>The exam has started!</strong></h2>\n");
    }

    public void waitExam() {
        clientUI.getGeneralEditorPane().setText(null);
        clientUI.getGeneralEditorPane().setText("<h2><strong> The exam will start in just a moment...</strong></h2>");
    }

    public void updateTime(Message mensaje) {
        int time = (int) mensaje.getContent();
        clientUI.getRemainingTimeValueLabel().setText(TimeUtils.toClockFormat(time));
    }

    public void processMessage(Message message) {
        System.out.println(message.getType().name());
        switch (message.getType()) {
            case EXAM_WAIT -> waitExam();
            case EXAM_START -> startExam(message);
            case QUESTION_PICK -> processQuestionPick(message);
            case TIME_UPDATE -> updateTime(message);
            default -> {
            }
        }
    }

    public void processQuestionPick(Message message) {
        Question question = (Question) message.getContent();

        if (question.getStatus() != QuestionStatus.FREE) {
            showQuestionNotFreeMessage(question.getStatus());
            return;
        }
        showQuestion(question);
        showQuestionOptions(question);
    }

    public void showQuestionNotFreeMessage(QuestionStatus questionStatus) {
        String template = "<h2><strong class='color: red;'>%1$s</strong></h2>";
        String formattedMessage = "";

        switch (questionStatus) {
            case TAKEN:
                formattedMessage = String.format(template, "This question has been taken, pick another one");
                break;
            case ANSWERED:
                formattedMessage = String.format(template, "Sorry! This question has already been answered.");
            default:
                break;
        }

        clientUI.getGeneralEditorPane().setText(formattedMessage);
    }

    public void showQuestion(Question question) {
        String formattedText = String.format("<h3><strong>%1$s</strong></h3>", question.getStatement());
        clientUI.getGeneralEditorPane().setText(formattedText);
    }

    public void showQuestionOptions(Question question) {

        ButtonGroup buttonGroup = new ButtonGroup();

        for (String option : question.getOptions()) {
            JRadioButton radioButton = new JRadioButton(option);
            buttonGroup.add(radioButton);
            clientUI.getOptionsPanel().add(radioButton);
        }
    }

    public void pickQuestion() {
        String selected = (String) clientUI.getQuestionsComboBox().getSelectedItem();

        if(selected != null  && !selected.equals(this.currentQuestion)) {
            this.currentQuestion = selected;
            sendMessage(new Message(MessageType.QUESTION_PICK, selected));
        }
    }

    public void enviarPregunta() {
        Message mensaje = new Message(MessageType.QUESTION_ANSWER, currentQuestion);
        sendMessage(mensaje);
    }

    public void initHandlers() {
        clientUI.getPickQuestionBtn().addActionListener(event -> pickQuestion());
        clientUI.getSendAnswerBtn().addActionListener(event -> enviarPregunta());
    }

    public void init() {
        clientUI.getGeneralEditorPane().setContentType("text/html");
        clientUI.getGeneralEditorPane().setEditable(false);
        initHandlers();
        clientUI.setVisible(true);
        receiveMessages();

    }
}
