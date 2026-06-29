package com.mp3player;

import com.mp3player.client.KeyInputHandler;
import com.mp3player.client.ModSounds;
import com.mp3player.client.audio.GlobalAudioTracker;
import com.mp3player.client.audio.JukeboxAudioPlayer;
import com.mp3player.client.audio.MusicPlayer;
import com.mp3player.client.config.ModConfig;
import com.mp3player.network.GlobalPlayClientPayload;
import com.mp3player.network.PlayCustomDiscPayload;
import com.mp3player.network.StopCustomDiscPayload;
import com.mp3player.network.FileTransferStartPayload;
import com.mp3player.network.FileTransferChunkPayload;
import com.mp3player.network.FileTransferEndPayload;
import com.mp3player.client.network.FileSenderManager;
import com.mp3player.client.network.FileReceiverManager;
import com.mp3player.network.RequestTrackClientPayload;
import com.mp3player.network.ShareRequestPayload;
import com.mp3player.network.ShareResponsePayload;
import com.mp3player.client.gui.ShareRequestScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MP3PlayerClient implements ClientModInitializer {
    public static final String MODID = "mp3player";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    
    private static final Map<BlockPos, JukeboxAudioPlayer> activeJukeboxes = new HashMap<>();
    private int tickCounter = 0;

    public static Map<BlockPos, JukeboxAudioPlayer> getActiveJukeboxes() {
        return activeJukeboxes;
    }

    public static File findWavFile(String trackName) {
        String searchBase = trackName;
        int dSearch = searchBase.lastIndexOf('.');
        if (dSearch > 0) searchBase = searchBase.substring(0, dSearch);
        
        MusicPlayer local = MusicPlayer.getInstance();
        for (File f : local.getPlaylist()) {
            String n = f.getName();
            int d = n.lastIndexOf('.');
            String fName = d > 0 ? n.substring(0, d) : n;
            if (fName.equalsIgnoreCase(searchBase)) return f;
        }
        
        // Force scan converted folder
        File convertedFolder = new File(new File(MinecraftClient.getInstance().runDirectory, "mp3player"), "converted");
        if (convertedFolder.exists()) {
            for (File f : convertedFolder.listFiles()) {
                if (f.getName().toLowerCase().endsWith(".wav")) {
                    String n = f.getName();
                    int d = n.lastIndexOf('.');
                    if ((d > 0 ? n.substring(0, d) : n).equalsIgnoreCase(searchBase)) return f;
                }
            }
        }
        
        // Force scan main folder
        File mainFolder = new File(MinecraftClient.getInstance().runDirectory, "mp3player");
        if (mainFolder.exists()) {
            for (File f : mainFolder.listFiles()) {
                if (f.getName().toLowerCase().endsWith(".wav")) {
                    String n = f.getName();
                    int d = n.lastIndexOf('.');
                    if ((d > 0 ? n.substring(0, d) : n).equalsIgnoreCase(searchBase)) return f;
                }
            }
        }
        
        return null;
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("MP3 Player initialized!");

        File instrFolder = new File(net.minecraft.client.MinecraftClient.getInstance().runDirectory, "mp3player");
        if (!instrFolder.exists()) instrFolder.mkdirs();
        File instructionFile = new File(instrFolder, "instruction.txt");
        if (!instructionFile.exists()) {
            try {
                java.nio.file.Files.write(instructionFile.toPath(), (
                    "===== ИНСТРУКЦИЯ К МОДУ MP3 PLAYER =====\n" +
                    "1. Закидывайте ваши .mp3 или .wav файлы В ЭТУ ПАПКУ (mp3player).\n" +
                    "2. НЕ закидывайте их в папку 'converted', мод сделает это сам.\n" +
                    "3. Зайдите в игру и нажмите Правый Shift (по умолчанию) чтобы открыть меню плеера.\n" +
                    "4. В режиме 'Колонка' музыку будут слышать все игроки вокруг!\n" +
                    "5. Вы можете делиться песнями с другими игроками прямо в меню мода.\n" +
                    "=========================================\n"
                ).getBytes("UTF-8"));
            } catch (Exception e) {}
        }

        
        ClientPlayNetworking.registerGlobalReceiver(PlayCustomDiscPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (ModConfig.getInstance().muteGlobal) return;
                
                String trackName = payload.trackName();
                BlockPos pos = payload.pos();
                MusicPlayer local = MusicPlayer.getInstance();
                
                if (local.getPlaylist().isEmpty()) {
                    File musicFolder = new File(MinecraftClient.getInstance().runDirectory, "mp3player");
                    if (!musicFolder.exists()) musicFolder.mkdirs();
                }
                
                if (local.isPlaying()) {
                    local.pause();
                }
                
                File trackFile = findWavFile(trackName);
                
                if (trackFile != null) {
                    if (activeJukeboxes.containsKey(pos)) {
                        activeJukeboxes.get(pos).stop();
                    }
                    JukeboxAudioPlayer player = new JukeboxAudioPlayer(pos, trackFile);
                    activeJukeboxes.put(pos, player);
                    player.play();
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(StopCustomDiscPayload.PACKET_ID, (payload, context) -> {
            context.client().execute(() -> {
                BlockPos pos = payload.pos();
                if (activeJukeboxes.containsKey(pos)) {
                    activeJukeboxes.get(pos).stop();
                    activeJukeboxes.remove(pos);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(GlobalPlayClientPayload.PACKET_ID, (payload, context) -> {
            context.client().execute(() -> {
                if (ModConfig.getInstance().muteGlobal) return;
                
                if (context.client().player != null && context.client().player.getUuid().equals(payload.sender())) {
                    return;
                }
                
                // Не останавливаем свою музыку на каждом периодическом пакете!
                // Только при первом получении нового трека от другого игрока.
                GlobalAudioTracker.getInstance().playGlobal(payload.sender(), payload.trackName(), payload.position(), payload.range(), payload.volume());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FileTransferStartPayload.PACKET_ID, (payload, context) -> {
            context.client().execute(() -> {
                FileReceiverManager.getInstance().startDownload(payload.fileName(), payload.fileSize(), payload.totalChunks(), payload.sender());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FileTransferChunkPayload.PACKET_ID, (payload, context) -> {
            context.client().execute(() -> {
                FileReceiverManager.getInstance().receiveChunk(payload.fileName(), payload.chunkIndex(), payload.data(), payload.sender());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FileTransferEndPayload.PACKET_ID, (payload, context) -> {
            context.client().execute(() -> {
                FileReceiverManager.getInstance().endDownload(payload.fileName(), payload.sender());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ShareRequestPayload.PACKET_ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().setScreen(new ShareRequestScreen(payload.fileName(), payload.sender(), payload.senderName()));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ShareResponsePayload.PACKET_ID, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.accepted()) {
                    // Start uploading
                    File fileToShare = null;
                    File musicFolder = new File(MinecraftClient.getInstance().runDirectory, "mp3player");
                    File f = new File(musicFolder, payload.fileName());
                    if (f.exists()) fileToShare = f;
                    else {
                        f = new File(new File(musicFolder, "converted"), payload.fileName());
                        if (f.exists()) fileToShare = f;
                    }
                    if (fileToShare != null) {
                        FileSenderManager.getInstance().startUpload(fileToShare, payload.responder());
                        if (context.client().player != null) {
                            context.client().player.sendMessage(net.minecraft.text.Text.literal("§aИгрок принял запрос. Начинаю отправку §e" + payload.fileName()), false);
                        }
                    }
                } else {
                    if (context.client().player != null) {
                        context.client().player.sendMessage(net.minecraft.text.Text.literal("§cИгрок отклонил запрос на песню §e" + payload.fileName()), false);
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(RequestTrackClientPayload.PACKET_ID, (payload, context) -> {
            context.client().execute(() -> {
                File trackFile = findWavFile(payload.trackName());
                if (trackFile == null) {
                    File mainFolder = new File(MinecraftClient.getInstance().runDirectory, "mp3player");
                    if (mainFolder.exists()) {
                        File[] files = mainFolder.listFiles();
                        if (files != null) {
                            for (File f : files) {
                                if (f.getName().toLowerCase().endsWith(".mp3")) {
                                    String n = f.getName();
                                    int d = n.lastIndexOf('.');
                                    if ((d > 0 ? n.substring(0, d) : n).equalsIgnoreCase(payload.trackName())) { 
                                        trackFile = f; 
                                        break; 
                                    }
                                }
                            }
                        }
                    }
                }
                if (trackFile != null) {
                    FileSenderManager.getInstance().startUpload(trackFile, payload.requester());
                }
            });
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            activeJukeboxes.values().removeIf(player -> {
                if (!player.isPlaying()) return true;
                player.updateVolume();
                return false;
            });
            GlobalAudioTracker.getInstance().updateAll();
            FileSenderManager.getInstance().tick();
            MusicPlayer.getInstance().applyVolume();
            
            // Периодическая рассылка состояния колонки (каждые 20 тиков = 1 секунду)
            tickCounter++;
            if (tickCounter >= 20) {
                tickCounter = 0;
                MusicPlayer.getInstance().broadcastState();
                // Убираем устаревших игроков (нет пакетов > 5 секунд)
                GlobalAudioTracker.getInstance().getActivePlayers().entrySet().removeIf(entry -> {
                    if (entry.getValue().isStale()) {
                        entry.getValue().stop();
                        return true;
                    }
                    return false;
                });
            }
        });
        
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            MusicPlayer.getInstance().stop();
            for (JukeboxAudioPlayer player : activeJukeboxes.values()) {
                player.stop();
            }
            activeJukeboxes.clear();
            GlobalAudioTracker.getInstance().stopAll();
        });


        ModSounds.register();
        KeyInputHandler.register();
    }
    
    
    public static void drawPlayerHologram(String trackName, long currentSec, long durationSec,
                                          net.minecraft.client.util.math.MatrixStack matrices, 
                                          net.minecraft.client.render.VertexConsumerProvider vertexConsumers,
                                          int light) {
        if (trackName == null || trackName.isEmpty()) return;
        
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        net.minecraft.client.font.TextRenderer textRenderer = client.textRenderer;
        
        String timeStr = String.format("%d:%02d / %d:%02d", 
                currentSec / 60, currentSec % 60,
                durationSec / 60, durationSec % 60);
                
        String displayText = "▶ " + trackName + " (" + timeStr + ")";
        float x = (float) (-textRenderer.getWidth(displayText) / 2);
        
        matrices.push();
        matrices.translate(0.0, 0.7, 0.0); // Немного поднимем над головой
        matrices.scale(-0.025f, -0.025f, 0.025f);
        
        org.joml.Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        
        // Background
        int bgColor = client.options.getTextBackgroundColor(0.25f);
        textRenderer.draw(
                displayText, x, 0, 0xFFCC00, false,
                positionMatrix, vertexConsumers,
                net.minecraft.client.font.TextRenderer.TextLayerType.SEE_THROUGH, bgColor,
                light
        );
        matrices.pop();
    }
}
