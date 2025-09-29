// Archivo: src/main/java/com/TNTStudios/ev/command/CompassCommand.java
package com.TNTStudios.ev.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent; // Importación necesaria
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.minecraft.server.command.CommandManager.literal;

public class CompassCommand {

    public static final String COMPASS_TAG = "desire_compass";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("brujula")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(CompassCommand::run));
    }

    private static int run(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("Este comando solo puede ser ejecutado por un jugador."));
            return 0;
        }

        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            sendTitleAndSubtitle(player,
                    Text.literal("Espera...").formatted(Formatting.YELLOW),
                    Text.literal("Esta cosa se te va a hacer conocida.").formatted(Formatting.GOLD));
        }, 0, TimeUnit.SECONDS);

        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            sendTitleAndSubtitle(player,
                    Text.literal("Y si es así...").formatted(Formatting.YELLOW),
                    Text.literal("...ya sabrás cómo funciona.").formatted(Formatting.GOLD));
        }, 4, TimeUnit.SECONDS);

        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            giveSpecialCompass(player);
        }, 8, TimeUnit.SECONDS);

        return 1;
    }

    private static void giveSpecialCompass(ServerPlayerEntity player) {
        ItemStack compassStack = new ItemStack(Items.COMPASS);

        compassStack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Brújula del Deseo").formatted(Formatting.YELLOW)
        );

        List<Text> lore = List.of(
                Text.literal("Es un artefacto mágico que no señala el norte,").formatted(Formatting.GOLD, Formatting.ITALIC),
                Text.literal("sino que siempre apunta a lo que su portador más desea.").formatted(Formatting.GOLD, Formatting.ITALIC)
        );
        compassStack.set(DataComponentTypes.LORE, new LoreComponent(lore));

        NbtCompound customNbt = new NbtCompound();
        customNbt.putBoolean(COMPASS_TAG, true);

        // ¡MEJORA!
        // Para establecer el componente CUSTOM_DATA, hay que envolver el NbtCompound con NbtComponent.of().
        compassStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customNbt));

        player.getInventory().offerOrDrop(compassStack);
    }

    private static void sendTitleAndSubtitle(ServerPlayerEntity player, Text title, Text subtitle) {
        player.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 80, 20));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(subtitle));
        player.networkHandler.sendPacket(new TitleS2CPacket(title));
    }
}