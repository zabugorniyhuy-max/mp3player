package com.mp3player.mixin;

import com.mp3player.network.PlayCustomDiscPayload;
import com.mp3player.network.StopCustomDiscPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.block.jukebox.JukeboxManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JukeboxManager.class)
public class JukeboxManagerMixin {

    @Shadow private BlockPos pos;

    @Inject(method = "startPlaying", at = @At("HEAD"), cancellable = true)
    private void onStartPlaying(WorldAccess world, RegistryEntry<?> song, CallbackInfo ci) {
        if (world instanceof ServerWorld serverWorld) {
            BlockEntity blockEntity = serverWorld.getBlockEntity(this.pos);
            if (blockEntity instanceof JukeboxBlockEntity jukebox) {
                // В 1.21.1 JukeboxBlockEntity.getStack() 
                ItemStack stack = jukebox.getStack();
                if (stack != null && !stack.isEmpty()) {
                    NbtComponent customData = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
                    if (customData.contains("CustomMp3")) {
                        String mp3 = customData.copyNbt().getString("CustomMp3");
                        for (ServerPlayerEntity p : PlayerLookup.tracking(serverWorld, this.pos)) {
                            ServerPlayNetworking.send(p, new PlayCustomDiscPayload(this.pos, mp3));
                        }
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Inject(method = "stopPlaying", at = @At("HEAD"))
    private void onStopPlaying(WorldAccess world, net.minecraft.block.BlockState state, CallbackInfo ci) {
        if (world instanceof ServerWorld serverWorld) {
            for (ServerPlayerEntity p : PlayerLookup.tracking(serverWorld, this.pos)) {
                ServerPlayNetworking.send(p, new StopCustomDiscPayload(this.pos));
            }
        }
    }
}
