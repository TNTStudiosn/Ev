// Archivo: src/main/java/com/TNTStudios/ev/Ev.java
package com.TNTStudios.ev;

import com.TNTStudios.ev.command.CompassCommand;
import com.TNTStudios.ev.command.DirectionCommand;
import com.TNTStudios.ev.command.Mission1Command;
import com.TNTStudios.ev.command.PrankCountdownCommand;
import com.TNTStudios.ev.command.StartMinigameCommand;
import com.TNTStudios.ev.event.MissionEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class Ev implements ModInitializer {

    @Override
    public void onInitialize() {
        // Registro mis eventos personalizados para que el mod pueda reaccionar a las acciones del jugador
        MissionEvents.register();

        // Registro los comandos para que estén disponibles en el juego
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            StartMinigameCommand.register(dispatcher);
            PrankCountdownCommand.register(dispatcher);
            Mission1Command.register(dispatcher);
            // Añado el registro de los nuevos comandos de la brújula
            CompassCommand.register(dispatcher);
            DirectionCommand.register(dispatcher);
        });
    }
}