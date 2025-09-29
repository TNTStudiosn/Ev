// Archivo: src/main/java/com/TNTStudios/ev/command/DirectionCommand.java
package com.TNTStudios.ev.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DirectionCommand {

    private static final SimpleCommandExceptionType NO_COMPASS_EXCEPTION = new SimpleCommandExceptionType(
            Text.literal("No tienes una Brújula del Deseo en tu inventario.")
    );

    // Ya no necesitamos la excepción de "ubicación desconocida".
    // private static final SimpleCommandExceptionType UNKNOWN_LOCATION_EXCEPTION = ...

    // El mapa de lugares sigue siendo útil para los destinos predefinidos.
    private static final Map<String, BlockPos> NAMED_LOCATIONS = new HashMap<>();

    static {
        NAMED_LOCATIONS.put("casa", new BlockPos(120, 75, -340));
        NAMED_LOCATIONS.put("mina de diamantes", new BlockPos(-250, 22, 1120));
        NAMED_LOCATIONS.put("aldea del norte", new BlockPos(800, 68, 500));
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("direccion")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("lugar", StringArgumentType.greedyString())
                        .executes(context -> setDirectionToName(
                                context,
                                StringArgumentType.getString(context, "lugar")
                        ))
                )
        );
    }

    private static int setDirectionToName(CommandContext<ServerCommandSource> context, String locationName) throws CommandSyntaxException {
        ServerPlayerEntity sourcePlayer = context.getSource().getPlayer();
        if (sourcePlayer == null) {
            context.getSource().sendError(Text.literal("Este comando solo puede ser ejecutado por un jugador."));
            return 0;
        }

        // Busco la coordenada en el mapa.
        BlockPos targetPos = NAMED_LOCATIONS.get(locationName.toLowerCase().trim());

        Optional<GlobalPos> finalTarget;

        if (targetPos != null) {
            // Si encontré el lugar, creo un objetivo real.
            GlobalPos globalTargetPos = GlobalPos.create(sourcePlayer.getWorld().getRegistryKey(), targetPos);
            finalTarget = Optional.of(globalTargetPos);
        } else {
            // Si NO encontré el lugar, el objetivo es un Optional vacío.
            // Esto hará que la brújula gire sin rumbo, ¡perfecto para un deseo abstracto!
            finalTarget = Optional.empty();
        }

        // Llamo a updatePlayerCompass con el objetivo (que puede estar vacío) y el nombre del deseo.
        updatePlayerCompass(sourcePlayer, finalTarget, locationName);

        // El mensaje de feedback ahora siempre es de éxito.
        context.getSource().sendFeedback(() -> Text.literal("Deberas ir a: ")
                .append(Text.literal(locationName).formatted(Formatting.YELLOW)), false);
        return 1;
    }

    // Modifico la firma para que acepte un Optional<GlobalPos> en lugar de un GlobalPos.
    private static void updatePlayerCompass(ServerPlayerEntity player, Optional<GlobalPos> target, String targetName) throws CommandSyntaxException {
        boolean foundAndUpdated = false;

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);

            if (!stack.isEmpty()) {
                NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);

                if (customData != null) {
                    NbtCompound nbt = customData.copyNbt();

                    if (nbt.getBoolean(CompassCommand.COMPASS_TAG).orElse(false)) {
                        // Le paso el Optional directamente al componente.
                        stack.set(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTrackerComponent(target, true));

                        // Actualizo el nombre del ítem con el nuevo deseo.
                        stack.set(DataComponentTypes.CUSTOM_NAME,
                                Text.literal("Brújula del Deseo -> ").formatted(Formatting.YELLOW)
                                        .append(Text.literal(targetName).formatted(Formatting.AQUA))
                        );

                        foundAndUpdated = true;
                    }
                }
            }
        }

        if (!foundAndUpdated) {
            throw NO_COMPASS_EXCEPTION.create();
        }
    }
}