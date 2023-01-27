package com.almeneses.exam.models;

import java.io.Serializable;

public class Question implements Serializable {
    private String status;
    private String statement;
    private String[] options;
    private String answerGiven;
    private String correctAnswer;
    private String answeredBy;
    private String number;
    private boolean isCorrect;

    public Question() {
    }

    public Question(String number, String statement, String[] options, String answer) {
        this.number = number;
        this.statement = statement;
        this.options = options;
        this.answerGiven = answer;
        this.status = "Libre";
        this.answeredBy = null;
        this.isCorrect = false;

    }

    public Question(String status, String statement, String[] options, String answer, String answeredBy,
            String number, boolean isCorrect) {
        this.status = status;
        this.statement = statement;
        this.options = options;
        this.answerGiven = answer;
        this.answeredBy = answeredBy;
        this.number = number;
        this.isCorrect = isCorrect;
    }

    public void replaceValuesFrom(Question question) {
        this.status = question.status;
        this.statement = question.statement;
        this.options = question.options;
        this.answerGiven = question.answerGiven;
        this.correctAnswer = question.correctAnswer;
        this.answeredBy = question.answeredBy;
        this.number = question.number;
        this.isCorrect = question.isCorrect;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public String getAnswerGiven() {
        return answerGiven;
    }

    public void setAnswerGiven(String answer) {
        this.answerGiven = answer;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getAnsweredBy() {
        return answeredBy;
    }

    public void setAnsweredBy(String answeredBy) {
        this.answeredBy = answeredBy;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String isCorrect) {
        this.correctAnswer = isCorrect;
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
        if (number.equals(other.getNumber()))
            return true;

        return false;
    }

}
