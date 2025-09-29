package com.TNTStudios.ev.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PrankCountdownCommand {

    /**
     * Registro el comando /bromacontador.
     * Acepta un argumento opcional de segundos; si no se proporciona, usa 60 por defecto.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("bromacontador")
                // Si se ejecuta sin argumentos, la cuenta regresiva es de 60 segundos
                .executes(context -> run(context, 60))
                // Permite especificar un número de segundos (entre 1 y 300)
                .then(argument("segundos", IntegerArgumentType.integer(1, 300))
                        .executes(context -> run(context, IntegerArgumentType.getInteger(context, "segundos")))));
    }

    /**
     * La lógica que se ejecuta cuando se usa el comando.
     * @param context El contexto del comando.
     * @param totalSeconds El total de segundos para la cuenta regresiva.
     * @return El resultado del comando.
     */
    private static int run(CommandContext<ServerCommandSource> context, int totalSeconds) {
        MinecraftServer server = context.getSource().getServer();

        // Este executor se encargará de todas las tareas programadas para este comando.
        // Para un mod grande con muchas tareas, sería mejor un gestor centralizado, pero para esto es perfecto.
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        // Programo los "ticks" sonoros y visuales de la cuenta regresiva
        for (int i = totalSeconds; i > 0; i--) {
            int remaining = i;
            scheduler.schedule(() -> {
                // Muestro el tiempo restante en la action bar de todos los jugadores
                Text actionBarText = Text.literal("Tiempo restante: " + remaining + "s").formatted(Formatting.YELLOW);
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    player.sendMessage(actionBarText, true); // true para que aparezca en la action bar
                }
            }, totalSeconds - remaining, TimeUnit.SECONDS);
        }

        // Programo el mensaje final de la broma para cuando el contador llegue a cero
        scheduler.schedule(() -> {
            Text title = Text.literal("¡Es broma!").formatted(Formatting.YELLOW);
            Text subtitle = Text.literal("Tómate el tiempo que quieras ♥").formatted(Formatting.YELLOW);

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                sendTitleAndSubtitle(player, title, subtitle);
                player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.2f);
            }

            // Apago el scheduler una vez que todas las tareas han terminado para liberar recursos
            scheduler.shutdown();
        }, totalSeconds, TimeUnit.SECONDS);

        // Envío un mensaje de confirmación a quien ejecutó el comando
        context.getSource().sendFeedback(() -> Text.literal("Iniciando cuenta regresiva de broma de " + totalSeconds + " segundos...").formatted(Formatting.GOLD), false);
        return 1; // Indico que el comando se ejecutó correctamente
    }

    /**
     * Un método de ayuda para enviar títulos y subtítulos a un jugador.
     * @param player El jugador que recibirá los títulos.
     * @param title El texto del título principal.
     * @param subtitle El texto del subtítulo.
     */
    private static void sendTitleAndSubtitle(ServerPlayerEntity player, Text title, Text subtitle) {
        // Establezco la duración del fade-in, stay y fade-out del título
        player.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 80, 20));
        // Envío el subtítulo
        player.networkHandler.sendPacket(new SubtitleS2CPacket(subtitle));
        // Envío el título principal
        player.networkHandler.sendPacket(new TitleS2CPacket(title));
    }
}