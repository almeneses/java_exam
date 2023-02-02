package com.almeneses.exam.server;

import com.almeneses.exam.models.Message;
import com.almeneses.exam.models.MessageType;
import com.almeneses.exam.models.Question;
import com.almeneses.exam.models.QuestionStatus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

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
            case QUESTION_ANSWER -> processQuestionAnswer(message);
            case QUESTION_PICK -> processQuestionPick(message);

            default -> {
            }
        }

    }

    public void qualifyQuestion(String answer) {
        boolean isCorrect = answer.equals(this.currentQuestion.getCorrectAnswer());

        this.currentQuestion.setCorrect(isCorrect);
        this.currentQuestion.setAnsweredBy(this.clientName);
        this.currentQuestion.setStatus(QuestionStatus.ANSWERED);
    }

    public void processQuestionAnswer(Message message) {
        if (this.currentQuestion != null && !this.currentQuestion.getStatus().equals(QuestionStatus.ANSWERED)) {
            String answer = (String) message.getContent();
            qualifyQuestion(answer);
        }
    }

    public void processQuestionPick(Message message) {
        String questionNumber = (String) message.getContent();
        Question updateQuestion = findQuestion(questionNumber);

        if(updateQuestion.getStatus() == QuestionStatus.FREE){
            sendMessage(new Message(MessageType.QUESTION_PICK, updateQuestion));

            if(this.currentQuestion == null){
                this.currentQuestion = updateQuestion;
                this.currentQuestion.setStatus(QuestionStatus.TAKEN);
            } else {
                this.currentQuestion.setStatus(QuestionStatus.FREE);
                this.currentQuestion = updateQuestion;
                this.currentQuestion.setStatus(QuestionStatus.TAKEN);
            }
        } else {
            sendMessage(new Message(MessageType.QUESTION_PICK, updateQuestion));
        }


//        if(updateQuestion.getStatus() == QuestionStatus.FREE){
//            if(this.currentQuestion == null){
//                this.currentQuestion = updateQuestion;
//            }
//            else {
//                this.currentQuestion.setStatus(QuestionStatus.FREE);
//            }
//            sendMessage(new Message(MessageType.QUESTION_PICK, updateQuestion));
//            updateQuestion.setStatus(QuestionStatus.TAKEN);
//            this.currentQuestion = updateQuestion;
//
//        } else {
//            sendMessage(new Message(MessageType.QUESTION_PICK, updateQuestion));
//        }


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
