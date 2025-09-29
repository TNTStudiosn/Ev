package com.TNTStudios.ev.event;

import com.TNTStudios.ev.command.Mission1Command;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;

public class MissionEvents {

    public static void register() {
        // Me suscribo al evento que se ejecuta al final de cada tick del servidor.
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Si la misión no está activa, no hago nada para no gastar recursos.
            if (!Mission1Command.missionActive) {
                return;
            }

            // Calculo el total de saltos actual de todos los jugadores en el servidor.
            int currentTotalJumps = 0;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                currentTotalJumps += player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.JUMP));
            }

            // El número de saltos para la misión es la diferencia con el valor que guardamos al inicio.
            int missionJumps = currentTotalJumps - Mission1Command.initialTotalJumps;

            // Para no saturar con sonidos y actualizaciones, solo actúo si el contador ha aumentado.
            if (missionJumps > Mission1Command.jumpsCompleted) {
                Mission1Command.jumpsCompleted = missionJumps;

                // Actualizo el progreso para todos y reproduzco el sonido de feedback.
                Mission1Command.updateJumpProgress(server);
            }

            // Si se alcanza el objetivo, completo la misión.
            // Lo compruebo aquí para que termine tan pronto como se alcance el número.
            if (Mission1Command.jumpsCompleted >= Mission1Command.JUMPS_REQUIRED) {
                Mission1Command.completeMission(server);
            }
        });
    }
}