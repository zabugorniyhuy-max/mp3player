package com.mp3player.mixin;

import com.mp3player.network.JukeboxSyncPayload;
import com.mp3player.network.StopCustomDiscPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin {
    @Inject(method = "dropRecord", at = @At("HEAD"))
    private void onDropRecord(CallbackInfo ci) {
        stopDiscPlayback();
    }

    @Inject(method = "onBlockReplaced", at = @At("HEAD"))
    private void onBlockReplaced(BlockPos pos, BlockState oldState, CallbackInfo ci) {
        stopDiscPlayback();
    }

    private void stopDiscPlayback() {
        JukeboxBlockEntity self = (JukeboxBlockEntity)(Object)this;
        World world = self.getWorld();
        if (world instanceof ServerWorld serverWorld) {
            BlockPos pos = self.getPos();
            StopCustomDiscPayload stopCustom = new StopCustomDiscPayload(pos);
            JukeboxSyncPayload stopSync = new JukeboxSyncPayload(pos, "", "", ItemStack.EMPTY, 0, false);
            for (ServerPlayerEntity p : PlayerLookup.tracking(serverWorld, pos)) {
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
