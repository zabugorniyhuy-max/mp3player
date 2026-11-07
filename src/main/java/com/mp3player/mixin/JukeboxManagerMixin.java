package com.mp3player.mixin;

import com.mp3player.network.JukeboxSyncPayload;
import com.mp3player.network.PlayCustomDiscPayload;
import com.mp3player.network.StopCustomDiscPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JukeboxSongPlayer.class)
public class JukeboxManagerMixin {
    @Shadow
    private BlockPos blockPos;

    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void onStartPlaying(LevelAccessor world, Holder<JukeboxSong> songEntry, CallbackInfo ci) {
        if (world instanceof ServerLevel serverWorld) {
            BlockEntity blockEntity = serverWorld.getBlockEntity(this.blockPos);
            if (blockEntity instanceof JukeboxBlockEntity jukebox) {
                ItemStack stack = jukebox.getTheItem();
                CustomData customData;
                if (stack != null && !stack.isEmpty() && (customData = (CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag().contains("CustomMp3")) {
                    String mp3 = customData.copyTag().getString("CustomMp3").orElse("");
                    String title = mp3;
                    String artist = "Неизвестен";
                    if (mp3.contains(" - ")) {
                        String[] p = mp3.split(" - ", 2);
                        artist = p[0].trim();
                        title = p[1].replace(".wav", "").replace(".mp3", "").trim();
                    } else {
                        title = mp3.replace(".wav", "").replace(".mp3", "").trim();
                    }
                    JukeboxSyncPayload sync = new JukeboxSyncPayload(this.blockPos, title, artist, stack, -1, true);
                    for (ServerPlayer p : PlayerLookup.tracking(serverWorld, this.blockPos)) {
                        if (ServerPlayNetworking.canSend(p, JukeboxSyncPayload.ID)) {
                            ServerPlayNetworking.send(p, sync);
                        }
                        if (ServerPlayNetworking.canSend(p, PlayCustomDiscPayload.ID)) {
                            ServerPlayNetworking.send(p, new PlayCustomDiscPayload(this.blockPos, mp3));
                        }
                    }
                    ci.cancel();
                    return;
                }

                String title = "Пластинка";
                String artist = "Minecraft";
                int dur = 180;
                if (songEntry != null) {
                    JukeboxSong song = songEntry.value();
                    if (song != null) {
                        dur = (int)song.lengthInSeconds();
                        String desc = song.description().getString();
                        if (desc.contains(" - ")) {
                            String[] p = desc.split(" - ", 2);
                            artist = p[0].trim();
                            title = p[1].trim();
                        } else {
                            title = desc;
                        }
                    }
                } else if (stack != null && !stack.isEmpty()) {
                    title = stack.getHoverName().getString();
                }

                JukeboxSyncPayload sync = new JukeboxSyncPayload(this.blockPos, title, artist, stack != null ? stack : ItemStack.EMPTY, dur, true);
                for (ServerPlayer p : PlayerLookup.tracking(serverWorld, this.blockPos)) {
                    if (ServerPlayNetworking.canSend(p, JukeboxSyncPayload.ID)) {
                        ServerPlayNetworking.send(p, sync);
                    }
                }
            }
        }
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void onStopPlaying(LevelAccessor world, BlockState state, CallbackInfo ci) {
        if (world instanceof ServerLevel serverWorld) {
            JukeboxSyncPayload stopSync = new JukeboxSyncPayload(this.blockPos, "", "", ItemStack.EMPTY, 0, false);
            for (ServerPlayer p : PlayerLookup.tracking(serverWorld, this.blockPos)) {
                if (ServerPlayNetworking.canSend(p, JukeboxSyncPayload.ID)) {
                    ServerPlayNetworking.send(p, stopSync);
                }
                if (ServerPlayNetworking.canSend(p, StopCustomDiscPayload.PACKET_ID)) {
                    ServerPlayNetworking.send(p, new StopCustomDiscPayload(this.blockPos));
                }
            }
        }
    }
}
