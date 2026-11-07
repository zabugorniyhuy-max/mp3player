package com.mp3player.mixin;

import com.mp3player.network.JukeboxSyncPayload;
import com.mp3player.network.StopCustomDiscPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin {
    @Inject(method = "popOutTheItem", at = @At("HEAD"))
    private void onDropRecord(CallbackInfo ci) {
        stopDiscPlayback();
    }

    @Inject(method = "preRemoveSideEffects", at = @At("HEAD"))
    private void onBlockReplaced(BlockPos pos, BlockState oldState, CallbackInfo ci) {
        stopDiscPlayback();
    }

    private void stopDiscPlayback() {
        JukeboxBlockEntity self = (JukeboxBlockEntity)(Object)this;
        Level world = self.getLevel();
        if (world instanceof ServerLevel serverWorld) {
            BlockPos pos = self.getBlockPos();
            StopCustomDiscPayload stopCustom = new StopCustomDiscPayload(pos);
            JukeboxSyncPayload stopSync = new JukeboxSyncPayload(pos, "", "", ItemStack.EMPTY, 0, false);
            for (ServerPlayer p : PlayerLookup.tracking(serverWorld, pos)) {
                if (ServerPlayNetworking.canSend(p, StopCustomDiscPayload.PACKET_ID)) {
                    ServerPlayNetworking.send(p, stopCustom);
                }
                if (ServerPlayNetworking.canSend(p, JukeboxSyncPayload.ID)) {
                    ServerPlayNetworking.send(p, stopSync);
                }
            }
        }
    }
}
