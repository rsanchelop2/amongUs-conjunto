package edu.masanz.da.en;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TxatClientController {

    @FXML
    private TextField hostTextField;

    @FXML
    private TextField portTextField;

    @FXML
    private TextField nameTextField;

    @FXML
    private Button connectionButton;

    @FXML
    private TextArea messagesTextArea;

    @FXML
    private TextField messageTextField;

    @FXML
    private Pane paneCocina;

    @FXML
    private Pane paneDespensa;

    @FXML
    private Pane paneDormitorio;

    @FXML
    private Pane panePasillo;

    @FXML
    private Button sendButton;

    @FXML
    private Button pasillo;

    @FXML
    private Button despensa;

    @FXML
    private Button dormitorio;

    @FXML
    private Button cocina;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenerThread;
    private boolean connected;

    private int tiempo = 1;

    @FXML
    private void initialize() {
        setConnected(false);
    }

    @FXML
    private void handleMoveButton(ActionEvent event) {


        if (tiempo % 2 == 0){
            System.out.println("¡¡ESPERA!!, NO PUEDES MOVERTE AUN, QUE ACABAS DE LLEGAR");
            return;
        }

        pasillo.setDisable(false);
        despensa.setDisable(false);
        dormitorio.setDisable(false);
        cocina.setDisable(false);

        Button botonElegido = (Button) event.getSource();
        String idBoton = botonElegido.getText().toLowerCase();
        String message = null;

        limpiarRecuadros();

        if (idBoton.equalsIgnoreCase("dormitorio")){
            cocina.setDisable(true);
            dormitorio.setDisable(true);
            pintarRecuadro(paneDormitorio);
        }
        if (idBoton.equalsIgnoreCase("cocina")){
            dormitorio.setDisable(true);
            cocina.setDisable(true);
            pintarRecuadro(paneCocina);
        }
        if (idBoton.equalsIgnoreCase("despensa")){
            despensa.setDisable(true);
            pintarRecuadro(paneDespensa);
        }
        if (idBoton.equalsIgnoreCase("pasillo")){
            pasillo.setDisable(true);
            pintarRecuadro(panePasillo);
        }


        switch (idBoton){
            case "pasillo":
                message = "/MOVE pasillo";
                break;
            case "dormitorio":
                message = "/MOVE dormitorio";
                break;
            case "despensa":
                message = "/MOVE despensa";
                break;
            case "cocina":
                message = "/MOVE cocina";
                break;
            default:
                appendMessage("ALGO HA IDO MAL");
                break;
        }
        out.println(message);
    }

    private void limpiarRecuadros() {
        paneDormitorio.getChildren().clear();
        paneCocina.getChildren().clear();
        paneDespensa.getChildren().clear();
        panePasillo.getChildren().clear();
    }

    private void pintarRecuadro(Pane paneDestino) {
        Pane pane = new Pane();

        pane.setMaxWidth(64);
        pane.setMinWidth(64);
        pane.setPrefWidth(64);

        pane.setMaxHeight(16);
        pane.setMinHeight(16);
        pane.setPrefHeight(16);

        pane.setLayoutX(30);
        pane.setLayoutY(30);

        pane.setStyle("-fx-background-color: white; -fx-border-color: black;");

        Text texto = new Text("Javi");
        texto.setWrappingWidth(64);
        texto.setTextAlignment(TextAlignment.CENTER);
        texto.setLayoutY(texto.getFont().getSize());
        pane.getChildren().add(texto);

        paneDestino.getChildren().add(pane);
    }


    @FXML
    private void handleConnectionButton() {
        if (connected) {
            disconnect(true);
        } else {
            connect();
        }
    }

    @FXML
    private void handleSendButton() {
        sendMessage();
    }

    @FXML
    private void handleMessageAction() {
        sendMessage();
    }

    private void connect() {
        String host = hostTextField.getText().trim();
        String portText = portTextField.getText().trim();
        String name = nameTextField.getText().trim();

        if (host.isEmpty() || portText.isEmpty() || name.isEmpty()) {
            appendMessage("Introduce IP, puerto y nombre.");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            appendMessage("El puerto debe ser un numero.");
            return;
        }

        connectionButton.setDisable(true);
        new Thread(() -> connectInBackground(host, port, name)).start();
    }

    private void connectInBackground(String host, int port, String name) {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(name);
            String response = in.readLine();
            if (!"OK".equals(response)) {
                closeResources();
                Platform.runLater(() -> {
                    appendMessage("Ese nombre no esta disponible. Elige otro.");
                    setConnected(false);
                    connectionButton.setDisable(false);
                });
                return;
            }

            Platform.runLater(() -> {
                appendMessage("Conectado al servidor.");
                setConnected(true);
                connectionButton.setDisable(false);
            });
            startListening();
        } catch (IOException e) {
            closeResources();
            Platform.runLater(() -> {
                appendMessage("No se pudo conectar al servidor.");
                setConnected(false);
                connectionButton.setDisable(false);
            });
        }
    }

    private void startListening() {
        listenerThread = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    String message = line;
                    Platform.runLater(() -> appendMessage(message));
                }
            } catch (IOException e) {
                if (connected) {
                    Platform.runLater(() -> appendMessage("Conexion cerrada por el servidor."));
                }
            } finally {
                Platform.runLater(() -> disconnect(false));
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void sendMessage() {
        if (!connected || out == null) {
            return;
        }

        String message = messageTextField.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        out.println(message);
        messageTextField.clear();

        if (message.equals("BYE")) {
            disconnect(false);
        }
    }

    private void disconnect(boolean sendBye) {
        if (sendBye && out != null) {
            out.println("BYE");
        }
        closeResources();
        setConnected(false);
    }

    private void closeResources() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            // El cierre del socket no requiere accion adicional en la GUI.
        }
        socket = null;
        out = null;
        in = null;
    }

    private void setConnected(boolean connected) {
        this.connected = connected;
        hostTextField.setDisable(connected);
        portTextField.setDisable(connected);
        nameTextField.setDisable(connected);
        messageTextField.setDisable(!connected);
        sendButton.setDisable(!connected);
        connectionButton.setText(connected ? "Desconectar" : "Conectar");
        messageTextField.requestFocus();
    }

    private void appendMessage(String message) {
        messagesTextArea.appendText(message + System.lineSeparator());
    }

}