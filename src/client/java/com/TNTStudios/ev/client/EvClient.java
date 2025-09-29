// Archivo: src/client/java/com/TNTStudios/ev/client/EvClient.java
package com.TNTStudios.ev.client;

import com.TNTStudios.ev.client.hud.MissionHud;
import com.TNTStudios.ev.networking.payload.MissionUpdatePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class EvClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Le digo al cliente cómo reaccionar cuando reciba nuestro paquete de misión
        ClientPlayNetworking.registerGlobalReceiver(MissionUpdatePayload.ID, (payload, context) -> {
            // Cuando llega el paquete, actualizo el texto en el HUD.
            // Esto se ejecuta en el hilo de red, así que lo agendo para el hilo principal del cliente.
            context.client().execute(() -> {
                MissionHud.setMissionText(payload.missionText());
            });
        });

        // Inicio el sistema que dibujará el HUD en la pantalla
        MissionHud.register();
    }
}