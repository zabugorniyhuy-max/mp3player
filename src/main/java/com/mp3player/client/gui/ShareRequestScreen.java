package com.mp3player.client.gui;

import com.mp3player.network.ShareResponsePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class ShareRequestScreen extends Screen {
    private final String fileName;
    private final UUID senderId;
    private final String senderName;

    public ShareRequestScreen(String fileName, UUID senderId, String senderName) {
        super(Text.literal("Запрос на получение песни"));
        this.fileName = fileName;
        this.senderId = senderId;
        this.senderName = senderName;
    }

    @Override
    protected void init() {
        super.init();

        int px = this.width / 2;
        int py = this.height / 2;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Принять").formatted(Formatting.GREEN), button -> {
            if (client != null && client.player != null) {
                ClientPlayNetworking.send(new ShareResponsePayload(fileName, senderId, client.player.getUuid(), true));
                client.player.sendMessage(Text.literal("§aВы приняли песню §e" + fileName), false);
            }
            this.close();
        }).dimensions(px - 105, py + 20, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Отклонить").formatted(Formatting.RED), button -> {
            if (client != null && client.player != null) {
                ClientPlayNetworking.send(new ShareResponsePayload(fileName, senderId, client.player.getUuid(), false));
                client.player.sendMessage(Text.literal("§cВы отклонили песню §e" + fileName), false);
            }
            this.close();
        }).dimensions(px + 5, py + 20, 100, 20).build());
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x88000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        
        int px = this.width / 2 - 140;
        int py = this.height / 2 - 50;
        context.fill(px, py, px + 280, py + 100, 0xEE1a1a1a);
        context.fill(px, py, px + 280, py + 2, 0xFFFFCC00);

        context.drawCenteredTextWithShadow(this.textRenderer, "Запрос на получение песни", this.width / 2, py + 10, 0xFFFFCC00);
        context.drawCenteredTextWithShadow(this.textRenderer, "Игрок §b" + senderName + "§f хочет отправить вам:", this.width / 2, py + 30, 0xFFFFFF);
        
        String shortName = fileName;
        if (shortName.length() > 30) {
            shortName = shortName.substring(0, 27) + "...";
        }
        context.drawCenteredTextWithShadow(this.textRenderer, "§e" + shortName, this.width / 2, py + 45, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }
}
