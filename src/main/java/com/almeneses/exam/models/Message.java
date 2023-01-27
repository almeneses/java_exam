package com.almeneses.exam.models;

import java.io.Serializable;

public class Message implements Serializable {

    private MessageType type;
    private Object content;

    public Message(MessageType tipo, Object contenido) {
        this.type = tipo;
        this.content = contenido;
    }

    public Message() {
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType tipo) {
        this.type = tipo;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object contenido) {
        this.content = contenido;
    }

}
