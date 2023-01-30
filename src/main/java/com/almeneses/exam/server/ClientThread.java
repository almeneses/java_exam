package com.almeneses.exam.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import com.almeneses.exam.models.Message;
import com.almeneses.exam.models.MessageType;
import com.almeneses.exam.models.Question;
import com.almeneses.exam.models.QuestionStatus;

public class ClientThread implements Runnable {

    private Socket socket;

    private List<Question> questions;
    private List<ClientThread> clients;
    private Question currentQuestion;
    private ObjectOutputStream outStream;
    private ObjectInputStream inStream;
    private String clientName;

    public ClientThread(Socket socket, List<Question> questions, List<ClientThread> clients) {
        try {
            this.socket = socket;
            this.questions = questions;
            this.clients = clients;
            this.outStream = new ObjectOutputStream(socket.getOutputStream());
            this.inStream = new ObjectInputStream(socket.getInputStream());
            this.currentQuestion = null;
        } catch (Exception e) {
            try {
                this.socket.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public void receiveMessage() {
        try {

            while (socket.isConnected()) {
                Message message = (Message) this.inStream.readObject();
                processMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processMessage(Message message) {
        switch (message.getType()) {
            case QUESTION_ANSWER -> processResponse(message);
            case QUESTION_PICK -> {
                pickQuestion(message);
                sendMessage(new Message(MessageType.QUESTION_PICK, this.currentQuestion));
                this.currentQuestion.setStatus(QuestionStatus.TAKEN);
            }
            default -> {
            }
        }

    }

    public void qualifyQuestion(Question question) {
        boolean isCorrect = question.getCorrectAnswer().equals(question.getAnswerGiven());

        question.setCorrect(isCorrect);
        question.setAnsweredBy(this.clientName);
        question.setStatus(QuestionStatus.ANSWERED);
    }

    public void processResponse(Message message) {
        Question answerQuestion = (Question) message.getContent();
        qualifyQuestion(answerQuestion);
    }

    public void pickQuestion(Message message) {
        String question = (String) message.getContent();
        Question updateQuestion = findQuestion(question);

        if (this.currentQuestion != null && !this.currentQuestion.equals(updateQuestion)) {
            this.currentQuestion.setStatus(QuestionStatus.FREE);
        }

        this.currentQuestion = updateQuestion;
    }

    public Question findQuestion(String numQuestion) {
        for (Question question : questions) {
            if (question.getNumber().equals(numQuestion)) {
                return question;
            }
        }

        return null;
    }

    public void sendMessage(Message message) {
        try {
            this.outStream.reset();
            this.outStream.writeObject(message);
            this.outStream.flush();
        } catch (IOException e) {
            try {
                this.outStream.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        receiveMessage();
        sendMessage(new Message(MessageType.EXAM_WAIT, "The exam will start shortly..."));
    }
}
