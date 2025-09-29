package com.TNTStudios.ev.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class Mission1Command {

    public static boolean missionActive = false;
    public static int jumpsCompleted = 0;
    public static final int JUMPS_REQUIRED = 40;
    public static int initialTotalJumps = 0;


    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mision1")
                .executes(Mission1Command::run));
    }

    private static int run(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        MinecraftServer server = source.getServer();

        if (missionActive) {
            source.sendFeedback(() -> Text.literal("La Misión 1 ya está en curso.").formatted(Formatting.RED), false);
            return 0;
        }

        missionActive = true;
        jumpsCompleted = 0;
        initialTotalJumps = 0;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            initialTotalJumps += player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.JUMP));
        }

        // CORRECCIÓN: Había cambiado el título, lo regreso al original que pediste
        Text title = Text.literal("Prueba de Salto").formatted(Formatting.YELLOW);
        Text subtitle = Text.literal("¡Salta 20 veces para continuar!").formatted(Formatting.GOLD);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sendTitleAndSubtitle(player, title, subtitle);
        }

        updateJumpProgress(server);

        source.sendFeedback(() -> Text.literal("¡Misión 1 iniciada!").formatted(Formatting.GREEN), true);
        return 1;
    }

    public static void updateJumpProgress(MinecraftServer server) {
        int displayJumps = Math.min(jumpsCompleted, JUMPS_REQUIRED);
        Text progressText = Text.literal("Saltos: " + displayJumps + " / " + JUMPS_REQUIRED).formatted(Formatting.YELLOW);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(progressText, true);
        }
    }

    public static void completeMission(MinecraftServer server) {
        missionActive = false;

        Text title = Text.literal("¡Misión 1 Cumplida!").formatted(Formatting.YELLOW);
        Text subtitle = Text.literal("¡Todo en orden!").formatted(Formatting.GOLD);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sendTitleAndSubtitle(player, title, subtitle);

            // LA MAGIA OCURRE AQUÍ:
            // 1. Envío el paquete que activa la animación del tótem en el cliente (sonido, overlay, etc.).
            //    El número '35' es el código de estado para la animación del tótem.
            player.networkHandler.sendPacket(new EntityStatusS2CPacket(player, (byte) 35));

            // 2. Inmediatamente después, genero mis partículas personalizadas de diente de león.
            spawnDandelionParticles(player);
        }
    }

    // Renombré el método para mayor claridad, ya que solo genera las partículas
    private static void spawnDandelionParticles(ServerPlayerEntity player) {
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            ItemStack dandelionStack = new ItemStack(Items.DANDELION);
            // El truco es que la animación del tótem ya genera sus propias partículas.
            // Las mías se sumarán a ellas, creando un efecto combinado y único.
            serverWorld.spawnParticles(
                    new ItemStackParticleEffect(ParticleTypes.ITEM, dandelionStack),
                    player.getX(),
                    player.getY() + player.getHeight() / 1.5,
                    player.getZ(),
                    70, // Aumenté un poco la cantidad para que se note bien
                    0.6, 0.8, 0.6, // Ajusto la dispersión para un efecto más vertical
                    0.15
            );
        }
    }

    private static void sendTitleAndSubtitle(ServerPlayerEntity player, Text title, Text subtitle) {
        player.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 70, 20));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(subtitle));
        player.networkHandler.sendPacket(new TitleS2CPacket(title));
    }
}