// Archivo: src/main/java/com/TNTStudios/ev/networking/payload/AchievementUnlockedPayload.java
package com.TNTStudios.ev.networking.payload;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Este es el paquete que viaja del servidor al cliente para mostrar un logro personalizado.
 * Es un mensaje muy simple que solo contiene el texto que quiero mostrar en el logro.
 */
public record AchievementUnlockedPayload(String achievementText) implements CustomPayload {
    // Un identificador único para nuestro paquete. Como siempre, es la "dirección" del mensaje.
    public static final CustomPayload.Id<AchievementUnlockedPayload> ID = new CustomPayload.Id<>(Identifier.of("ev", "achievement_unlocked"));

    // El códec que sabe cómo escribir y leer un String en el flujo de datos de la red.
    public static final PacketCodec<RegistryByteBuf, AchievementUnlockedPayload> CODEC = PacketCodec.of(AchievementUnlockedPayload::write, AchievementUnlockedPayload::new);

    // Constructor que lee desde el buffer de red.
    private AchievementUnlockedPayload(RegistryByteBuf buf) {
        this(buf.readString());
    }

    // Método que escribe en el buffer.
    private void write(RegistryByteBuf buf) {
        buf.writeString(this.achievementText);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * Registro el tipo de payload. Es crucial que se llame tanto en el servidor como en el cliente.
     * En este caso, es Server-to-Client (S2C), ya que el servidor le ordena al cliente que muestre algo.
     */
    public static void register() {
        // Le digo a Fabric que este paquete viajará del servidor al cliente (S2C).
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
    }
}