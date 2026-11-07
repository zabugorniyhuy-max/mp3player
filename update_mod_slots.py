import re

file_path = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric\src\main\java\com\mp3player\MP3PlayerMod.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

bind_disc_old = """        ServerPlayNetworking.registerGlobalReceiver(NetworkPackets.BIND_DISC, (server, player, handler, buf, responseSender) -> {
            String trackName = buf.readString();
            String discId = buf.readString();
            server.execute(() -> {
                net.minecraft.util.Identifier id = net.minecraft.util.Identifier.tryParse(discId);
                if (id != null) {
                    net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM.get(id);
                    if (item != null) {
                        int slot = -1;
                        for (int i = 0; i < player.getInventory().size(); i++) {
                            ItemStack invStack = player.getInventory().getStack(i);
                            if (invStack.isOf(item)) {
                                slot = i;
                                break;
                            }
                        }

                        if (slot != -1) {
                            player.getInventory().removeStack(slot, 1);
                            
                            ItemStack stack = new ItemStack(item);
                            stack.getOrCreateNbt().putString("CustomMp3", trackName);
                            stack.setCustomName(net.minecraft.text.Text.literal("§eПластинка: §f" + trackName));
                            
                            player.getInventory().offerOrDrop(stack);
                            player.sendMessage(net.minecraft.text.Text.literal("§aПесня успешно записана на пластинку!"), false);
                        } else {
                            player.sendMessage(net.minecraft.text.Text.literal("§cУ вас нет нужной пластинки в инвентаре!"), false);
                        }
                    }
                }
            });
        });"""

bind_disc_new = """        ServerPlayNetworking.registerGlobalReceiver(NetworkPackets.BIND_DISC, (server, player, handler, buf, responseSender) -> {
            String trackName = buf.readString();
            int slot = buf.readInt();
            server.execute(() -> {
                if (slot >= 0 && slot < player.getInventory().size()) {
                    ItemStack stack = player.getInventory().getStack(slot);
                    if (!stack.isEmpty() && net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).getPath().contains("music_disc")) {
                        net.minecraft.nbt.NbtCompound nbt = stack.getOrCreateNbt();
                        nbt.putString("CustomMp3", trackName);
                        stack.setNbt(nbt);
                        stack.setCustomName(net.minecraft.text.Text.literal("§eПластинка: §f" + trackName));
                        player.sendMessage(net.minecraft.text.Text.literal("§aПесня успешно записана на пластинку!"), false);
                    }
                }
            });
        });"""

clear_disc_old = """        ServerPlayNetworking.registerGlobalReceiver(NetworkPackets.CLEAR_DISC, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                int slot = -1;
                for (int i = 0; i < player.getInventory().size(); i++) {
                    ItemStack invStack = player.getInventory().getStack(i);
                    if (invStack != null && invStack.hasNbt() && invStack.getNbt().contains("CustomMp3")) {
                        slot = i;
                        break;
                    }
                }

                if (slot != -1) {
                    ItemStack invStack = player.getInventory().getStack(slot);
                    ItemStack singleDisc = invStack.split(1);
                    
                    singleDisc.getNbt().remove("CustomMp3");
                    singleDisc.removeCustomName();
                    
                    player.getInventory().offerOrDrop(singleDisc);
                    player.sendMessage(net.minecraft.text.Text.literal("§aПесня удалена с пластинки!"), false);
                }
            });
        });"""

clear_disc_new = """        ServerPlayNetworking.registerGlobalReceiver(NetworkPackets.CLEAR_DISC, (server, player, handler, buf, responseSender) -> {
            int slot = buf.readInt();
            server.execute(() -> {
                if (slot >= 0 && slot < player.getInventory().size()) {
                    ItemStack stack = player.getInventory().getStack(slot);
                    if (!stack.isEmpty() && stack.hasNbt() && stack.getNbt().contains("CustomMp3")) {
                        stack.getNbt().remove("CustomMp3");
                        stack.removeCustomName();
                        player.sendMessage(net.minecraft.text.Text.literal("§aПесня удалена с пластинки!"), false);
                    }
                }
            });
        });"""

content = content.replace(bind_disc_old, bind_disc_new)
content = content.replace(clear_disc_old, clear_disc_new)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("MP3PlayerMod updated successfully")
