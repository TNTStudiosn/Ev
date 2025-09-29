package com.TNTStudios.ev;

import com.TNTStudios.ev.command.PrankCountdownCommand;
import com.TNTStudios.ev.command.StartMinigameCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class Ev implements ModInitializer {

    @Override
    public void onInitialize() {
        // Registro los comandos para que estén disponibles en el juego
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            StartMinigameCommand.register(dispatcher);
            // Añado el registro de mi nuevo comando de broma
            PrankCountdownCommand.register(dispatcher);
        });
    }
}