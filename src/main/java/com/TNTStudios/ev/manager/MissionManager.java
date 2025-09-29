// Archivo: src/main/java/com/TNTStudios/ev/manager/MissionManager.java
package com.TNTStudios.ev.manager;

/**
 * Gestor simple para mantener el estado de la misión activa en el servidor.
 * Usar una clase dedicada para esto mantiene el código organizado y escalable,
 * en lugar de usar variables estáticas en una clase de comando.
 */
public class MissionManager {

    // El texto de la misión actual. Si es nulo o vacío, no hay misión activa.
    public static String currentMission = null;

}