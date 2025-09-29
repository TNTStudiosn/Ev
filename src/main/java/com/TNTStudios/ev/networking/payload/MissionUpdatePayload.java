// Archivo: src/main/java/com/TNTStudios/ev/networking/payload/MissionUpdatePayload.java
package com.TNTStudios.ev.networking.payload;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Este es el paquete que viaja del servidor al cliente.
 * Contiene la información de la misión (el texto o una cadena vacía para limpiarla).
 * Es como un telegrama que le dice al cliente qué mostrar en la pantalla.
 */
public record MissionUpdatePayload(String missionText) implements CustomPayload {
    // Un identificador único para nuestro paquete. Es como la dirección en un sobre.
    // ¡CORRECCIÓN! El constructor de Identifier es privado. La forma correcta de crearlo es con el método estático `Identifier.of()`.
    public static final CustomPayload.Id<MissionUpdatePayload> ID = new CustomPayload.Id<>(Identifier.of("ev", "mission_update"));

    // El códec se encarga de escribir y leer los datos del paquete de forma eficiente.
    // Aquí, simplemente le decimos cómo manejar un String.
    public static final PacketCodec<RegistryByteBuf, MissionUpdatePayload> CODEC = PacketCodec.of(MissionUpdatePayload::write, MissionUpdatePayload::new);

    // El constructor que lee los datos del buffer de red.
    private MissionUpdatePayload(RegistryByteBuf buf) {
        this(buf.readString());
    }

    // El método que escribe los datos en el buffer.
    private void write(RegistryByteBuf buf) {
        buf.writeString(this.missionText);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * Registro el tipo de payload para que Fabric sepa cómo manejarlo.
     * Es crucial registrarlo tanto en el servidor como en el cliente para que la comunicación funcione.
     */
    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
    }
}