/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
 *  net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
 *  net.fabricmc.fabric.api.event.player.UseBlockCallback
 *  net.minecraft.block.entity.BlockEntity
 *  net.minecraft.block.entity.JukeboxBlockEntity
 *  net.minecraft.block.jukebox.JukeboxSong
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.font.TextRenderer$TextLayerType
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.render.OverlayTexture
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.model.json.ModelTransformationMode
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.NbtComponent
 *  net.minecraft.item.ItemConvertible
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.state.property.Properties
 *  net.minecraft.state.property.Property
 *  net.minecraft.text.Text
 *  net.minecraft.util.ActionResult
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.RotationAxis
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.World
 *  net.minecraft.world.chunk.WorldChunk
 *  org.joml.Matrix4f
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.mp3player;

import com.mp3player.client.KeyInputHandler;
import com.mp3player.client.ModSounds;
import com.mp3player.client.audio.AudioConverter;
import com.mp3player.client.audio.GlobalAudioTracker;
import com.mp3player.client.audio.JukeboxAudioPlayer;
import com.mp3player.client.audio.MusicPlayer;
import com.mp3player.client.config.ModConfig;
import com.mp3player.client.gsmtc.GsmtcHudOverlay;
import com.mp3player.client.gsmtc.GsmtcManager;
import com.mp3player.client.gui.ShareRequestScreen;
import com.mp3player.client.network.FileReceiverManager;
import com.mp3player.client.network.FileSenderManager;
import com.mp3player.network.FileTransferChunkPayload;
import com.mp3player.network.FileTransferEndPayload;
import com.mp3player.network.FileTransferStartPayload;
import com.mp3player.network.GlobalPlayClientPayload;
import com.mp3player.network.JukeboxSyncPayload;
import com.mp3player.network.PlayCustomDiscPayload;
import com.mp3player.network.RequestTrackClientPayload;
import com.mp3player.network.ShareRequestPayload;
import com.mp3player.network.ShareResponsePayload;
import com.mp3player.network.StopCustomDiscPayload;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MP3PlayerClient
implements ClientModInitializer {
    public static final String MODID = "mp3player";
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"mp3player");
    private static final Map<BlockPos, ActiveHologram> activeHolograms = new ConcurrentHashMap<BlockPos, ActiveHologram>();
    private static final Map<BlockPos, JukeboxAudioPlayer> activeJukeboxes = new HashMap<BlockPos, JukeboxAudioPlayer>();
    private int tickCounter = 0;

    public static Map<BlockPos, ActiveHologram> getActiveHolograms() {
        return activeHolograms;
    }

    public static Map<BlockPos, JukeboxAudioPlayer> getActiveJukeboxes() {
        return activeJukeboxes;
    }

    public static File findWavFile(String trackName) {
        File[] files;
        File file;
        String searchBase = trackName;
        int dSearch = searchBase.lastIndexOf(46);
        if (dSearch > 0) {
            searchBase = searchBase.substring(0, dSearch);
        }
        MusicPlayer local = MusicPlayer.getInstance();
        for (File file2 : local.getPlaylist()) {
            String n = file2.getName();
            int d = n.lastIndexOf(46);
            String fName = d > 0 ? n.substring(0, d) : n;
            if (!fName.equalsIgnoreCase(searchBase)) continue;
            if (file2.getName().toLowerCase().endsWith(".wav")) {
                return file2;
            }
            File wav = AudioConverter.convertToWav(file2);
            if (wav == null || !wav.exists()) continue;
            return wav;
        }
        File convertedFolder = new File(new File(MinecraftClient.getInstance().runDirectory, MODID), "converted");
        if (convertedFolder.exists()) {
            for (File f : convertedFolder.listFiles()) {
                String n;
                int d;
                if (!f.getName().toLowerCase().endsWith(".wav") || !((d = (n = f.getName()).lastIndexOf(46)) > 0 ? n.substring(0, d) : n).equalsIgnoreCase(searchBase)) continue;
                return f;
            }
        }
        if ((file = new File(MinecraftClient.getInstance().runDirectory, MODID)).exists() && (files = file.listFiles()) != null) {
            for (File f : files) {
                String fName;
                String n = f.getName();
                int d = n.lastIndexOf(46);
                String string = fName = d > 0 ? n.substring(0, d) : n;
                if (!fName.equalsIgnoreCase(searchBase)) continue;
                if (f.getName().toLowerCase().endsWith(".wav")) {
                    return f;
                }
                File wav = AudioConverter.convertToWav(f);
                if (wav == null || !wav.exists()) continue;
                return wav;
            }
        }
        return null;
    }

    public void onInitializeClient() {
        File instructionFile;
        LOGGER.info("MP3 Player initialized!");
        File instrFolder = new File(MinecraftClient.getInstance().runDirectory, MODID);
        if (!instrFolder.exists()) {
            instrFolder.mkdirs();
        }
        if (!(instructionFile = new File(instrFolder, "instruction.txt")).exists()) {
            try {
                Files.write(instructionFile.toPath(), "===== \u0418\u041d\u0421\u0422\u0420\u0423\u041a\u0426\u0418\u042f \u041a \u041c\u041e\u0414\u0423 MP3 PLAYER =====\n1. \u0417\u0430\u043a\u0438\u0434\u044b\u0432\u0430\u0439\u0442\u0435 \u0432\u0430\u0448\u0438 .mp3 \u0438\u043b\u0438 .wav \u0444\u0430\u0439\u043b\u044b \u0412 \u042d\u0422\u0423 \u041f\u0410\u041f\u041a\u0423 (mp3player).\n2. \u041d\u0415 \u0437\u0430\u043a\u0438\u0434\u044b\u0432\u0430\u0439\u0442\u0435 \u0438\u0445 \u0432 \u043f\u0430\u043f\u043a\u0443 'converted', \u043c\u043e\u0434 \u0441\u0434\u0435\u043b\u0430\u0435\u0442 \u044d\u0442\u043e \u0441\u0430\u043c.\n3. \u0417\u0430\u0439\u0434\u0438\u0442\u0435 \u0432 \u0438\u0433\u0440\u0443 \u0438 \u043d\u0430\u0436\u043c\u0438\u0442\u0435 \u041f\u0440\u0430\u0432\u044b\u0439 Shift (\u043f\u043e \u0443\u043c\u043e\u043b\u0447\u0430\u043d\u0438\u044e) \u0447\u0442\u043e\u0431\u044b \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u043c\u0435\u043d\u044e \u043f\u043b\u0435\u0435\u0440\u0430.\n4. \u0412 \u0440\u0435\u0436\u0438\u043c\u0435 '\u041a\u043e\u043b\u043e\u043d\u043a\u0430' \u043c\u0443\u0437\u044b\u043a\u0443 \u0431\u0443\u0434\u0443\u0442 \u0441\u043b\u044b\u0448\u0430\u0442\u044c \u0432\u0441\u0435 \u0438\u0433\u0440\u043e\u043a\u0438 \u0432\u043e\u043a\u0440\u0443\u0433!\n5. \u0412\u044b \u043c\u043e\u0436\u0435\u0442\u0435 \u0434\u0435\u043b\u0438\u0442\u044c\u0441\u044f \u043f\u0435\u0441\u043d\u044f\u043c\u0438 \u0441 \u0434\u0440\u0443\u0433\u0438\u043c\u0438 \u0438\u0433\u0440\u043e\u043a\u0430\u043c\u0438 \u043f\u0440\u044f\u043c\u043e \u0432 \u043c\u0435\u043d\u044e \u043c\u043e\u0434\u0430.\n=========================================\n".getBytes("UTF-8"), new OpenOption[0]);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        ClientPlayNetworking.registerGlobalReceiver(JukeboxSyncPayload.ID, (payload, context) -> context.client().execute(() -> {
            BlockPos pos = payload.pos();
            if (payload.isPlaying()) {
                activeHolograms.put(pos, new ActiveHologram(pos, payload.title(), payload.artist(), payload.discStack(), payload.durationSec()));
            } else {
                activeHolograms.remove(pos);
                if (activeJukeboxes.containsKey(pos)) {
                    activeJukeboxes.get(pos).stop();
                    activeJukeboxes.remove(pos);
                }
            }
        }));
        ClientPlayNetworking.registerGlobalReceiver(PlayCustomDiscPayload.ID, (payload, context) -> context.client().execute(() -> {
            File trackFile;
            File musicFolder;
            if (ModConfig.getInstance().muteGlobal) {
                return;
            }
            String trackName = payload.trackName();
            BlockPos pos = payload.pos();
            MusicPlayer local = MusicPlayer.getInstance();
            if (local.getPlaylist().isEmpty() && !(musicFolder = new File(MinecraftClient.getInstance().runDirectory, MODID)).exists()) {
                musicFolder.mkdirs();
            }
            if (local.isPlaying()) {
                local.pause();
            }
            if (!activeHolograms.containsKey(pos)) {
                String title = trackName;
                String artist = "\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u0435\u043d";
                if (trackName.contains(" - ")) {
                    String[] p = trackName.split(" - ", 2);
                    artist = p[0].trim();
                    title = p[1].replace(".wav", "").replace(".mp3", "").trim();
                } else {
                    title = trackName.replace(".wav", "").replace(".mp3", "").trim();
                }
                activeHolograms.put(pos, new ActiveHologram(pos, title, artist, null, -1L));
            }
            if ((trackFile = MP3PlayerClient.findWavFile(trackName)) != null) {
                if (activeJukeboxes.containsKey(pos)) {
                    activeJukeboxes.get(pos).stop();
                }
                JukeboxAudioPlayer player = new JukeboxAudioPlayer(pos, trackFile);
                activeJukeboxes.put(pos, player);
                player.play();
            }
        }));
        ClientPlayNetworking.registerGlobalReceiver(StopCustomDiscPayload.PACKET_ID, (payload, context) -> context.client().execute(() -> {
            BlockPos pos = payload.pos();
            activeHolograms.remove(pos);
            if (activeJukeboxes.containsKey(pos)) {
                activeJukeboxes.get(pos).stop();
                activeJukeboxes.remove(pos);
            }
        }));
        ClientPlayNetworking.registerGlobalReceiver(GlobalPlayClientPayload.PACKET_ID, (payload, context) -> context.client().execute(() -> {
            if (ModConfig.getInstance().muteGlobal) {
                return;
            }
            if (context.client().player != null && context.client().player.getUuid().equals(payload.sender())) {
                return;
            }
            GlobalAudioTracker.getInstance().playGlobal(payload.sender(), payload.trackName(), payload.position(), payload.range(), payload.volume());
        }));
        ClientPlayNetworking.registerGlobalReceiver(FileTransferStartPayload.PACKET_ID, (payload, context) -> context.client().execute(() -> FileReceiverManager.getInstance().startDownload(payload.fileName(), payload.fileSize(), payload.totalChunks(), payload.sender())));
        ClientPlayNetworking.registerGlobalReceiver(FileTransferChunkPayload.PACKET_ID, (payload, context) -> context.client().execute(() -> FileReceiverManager.getInstance().receiveChunk(payload.fileName(), payload.chunkIndex(), payload.data(), payload.sender())));
        ClientPlayNetworking.registerGlobalReceiver(FileTransferEndPayload.PACKET_ID, (payload, context) -> context.client().execute(() -> FileReceiverManager.getInstance().endDownload(payload.fileName(), payload.sender())));
        ClientPlayNetworking.registerGlobalReceiver(ShareRequestPayload.PACKET_ID, (payload, context) -> context.client().execute(() -> context.client().setScreen((Screen)new ShareRequestScreen(payload.fileName(), payload.sender(), payload.senderName()))));
        ClientPlayNetworking.registerGlobalReceiver(ShareResponsePayload.PACKET_ID, (payload, context) -> context.client().execute(() -> {
            if (payload.accepted()) {
                File fileToShare = null;
                File musicFolder = new File(MinecraftClient.getInstance().runDirectory, MODID);
                File f = new File(musicFolder, payload.fileName());
                if (f.exists()) {
                    fileToShare = f;
                } else {
                    f = new File(new File(musicFolder, "converted"), payload.fileName());
                    if (f.exists()) {
                        fileToShare = f;
                    }
                }
                if (fileToShare != null) {
                    FileSenderManager.getInstance().startUpload(fileToShare, payload.responder());
                    if (context.client().player != null) {
                        context.client().player.sendMessage((Text)Text.literal((String)("\u00a7a\u0418\u0433\u0440\u043e\u043a \u043f\u0440\u0438\u043d\u044f\u043b \u0437\u0430\u043f\u0440\u043e\u0441. \u041d\u0430\u0447\u0438\u043d\u0430\u044e \u043e\u0442\u043f\u0440\u0430\u0432\u043a\u0443 \u00a7e" + payload.fileName())), false);
                    }
                }
            } else if (context.client().player != null) {
                context.client().player.sendMessage((Text)Text.literal((String)("\u00a7c\u0418\u0433\u0440\u043e\u043a \u043e\u0442\u043a\u043b\u043e\u043d\u0438\u043b \u0437\u0430\u043f\u0440\u043e\u0441 \u043d\u0430 \u043f\u0435\u0441\u043d\u044e \u00a7e" + payload.fileName())), false);
            }
        }));
        ClientPlayNetworking.registerGlobalReceiver(RequestTrackClientPayload.PACKET_ID, (payload, context) -> context.client().execute(() -> {
            File[] files;
            File mainFolder;
            File trackFile = MP3PlayerClient.findWavFile(payload.trackName());
            if (trackFile == null && (mainFolder = new File(MinecraftClient.getInstance().runDirectory, MODID)).exists() && (files = mainFolder.listFiles()) != null) {
                for (File f : files) {
                    String n;
                    int d;
                    if (!f.getName().toLowerCase().endsWith(".mp3") || !((d = (n = f.getName()).lastIndexOf(46)) > 0 ? n.substring(0, d) : n).equalsIgnoreCase(payload.trackName())) continue;
                    trackFile = f;
                    break;
                }
            }
            if (trackFile != null) {
                FileSenderManager.getInstance().startUpload(trackFile, payload.requester());
            }
        }));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            activeJukeboxes.values().removeIf(player -> {
                if (!player.isPlayingOrPaused()) {
                    return true;
                }
                player.updateVolume();
                return false;
            });
            GlobalAudioTracker.getInstance().updateAll();
            FileSenderManager.getInstance().tick();
            MusicPlayer.getInstance().applyVolume();
            GsmtcManager.getInstance().tick();
            ++this.tickCounter;
            if (this.tickCounter >= 20) {
                this.tickCounter = 0;
                MusicPlayer.getInstance().broadcastState();
                GlobalAudioTracker.getInstance().getActivePlayers().entrySet().removeIf(entry -> {
                    if (((GlobalAudioTracker.GlobalPlayer)entry.getValue()).isStale()) {
                        ((GlobalAudioTracker.GlobalPlayer)entry.getValue()).stop();
                        return true;
                    }
                    return false;
                });
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            MusicPlayer.getInstance().stop();
            for (JukeboxAudioPlayer player : activeJukeboxes.values()) {
                player.stop();
            }
            activeJukeboxes.clear();
            GlobalAudioTracker.getInstance().stopAll();
        });
        GsmtcHudOverlay.register();
        ModSounds.register();
        KeyInputHandler.register();
        ScreenEvents.AFTER_INIT.register((MinecraftClient mc, Screen scr, int sw, int sh) -> {
            ScreenMouseEvents.allowMouseClick(scr).register((Screen s, net.minecraft.client.gui.Click click) -> {
                return click.button() != 0 || mc.world == null || !GsmtcHudOverlay.handleClick(click.x(), click.y());
            });
        });
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            BlockPos pos;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null) {
                return;
            }
            Camera camera = client.gameRenderer != null ? client.gameRenderer.getCamera() : null;
            if (camera == null) {
                return;
            }
            Vec3d camPos = camera.getCameraPos();
            if (camPos == null) {
                return;
            }
            MatrixStack matrices = context.matrices();
            VertexConsumerProvider vertexConsumers = context.consumers();
            net.minecraft.client.render.command.OrderedRenderCommandQueue commandQueue = context.commandQueue();
            if (matrices == null || vertexConsumers == null) {
                return;
            }
            HashSet<BlockPos> rendered = new HashSet<BlockPos>();
            for (ActiveHologram activeHologram : activeHolograms.values()) {
                double dz;
                double dy;
                pos = activeHologram.pos;
                double dx = (double)pos.getX() + 0.5 - camPos.x;
                double distSq = dx * dx + (dy = (double)pos.getY() + 1.25 - camPos.y) * dy + (dz = (double)pos.getZ() + 0.5 - camPos.z) * dz;
                if (distSq > 1296.0) continue;
                rendered.add(pos);
                long curSec = (System.currentTimeMillis() - activeHologram.startTimeMs) / 1000L;
                long durSec = activeHologram.durationSec;
                boolean isPaused = activeHologram.paused;
                JukeboxAudioPlayer player = activeJukeboxes.get(pos);
                if (player != null) {
                    curSec = player.getCurrentPositionMs() / 1000L;
                    durSec = player.getDurationMs() / 1000L;
                    isPaused = player.isPaused();
                }
                MP3PlayerClient.drawJukeboxHologramUnified(client, activeHologram.title, activeHologram.artist, activeHologram.discStack, curSec, durSec, isPaused, dx, dy, dz, matrices, vertexConsumers, commandQueue, camera);
            }
            for (Map.Entry entry : activeJukeboxes.entrySet()) {
                double dz;
                double dy;
                double dx;
                double distSq;
                JukeboxAudioPlayer player;
                pos = (BlockPos)entry.getKey();
                if (rendered.contains(pos) || !(player = (JukeboxAudioPlayer)entry.getValue()).isPlayingOrPaused() || (distSq = (dx = (double)pos.getX() + 0.5 - camPos.x) * dx + (dy = (double)pos.getY() + 1.25 - camPos.y) * dy + (dz = (double)pos.getZ() + 0.5 - camPos.z) * dz) > 1296.0) continue;
                rendered.add(pos);
                String rawName = player.getTrackName();
                String artist = "\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u0435\u043d";
                String title = rawName;
                if (rawName.contains(" - ")) {
                    String[] p = rawName.split(" - ", 2);
                    artist = p[0].trim();
                    title = p[1].replace(".wav", "").replace(".mp3", "").trim();
                } else {
                    title = rawName.replace(".wav", "").replace(".mp3", "").trim();
                }
                ItemStack discStack = null;
                BlockEntity be = client.world.getBlockEntity(pos);
                if (be instanceof JukeboxBlockEntity) {
                    JukeboxBlockEntity jbe = (JukeboxBlockEntity)be;
                    discStack = jbe.getStack();
                }
                long curSec = player.getCurrentPositionMs() / 1000L;
                long durSec = player.getDurationMs() / 1000L;
                MP3PlayerClient.drawJukeboxHologramUnified(client, title, artist, discStack, curSec, durSec, player.isPaused(), dx, dy, dz, matrices, vertexConsumers, commandQueue, camera);
            }
            int chunkX = (int)camPos.x >> 4;
            int n = (int)camPos.z >> 4;
            for (int cx = chunkX - 1; cx <= chunkX + 1; ++cx) {
                for (int cz = n - 1; cz <= n + 1; ++cz) {
                    WorldChunk chunk;
                    if (!client.world.isChunkLoaded(cx, cz) || (chunk = client.world.getChunk(cx, cz)) == null) continue;
                    for (BlockEntity be : chunk.getBlockEntities().values()) {
                        NbtComponent customData;
                        String[] p;
                        double dz;
                        double dy;
                        double dx;
                        double distSq;
                        JukeboxBlockEntity jukebox;
                        BlockPos pos2;
                        if (!(be instanceof JukeboxBlockEntity) || rendered.contains(pos2 = (jukebox = (JukeboxBlockEntity)be).getPos()) || (distSq = (dx = (double)pos2.getX() + 0.5 - camPos.x) * dx + (dy = (double)pos2.getY() + 1.25 - camPos.y) * dy + (dz = (double)pos2.getZ() + 0.5 - camPos.z) * dz) > 1296.0) continue;
                        ItemStack stack = jukebox.getStack();
                        boolean hasRecordBlock = client.world.getBlockState(pos2).contains(Properties.HAS_RECORD) && client.world.getBlockState(pos2).get(Properties.HAS_RECORD);
                        if (!hasRecordBlock && (stack == null || stack.isEmpty())) continue;
                        boolean isPlaying = false;
                        long curSec = 0L;
                        long durSec = 0L;
                        String title = stack != null && !stack.isEmpty() ? stack.getName().getString() : "\u041f\u043b\u0430\u0441\u0442\u0438\u043d\u043a\u0430";
                        String artist = "Minecraft";
                        if (jukebox.getManager() != null) {
                            isPlaying = jukebox.getManager().isPlaying();
                            curSec = jukebox.getManager().getTicksSinceSongStarted() / 20L;
                            JukeboxSong song = jukebox.getManager().getSong();
                            if (song != null) {
                                durSec = (long)song.lengthInSeconds();
                                String desc = song.description().getString();
                                if (desc.contains(" - ")) {
                                    p = desc.split(" - ", 2);
                                    artist = p[0].trim();
                                    title = p[1].trim();
                                } else {
                                    title = desc;
                                }
                            }
                        }
                        if (stack != null && !stack.isEmpty() && (customData = (NbtComponent)stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, (Object)NbtComponent.DEFAULT)).copyNbt().contains("CustomMp3")) {
                            String customTrack = customData.copyNbt().getString("CustomMp3").orElse("");
                            if (customTrack.contains(" - ")) {
                                p = customTrack.split(" - ", 2);
                                artist = p[0].trim();
                                title = p[1].replace(".wav", "").replace(".mp3", "").trim();
                            } else {
                                title = customTrack.replace(".wav", "").replace(".mp3", "").trim();
                            }
                            isPlaying = true;
                        }
                        if (!isPlaying && !hasRecordBlock) continue;
                        rendered.add(pos2);
                        MP3PlayerClient.drawJukeboxHologramUnified(client, title, artist, stack, curSec, durSec, false, dx, dy, dz, matrices, vertexConsumers, commandQueue, camera);
                    }
                }
            }

            float tickProgress = client.getRenderTickCounter().getTickProgress(false);
            for (net.minecraft.client.network.AbstractClientPlayerEntity player : client.world.getPlayers()) {
                String pTrack = null;
                long pCurSec = 0L;
                long pDurSec = 0L;

                if (player.getUuid().equals(client.player.getUuid())) {
                    MusicPlayer localP = MusicPlayer.getInstance();
                    if (ModConfig.getInstance().speakerMode) {
                        if (localP.isPlaying()) {
                            pTrack = localP.getCurrentTrackName();
                            pCurSec = localP.getCurrentPosition() / 1_000_000L;
                            pDurSec = localP.getDuration() / 1_000_000L;
                        } else if (GsmtcManager.getInstance().isPlaying()) {
                            GsmtcManager g = GsmtcManager.getInstance();
                            String t = g.getTitle();
                            String a = g.getArtist();
                            pTrack = a != null && !a.isEmpty() ? a + " - " + t : t;
                            pCurSec = (long)g.getPosition();
                            pDurSec = (long)g.getDuration();
                        }
                    }
                } else {
                    GlobalAudioTracker.GlobalPlayer gp = GlobalAudioTracker.getInstance().getActivePlayers().get(player.getUuid());
                    if (gp != null && gp.isPlaying()) {
                        pTrack = gp.getTrackName();
                        pCurSec = gp.getCurrentPositionMs() / 1000L;
                        pDurSec = gp.getDurationMs() / 1000L;
                    }
                }

                if (pTrack != null && !pTrack.isEmpty()) {
                    Vec3d pPos = player.getLerpedPos(tickProgress);
                    double px = pPos.x - camPos.x;
                    double py = pPos.y - camPos.y + player.getHeight() + 0.6;
                    double pz = pPos.z - camPos.z;

                    matrices.push();
                    matrices.translate(px, py, pz);
                    matrices.multiply(camera.getRotation());
                    drawPlayerHologram(pTrack, pCurSec, pDurSec, matrices, vertexConsumers, 0xF000F0);
                    matrices.pop();
                }
            }
        });
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient() || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            BlockPos pos = hitResult.getBlockPos();
            ActiveHologram holo = activeHolograms.get(pos);
            JukeboxAudioPlayer jPlayer = activeJukeboxes.get(pos);
            if (jPlayer != null && jPlayer.isPlayingOrPaused()) {
                jPlayer.togglePlayPause();
                if (holo != null) {
                    holo.paused = jPlayer.isPaused();
                }
                String msg = jPlayer.isPaused() ? "\u00a76\u23f8 \u041f\u0430\u0443\u0437\u0430 \u043f\u0440\u043e\u0438\u0433\u0440\u044b\u0432\u0430\u0442\u0435\u043b\u044f: \u00a7f" : "\u00a7a\u25b6 \u0412\u043e\u0441\u043f\u0440\u043e\u0438\u0437\u0432\u0435\u0434\u0435\u043d\u0438\u0435: \u00a7f";
                player.sendMessage((Text)Text.literal((String)(msg + jPlayer.getTrackName())), true);
                return ActionResult.SUCCESS;
            }
            if (holo != null) {
                holo.paused = !holo.paused;
                String msg = holo.paused ? "\u00a76\u23f8 \u041f\u0430\u0443\u0437\u0430 \u043f\u0440\u043e\u0438\u0433\u0440\u044b\u0432\u0430\u0442\u0435\u043b\u044f: \u00a7f" : "\u00a7a\u25b6 \u0412\u043e\u0441\u043f\u0440\u043e\u0438\u0437\u0432\u0435\u0434\u0435\u043d\u0438\u0435: \u00a7f";
                player.sendMessage((Text)Text.literal((String)(msg + holo.title)), true);
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }

    public static void drawJukeboxHologramUnified(MinecraftClient client, String title, String artist, ItemStack discStack, long curSec, long durSec, boolean isPaused, double dx, double dy, double dz, MatrixStack matrices, VertexConsumerProvider vertexConsumers, net.minecraft.client.render.command.OrderedRenderCommandQueue commandQueue, Camera camera) {
        int i;
        TextRenderer tr = client.textRenderer;
        ItemStack renderStack = discStack != null && !discStack.isEmpty() ? discStack : new ItemStack((ItemConvertible)Items.MUSIC_DISC_CAT);
        matrices.push();
        matrices.translate(dx, dy + 0.18, dz);
        float spin = (float)(System.currentTimeMillis() % 3500L) / 3500.0f * 360.0f;
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spin));
        matrices.scale(0.42f, 0.42f, 0.42f);
        if (commandQueue != null && client.getItemModelManager() != null) {
            net.minecraft.client.render.item.ItemRenderState itemState = new net.minecraft.client.render.item.ItemRenderState();
            client.getItemModelManager().clearAndUpdate(itemState, renderStack, ItemDisplayContext.FIXED, (World)client.world, null, 0);
            itemState.render(matrices, commandQueue, 0xF000F0, OverlayTexture.DEFAULT_UV, 0);
        }
        matrices.pop();
        String statusIcon = isPaused ? "\u00a76\u23f8" : "\u00a7a\u25b6";
        String line1 = statusIcon + " \u00a7e\u00a7l" + title;
        String line2 = "\u00a77\u0410\u0432\u0442\u043e\u0440: \u00a7f" + (artist.isEmpty() ? "\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u0435\u043d" : artist);
        float frac = durSec > 0L ? Math.max(0.0f, Math.min(1.0f, (float)curSec / (float)durSec)) : 0.0f;
        int totalBars = 12;
        int filled = (int)(frac * (float)totalBars);
        StringBuilder bar = new StringBuilder("\u00a76");
        for (i = 0; i < filled; ++i) {
            bar.append("\u2501");
        }
        bar.append("\u00a78");
        for (i = filled; i < totalBars; ++i) {
            bar.append("\u2501");
        }
        String line3 = String.format("\u00a7f%d:%02d \u00a78[%s\u00a78] \u00a77%d:%02d", curSec / 60L, curSec % 60L, bar.toString(), durSec / 60L, durSec % 60L);
        String line4 = isPaused ? "\u00a7e[\u041f\u041a\u041c: \u0412\u043e\u0437\u043e\u0431\u043d\u043e\u0432\u0438\u0442\u044c]" : "\u00a7b[\u041f\u041a\u041c: \u041f\u0430\u0443\u0437\u0430]";
        matrices.push();
        matrices.translate(dx, dy + 0.4, dz);
        matrices.multiply(camera.getRotation());
        matrices.scale(-0.022f, -0.022f, 0.022f);
        Matrix4f posMat = matrices.peek().getPositionMatrix();
        int bgColor = 0x66000000;
        float x1 = (float)(-tr.getWidth(line1)) / 2.0f;
        tr.draw(line1, x1, -22.0f, -1, false, posMat, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, bgColor, 0xF000F0);
        float x2 = (float)(-tr.getWidth(line2)) / 2.0f;
        tr.draw(line2, x2, -11.0f, -1, false, posMat, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, bgColor, 0xF000F0);
        float x3 = (float)(-tr.getWidth(line3)) / 2.0f;
        tr.draw(line3, x3, 0.0f, -1, false, posMat, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, bgColor, 0xF000F0);
        float x4 = (float)(-tr.getWidth(line4)) / 2.0f;
        tr.draw(line4, x4, 11.0f, -1, false, posMat, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, bgColor, 0xF000F0);
        matrices.pop();
    }

    public static void drawPlayerHologram(String trackName, long currentSec, long durationSec, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (trackName == null || trackName.isEmpty()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        String timeStr = durationSec > 0L ? String.format("%d:%02d / %d:%02d", currentSec / 60L, currentSec % 60L, durationSec / 60L, durationSec % 60L) : String.format("%d:%02d", currentSec / 60L, currentSec % 60L);
        String displayText = "\u25b6 " + trackName + " (" + timeStr + ")";
        float x = -textRenderer.getWidth(displayText) / 2;
        matrices.push();
        matrices.translate(0.0, 0.7, 0.0);
        matrices.scale(-0.025f, -0.025f, 0.025f);
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        int bgColor = client.options.getTextBackgroundColor(0.25f);
        textRenderer.draw(displayText, x, 0.0f, 0xFFCC00, false, positionMatrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, bgColor, light);
        matrices.pop();
    }

    public static class ActiveHologram {
        public final BlockPos pos;
        public final String title;
        public final String artist;
        public final ItemStack discStack;
        public final long startTimeMs;
        public final long durationSec;
        public boolean paused;

        public ActiveHologram(BlockPos pos, String title, String artist, ItemStack discStack, long durationSec) {
            this.pos = pos;
            this.title = title;
            this.artist = artist;
            this.discStack = discStack != null && !discStack.isEmpty() ? discStack.copy() : new ItemStack((ItemConvertible)Items.MUSIC_DISC_CAT);
            this.startTimeMs = System.currentTimeMillis();
            this.durationSec = durationSec;
            this.paused = false;
        }
    }
}

