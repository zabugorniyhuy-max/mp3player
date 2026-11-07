package com.mp3player.client.gui;

import com.mp3player.client.gui.DrawUtils;
import com.mp3player.network.ShareResponsePayload;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ShareRequestScreen
extends Screen {
    private final String fileName;
    private final UUID senderId;
    private final String senderName;

    public ShareRequestScreen(String fileName, UUID senderId, String senderName) {
        super((Component)Component.literal((String)DrawUtils.t("Track request", "\u0417\u0430\u043f\u0440\u043e\u0441 \u043d\u0430 \u043f\u043e\u043b\u0443\u0447\u0435\u043d\u0438\u0435 \u0442\u0440\u0435\u043a\u0430")));
        this.fileName = fileName;
        this.senderId = senderId;
        this.senderName = senderName;
    }

    @Override
    protected void init() {
        super.init();
        int px = this.width / 2;
        int py = this.height / 2;
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)DrawUtils.t("Accept", "\u041f\u0440\u0438\u043d\u044f\u0442\u044c")).withStyle(ChatFormatting.GREEN), button -> {
            if (this.minecraft != null && this.minecraft.player != null) {
                ClientPlayNetworking.send((CustomPacketPayload)new ShareResponsePayload(this.fileName, this.senderId, this.minecraft.player.getUUID(), true));
                this.minecraft.player.sendSystemMessage((Component)Component.literal((String)(DrawUtils.t("\u00a7aYou accepted track \u00a7e", "\u00a7a\u0412\u044b \u043f\u0440\u0438\u043d\u044f\u043b\u0438 \u0442\u0440\u0435\u043a \u00a7e") + this.fileName)));
            }
            this.onClose();
        }).bounds(px - 105, py + 20, 100, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)DrawUtils.t("Decline", "\u041e\u0442\u043a\u043b\u043e\u043d\u0438\u0442\u044c")).withStyle(ChatFormatting.RED), button -> {
            if (this.minecraft != null && this.minecraft.player != null) {
                ClientPlayNetworking.send((CustomPacketPayload)new ShareResponsePayload(this.fileName, this.senderId, this.minecraft.player.getUUID(), false));
                this.minecraft.player.sendSystemMessage((Component)Component.literal((String)(DrawUtils.t("\u00a7cYou declined track \u00a7e", "\u00a7c\u0412\u044b \u043e\u0442\u043a\u043b\u043e\u043d\u0438\u043b\u0438 \u0442\u0440\u0435\u043a \u00a7e") + this.fileName)));
            }
            this.onClose();
        }).bounds(px + 5, py + 20, 100, 20).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, -2013265920);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int px = this.width / 2 - 140;
        int py = this.height / 2 - 50;
        context.fill(px, py, px + 280, py + 100, -300279270);
        context.fill(px, py, px + 280, py + 2, -13312);
        context.centeredText(this.font, (Component)Component.literal((String)DrawUtils.t("Track request", "\u0417\u0430\u043f\u0440\u043e\u0441 \u043d\u0430 \u043f\u043e\u043b\u0443\u0447\u0435\u043d\u0438\u0435 \u0442\u0440\u0435\u043a\u0430")), this.width / 2, py + 10, -13312);
        context.centeredText(this.font, (Component)Component.literal((String)(DrawUtils.t("Player \u00a7b", "\u0418\u0433\u0440\u043e\u043a \u00a7b") + this.senderName + DrawUtils.t("\u00a7f wants to share:", "\u00a7f \u0445\u043e\u0447\u0435\u0442 \u043f\u0435\u0440\u0435\u0434\u0430\u0442\u044c \u0432\u0430\u043c:"))), this.width / 2, py + 30, -1);
        Object shortName = this.fileName;
        if (((String)shortName).length() > 30) {
            shortName = ((String)shortName).substring(0, 27) + "...";
        }
        context.centeredText(this.font, (Component)Component.literal((String)("\u00a7e" + (String)shortName)), this.width / 2, py + 45, -1);
        super.extractRenderState(context, mouseX, mouseY, delta);
    }
}
