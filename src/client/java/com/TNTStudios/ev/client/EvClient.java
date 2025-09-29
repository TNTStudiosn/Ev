// Archivo: src/client/java/com/TNTStudios/ev/client/EvClient.java
package com.TNTStudios.ev.client;

import com.TNTStudios.ev.client.hud.MissionHud;
import com.TNTStudios.ev.networking.payload.AchievementUnlockedPayload;
import com.TNTStudios.ev.networking.payload.MissionUpdatePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.AdvancementToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class EvClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Le digo al cliente cómo reaccionar cuando reciba nuestro paquete de misión
        ClientPlayNetworking.registerGlobalReceiver(MissionUpdatePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                MissionHud.setMissionText(payload.missionText());
            });
        });

        // Receptor para el paquete del logro
        ClientPlayNetworking.registerGlobalReceiver(AchievementUnlockedPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                MinecraftClient client = context.client();
                ToastManager toastManager = client.getToastManager();

                // 1. ¡CAMBIO REALIZADO AQUÍ!
                // Ahora, el texto que envías con el comando es el título principal.
                Text title = Text.literal(payload.achievementText());
                // Y "Logro completado" es el subtítulo o descripción.
                Text description = Text.literal("Logro completado");
                ItemStack icon = new ItemStack(Items.DANDELION);
                AdvancementFrame frame = AdvancementFrame.CHALLENGE;

                // 2. Creo el objeto que contiene la información visual.
                AdvancementDisplay display = new AdvancementDisplay(
                        icon,
                        title,
                        description,
                        Optional.empty(),
                        frame,
                        true,
                        false,
                        true
                );

                // 3. Creo el AdvancementEntry falso para poder mostrar el toast.
                Identifier tempId = Identifier.of("ev", "temp_achievement");
                AdvancementEntry dummyAdvancementEntry = Advancement.Builder.create()
                        .display(display)
                        .build(tempId);

                // 4. Creo el toast y lo muestro.
                toastManager.add(new AdvancementToast(dummyAdvancementEntry));
            });
        });

        // Inicio el sistema que dibujará el HUD en la pantalla
        MissionHud.register();
    }
}