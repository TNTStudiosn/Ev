// Archivo: src/main/java/com/TNTStudios/ev/command/LogroCommand.java
package com.TNTStudios.ev.command;

import com.TNTStudios.ev.networking.payload.AchievementUnlockedPayload;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class LogroCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("logro")
                .requires(source -> source.hasPermissionLevel(2)) // Solo para admins
                .then(argument("texto", StringArgumentType.greedyString()) // Acepta texto con espacios
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            // El comando debe ser ejecutado por un jugador para saber a quién enviarle el paquete.
                            if (player == null) {
                                context.getSource().sendError(Text.literal("Este comando solo puede ser ejecutado por un jugador."));
                                return 0;
                            }

                            // Obtengo el texto que el admin escribió en el comando.
                            String achievementText = StringArgumentType.getString(context, "texto");

                            // Creo el paquete con la información del logro.
                            AchievementUnlockedPayload payload = new AchievementUnlockedPayload(achievementText);

                            // Le envío el paquete específicamente al jugador que usó el comando.
                            ServerPlayNetworking.send(player, payload);

                            // Doy un pequeño feedback en el chat para confirmar que funcionó.
                            context.getSource().sendFeedback(() -> Text.literal("Notificación de logro enviada.").formatted(Formatting.GRAY), false);
                            return 1;
                        })));
    }
}