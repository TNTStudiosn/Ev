// Archivo: src/main/java/com/TNTStudios/ev/command/MissionCompleteCommand.java
package com.TNTStudios.ev.command;

import com.TNTStudios.ev.manager.MissionManager;
import com.TNTStudios.ev.networking.payload.MissionUpdatePayload;
import com.mojang.brigadier.CommandDispatcher;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.minecraft.server.command.CommandManager.literal;

public class MissionCompleteCommand {

    private static final Random RANDOM = new Random();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("misioncumplida")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    // Verifico si realmente hay una misión activa
                    if (MissionManager.currentMission == null || MissionManager.currentMission.isEmpty()) {
                        context.getSource().sendError(Text.literal("No hay ninguna misión activa para completar."));
                        return 0;
                    }

                    // Preparo los títulos y subtítulos
                    Text title = Text.literal("¡Misión Cumplida!").formatted(Formatting.YELLOW);
                    Text subtitle = Text.literal(MissionManager.currentMission).formatted(Formatting.GOLD);

                    // Limpio la misión en el servidor
                    MissionManager.currentMission = null;

                    // Preparo el paquete para limpiar el HUD en los clientes
                    MissionUpdatePayload payload = new MissionUpdatePayload("");

                    // Recorro todos los jugadores para notificarles
                    for (ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                        // Envío el paquete para limpiar el HUD
                        ServerPlayNetworking.send(player, payload);

                        // Muestro los títulos
                        sendTitleAndSubtitle(player, title, subtitle);

                        // Lanzo los fuegos artificiales
                        spawnYellowFireworks(player, 8);

                        // Reproduzco un sonido de victoria
                        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    }

                    context.getSource().sendFeedback(() -> Text.literal("¡Misión completada!").formatted(Formatting.GREEN), true);
                    return 1;
                }));
    }

    private static void sendTitleAndSubtitle(ServerPlayerEntity player, Text title, Text subtitle) {
        player.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 80, 20));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(subtitle));
        player.networkHandler.sendPacket(new TitleS2CPacket(title));
    }

    // Método reutilizado y adaptado para generar fuegos artificiales amarillos
    private static void spawnYellowFireworks(ServerPlayerEntity player, int count) {
        World world = player.getWorld();
        if (world instanceof ServerWorld) {
            for (int i = 0; i < count; i++) {
                ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET);

                // Creo una explosión de color amarillo
                FireworkExplosionComponent explosion = new FireworkExplosionComponent(
                        FireworkExplosionComponent.Type.STAR,
                        IntList.of(0xFFFF00), // Color amarillo
                        IntList.of(),
                        RANDOM.nextBoolean(),
                        RANDOM.nextBoolean()
                );

                // Configuro el cohete con la explosión
                FireworksComponent fireworks = new FireworksComponent((byte) (RANDOM.nextInt(2)), List.of(explosion));
                fireworkStack.set(DataComponentTypes.FIREWORKS, fireworks);

                // Genero la entidad del cohete en el mundo
                FireworkRocketEntity fireworkEntity = new FireworkRocketEntity(
                        world,
                        player.getX() + (RANDOM.nextDouble() - 0.5) * 20,
                        player.getY(),
                        player.getZ() + (RANDOM.nextDouble() - 0.5) * 20,
                        fireworkStack
                );
                world.spawnEntity(fireworkEntity);
            }
        }
    }
}