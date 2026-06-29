package com.mp3player.client.gui;

import com.mp3player.network.ShareRequestPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.client.util.SkinTextures;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ShareScreen extends Screen {
    private final Screen parent;
    private final File fileToShare;
    private final List<PlayerListEntry> players = new ArrayList<>();
    
    private int scrollOffset = 0;

    public ShareScreen(Screen parent, File fileToShare) {
        super(Text.literal("Поделиться: " + fileToShare.getName()));
        this.parent = parent;
        this.fileToShare = fileToShare;
    }

    @Override
    protected void init() {
        super.init();
        this.players.clear();
        
        if (client != null && client.getNetworkHandler() != null) {
            Collection<PlayerListEntry> entries = client.getNetworkHandler().getPlayerList();
            for (PlayerListEntry entry : entries) {
                if (client.player != null && !entry.getProfile().getId().equals(client.player.getUuid())) {
                    players.add(entry);
                }
            }
        }
        
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), button -> {
            client.setScreen(parent);
        }).dimensions(this.width / 2 - 100, this.height - 35, 200, 20).build());
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset = Math.max(0, Math.min(Math.max(0, players.size() - 5), scrollOffset - (int) verticalAmount));
        return true;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int panelH = 220;
        int py = this.height / 2 - panelH / 2;
        int startY = py + 40;
        
        for (int i = 0; i < Math.min(5, players.size() - scrollOffset); i++) {
            int idx = scrollOffset + i;
            int y = startY + (i * 30);
            int x = this.width / 2 - 120;
            
            if (mouseX >= x && mouseX <= x + 240 && mouseY >= y && mouseY <= y + 26) {
                PlayerListEntry player = players.get(idx);
                if (client != null && client.player != null) {
                    ClientPlayNetworking.send(new ShareRequestPayload(fileToShare.getName(), client.player.getUuid(), client.player.getName().getString(), player.getProfile().getId()));
                    client.player.sendMessage(Text.literal("§eОтправлен запрос игроку §b" + player.getProfile().getName()), false);
                }
                client.setScreen(parent);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Закрашиваем темным полупрозрачным фоном без блюра
        ctx.fill(0, 0, this.width, this.height, 0x44000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Panel background
        int panelW = 280;
        int panelH = 220;
        int px = this.width / 2 - panelW / 2;
        int py = this.height / 2 - panelH / 2;
        context.fill(px, py, px + panelW, py + panelH, 0xEE1a1a1a);
        context.fill(px, py, px + panelW, py + 2, 0xFFFFCC00);

        String titleText = this.title.getString();
        if (titleText.length() > 38) {
            titleText = titleText.substring(0, 35) + "...";
        }
        context.drawCenteredTextWithShadow(this.textRenderer, titleText, this.width / 2, py + 15, 0xFFFFCC00);

        if (players.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("На сервере нет других игроков.").formatted(Formatting.RED), this.width / 2, py + 100, 0xFFFFFF);
        } else {
            int startY = py + 40;
            for (int i = 0; i < Math.min(5, players.size() - scrollOffset); i++) {
                int idx = scrollOffset + i;
                PlayerListEntry player = players.get(idx);
                String name = player.getProfile().getName();
                
                int y = startY + (i * 30);
                int x = this.width / 2 - 120;
                
                boolean hover = mouseX >= x && mouseX <= x + 240 && mouseY >= y && mouseY <= y + 26;
                context.fill(x, y, x + 240, y + 26, hover ? 0xFF3a3a3a : 0xFF2a2a2a);
                if (hover) {
                    context.fill(x, y + 24, x + 240, y + 26, 0xFFFFCC00);
                }
                
                // Draw Player Head properly using PlayerSkinDrawer (No blur, includes hat)
                SkinTextures skinTextures = client.getSkinProvider().getSkinTextures(player.getProfile());
                PlayerSkinDrawer.draw(context, skinTextures, x + 5, y + 5, 16);
                
                DrawUtils.drawText(context, this.textRenderer, name, x + 30, y + 9, 0xFFFFFF, false);
                DrawUtils.drawText(context, this.textRenderer, "Поделиться 📤", x + 155, y + 9, hover ? 0xFFFFCC00 : 0xFF888888, false);
            }
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
}
