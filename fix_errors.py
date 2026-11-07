import os

def replace_in_file(path, old, new):
    with open(path, 'r', encoding='utf-8') as f:
        c = f.read()
    c = c.replace(old, new)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(c)

base = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric-1.20.6\src\main\java\com\mp3player'

# 1. MP3PlayerClient.java
mp3_client = os.path.join(base, 'MP3PlayerClient.java')
replace_in_file(mp3_client, 'String fileName = buf.readString();', '')
replace_in_file(mp3_client, 'UUID sender = buf.readUuid();', '')
replace_in_file(mp3_client, 'String senderName = buf.readString();', '')
replace_in_file(mp3_client, 'UUID target = buf.readUuid();', '')
replace_in_file(mp3_client, 'String trackName = buf.readString();', '')
replace_in_file(mp3_client, 'UUID requester = buf.readUuid();', '')

# 2. MainScreen.java
main_screen = os.path.join(base, r'client\gui\MainScreen.java')
replace_in_file(main_screen, 'PacketByteBuf buf = PacketByteBufs.create();\n                buf.writeString(file.getName());\n                buf.writeVarLong(0);\n                buf.writeInt(volumeScale);\n                buf.writeFloat(1.0f);\n                ClientPlayNetworking.send(com.mp3player.network.NetworkPackets.GLOBAL_PLAY_C2S, buf);', 'ClientPlayNetworking.send(new com.mp3player.network.NetworkPackets.GlobalPlayC2SPayload(file.getName(), 0, volumeScale, 1.0f));')
replace_in_file(main_screen, 'public boolean mouseScrolled(double mouseX, double mouseY, double amount) {', 'public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double amount) {')

# 3. ShareScreen.java
share_screen = os.path.join(base, r'client\gui\ShareScreen.java')
replace_in_file(share_screen, 'public boolean mouseScrolled(double mouseX, double mouseY, double amount) {', 'public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double amount) {')

# 4. MusicDiscScreen.java
music_disc = os.path.join(base, r'client\gui\MusicDiscScreen.java')
has_custom_old = 'boolean hasCustom = stack.hasNbt() && stack.getNbt().contains("CustomMp3");'
has_custom_new = 'boolean hasCustom = stack.get(net.minecraft.component.DataComponentTypes.CUSTOM_DATA) != null && stack.get(net.minecraft.component.DataComponentTypes.CUSTOM_DATA).copyNbt().contains("CustomMp3");'
replace_in_file(music_disc, has_custom_old, has_custom_new)

clear_disc_old = 'PacketByteBuf buf = PacketByteBufs.create();\n                    buf.writeInt(finalSlot);\n                    ClientPlayNetworking.send(NetworkPackets.CLEAR_DISC, buf);'
clear_disc_new = 'ClientPlayNetworking.send(new NetworkPackets.ClearDiscPayload(finalSlot));'
replace_in_file(music_disc, clear_disc_old, clear_disc_new)

bind_disc_old = 'PacketByteBuf buf = PacketByteBufs.create();\n                            buf.writeString(name);\n                            buf.writeInt(finalSlot);\n                            ClientPlayNetworking.send(NetworkPackets.BIND_DISC, buf);'
bind_disc_new = 'ClientPlayNetworking.send(new NetworkPackets.BindDiscPayload(name, finalSlot));'
replace_in_file(music_disc, bind_disc_old, bind_disc_new)

replace_in_file(music_disc, 'public boolean mouseScrolled(double mouseX, double mouseY, double amount) {', 'public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double amount) {')

# 5. MusicPlayer.java
music_player = os.path.join(base, r'client\audio\MusicPlayer.java')
replace_in_file(music_player, 'PacketByteBuf buf = PacketByteBufs.create();\n            buf.writeString(trackName);\n            buf.writeVarLong((long) position);\n            buf.writeInt(range);\n            buf.writeFloat(volume);\n            ClientPlayNetworking.send(com.mp3player.network.NetworkPackets.GLOBAL_PLAY_C2S, buf);', 'ClientPlayNetworking.send(new com.mp3player.network.NetworkPackets.GlobalPlayC2SPayload(trackName, (long) position, range, volume));')

# 6. GlobalAudioTracker.java
global_audio = os.path.join(base, r'client\audio\GlobalAudioTracker.java')
replace_in_file(global_audio, 'PacketByteBuf buf = PacketByteBufs.create();\n                    buf.writeString(requestName);\n                    buf.writeUuid(owner);\n                    ClientPlayNetworking.send(com.mp3player.network.NetworkPackets.REQUEST_TRACK_C2S, buf);', 'ClientPlayNetworking.send(new com.mp3player.network.NetworkPackets.RequestTrackC2SPayload(requestName, owner));')
