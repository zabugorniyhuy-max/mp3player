package com.mp3player.mixin;

import com.mp3player.MP3PlayerClient;
import com.mp3player.client.audio.GlobalAudioTracker;
import com.mp3player.client.audio.MusicPlayer;
import com.mp3player.client.config.ModConfig;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    @Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"), require = 0)
    private void onRender(AbstractClientPlayerEntity player, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.player == null) return;
        
        String trackName = null;
        long currentSec = 0;
        long durationSec = 0;
        
        if (player.getUuid().equals(client.player.getUuid())) {
            MusicPlayer localP = MusicPlayer.getInstance();
            if (ModConfig.getInstance().speakerMode && localP.isPlaying()) {
                trackName = localP.getCurrentTrackName();
                currentSec = localP.getCurrentPosition() / 1_000_000;
                durationSec = localP.getDuration() / 1_000_000;
            }
        } else {
            GlobalAudioTracker.GlobalPlayer gp = GlobalAudioTracker.getInstance().getActivePlayers().get(player.getUuid());
            if (gp != null && gp.isPlaying()) {
                trackName = gp.getTrackName();
                currentSec = gp.getCurrentPositionMs() / 1000;
                durationSec = gp.getDurationMs() / 1000;
            }
        }
        
        if (trackName != null && !trackName.isEmpty()) {
            matrices.push();
            // The matrix is already positioned at the player entity.
            // renderLabelIfPresent already translates up by entity.getHeight() + 0.5F
            // We just translate a bit higher
            matrices.translate(0.0D, player.getHeight() + 0.5D + 0.3D, 0.0D);
            matrices.multiply(client.gameRenderer.getCamera().getRotation());
            MP3PlayerClient.drawPlayerHologram(trackName, currentSec, durationSec, matrices, vertexConsumers, light);
            matrices.pop();
        }
    }
}
