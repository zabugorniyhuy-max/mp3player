import os
import re

file_path = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric-1.20.6\src\main\java\com\mp3player\MP3PlayerClient.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace PLAY_CUSTOM_DISC
content = re.sub(
    r'ClientPlayNetworking\.registerGlobalReceiver\(NetworkPackets\.PLAY_CUSTOM_DISC, \(client, handler, buf, responseSender\) -> \{\s*BlockPos pos = buf\.readBlockPos\(\);\s*String trackName = buf\.readString\(\);\s*client\.execute',
    r'ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.PlayCustomDiscPayload.ID, (payload, context) -> {\n            BlockPos pos = payload.pos();\n            String trackName = payload.trackName();\n            context.client().execute',
    content
)

# Replace STOP_CUSTOM_DISC
content = re.sub(
    r'ClientPlayNetworking\.registerGlobalReceiver\(NetworkPackets\.STOP_CUSTOM_DISC, \(client, handler, buf, responseSender\) -> \{\s*BlockPos pos = buf\.readBlockPos\(\);\s*client\.execute',
    r'ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.StopCustomDiscPayload.ID, (payload, context) -> {\n            BlockPos pos = payload.pos();\n            context.client().execute',
    content
)

# Replace GLOBAL_PLAY_S2C
content = re.sub(
    r'ClientPlayNetworking\.registerGlobalReceiver\(NetworkPackets\.GLOBAL_PLAY_S2C, \(client, handler, buf, responseSender\) -> \{\s*UUID sender = buf\.readUuid\(\);\s*String trackName = buf\.readString\(\);\s*long position = buf\.readVarLong\(\);\s*int range = buf\.readInt\(\);\s*float volume = buf\.readFloat\(\);\s*client\.execute\(\(\) -> \{\s*if \(ModConfig\.getInstance\(\)\.muteGlobal\) return;\s*if \(client\.player',
    r'ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.GlobalPlayS2CPayload.ID, (payload, context) -> {\n            UUID sender = payload.player();\n            String trackName = payload.trackName();\n            long position = payload.position();\n            int range = payload.range();\n            float volume = payload.volume();\n            context.client().execute(() -> {\n                if (ModConfig.getInstance().muteGlobal) return;\n                \n                if (context.client().player',
    content
)

# Replace FILE_TRANSFER_START
content = re.sub(
    r'ClientPlayNetworking\.registerGlobalReceiver\(NetworkPackets\.FILE_TRANSFER_START, \(client, handler, buf, responseSender\) -> \{\s*String fileName = buf\.readString\(\);\s*long fileSize = buf\.readLong\(\);\s*int totalChunks = buf\.readInt\(\);\s*UUID sender = buf\.readUuid\(\);\s*UUID target = buf\.readUuid\(\);\s*client\.execute',
    r'ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.FileTransferStartPayload.ID, (payload, context) -> {\n            String fileName = payload.fileName();\n            long fileSize = payload.totalSize();\n            int totalChunks = payload.totalChunks();\n            UUID sender = payload.sender();\n            UUID target = payload.targetUuid();\n            context.client().execute',
    content
)

# Replace FILE_TRANSFER_CHUNK
content = re.sub(
    r'ClientPlayNetworking\.registerGlobalReceiver\(NetworkPackets\.FILE_TRANSFER_CHUNK, \(client, handler, buf, responseSender\) -> \{\s*String fileName = buf\.readString\(\);\s*int chunkIndex = buf\.readInt\(\);\s*byte\[\] data = buf\.readByteArray\(\);\s*UUID sender = buf\.readUuid\(\);\s*UUID target = buf\.readUuid\(\);\s*client\.execute',
    r'ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.FileTransferChunkPayload.ID, (payload, context) -> {\n            String fileName = payload.fileName();\n            int chunkIndex = payload.chunkIndex();\n            byte[] data = payload.data();\n            UUID sender = payload.sender();\n            UUID target = payload.targetUuid();\n            context.client().execute',
    content
)

# Replace FILE_TRANSFER_END
content = re.sub(
    r'ClientPlayNetworking\.registerGlobalReceiver\(NetworkPackets\.FILE_TRANSFER_END, \(client, handler, buf, responseSender\) -> \{\s*String fileName = buf\.readString\(\);\s*UUID sender = buf\.readUuid\(\);\s*UUID target = buf\.readUuid\(\);\s*client\.execute',
    r'ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.FileTransferEndPayload.ID, (payload, context) -> {\n            String fileName = payload.fileName();\n            UUID sender = payload.sender();\n            UUID target = payload.targetUuid();\n            context.client().execute',
    content
)

# Replace SHARE_REQUEST
content = re.sub(
    r'ClientPlayNetworking\.registerGlobalReceiver\(NetworkPackets\.SHARE_REQUEST, \(client, handler, buf, responseSender\) -> \{\s*String fileName = buf\.readString\(\);\s*UUID sender = buf\.readUuid\(\);\s*String senderName = buf\.readString\(\);\s*UUID targetUuid = buf\.readUuid\(\);\s*client\.execute\(',
    r'ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.ShareRequestPayload.ID, (payload, context) -> {\n            String fileName = payload.fileName();\n            UUID sender = payload.sender();\n            String senderName = payload.senderName();\n            UUID targetUuid = payload.targetUuid();\n            context.client().execute(',
    content
)

# Replace SHARE_RESPONSE
content = re.sub(
    r'ClientPlayNetworking\.registerGlobalReceiver\(NetworkPackets\.SHARE_RESPONSE, \(client, handler, buf, responseSender\) -> \{\s*String fileName = buf\.readString\(\);\s*UUID originalSenderId = buf\.readUuid\(\);\s*UUID responder = buf\.readUuid\(\);\s*boolean accepted = buf\.readBoolean\(\);\s*client\.execute\(',
    r'ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.ShareResponsePayload.ID, (payload, context) -> {\n            String fileName = payload.fileName();\n            UUID originalSenderId = payload.originalSenderId();\n            UUID responder = payload.responder();\n            boolean accepted = payload.accepted();\n            context.client().execute(',
    content
)

# Replace REQUEST_TRACK_C2S (actually the client handles S2C)
content = re.sub(
    r'ClientPlayNetworking\.registerGlobalReceiver\(NetworkPackets\.REQUEST_TRACK_S2C, \(client, handler, buf, responseSender\) -> \{\s*String trackName = buf\.readString\(\);\s*UUID requestor = buf\.readUuid\(\);\s*client\.execute\(',
    r'ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.RequestTrackS2CPayload.ID, (payload, context) -> {\n            String trackName = payload.trackName();\n            UUID requestor = payload.owner();\n            context.client().execute(',
    content
)

# In SHARE_REQUEST logic, we do client.setScreen -> context.client().setScreen
content = content.replace('client.setScreen', 'context.client().setScreen')
content = content.replace('client.player.sendMessage', 'context.client().player.sendMessage')

# We must replace ClientPlayNetworking.send(..., PacketByteBufs) logic.
# Wait, let's just use Python to find ClientPlayNetworking.send and replace.
# Actually, the python script will replace MP3PlayerClient properly.

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("MP3PlayerClient rewritten.")
