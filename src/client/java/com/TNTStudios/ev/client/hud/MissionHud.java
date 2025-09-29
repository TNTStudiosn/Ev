// Archivo: src/client/java/com/TNTStudios/ev/client/hud/MissionHud.java
package com.TNTStudios.ev.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Clase responsable de dibujar la misión actual en la pantalla (HUD).
 * Todo el código aquí se ejecuta únicamente en el lado del cliente.
 */
public class MissionHud {

    // Variable estática para almacenar el texto de la misión actual en el cliente
    private static String currentMissionText = "";

    public static void setMissionText(String text) {
        currentMissionText = text;
    }

    /**
     * Registro el callback para el renderizado del HUD.
     * Este evento se dispara cada frame, permitiéndome dibujar sobre la pantalla del juego.
     */
    public static void register() {
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            // Si no hay misión, no hago nada para optimizar el rendimiento.
            if (currentMissionText == null || currentMissionText.isEmpty()) {
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) {
                return;
            }

            TextRenderer textRenderer = client.textRenderer;
            int screenWidth = drawContext.getScaledWindowWidth();

            // Defino el título y lo preparo para renderizar
            Text titleText = Text.literal("Misión:").formatted(Formatting.YELLOW, Formatting.BOLD, Formatting.UNDERLINE);

            // ¡LÓGICA DINÁMICA!
            // 1. Defino un ancho máximo para evitar que una palabra muy larga ocupe toda la pantalla.
            int maxAllowedWidth = 250;

            // 2. Divido el texto de la misión en líneas basándome en ese ancho máximo.
            List<OrderedText> wrappedMissionLines = textRenderer.wrapLines(Text.literal(currentMissionText).formatted(Formatting.WHITE), maxAllowedWidth);

            // 3. Calculo el ancho real que necesita el contenido.
            // Empiezo con el ancho del título.
            int dynamicWidth = textRenderer.getWidth(titleText);
            // Reviso cada línea de la misión y si alguna es más ancha que el título, actualizo el ancho.
            for (OrderedText line : wrappedMissionLines) {
                dynamicWidth = Math.max(dynamicWidth, textRenderer.getWidth(line));
            }

            // 4. Calculo la altura total del cuadro de texto, esto no cambia.
            int totalHeight = (wrappedMissionLines.size() + 1) * (textRenderer.fontHeight + 2) + 10;

            // 5. Calculo la posición 'x' para centrar el cuadro, ¡pero ahora usando el ancho dinámico!
            int x = (screenWidth / 2) - (dynamicWidth / 2);
            int y = 10;

            // 6. Dibujo el fondo semitransparente usando el ancho dinámico.
            drawContext.fill(x - 5, y - 5, x + dynamicWidth + 5, y + totalHeight - 5, 0x90000000);

            // 7. Dibujo el texto. El texto quedará alineado a la izquierda dentro del cuadro centrado.
            // Esto es visualmente más agradable que centrar cada línea individualmente.
            drawContext.drawTextWithShadow(textRenderer, titleText, x, y, -1);

            int currentY = y + textRenderer.fontHeight + 5;
            for (OrderedText line : wrappedMissionLines) {
                drawContext.drawTextWithShadow(textRenderer, line, x, currentY, -1);
                currentY += textRenderer.fontHeight + 2;
            }
        });
    }
}