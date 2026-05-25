package edu.masanz.da.en;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class TxatClientHandler extends Thread {

    private final Socket socket;
    private final Map<String, PrintWriter> mapaClientsWriters;
    private PrintWriter out;
    private String clientName;

    public TxatClientHandler(Socket socket, Map<String, PrintWriter> mapaClientsWriters) {
        this.socket = socket;
        this.mapaClientsWriters = mapaClientsWriters;
    }

    @Override
    public void run() {

        if (GameManager.getInstance().getEstadoJuego() != EstadoJuego.ESPERANDO) {
            closeConnection();
            return;
        }

        System.out.println("Nuevo cliente conectado: " + socket.getInetAddress());
        try (Scanner in = new Scanner(socket.getInputStream())) {
            out = new PrintWriter(socket.getOutputStream(), true);

            registerClient(in);

            while (in.hasNextLine()) {
                String message = in.nextLine();
                if (message.startsWith("/")){
                    processCommand(message);
                    continue;
                }
                String formattedMessage = clientName + ": " + message;
                System.out.println("Mensaje recibido: " + formattedMessage);
                broadcast(formattedMessage);
            }
        } catch (IOException e) {
            System.out.println("Error en la conexion con un cliente.");
        } finally {
            closeConnection();
        }
    }

    private void processCommand(String message) {
        try {
            String cmd = message.split("\\s+")[0].substring(1);
            String par1 = "";
            if(message.split("\\s+").length > 1){
                par1 = message.split("\\s+")[1];
            }
            System.out.println("Comando: " + cmd);
            EstadoJuego estadoJuego = GameManager.getInstance().getEstadoJuego();
//            switch (cmd) {
//                case "KILL" -> {if(estadoJuego==EstadoJuego.JUGANDO) {kill(par1);} }
//                case "MOVE" -> {if(estadoJuego==EstadoJuego.JUGANDO || estadoJuego==EstadoJuego.ESPERANDO) {move(par1);} }
//                case "MAPA" -> {if(estadoJuego==EstadoJuego.JUGANDO || estadoJuego==EstadoJuego.ESPERANDO) {sendMapa();} }
//                //case "PWD" -> {if(estadoJuego==EstadoJuego.JUGANDO) {whereAmI();} }
//                case "PWD" -> descMyPlace();
//                case "ALERT" -> {if(estadoJuego==EstadoJuego.JUGANDO) {alert();} }
//                case "VOTE" -> {if(estadoJuego==EstadoJuego.REUNION) {vote(par1);} }
//                case "ALIVE" -> sendPeopleAlive();
//                case "FIX" -> {if(estadoJuego==EstadoJuego.JUGANDO) {arreglaTarea(par1);} }
//                case "DESTROY" -> {if(estadoJuego==EstadoJuego.JUGANDO) {destrozaTarea(par1);} }
//                case "READY" -> {if(estadoJuego==EstadoJuego.ESPERANDO) {manageReadiness();} }
//                case "HELP" -> sendCommandsList();
//            }

            if (cmd.equalsIgnoreCase("KILL") && estadoJuego==EstadoJuego.JUGANDO) {
                kill(par1);
            } else if (cmd.equalsIgnoreCase("MOVE") && (estadoJuego==EstadoJuego.JUGANDO || estadoJuego==EstadoJuego.ESPERANDO)) {
                move(par1);
            } else if (cmd.equalsIgnoreCase("MAPA") && (estadoJuego==EstadoJuego.JUGANDO || estadoJuego==EstadoJuego.ESPERANDO)) {
                sendMapa();
            } else if (cmd.equalsIgnoreCase("PWD")) {
                descMyPlace();
            } else if (cmd.equalsIgnoreCase("ALERT") && estadoJuego==EstadoJuego.JUGANDO) {
                alert();
            } else if (cmd.equalsIgnoreCase("VOTE") && estadoJuego==EstadoJuego.REUNION) {
                vote(par1);
            } else if (cmd.equalsIgnoreCase("ALIVE") && (estadoJuego==EstadoJuego.JUGANDO || estadoJuego==EstadoJuego.ESPERANDO || estadoJuego==EstadoJuego.REUNION)) {
                sendPeopleAlive();
            } else if (cmd.equalsIgnoreCase("FIX") && estadoJuego==EstadoJuego.JUGANDO) {
                arreglaTarea(par1);
            } else if (cmd.equalsIgnoreCase("DESTROY") && estadoJuego==EstadoJuego.JUGANDO) {
                destrozaTarea(par1);
            } else if (cmd.equalsIgnoreCase("READY") && estadoJuego==EstadoJuego.ESPERANDO) {
                manageReadiness();
            } else if (cmd.equalsIgnoreCase("HELP") && (estadoJuego==EstadoJuego.JUGANDO || estadoJuego==EstadoJuego.ESPERANDO || estadoJuego==EstadoJuego.REUNION)) {
                sendCommandsList();
            } else {
                out.println("COMANDO DESCONOCIDO O NO PERMITIDO EN EL ESTADO ACTUAL DEL JUEGO");
            }
        } catch (Exception e) {
            out.println("MENSAJE MAL PROCESADO");
            e.printStackTrace();
        }
    }

    private void manageReadiness() {
        // TODO manageReadiness
        GameManager.getInstance().setJugadorReady(clientName);
        if (GameManager.getInstance().getEstadoJuego() == EstadoJuego.JUGANDO) {

            // TODO manageReadiness

            // utilizaré mapaClientsWriters para informar
            List<String> impostores = new ArrayList<>();
            for (Map.Entry<String, PrintWriter> entry : mapaClientsWriters.entrySet()) {
                String nombreJugador = entry.getKey();
                PrintWriter writer = entry.getValue();
                boolean esImpostor = GameManager.getInstance().esImpostor(nombreJugador);
                if(esImpostor){
                    writer.println("ERES UN IMPOSTOR");
                    impostores.add(nombreJugador);
                } else {
                    writer.println("ERES UN TRIPULANTE");
                }
            }

            // Al GM pedirle lista impostores y lista tripulantes
//            List<String> listaTripulantes =

            //  lista tripulantes -> informar a tripulantes que son tripulantes

            // informar impostores quienes son los impostores
            for (String impostore : impostores) {
                PrintWriter writer = mapaClientsWriters.get(impostore);
                writer.println("LOS IMPOSTORES SON: " + String.join(", ", impostores));
            }
            // broadcast COMIENZA EL JUEGO
            broadcast("COMIENZA EL JUEGO");

        }else {
            out.println("ESPERANDO AL RESTO");
        }
    }

    private void sendCommandsList() {
        // TODO sendCommandsList
    }

    private void arreglaTarea(String nombreTarea) {
        if(nombreTarea==null || nombreTarea.isEmpty()){
            out.println("TAREA INCORRECTA");
            return;
        }
        boolean exito = GameManager.getInstance().arreglaTarea(clientName, nombreTarea);
        if(exito){
            out.println("HAS ARREGLADO ["+nombreTarea+"]");
        } else {
            out.println("NO SE HA PODIDO ARREGLAR ["+nombreTarea+"]");
        }
        if (GameManager.getInstance().tareasCompletadas()) {
            broadcast("GANAN LOS TRIPULANTES POR COMPLETAR TODAS LAS TAREAS");
        }
    }

    private void destrozaTarea(String nombreTarea) {
        // TODO destrozaTarea
    }

    private void sendPeopleAlive() {
        List<Jugador> listaJugadores = GameManager.getInstance().getJugadores();

        List<String> listaVivos = new ArrayList<>();
        List<String> listaMuertos = new ArrayList<>();

        for (Jugador jugador : listaJugadores) {
            if(jugador.isVivo()){
                listaVivos.add(jugador.getNombre());
            } else {
                listaMuertos.add(jugador.getNombre());
            }
        }

        int n = Math.max(listaVivos.size(), listaMuertos.size());

        // TODO: sendPeopleAlive Si estas ESPERANDO mandar una lista de quienes están READY o NO
        // Por ejemplo con un asterísco entre paréntesis los que están listos

        out.println("*".repeat(51));
        out.printf("*%5s%-14s%5s*%5s%-14s%5s*\n"," ", "VIVOS", " "," ", "MUERTOS", " ");
        out.println("*".repeat(51));
        for (int i = 0; i < n; i++) {
            String vivo = "";
            String muerto = "";
            try {
                vivo = listaVivos.get(i);
            }catch (Exception e){}
            try {
                muerto = listaMuertos.get(i);
            }catch (Exception e){

            }
            out.printf("*%5s%-14s%5s*%5s%-14s%5s*\n"," ", vivo, " "," ", muerto, " ");
        }
        out.println("*".repeat(51));
    }

    private void vote(String nombreSospechoso) {
        boolean hasVotadoBien = GameManager.getInstance().vote(clientName, nombreSospechoso);
        if (hasVotadoBien) {
            out.println("HAS VOTADO A ["+nombreSospechoso+"]");
        }
        boolean votacionFinalizada = GameManager.getInstance().votacionFinalizada();
        if (votacionFinalizada) {
            Jugador muerto = GameManager.getInstance().resultadoVotacion();
            out.println(" MUERE... ["+muerto.getNombre()+"] y es un ["+(muerto.isImpostor() ? "IMPOSTOR" : "TRIPULANTE")+"]");
            PrintWriter objetivoOut = mapaClientsWriters.get(muerto.getNombre());
            objetivoOut.println("HAS SIDO ASESINADO.");
            EstadoJuego estadoJuego = GameManager.getInstance().getEstadoJuego();
            switch (estadoJuego){
                case JUGANDO -> broadcast("SEGUIMOS JUGANDO");
                case GANAN_IMPOSTORES -> broadcast("GANAN LOS IMPOSTORES");
                case GANAN_TRIPULANTES -> broadcast("GANAN LOS TRIPULANTES");
            }
        }
    }

    private void alert() {
        boolean exito = GameManager.getInstance().alert();
        if(exito) {
            broadcast(" - ALERTA - ["+clientName+"] Convoca una reunion - ALERTA");
        } else {
            out.println(" YA ESTAS EN ALERTA ");
        }
    }

    private void kill(String nombreObjetivo) {
        if(nombreObjetivo==null || nombreObjetivo.isEmpty()){
            out.println("OBJETIVO INCORRECTO");
            return;
        }
        if(nombreObjetivo.equalsIgnoreCase(clientName)){
            out.println("NO TE PUEDES MARCAR COMO OBJETIVO");
            return;
        }

        boolean exito = GameManager.getInstance().kill(clientName, nombreObjetivo);

        if(exito){
            out.println("HAS MATADO A ["+nombreObjetivo+"]");
            PrintWriter objetivoOut = mapaClientsWriters.get(nombreObjetivo);
            objetivoOut.println("HAS SIDO ASESINADO.");
        } else {
            out.println("NO SE HA PODIDO REALIZAR LA ACCION DESEADA.");
        }
    }

    private void descMyPlace() {
        //clientName
        Sala sala = GameManager.getInstance().whereIs(clientName);
        out.println("*".repeat(30));
        out.printf("* %8s%-10s%8s *\n"," ", sala.getNombre(), " ");
        out.println("* "+"-".repeat(26)+" *");
        out.printf("* %-26s *\n", "JUGADORES");
        List<Jugador> jugadores = GameManager.getInstance().getJugadores(sala);
        for (Jugador jugador : jugadores) {
            out.printf("*    %-14s: %5s   *\n", jugador.getNombre(), ""+jugador.isVivo());
        }
        out.println("* "+"-".repeat(26)+" *");
        out.printf("* %-26s *\n", "TAREAS");
//        List<Tarea> tareas = GameManager.getInstance().getMapSalasListaTareas().get(sala);
        List<Tarea> tareas = GameManager.getInstance().getTareasDeSala(sala);
        if(tareas.isEmpty()){
            out.printf("*    %-20s   *\n", "No hay tareas");
        } else {
            for (Tarea tarea : tareas) {
                String estado = tarea.isFunciona() ? "OK" : "ROTA";
                out.printf("*    %-14s: %5s   *\n", tarea.getNombre(), estado);
            }
        }
        out.println("*".repeat(30));
    }

    private void registerClient(Scanner in) {
        while (clientName == null && in.hasNextLine()) {
            String requestedName = in.nextLine().trim();
            synchronized (mapaClientsWriters) {
                if (!requestedName.isEmpty() && !mapaClientsWriters.containsKey(requestedName)) {
                    clientName = requestedName;
                    mapaClientsWriters.put(clientName, out);
                    System.out.println(clientName + " se ha registrado.");
                    out.println("OK");
                    broadcast(clientName + " se ha conectado.");

                    GameManager.getInstance().addJugador(clientName);
//                    out.println("ERES UN ["+(yo.isImpostor() ? "IMPOSTOR" : "TRIPULANTE")+"]");
                    out.println("Cuando estes listo, escribe /READY también puedes saber quién está con /ALIVE");
                } else {
                    out.println("NO");
                    // clientName seguirá siendo null
                }
            }
        }
    }

    private void move(String nombreSalaDestino) {
        if (GameManager.getInstance().cambiaSala(clientName,nombreSalaDestino)) {
            out.println("Estás en " + nombreSalaDestino);
        }else{
            out.println("No puedes ir a " + nombreSalaDestino);
        }
    }

    private void sendMapa() {
        String mapa = GameManager.getInstance().getMapaTextual();
//        String mapa = "Este es el MAPA";
        out.println(mapa);
    }

    private void sendConnectedClients() {
        synchronized (mapaClientsWriters) {
            out.println("Clientes conectados: " + String.join(", ", mapaClientsWriters.keySet()));
        }
    }

    private void broadcast(String message) {
        synchronized (mapaClientsWriters) {
            for (PrintWriter writer : mapaClientsWriters.values()) {
                writer.println(message);
            }
        }
    }

    private void closeConnection() {
        if (clientName != null) {
            synchronized (mapaClientsWriters) {
                mapaClientsWriters.remove(clientName);
//                GameManager.getInstance().removeJugador(clientName);
                GameManager.getInstance().killJugador(clientName);
            }
            System.out.println(clientName + " se ha desconectado.");
            broadcast(clientName + " se ha desconectado.");
            GameManager.getInstance().removeJugador(clientName);
            EstadoJuego estadoJuego = GameManager.getInstance().getEstadoJuego();
            switch (estadoJuego){
                case JUGANDO -> broadcast("SEGUIMOS JUGANDO");
                case GANAN_IMPOSTORES -> broadcast("GANAN LOS IMPOSTORES");
                case GANAN_TRIPULANTES -> broadcast("GANAN LOS TRIPULANTES");
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            // No hay nada mas que liberar si falla el cierre del socket.
        }
    }

}
