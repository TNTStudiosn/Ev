package com.TNTStudios.ev.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.ints.IntList;
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
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.minecraft.server.command.CommandManager.literal;

public class StartMinigameCommand {

    private static final Random RANDOM = new Random();

    // Registro el comando /empezar
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("empezar")
                .executes(StartMinigameCommand::run));
    }

    // Lógica principal del comando
    private static int run(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player != null) {
            // Creo un executor para las tareas programadas
            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                sendTitleAndSubtitle(player, "¡Holaa wenas!", "esta es la sorpresa.");
                spawnYellowFireworks(player, 3, false);
            }, 0, TimeUnit.SECONDS);

            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                sendTitleAndSubtitle(player, "Antes de empezar...", "Guarda todas tus cosas en la casa.");
                spawnAmbientParticles(player);
            }, 5, TimeUnit.SECONDS);

            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                sendTitleAndSubtitle(player, "Sigue instrucciones", "Y no hagas preguntas...");
                // CORRECCIÓN FINAL: Se usa el SoundEvent directamente
                player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.8f, 1.0f);
                spawnYellowFireworks(player, 5, false);
            }, 10, TimeUnit.SECONDS);

            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                sendTitleAndSubtitle(player, "♥", "Te quieroooooo");
                // CORRECCIÓN FINAL: Se usa el SoundEvent directamente
                player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                spawnYellowFireworks(player, 10, true);
                spawnAmbientParticles(player);
            }, 15, TimeUnit.SECONDS);
        }
        return 1;
    }

    // Envío los títulos y subtítulos al jugador
    private static void sendTitleAndSubtitle(ServerPlayerEntity player, String title, String subtitle) {
        player.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 70, 20));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.of(subtitle)));
        player.networkHandler.sendPacket(new TitleS2CPacket(Text.of(title)));
    }

    // Genero fuegos artificiales amarillos con más variedad
    private static void spawnYellowFireworks(ServerPlayerEntity player, int count, boolean isFinale) {
        World world = player.getWorld();
        if (!world.isClient) {
            for (int i = 0; i < count; i++) {
                ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET, isFinale ? 3 : 1);
                List<FireworkExplosionComponent> explosions = new ArrayList<>();
                int explosionCount = isFinale ? RANDOM.nextInt(2) + 2 : 1;

                for (int j = 0; j < explosionCount; j++) {
                    FireworkExplosionComponent.Type shape = FireworkExplosionComponent.Type.values()[RANDOM.nextInt(FireworkExplosionComponent.Type.values().length)];
                    if (shape == FireworkExplosionComponent.Type.CREEPER) {
                        // CORRECCIÓN: Arreglado un typo aquí
                        shape = FireworkExplosionComponent.Type.STAR;
                    }
                    FireworkExplosionComponent explosion = new FireworkExplosionComponent(
                            shape,
                            IntList.of(0xFFFF00),
                            IntList.of(0xFFFFFF),
                            RANDOM.nextBoolean(),
                            isFinale || RANDOM.nextBoolean()
                    );
                    explosions.add(explosion);
                }

                FireworksComponent fireworks = new FireworksComponent((byte) (RANDOM.nextInt(2) + 1), explosions);
                fireworkStack.set(DataComponentTypes.FIREWORKS, fireworks);

                FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(
                        world,
                        player.getX() + (world.random.nextDouble() - 0.5) * 15,
                        player.getY() + 2,
                        player.getZ() + (world.random.nextDouble() - 0.5) * 15,
                        fireworkStack
                );
                world.spawnEntity(fireworkRocketEntity);
            }
        }
    }

    // Genero partículas amarillas flotando alrededor del jugador
    private static void spawnAmbientParticles(ServerPlayerEntity player) {
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            // CORRECCIÓN DEFINITIVA: Usamos una partícula simple que no da errores.
            serverWorld.spawnParticles(
                    net.minecraft.particle.ParticleTypes.CRIT, // Usamos la partícula de golpe crítico
                    player.getX(),
                    player.getY() + 1,
                    player.getZ(),
                    70,      // Aumenté un poco la cantidad para que se note
                    1.5, 1.5, 1.5,
                    0.1      // Aumenté un poco la velocidad para un efecto más dinámico
            );
        }
    }
}