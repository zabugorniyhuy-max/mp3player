/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.ConfirmScreen
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.NbtComponent
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.packet.CustomPayload
 *  net.minecraft.registry.Registries
 *  net.minecraft.text.Text
 */
package com.mp3player.client.gui;

import com.mp3player.client.gui.DrawUtils;
import com.mp3player.network.BindDiscPayload;
import com.mp3player.network.ClearDiscPayload;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class MusicDiscScreen
extends Screen {
    private final Screen parent;
    private final File trackFile;
    private final List<ItemStack> discs = new ArrayList<ItemStack>();
    private int scroll = 0;

    public MusicDiscScreen(Screen parent, File trackFile) {
        super((Component)Component.literal((String)DrawUtils.t("Select a disc", "\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u043f\u043b\u0430\u0441\u0442\u0438\u043d\u043a\u0443")));
        this.parent = parent;
        this.trackFile = trackFile;
    }

    protected void init() {
        this.discs.clear();
        if (this.minecraft != null && this.minecraft.player != null) {
            for (int i = 0; i < this.minecraft.player.getInventory().getContainerSize(); ++i) {
                ItemStack stack = this.minecraft.player.getInventory().getItem(i);
                if (stack.isEmpty() || !stack.getComponents().has(DataComponents.JUKEBOX_PLAYABLE)) continue;
                this.discs.add(stack);
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, -2013265920);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        int pw = 250;
        int ph = 200;
        int px = this.width / 2 - pw / 2;
        int py = this.height / 2 - ph / 2;
        context.fill(px, py, px + pw, py + ph, -15066598);
        context.fill(px, py, px + pw, py + 1, -65451);
        context.centeredText(this.font, this.title, this.width / 2, py + 10, -1);
        int cbW = 80;
        int cbX = px + pw / 2 - cbW / 2;
        int cbY = py + ph - 22;
        boolean cbHover = mouseX >= cbX && mouseX < cbX + cbW && mouseY >= cbY && mouseY < cbY + 14;
        context.fill(cbX, cbY, cbX + cbW, cbY + 14, cbHover ? -11184811 : -13421773);
        context.centeredText(this.font, (Component)Component.literal((String)DrawUtils.t("Cancel", "\u041e\u0442\u043c\u0435\u043d\u0430")), cbX + cbW / 2, cbY + 3, -1);
        int listY = py + 30;
        if (this.discs.isEmpty()) {
            context.centeredText(this.font, (Component)Component.literal((String)DrawUtils.t("No discs in inventory", "\u041d\u0435\u0442 \u043f\u043b\u0430\u0441\u0442\u0438\u043d\u043e\u043a \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435")), px + pw / 2, listY + 40, -5592406);
        } else {
            for (int i = this.scroll; i < Math.min(this.discs.size(), this.scroll + 7); ++i) {
                boolean hover;
                ItemStack stack = this.discs.get(i);
                int y = listY + (i - this.scroll) * 20;
                boolean bl = hover = mouseX >= px + 10 && mouseX < px + pw - 10 && mouseY >= y && mouseY < y + 20;
                if (hover) {
                    context.fill(px + 10, y, px + pw - 10, y + 20, -13421773);
                }
                context.item(stack, px + 12, y + 2);
                boolean hasCustom = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, (Object)CustomData.EMPTY)).copyTag().contains("CustomMp3");
                String displayName = stack.getHoverName().getString();
                if (hasCustom && displayName.length() > 35) {
                    displayName = displayName.substring(0, 33) + "..";
                }
                DrawUtils.drawText(context, this.font, displayName, px + 35, y + 6, hover ? -65451 : -5592406, false);
                if (!hasCustom) continue;
                boolean btnHover = mouseX >= px + pw - 70 && mouseX < px + pw - 10 && mouseY >= y + 2 && mouseY < y + 18;
                context.fill(px + pw - 70, y + 2, px + pw - 10, y + 18, btnHover ? -43691 : -7855582);
                context.centeredText(this.font, (Component)Component.literal((String)DrawUtils.t("Clear", "\u041e\u0447\u0438\u0441\u0442")), px + pw - 40, y + 6, -1);
            }
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent click, boolean bl) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        int pw = 250;
        int ph = 200;
        int px = this.width / 2 - pw / 2;
        int py = this.height / 2 - ph / 2;
        int cbW = 80;
        int cbX = px + pw / 2 - cbW / 2;
        int cbY = py + ph - 22;
        if (mouseX >= (double)cbX && mouseX < (double)(cbX + cbW) && mouseY >= (double)cbY && mouseY < (double)(cbY + 14)) {
            this.minecraft.gui.setScreen(this.parent);
            return true;
        }
        int listY = py + 30;
        for (int i = this.scroll; i < Math.min(this.discs.size(), this.scroll + 7); ++i) {
            int y = listY + (i - this.scroll) * 20;
            if (!(mouseX >= (double)(px + 10)) || !(mouseX < (double)(px + pw - 10)) || !(mouseY >= (double)y) || !(mouseY < (double)(y + 20))) continue;
            ItemStack stack = this.discs.get(i);
            boolean hasCustom = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, (Object)CustomData.EMPTY)).copyTag().contains("CustomMp3");
            if (hasCustom && mouseX >= (double)(px + pw - 70) && mouseX < (double)(px + pw - 10)) {
                ClientPlayNetworking.send((CustomPacketPayload)new ClearDiscPayload());
                this.minecraft.gui.setScreen(this.parent);
                return true;
            }
            this.minecraft.gui.setScreen((Screen)new ConfirmScreen(confirmed -> {
                if (confirmed) {
                    String name = this.trackFile != null ? this.trackFile.getName() : "Yandex Track";
                    String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                    ClientPlayNetworking.send((CustomPacketPayload)new BindDiscPayload(name, id));
                    this.minecraft.gui.setScreen(this.parent);
                } else {
                    this.minecraft.gui.setScreen((Screen)this);
                }
            }, (Component)Component.literal((String)DrawUtils.t("Burn song to disc?", "\u0417\u0430\u043f\u0438\u0441\u0430\u0442\u044c \u043f\u0435\u0441\u043d\u044e \u043d\u0430 \u043f\u043b\u0430\u0441\u0442\u0438\u043d\u043a\u0443?")), (Component)Component.literal((String)(this.trackFile != null ? this.trackFile.getName() : "Yandex Track"))));
            return true;
        }
        return super.mouseClicked(click, bl);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scroll = Math.max(0, Math.min(this.discs.size() - 7, this.scroll - (int)verticalAmount));
        return true;
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent input) {
        if (input.key() == 256) {
            this.minecraft.gui.setScreen(this.parent);
            return true;
        }
        return super.keyPressed(input);
    }
}

