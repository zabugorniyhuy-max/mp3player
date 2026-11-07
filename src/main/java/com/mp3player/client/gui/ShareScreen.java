/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.Element
 *  net.minecraft.client.gui.PlayerSkinDrawer
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.widget.ButtonWidget
 *  net.minecraft.client.network.PlayerListEntry
 *  net.minecraft.client.util.SkinTextures
 *  net.minecraft.network.packet.CustomPayload
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 */
package com.mp3player.client.gui;

import com.mp3player.client.gui.DrawUtils;
import com.mp3player.network.ShareRequestPayload;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ShareScreen
extends Screen {
    private final Screen parent;
    private final File fileToShare;
    private final List<PlayerListEntry> players = new ArrayList<PlayerListEntry>();
    private int scrollOffset = 0;

    public ShareScreen(Screen parent, File fileToShare) {
        super((Text)Text.literal((String)(DrawUtils.t("Share: ", "\u041f\u043e\u0434\u0435\u043b\u0438\u0442\u044c\u0441\u044f: ") + fileToShare.getName())));
        this.parent = parent;
        this.fileToShare = fileToShare;
    }

    protected void init() {
        super.init();
        this.players.clear();
        if (this.client != null && this.client.getNetworkHandler() != null) {
            Collection entries = this.client.getNetworkHandler().getPlayerList();
            for (Object obj : entries) {
                PlayerListEntry entry = (PlayerListEntry) obj;
                if (this.client.player == null || entry.getProfile().id().equals(this.client.player.getUuid())) continue;
                this.players.add(entry);
            }
        }
        this.addDrawableChild(ButtonWidget.builder((Text)Text.literal((String)DrawUtils.t("Back", "\u041d\u0430\u0437\u0430\u0434")), button -> this.client.setScreen(this.parent)).dimensions(this.width / 2 - 100, this.height - 35, 200, 20).build());
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scrollOffset = Math.max(0, Math.min(Math.max(0, this.players.size() - 5), this.scrollOffset - (int)verticalAmount));
        return true;
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean bl) {
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
            PlayerListEntry player = this.players.get(idx);
            if (this.client != null && this.client.player != null) {
                ClientPlayNetworking.send((CustomPayload)new ShareRequestPayload(this.fileToShare.getName(), this.client.player.getUuid(), this.client.player.getName().getString(), player.getProfile().id()));
                this.client.player.sendMessage((Text)Text.literal((String)(DrawUtils.t("Shared track with ", "\u0412\u044b \u043f\u043e\u0434\u0435\u043b\u0438\u043b\u0438\u0441\u044c \u0442\u0440\u0435\u043a\u043e\u043c \u0441 ") + player.getProfile().name())), false);
            }
            this.client.setScreen(this.parent);
            return true;
        }
        return super.mouseClicked(click, bl);
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x44000000);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
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
        context.drawCenteredTextWithShadow(this.textRenderer, (Text)Text.literal((String)titleText), this.width / 2, py + 15, -13312);
        if (this.players.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, (Text)Text.literal((String)DrawUtils.t("No other players found.", "\u041d\u0435\u0442 \u0434\u0440\u0443\u0433\u0438\u0445 \u0438\u0433\u0440\u043e\u043a\u043e\u0432 \u043d\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435.")).formatted(Formatting.RED), this.width / 2, py + 100, -1);
        } else {
            int startY = py + 40;
            for (int i = 0; i < Math.min(5, this.players.size() - this.scrollOffset); ++i) {
                int idx = this.scrollOffset + i;
                PlayerListEntry player = this.players.get(idx);
                String name = player.getProfile().name();
                int y = startY + i * 30;
                int x = this.width / 2 - 120;
                boolean hover = mouseX >= x && mouseX <= x + 240 && mouseY >= y && mouseY <= y + 26;
                context.fill(x, y, x + 240, y + 26, hover ? -12961222 : -14013910);
                if (hover) {
                    context.fill(x, y + 24, x + 240, y + 26, -13312);
                }
                SkinTextures skinTextures = player.getSkinTextures();
                PlayerSkinDrawer.draw((DrawContext)context, (SkinTextures)skinTextures, (int)(x + 5), (int)(y + 5), (int)16);
                DrawUtils.drawText(context, this.textRenderer, name, x + 30, y + 9, -1, false);
                DrawUtils.drawText(context, this.textRenderer, DrawUtils.t("Share track", "\u041f\u043e\u0434\u0435\u043b\u0438\u0442\u044c\u0441\u044f \u0442\u0440\u0435\u043a\u043e\u043c"), x + 155, y + 9, hover ? -13312 : -7829368, false);
            }
        }
        super.render(context, mouseX, mouseY, delta);
    }
}

