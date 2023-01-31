package com.almeneses.exam.models;

import java.io.Serializable;

public class Message implements Serializable {

    private MessageType type;
    private Object content;

    public Message(MessageType tipo, Object content) {
        this.type = tipo;
        this.content = content;
    }

    public Message() {
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

}
