package com.almeneses.exam.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import com.almeneses.exam.models.Message;
import com.almeneses.exam.models.MessageType;
import com.almeneses.exam.models.Question;

public class ClientThread implements Runnable {

    private Socket socket;

    private List<Question> questions;
    private List<ClientThread> clients;
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
            case ANSWER:
                processResponse(message);
                break;
            case QUESTION_UPDATE:
                updateQuestion(message);
                System.out.println(questions);
                break;
            default:
                break;
        }

    }

    public Question qualifyQuestion(Question question) {
        boolean isCorrect = question.getCorrectAnswer().equals(question.getAnswerGiven());

        question.setCorrect(isCorrect);
        question.setAnsweredBy(this.clientName);
        question.setStatus("Answered");

        return question;
    }

    public void processResponse(Message message) {
        Question answerQuestion = (Question) message.getContent();
        answerQuestion = qualifyQuestion(answerQuestion);

    }

    public void updateQuestion(Message message) {
        Question question = (Question) message.getContent();
        Question updateQuestion = findQuestion(question.getNumber());

        if (updateQuestion != null) {
            updateQuestion.replaceValuesFrom(question);
        }

        notifyCustomers(message);
    }

    public void notifyCustomers(Message message) {
        for (ClientThread client : clients) {
            if (!client.getSocket().equals(this.socket)) {
                Message messageF = new Message(message.getType(), message.getContent());
                client.sendMessage(messageF);
            }
        }
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

    public Socket getSocket() {
        return socket;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public List<ClientThread> getClients() {
        return clients;
    }

    public ObjectOutputStream getOutStream() {
        return outStream;
    }

    public ObjectInputStream getInStream() {
        return inStream;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

}
