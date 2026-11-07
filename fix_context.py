import os
import re

mod_path = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric-1.20.6\src\main\java\com\mp3player\MP3PlayerMod.java'
with open(mod_path, 'r', encoding='utf-8') as f:
    mod = f.read()

mod = mod.replace('context.server()', 'context.player().getServer()')
with open(mod_path, 'w', encoding='utf-8') as f:
    f.write(mod)

client_path = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric-1.20.6\src\main\java\com\mp3player\MP3PlayerClient.java'
with open(client_path, 'r', encoding='utf-8') as f:
    client = f.read()

client = client.replace('context.client()', 'MinecraftClient.getInstance()')
client = client.replace('NetworkPackets.REQUEST_TRACK_C2S', 'NetworkPackets.RequestTrackC2SPayload.ID')
client = client.replace('client.player != null', 'MinecraftClient.getInstance().player != null')
client = client.replace('client.execute', 'MinecraftClient.getInstance().execute')

with open(client_path, 'w', encoding='utf-8') as f:
    f.write(client)
