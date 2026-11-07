import os

file_sender = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric-1.20.6\src\main\java\com\mp3player\client\network\FileSenderManager.java'
with open(file_sender, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace(
    'PacketByteBuf buf = PacketByteBufs.create();\n            buf.writeString(fileName);\n            buf.writeLong(fileSize);\n            buf.writeInt(totalChunks);\n            buf.writeUuid(MinecraftClient.getInstance().player.getUuid());\n            buf.writeUuid(targetUuid);\n            ClientPlayNetworking.send(NetworkPackets.FILE_TRANSFER_START, buf);',
    'ClientPlayNetworking.send(new NetworkPackets.FileTransferStartPayload(fileName, fileSize, totalChunks, MinecraftClient.getInstance().player.getUuid(), targetUuid));'
)
c = c.replace(
    'PacketByteBuf buf = PacketByteBufs.create();\n                    buf.writeString(currentFileName);\n                    buf.writeInt(i);\n                    buf.writeByteArray(chunkData);\n                    buf.writeUuid(MinecraftClient.getInstance().player.getUuid());\n                    buf.writeUuid(currentTargetUuid);\n                    ClientPlayNetworking.send(NetworkPackets.FILE_TRANSFER_CHUNK, buf);',
    'ClientPlayNetworking.send(new NetworkPackets.FileTransferChunkPayload(currentFileName, i, chunkData, MinecraftClient.getInstance().player.getUuid(), currentTargetUuid));'
)
c = c.replace(
    'PacketByteBuf buf = PacketByteBufs.create();\n            buf.writeString(fileName);\n            buf.writeUuid(MinecraftClient.getInstance().player.getUuid());\n            buf.writeUuid(targetUuid);\n            ClientPlayNetworking.send(NetworkPackets.FILE_TRANSFER_END, buf);',
    'ClientPlayNetworking.send(new NetworkPackets.FileTransferEndPayload(fileName, MinecraftClient.getInstance().player.getUuid(), targetUuid));'
)

with open(file_sender, 'w', encoding='utf-8') as f: f.write(c)


share_screen = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric-1.20.6\src\main\java\com\mp3player\client\gui\ShareScreen.java'
with open(share_screen, 'r', encoding='utf-8') as f: c = f.read()
c = c.replace(
    'PacketByteBuf buf = PacketByteBufs.create();\n                    buf.writeString(fileName);\n                    buf.writeUuid(MinecraftClient.getInstance().player.getUuid());\n                    buf.writeString(MinecraftClient.getInstance().player.getName().getString());\n                    buf.writeUuid(p.id());\n                    ClientPlayNetworking.send(NetworkPackets.SHARE_REQUEST, buf);',
    'ClientPlayNetworking.send(new NetworkPackets.ShareRequestPayload(fileName, MinecraftClient.getInstance().player.getUuid(), MinecraftClient.getInstance().player.getName().getString(), p.id()));'
)
with open(share_screen, 'w', encoding='utf-8') as f: f.write(c)


share_req_screen = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric-1.20.6\src\main\java\com\mp3player\client\gui\ShareRequestScreen.java'
with open(share_req_screen, 'r', encoding='utf-8') as f: c = f.read()
c = c.replace(
    'PacketByteBuf buf = PacketByteBufs.create();\n                buf.writeString(fileName);\n                buf.writeUuid(senderId);\n                buf.writeUuid(MinecraftClient.getInstance().player.getUuid());\n                buf.writeBoolean(true);\n                ClientPlayNetworking.send(NetworkPackets.SHARE_RESPONSE, buf);',
    'ClientPlayNetworking.send(new NetworkPackets.ShareResponsePayload(fileName, senderId, MinecraftClient.getInstance().player.getUuid(), true));'
)
c = c.replace(
    'PacketByteBuf buf = PacketByteBufs.create();\n                buf.writeString(fileName);\n                buf.writeUuid(senderId);\n                buf.writeUuid(MinecraftClient.getInstance().player.getUuid());\n                buf.writeBoolean(false);\n                ClientPlayNetworking.send(NetworkPackets.SHARE_RESPONSE, buf);',
    'ClientPlayNetworking.send(new NetworkPackets.ShareResponsePayload(fileName, senderId, MinecraftClient.getInstance().player.getUuid(), false));'
)
with open(share_req_screen, 'w', encoding='utf-8') as f: f.write(c)


music_disc = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric-1.20.6\src\main\java\com\mp3player\client\gui\MusicDiscScreen.java'
with open(music_disc, 'r', encoding='utf-8') as f: c = f.read()
c = c.replace(
    'PacketByteBuf buf = PacketByteBufs.create();\n                    buf.writeInt(0);\n                    ClientPlayNetworking.send(NetworkPackets.CLEAR_DISC, buf);',
    'ClientPlayNetworking.send(new NetworkPackets.ClearDiscPayload(0));'
)
c = c.replace(
    'PacketByteBuf buf = PacketByteBufs.create();\n                            buf.writeString(currentFile.getName());\n                            buf.writeInt(selectedSlot);\n                            ClientPlayNetworking.send(NetworkPackets.BIND_DISC, buf);',
    'ClientPlayNetworking.send(new NetworkPackets.BindDiscPayload(currentFile.getName(), selectedSlot));'
)
with open(music_disc, 'w', encoding='utf-8') as f: f.write(c)


main_screen = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric-1.20.6\src\main\java\com\mp3player\client\gui\MainScreen.java'
with open(main_screen, 'r', encoding='utf-8') as f: c = f.read()
c = c.replace(
    'PacketByteBuf buf = PacketByteBufs.create();\n                buf.writeString(file.getName());\n                buf.writeVarLong(0);\n                buf.writeInt(volumeScale);\n                buf.writeFloat(1.0f);\n                ClientPlayNetworking.send(com.mp3player.network.NetworkPackets.GLOBAL_PLAY_C2S, buf);',
    'ClientPlayNetworking.send(new com.mp3player.network.NetworkPackets.GlobalPlayC2SPayload(file.getName(), 0, volumeScale, 1.0f));'
)
with open(main_screen, 'w', encoding='utf-8') as f: f.write(c)


music_player = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric-1.20.6\src\main\java\com\mp3player\client\audio\MusicPlayer.java'
with open(music_player, 'r', encoding='utf-8') as f: c = f.read()
c = c.replace(
    'PacketByteBuf buf = PacketByteBufs.create();\n            buf.writeString(trackName);\n            buf.writeVarLong((long) position);\n            buf.writeInt(range);\n            buf.writeFloat(volume);\n            ClientPlayNetworking.send(com.mp3player.network.NetworkPackets.GLOBAL_PLAY_C2S, buf);',
    'ClientPlayNetworking.send(new com.mp3player.network.NetworkPackets.GlobalPlayC2SPayload(trackName, (long) position, range, volume));'
)
with open(music_player, 'w', encoding='utf-8') as f: f.write(c)


global_audio = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric-1.20.6\src\main\java\com\mp3player\client\audio\GlobalAudioTracker.java'
with open(global_audio, 'r', encoding='utf-8') as f: c = f.read()
c = c.replace(
    'PacketByteBuf buf = PacketByteBufs.create();\n                    buf.writeString(requestName);\n                    buf.writeUuid(owner);\n                    ClientPlayNetworking.send(com.mp3player.network.NetworkPackets.REQUEST_TRACK_C2S, buf);',
    'ClientPlayNetworking.send(new com.mp3player.network.NetworkPackets.RequestTrackC2SPayload(requestName, owner));'
)
with open(global_audio, 'w', encoding='utf-8') as f: f.write(c)
