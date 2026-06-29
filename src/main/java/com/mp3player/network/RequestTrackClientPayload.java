package com.mp3player.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import java.util.UUID;

public record RequestTrackClientPayload(String trackName, UUID requester) implements CustomPayload {
    public static final Identifier ID = Identifier.of("mp3player", "request_track_s2c");
    public static final CustomPayload.Id<RequestTrackClientPayload> PACKET_ID = new CustomPayload.Id<>(ID);

    public static final PacketCodec<RegistryByteBuf, RequestTrackClientPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, RequestTrackClientPayload::trackName,
        Uuids.PACKET_CODEC, RequestTrackClientPayload::requester,
        RequestTrackClientPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
