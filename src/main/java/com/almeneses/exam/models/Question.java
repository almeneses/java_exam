package com.almeneses.exam.models;

import java.io.Serializable;

public class Question implements Serializable {
    private QuestionStatus status;
    private final String statement;
    private final String[] options;
    private final String answerGiven;
    private String correctAnswer;
    private String answeredBy;
    private final String number;
    private boolean isCorrect;

    public Question(String number, String statement, String[] options, String answer) {
        this.number = number;
        this.statement = statement;
        this.options = options;
        this.answerGiven = answer;
        this.status = QuestionStatus.FREE;
        this.answeredBy = null;
        this.isCorrect = false;

    }

    public QuestionStatus getStatus() {
        return status;
    }

    public void setStatus(QuestionStatus status) {
        this.status = status;
    }

    public String getNumber() {
        return number;
    }

    public String[] getOptions() {
        return options;
    }

    public String getAnswerGiven() {
        return answerGiven;
    }

    public String getStatement() {
        return statement;
    }

    public void setAnsweredBy(String answeredBy) {
        this.answeredBy = answeredBy;
    }

    public void setCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    @Override
    public String toString() {
        return "Pregunta [status=" + status + ", number=" + number + "]";
        // , statement=" + statement + ", options=" + Arrays.toString(options)
        // + ", answer=" + answer + ", isCorrect=" + isCorrect +
        // ", answeredBy="
        // + answeredBy + ", number=" + number + ", esCorrecta=" + esCorrecta
        // + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Question other = (Question) obj;
        return number.equals(other.getNumber());
    }

}
