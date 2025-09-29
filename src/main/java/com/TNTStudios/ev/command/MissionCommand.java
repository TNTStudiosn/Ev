// Archivo: src/main/java/com/TNTStudios/ev/command/MissionCommand.java
package com.TNTStudios.ev.command;

import com.TNTStudios.ev.manager.MissionManager;
import com.TNTStudios.ev.networking.payload.MissionUpdatePayload;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MissionCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mision")
                .requires(source -> source.hasPermissionLevel(2)) // Solo para operadores
                .then(argument("texto", StringArgumentType.greedyString()) // Acepta texto con espacios
                        .executes(context -> {
                            // Obtengo el texto de la misión desde los argumentos del comando
                            String missionText = StringArgumentType.getString(context, "texto");

                            // Actualizo el estado de la misión en el servidor
                            MissionManager.currentMission = missionText;

                            // Preparo el paquete para enviar a los clientes
                            MissionUpdatePayload payload = new MissionUpdatePayload(missionText);

                            // Envío el paquete a cada jugador conectado para que actualicen su HUD
                            for (ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                                ServerPlayNetworking.send(player, payload);
                            }

                            // Doy feedback a quien ejecutó el comando
                            context.getSource().sendFeedback(() -> Text.literal("Misión actualizada para todos los jugadores.").formatted(Formatting.GREEN), true);
                            return 1;
                        })));
    }
}