package com.TNTStudios.ev;

import com.TNTStudios.ev.command.StartMinigameCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class Ev implements ModInitializer {

    @Override
    public void onInitialize() {
        // Registro el comando para que esté disponible en el juego
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            StartMinigameCommand.register(dispatcher);
        });
    }
}