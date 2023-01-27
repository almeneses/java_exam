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
import com.almeneses.exam.utils.TimeUtils;

public class ClientController {

    private Socket socket;
    private ObjectOutputStream outStream;
    private ObjectInputStream inStream;
    private String nombreCliente;
    private List<Question> preguntas;
    private ClientUI clientUI;
    private Question preguntaActual;

    public ClientController(Socket socket, ClientUI clientUI) {
        try {
            this.socket = socket;
            this.clientUI = clientUI;
            this.nombreCliente = "Cliente - " + LocalDateTime.now().toString();
            this.outStream = new ObjectOutputStream(socket.getOutputStream());
            this.inStream = new ObjectInputStream(socket.getInputStream());
            this.clientUI.getGeneralEditorPane().setContentType("text/html");
            this.preguntaActual = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enviarMensaje(Message mensaje) {
        try {
            this.outStream.reset();
            this.outStream.writeObject(mensaje);
            this.outStream.flush();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public void recibirMensajes() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    while (socket.isConnected()) {

                        Message mensajeDelServer = (Message) inStream.readObject();
                        procesarMensaje(mensajeDelServer);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    public void iniciarExamen(Message mensaje) {
        this.preguntas = (ArrayList) mensaje.getContent();
        clientUI.getQuestionsComboBox().addItem(null);

        for (Question pregunta : preguntas) {
            clientUI.getQuestionsComboBox().addItem(pregunta.getNumber());
        }

        this.clientUI.getGeneralEditorPane().setText(null);
        this.clientUI.getGeneralEditorPane().setText("<h2><strong>Examen iniciado</strong></h2>\n");
    }

    public void actualizarPregunta(Message mensaje) {
        Question preguntaServer = (Question) mensaje.getContent();
        Question preguntaActualizar = buscarPregunta(preguntaServer.getNumber());
        boolean actualEqualsServerPregunta = this.preguntaActual != null && this.preguntaActual.getNumber()
                .equals(preguntaServer.getNumber());

        if (preguntaActualizar != null) {
            preguntaActualizar.replaceValuesFrom(preguntaServer);
        }

        if (this.preguntaActual != null && actualEqualsServerPregunta) {
            this.preguntaActual.replaceValuesFrom(preguntaServer);
        }

    }

    public void esperarExamen() {
        clientUI.getGeneralEditorPane().setText(null);
        clientUI.getGeneralEditorPane().setText("<h2><strong> El examen iniciará en un momento... </strong></h2>");
    }

    public void actualizarTiempo(Message mensaje) {
        int time = (int) mensaje.getContent();
        clientUI.getRemainingTimeValueLabel().setText(TimeUtils.toClockFormat(time));
    }

    public void procesarMensaje(Message mensaje) {
        System.out.println(mensaje.getType().name());
        switch (mensaje.getType()) {
            case EXAM_WAIT:
                esperarExamen();
                break;
            case EXAM_START:
                iniciarExamen(mensaje);
                break;
            case QUESTION_UPDATE:
                actualizarPregunta(mensaje);
                System.out.println(this.preguntas);
                break;
            case TIME_UPDATE:
                actualizarTiempo(mensaje);
                break;
            default:
                break;
        }
    }

    public Question buscarPregunta(String numPregunta) {
        for (Question pregunta : preguntas) {
            if (pregunta.getNumber().equals(numPregunta)) {
                return pregunta;
            }
        }

        return null;
    }

    public void seleccionarPregunta() {
        String seleccion = (String) clientUI.getQuestionsComboBox().getSelectedItem();
        Question preguntaSeleccionada = buscarPregunta(seleccion);

        if (this.preguntaActual != null) {
            if (this.preguntaActual.equals(preguntaSeleccionada)) {
                return;
            }
            this.preguntaActual.setStatus("Libre");
            enviarMensaje(new Message(MessageType.QUESTION_UPDATE, preguntaActual));
        }

        if (preguntaSeleccionada != null && !preguntaSeleccionada.getStatus().equals("Libre")) {
            clientUI.getGeneralEditorPane().setText(
                    "<h2><strong class='color: red;'>"
                            + "Esta pregunta no está disponible, seleccione otra</strong></h2>");
            return;
        }

        this.preguntaActual = preguntaSeleccionada;
        this.preguntaActual.setStatus("Ocupada");

        clientUI.getGeneralEditorPane().setText("<h3>" + preguntaActual.getStatement() + "</h3>\n");

        ButtonGroup buttonGroup = new ButtonGroup();

        for (String opcion : preguntaActual.getOptions()) {
            JRadioButton radioButton = new JRadioButton(opcion);
            buttonGroup.add(radioButton);
            clientUI.getOptionsPanel().add(radioButton);
        }

        enviarMensaje(new Message(MessageType.QUESTION_UPDATE, preguntaSeleccionada));

    }

    public void enviarPregunta() {
        Message mensaje = new Message(MessageType.ANSWER, preguntaActual);
        enviarMensaje(mensaje);
    }

    public void initHandlers() {
        clientUI.getPickQuestionBtn().addActionListener(event -> seleccionarPregunta());
        clientUI.getSendAnswerBtn().addActionListener(event -> enviarPregunta());
    }

    public void init() {
        clientUI.getGeneralEditorPane().setContentType("text/html");
        clientUI.getGeneralEditorPane().setEditable(false);
        initHandlers();
        clientUI.setVisible(true);
        recibirMensajes();

    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public ObjectOutputStream getOutStream() {
        return outStream;
    }

    public void setOutStream(ObjectOutputStream outStream) {
        this.outStream = outStream;
    }

    public ObjectInputStream getInStream() {
        return inStream;
    }

    public void setInStream(ObjectInputStream inStream) {
        this.inStream = inStream;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String username) {
        this.nombreCliente = username;
    }

}
