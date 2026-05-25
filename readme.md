# EntreNosotros - Un juego de engaño y chat

Este proyecto es una adaptación del clásico juego "Among Us" a un formato de chat en tiempo real. Basado en la funcionalidad de la aplicación de chat "Txatrea", "EntreNosotros" introduce roles, objetivos y la emoción del engaño y la deducción a través de la comunicación textual.

## Funcionamiento del Juego

La partida se desarrolla en una "nave" virtual gestionada por el servidor. Los jugadores se conectan y se les asigna un rol en secreto. La comunicación es la clave para descubrir a los impostores o, si eres uno de ellos, para engañar a los demás.

### Roles

-   **Tripulantes:** Su objetivo es sobrevivir y descubrir a los impostores. Deben comunicarse y colaborar para identificar a los culpables y expulsarlos de la nave mediante votación.
-   **Impostores:** Su objetivo es eliminar a los tripulantes sin ser descubiertos hasta que su número sea igual al de los tripulantes. Deben mezclarse, fingir inocencia y usar la astucia para acusar a otros.

### Fases del Juego

El juego se divide en dos fases principales que se alternan:

**1. Fase de Chat Libre:**
-   Todos los jugadores pueden hablar en un canal de chat general.
-   Los impostores pueden enviar un comando especial al servidor para "eliminar" a un tripulante. Esta acción es secreta y solo el impostor y el servidor la conocen en el momento.
-   Los tripulantes deben estar atentos a quién habla, quién no, y a posibles silencios sospechosos.

**2. Fase de Discusión y Votación:**
-   Esta fase se activa cuando un jugador "reporta" haber encontrado a un jugador eliminado o convoca una "reunión de emergencia" mediante un comando.
-   El chat general se pausa y se abre un canal de chat específico para la discusión.
-   Los jugadores debaten sobre quién puede ser el impostor.
-   Al final de un tiempo determinado, se inicia una votación. Cada jugador emite un voto por el jugador que cree que es el culpable.
-   El jugador con más votos es "expulsado" de la nave. El servidor revela si era o no un impostor.

### Condiciones de Victoria

-   **Ganan los Tripulantes si...**
    -   Logran expulsar a todos los impostores.

-   **Ganan los Impostores si...**
    -   El número de impostores es igual al número de tripulantes restantes.

## Comandos del Juego

Los siguientes comandos están disponibles para los jugadores:

-   `/KILL <nombre_jugador>`: (Solo impostores) Intenta eliminar a un tripulante en la misma sala.
-   `/MOVE <nombre_sala>`: Permite al jugador moverse entre las salas de la nave.
-   `/MAPA`: Muestra un mapa con la distribución de las salas.
-   `/PWD`: Muestra la sala en la que te encuentras y los jugadores presentes.
-   `/ALERT`: Convoca una reunión de emergencia para iniciar una fase de discusión y votación.
-   `/VOTE <nombre_jugador>`: Emite un voto para expulsar a un jugador durante una reunión.

## Base Tecnológica

El sistema de comunicación y la estructura cliente-servidor se basan en el proyecto **"Txatrea"**, extendiendo su funcionalidad para soportar la lógica del juego, la gestión de roles, los comandos especiales y los diferentes estados de la partida (chat libre, discusión, votación).

## TODO - Tareas Pendientes

-   [ ] Implementar un sistema de "cooldown" para el comando `/KILL` para evitar asesinatos consecutivos.
-   [ ] Limitar el número de reuniones de emergencia que se pueden convocar por partida.
-   [ ] Añadir un sistema de tareas para los tripulantes.
-   [ ] Mejorar la visualización del mapa y la información de los jugadores.
-   [ ] Implementar un sistema de "fantasmas" para los jugadores eliminados, que puedan observar la partida sin participar.
-   [ ] Refinar el sistema de votación para gestionar empates y votos nulos.
