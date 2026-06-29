package com.mp3player.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import java.util.UUID;

public record RequestTrackPayload(String trackName, UUID owner) implements CustomPayload {
    public static final Identifier ID = Identifier.of("mp3player", "request_track_c2s");
    public static final CustomPayload.Id<RequestTrackPayload> PACKET_ID = new CustomPayload.Id<>(ID);

    public static final PacketCodec<RegistryByteBuf, RequestTrackPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, RequestTrackPayload::trackName,
        Uuids.PACKET_CODEC, RequestTrackPayload::owner,
        RequestTrackPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
