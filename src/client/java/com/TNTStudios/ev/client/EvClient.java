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

        // Receptor para el paquete del logro (versión corregida final)
        ClientPlayNetworking.registerGlobalReceiver(AchievementUnlockedPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                MinecraftClient client = context.client();
                ToastManager toastManager = client.getToastManager();

                // 1. Preparo los componentes visuales del logro.
                Text title = Text.literal("Logro completado");
                Text description = Text.literal(payload.achievementText());
                ItemStack icon = new ItemStack(Items.DANDELION);
                AdvancementFrame frame = AdvancementFrame.CHALLENGE;

                // 2. Creo el objeto que contiene la información visual (esto estaba bien).
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

                // 3. ¡CORRECCIÓN FINAL!
                // El método Advancement.Builder.build() ahora devuelve directamente un AdvancementEntry.
                // Así que lo capturo en una variable de ese tipo y listo.
                Identifier tempId = Identifier.of("ev", "temp_achievement");
                AdvancementEntry dummyAdvancementEntry = Advancement.Builder.create()
                        .display(display)
                        .build(tempId);

                // 4. Creo el toast con el AdvancementEntry y lo muestro.
                toastManager.add(new AdvancementToast(dummyAdvancementEntry));
            });
        });

        // Inicio el sistema que dibujará el HUD en la pantalla
        MissionHud.register();
    }
}