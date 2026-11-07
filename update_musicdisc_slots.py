import re

file_path = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric\src\main\java\com\mp3player\client\gui\MusicDiscScreen.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

mouse_clicked_old = """    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int pw = 250;
        int ph = 200;
        int px = this.width / 2 - pw / 2;
        int py = this.height / 2 - ph / 2;
        
        int listY = py + 30;
        for (int i = scroll; i < Math.min(discs.size(), scroll + 8); i++) {
            int y = listY + (i - scroll) * 20;
            if (mouseX >= px + 10 && mouseX < px + pw - 10 && mouseY >= y && mouseY < y + 20) {
                ItemStack stack = discs.get(i);
                boolean hasCustom = stack.hasNbt() && stack.getNbt().contains("CustomMp3");
                
                if (hasCustom && mouseX >= px + pw - 70 && mouseX < px + pw - 10) {
                    ClientPlayNetworking.send(NetworkPackets.CLEAR_DISC, PacketByteBufs.create());
                    this.client.setScreen(parent);
                    return true;
                }
                
                this.client.setScreen(new ConfirmScreen(
                    confirmed -> {
                        if (confirmed) {
                            String name = trackFile != null ? trackFile.getName() : "Yandex Track";
                            String id = Registries.ITEM.getId(stack.getItem()).toString();
                            PacketByteBuf buf = PacketByteBufs.create();
                            buf.writeString(name);
                            buf.writeString(id);
                            ClientPlayNetworking.send(NetworkPackets.BIND_DISC, buf);
                            this.client.setScreen(parent);
                        } else {
                            this.client.setScreen(this);
                        }
                    },
                    Text.literal(DrawUtils.t("Record song to disc?", "Записать песню на пластинку?")),
                    Text.literal(trackFile != null ? trackFile.getName() : "Yandex Track")
                ));
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }"""

mouse_clicked_new = """    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int pw = 250;
        int ph = 200;
        int px = this.width / 2 - pw / 2;
        int py = this.height / 2 - ph / 2;
        
        int listY = py + 30;
        for (int i = scroll; i < Math.min(discs.size(), scroll + 8); i++) {
            int y = listY + (i - scroll) * 20;
            if (mouseX >= px + 10 && mouseX < px + pw - 10 && mouseY >= y && mouseY < y + 20) {
                ItemStack stack = discs.get(i);
                boolean hasCustom = stack.hasNbt() && stack.getNbt().contains("CustomMp3");
                
                int slot = -1;
                if (this.client != null && this.client.player != null) {
                    for (int invSlot = 0; invSlot < this.client.player.getInventory().size(); invSlot++) {
                        if (this.client.player.getInventory().getStack(invSlot) == stack) {
                            slot = invSlot;
                            break;
                        }
                    }
                }
                
                if (slot == -1) return true;
                final int finalSlot = slot;
                
                if (hasCustom && mouseX >= px + pw - 70 && mouseX < px + pw - 10) {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeInt(finalSlot);
                    ClientPlayNetworking.send(NetworkPackets.CLEAR_DISC, buf);
                    this.client.setScreen(parent);
                    return true;
                }
                
                this.client.setScreen(new ConfirmScreen(
                    confirmed -> {
                        if (confirmed) {
                            String name = trackFile != null ? trackFile.getName() : "Yandex Track";
                            PacketByteBuf buf = PacketByteBufs.create();
                            buf.writeString(name);
                            buf.writeInt(finalSlot);
                            ClientPlayNetworking.send(NetworkPackets.BIND_DISC, buf);
                            this.client.setScreen(parent);
                        } else {
                            this.client.setScreen(this);
                        }
                    },
                    Text.literal(DrawUtils.t("Record song to disc?", "Записать песню на пластинку?")),
                    Text.literal(trackFile != null ? trackFile.getName() : "Yandex Track")
                ));
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }"""

content = content.replace(mouse_clicked_old, mouse_clicked_new)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("MusicDiscScreen updated successfully")
