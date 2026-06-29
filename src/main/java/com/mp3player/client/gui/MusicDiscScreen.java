package com.mp3player.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.component.DataComponentTypes;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.mp3player.network.BindDiscPayload;
import com.mp3player.network.ClearDiscPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class MusicDiscScreen extends Screen {
    private final Screen parent;
    private final File trackFile;
    private final List<ItemStack> discs = new ArrayList<>();
    private int scroll = 0;

    public MusicDiscScreen(Screen parent, File trackFile) {
        super(Text.literal("Выберите пластинку для записи"));
        this.parent = parent;
        this.trackFile = trackFile;
    }

    @Override
    protected void init() {
        discs.clear();
        if (this.client != null && this.client.player != null) {
            for (int i = 0; i < this.client.player.getInventory().size(); i++) {
                ItemStack stack = this.client.player.getInventory().getStack(i);
                if (!stack.isEmpty() && stack.getComponents().contains(DataComponentTypes.JUKEBOX_PLAYABLE)) {
                    discs.add(stack);
                }
            }
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        this.renderBackground(ctx, mouseX, mouseY, delta);
        
        int pw = 250;
        int ph = 200;
        int px = this.width / 2 - pw / 2;
        int py = this.height / 2 - ph / 2;
        
        ctx.fill(px, py, px + pw, py + ph, 0xFF1A1A1A);
        ctx.fill(px, py, px + pw, py + 1, 0xFFFF0055);
        
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, py + 10, 0xFFFFFF);
        
        int listY = py + 30;
        if (discs.isEmpty()) {
            ctx.drawCenteredTextWithShadow(this.textRenderer, "Нет пластинок в инвентаре", px + pw / 2, listY + 20, 0xAAAAAA);
        } else {
            for (int i = scroll; i < Math.min(discs.size(), scroll + 8); i++) {
                ItemStack stack = discs.get(i);
                int y = listY + (i - scroll) * 20;
                boolean hover = mouseX >= px + 10 && mouseX < px + pw - 10 && mouseY >= y && mouseY < y + 20;
                
                if (hover) {
                    ctx.fill(px + 10, y, px + pw - 10, y + 20, 0xFF333333);
                }
                
                ctx.drawItem(stack, px + 12, y + 2);
                
                boolean hasCustom = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, net.minecraft.component.type.NbtComponent.DEFAULT).contains("CustomMp3");
                
                String displayName = stack.getName().getString();
                if (hasCustom && displayName.length() > 35) {
                    displayName = displayName.substring(0, 33) + "..";
                }
                
                DrawUtils.drawText(ctx, this.textRenderer, displayName, px + 35, y + 6, hover ? 0xFFFF0055 : 0xAAAAAA, false);
                
                if (hasCustom) {
                    boolean btnHover = mouseX >= px + pw - 70 && mouseX < px + pw - 10 && mouseY >= y + 2 && mouseY < y + 18;
                    ctx.fill(px + pw - 70, y + 2, px + pw - 10, y + 18, btnHover ? 0xFFFF5555 : 0xFF882222);
                    ctx.drawCenteredTextWithShadow(this.textRenderer, "Отменить", px + pw - 40, y + 6, 0xFFFFFF);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int pw = 250;
        int ph = 200;
        int px = this.width / 2 - pw / 2;
        int py = this.height / 2 - ph / 2;
        
        int listY = py + 30;
        for (int i = scroll; i < Math.min(discs.size(), scroll + 8); i++) {
            int y = listY + (i - scroll) * 20;
            if (mouseX >= px + 10 && mouseX < px + pw - 10 && mouseY >= y && mouseY < y + 20) {
                ItemStack stack = discs.get(i);
                boolean hasCustom = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, net.minecraft.component.type.NbtComponent.DEFAULT).contains("CustomMp3");
                
                if (hasCustom && mouseX >= px + pw - 70 && mouseX < px + pw - 10) {
                    ClientPlayNetworking.send(new ClearDiscPayload());
                    this.client.setScreen(parent);
                    return true;
                }
                
                this.client.setScreen(new ConfirmScreen(
                    confirmed -> {
                        if (confirmed) {
                            String name = trackFile != null ? trackFile.getName() : "Yandex Track";
                            String id = Registries.ITEM.getId(stack.getItem()).toString();
                            ClientPlayNetworking.send(new BindDiscPayload(name, id));
                            this.client.setScreen(parent);
                        } else {
                            this.client.setScreen(this);
                        }
                    },
                    Text.literal("Записать песню на пластинку?"),
                    Text.literal(trackFile != null ? trackFile.getName() : "Yandex Track")
                ));
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll = Math.max(0, Math.min(discs.size() - 8, scroll - (int) verticalAmount));
        return true;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            this.client.setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
