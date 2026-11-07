package com.mp3player.client.gui;

import com.mp3player.network.ShareRequestPayload;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.PlayerSkin;

public class ShareScreen
extends Screen {
    private final Screen parent;
    private final File fileToShare;
    private final List<PlayerInfo> players = new ArrayList<PlayerInfo>();
    private int scrollOffset = 0;

    public ShareScreen(Screen parent, File fileToShare) {
        super((Component)Component.literal((String)(DrawUtils.t("Share: ", "\u041f\u043e\u0434\u0435\u043b\u0438\u0442\u044c\u0441\u044f: ") + fileToShare.getName())));
        this.parent = parent;
        this.fileToShare = fileToShare;
    }

    @Override
    protected void init() {
        super.init();
        this.players.clear();
        if (this.minecraft != null && this.minecraft.getConnection() != null) {
            Collection entries = this.minecraft.getConnection().getOnlinePlayers();
            for (Object obj : entries) {
                PlayerInfo entry = (PlayerInfo) obj;
                if (this.minecraft.player == null || entry.getProfile().id().equals(this.minecraft.player.getUUID())) continue;
                this.players.add(entry);
            }
        }
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)DrawUtils.t("Back", "\u041d\u0430\u0437\u0430\u0434")), button -> {
            if (this.minecraft != null) this.minecraft.gui.setScreen(this.parent);
        }).bounds(this.width / 2 - 100, this.height - 35, 200, 20).build());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scrollOffset = Math.max(0, Math.min(Math.max(0, this.players.size() - 5), this.scrollOffset - (int)verticalAmount));
        return true;
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent click, boolean bl) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        int panelH = 220;
        int py = this.height / 2 - panelH / 2;
        int startY = py + 40;
        for (int i = 0; i < Math.min(5, this.players.size() - this.scrollOffset); ++i) {
            int idx = this.scrollOffset + i;
            int y = startY + i * 30;
            int x = this.width / 2 - 120;
            if (!(mouseX >= (double)x) || !(mouseX <= (double)(x + 240)) || !(mouseY >= (double)y) || !(mouseY <= (double)(y + 26))) continue;
            PlayerInfo player = this.players.get(idx);
            if (this.minecraft != null && this.minecraft.player != null) {
                ClientPlayNetworking.send((CustomPacketPayload)new ShareRequestPayload(this.fileToShare.getName(), this.minecraft.player.getUUID(), this.minecraft.player.getName().getString(), player.getProfile().id()));
                this.minecraft.player.sendSystemMessage((Component)Component.literal((String)(DrawUtils.t("Shared track with ", "\u0412\u044b \u043f\u043e\u0434\u0435\u043b\u0438\u043b\u0438\u0441\u044c \u0442\u0440\u0435\u043a\u043e\u043c \u0441 ") + player.getProfile().name())));
            }
            if (this.minecraft != null) this.minecraft.gui.setScreen(this.parent);
            return true;
        }
        return super.mouseClicked(click, bl);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x44000000);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int panelW = 280;
        int panelH = 220;
        int px = this.width / 2 - panelW / 2;
        int py = this.height / 2 - panelH / 2;
        context.fill(px, py, px + panelW, py + panelH, -300279270);
        context.fill(px, py, px + panelW, py + 2, -13312);
        String titleText = this.title.getString();
        if (titleText.length() > 38) {
            titleText = titleText.substring(0, 35) + "...";
        }
        context.centeredText(this.font, (Component)Component.literal((String)titleText), this.width / 2, py + 15, -13312);
        if (this.players.isEmpty()) {
            context.centeredText(this.font, (Component)Component.literal((String)DrawUtils.t("No other players found.", "\u041d\u0435\u0442 \u0434\u0440\u0443\u0433\u0438\u0445 \u0438\u0433\u0440\u043e\u043a\u043e\u0432 \u043d\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435.")).withStyle(ChatFormatting.RED), this.width / 2, py + 100, -1);
        } else {
            int startY = py + 40;
            for (int i = 0; i < Math.min(5, this.players.size() - this.scrollOffset); ++i) {
                int idx = this.scrollOffset + i;
                PlayerInfo player = this.players.get(idx);
                String name = player.getProfile().name();
                int y = startY + i * 30;
                int x = this.width / 2 - 120;
                boolean hover = mouseX >= x && mouseX <= x + 240 && mouseY >= y && mouseY <= y + 26;
                context.fill(x, y, x + 240, y + 26, hover ? -12961222 : -14013910);
                if (hover) {
                    context.fill(x, y + 24, x + 240, y + 26, -13312);
                }
                PlayerSkin skinTextures = player.getSkin();
                if (skinTextures != null && skinTextures.body() != null) {
                    context.blit(skinTextures.body().texturePath(), x + 5, y + 5, 16, 16, 0.125f, 0.25f, 0.125f, 0.25f);
                }
                DrawUtils.drawText(context, this.font, name, x + 30, y + 9, -1, false);
                DrawUtils.drawText(context, this.font, DrawUtils.t("Share track", "\u041f\u043e\u0434\u0435\u043b\u0438\u0442\u044c\u0441\u044f \u0442\u0440\u0435\u043a\u043e\u043c"), x + 155, y + 9, hover ? -13312 : -7829368, false);
            }
        }
        super.extractRenderState(context, mouseX, mouseY, delta);
    }
}
