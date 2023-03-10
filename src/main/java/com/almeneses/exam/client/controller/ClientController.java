package com.almeneses.exam.client.controller;

import com.almeneses.exam.client.ui.ClientUI;
import com.almeneses.exam.models.Message;
import com.almeneses.exam.models.MessageType;
import com.almeneses.exam.models.Question;
import com.almeneses.exam.models.QuestionStatus;
import com.almeneses.exam.utils.TimeUtils;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

                    Message serverMessage = (Message) inStream.readObject();
                    processMessage(serverMessage);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void startExam(Message message) {
        List<String> questions = (ArrayList) message.getContent();

        for (String questionNumber : questions) {
            clientUI.getQuestionsComboBox().addItem(questionNumber);
        }

        this.clientUI.getGeneralEditorPane().setText(null);
        this.clientUI.getGeneralEditorPane().setText("<h2><strong>The exam has started!</strong></h2>\n");
        enableControls(true);
    }

    public void waitExam() {
        clientUI.getGeneralEditorPane().setText("<h2><strong>The exam will start in just a moment...</strong></h2>");
        enableControls(false);
    }

    public void finishExam() {
        clientUI.getGeneralEditorPane().setText("<h2 style='color: red;'><strong>Exam has ended!</strong></h2>");
        clientUI.getRemainingTimeValueLabel().setText(TimeUtils.toClockFormat(0));
        enableControls(false);
    }

    public void enableControls(boolean isEnabled) {
        clientUI.getSendAnswerBtn().setEnabled(isEnabled);
        clientUI.getQuestionsComboBox().setEnabled(isEnabled);
        enableQuestionActions(isEnabled);
    }

    public void updateTime(Message message) {
        int time = (int) message.getContent();
        clientUI.getRemainingTimeValueLabel().setText(TimeUtils.toClockFormat(time));
    }

    public void processMessage(Message message) {
        System.out.println(message.getType().name());
        switch (message.getType()) {
            case EXAM_WAIT -> waitExam();
            case EXAM_START -> startExam(message);
            case EXAM_END -> finishExam();
            case QUESTION_PICK -> processQuestionPick(message);
            case TIME_UPDATE -> updateTime(message);
            default -> {
            }
        }
    }

    public void enableQuestionActions(boolean isEnabled) {
        clientUI.getOptionsPanel().setEnabled(isEnabled);
        clientUI.getSendAnswerBtn().setEnabled(isEnabled);
    }

    public void processQuestionPick(Message message) {
        Question question = (Question) message.getContent();

        if (question.getStatus() != QuestionStatus.FREE) {
            showQuestionNotFreeMessage(question.getStatus());
            return;
        }
        showQuestion(question);
        showQuestionOptions(question);
        enableQuestionActions(true);
    }

    public void showQuestionNotFreeMessage(QuestionStatus questionStatus) {
        String template = "<h2><strong class='color: red;'>%1$s</strong></h2>";
        String formattedMessage = "";

        switch (questionStatus) {
            case TAKEN -> formattedMessage = String.format(template, "This question has been taken, pick another one");
            case ANSWERED ->
                    formattedMessage = String.format(template, "Sorry! This question has already been answered.");
            default -> {
            }
        }

        clientUI.getGeneralEditorPane().setText(formattedMessage);
        enableQuestionActions(false);

    }

    public void showQuestion(Question question) {
        String formattedText = String.format("<h3><strong>%1$s</strong></h3>", question.getStatement());
        clientUI.getGeneralEditorPane().setText(formattedText);
    }

    public void showQuestionOptions(Question question) {
        clientUI.getOptionsPanel().removeAll();

        for (String option : question.getOptions()) {
            JRadioButton radioButton = new JRadioButton(option);
            radioButton.setBounds(10, 10, 100, 25);
            radioButton.setActionCommand(option);
            clientUI.getOptionsButtonGroup().add(radioButton);
            clientUI.getOptionsPanel().add(radioButton);
        }
    }

    public void pickQuestion() {
        String selected = (String) clientUI.getQuestionsComboBox().getSelectedItem();

        if (selected != null && !selected.equals(this.currentQuestion)) {
            this.currentQuestion = selected;
            sendMessage(new Message(MessageType.QUESTION_PICK, selected));
        }
    }

    public void sendAnswer() {
        String answer = clientUI.getOptionsButtonGroup().getSelection().getActionCommand();
        Message message = new Message(MessageType.QUESTION_ANSWER, answer);
        sendMessage(message);

        clientUI.getGeneralEditorPane().setText("<strong>Answer sent! pick another question</strong>");
        enableQuestionActions(false);

    }

    public void initHandlers() {
        clientUI.getPickQuestionBtn().addActionListener(event -> pickQuestion());
        clientUI.getSendAnswerBtn().addActionListener(event -> sendAnswer());
    }

    public void init() {
        clientUI.getGeneralEditorPane().setContentType("text/html");
        clientUI.getGeneralEditorPane().setEditable(false);
        initHandlers();
        clientUI.setVisible(true);
        receiveMessages();

    }
}
