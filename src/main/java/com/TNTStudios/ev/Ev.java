// Archivo: src/main/java/com/TNTStudios/ev/Ev.java
package com.TNTStudios.ev;

import com.TNTStudios.ev.command.*;
import com.TNTStudios.ev.event.MissionEvents;
import com.TNTStudios.ev.networking.payload.AchievementUnlockedPayload; // Importo el nuevo payload
import com.TNTStudios.ev.networking.payload.MissionUpdatePayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class Ev implements ModInitializer {

    @Override
    public void onInitialize() {
        // Registro mis eventos personalizados para que el mod pueda reaccionar a las acciones del jugador
        MissionEvents.register();

        // Registro los payloads para la comunicación servidor -> cliente
        MissionUpdatePayload.register();
        AchievementUnlockedPayload.register(); // ¡Registro el nuevo payload aquí!

        // Registro los comandos para que estén disponibles en el juego
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            StartMinigameCommand.register(dispatcher);
            PrankCountdownCommand.register(dispatcher);
            Mission1Command.register(dispatcher);
            CompassCommand.register(dispatcher);
            DirectionCommand.register(dispatcher);
            MissionCommand.register(dispatcher);
            MissionCompleteCommand.register(dispatcher);
            LogroCommand.register(dispatcher); // ¡Y aquí registro mi nuevo comando!
        });
    }
}