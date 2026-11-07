import os
import re

java_dir = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric-1.20.6\src\main\java'

for root, _, files in os.walk(java_dir):
    for f in files:
        if f.endswith('.java'):
            path = os.path.join(root, f)
            with open(path, 'r', encoding='utf-8') as f_in:
                c = f_in.read()
            
            c = c.replace('ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.SHARE_REQUEST, (client, handler, buf, responseSender) -> {', 'ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.ShareRequestPayload.ID, (payload, context) -> {')
            c = c.replace('ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.SHARE_RESPONSE, (client, handler, buf, responseSender) -> {', 'ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.ShareResponsePayload.ID, (payload, context) -> {')
            c = c.replace('ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.REQUEST_TRACK_S2C, (client, handler, buf, responseSender) -> {', 'ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.RequestTrackS2CPayload.ID, (payload, context) -> {')
            
            c = c.replace('ClientPlayNetworking.send(NetworkPackets.FILE_TRANSFER_START, buf);', '/* Replaced in previous script but maybe not */')
            c = c.replace('ClientPlayNetworking.send(NetworkPackets.FILE_TRANSFER_CHUNK, buf);', '/* Replaced */')
            c = c.replace('ClientPlayNetworking.send(NetworkPackets.FILE_TRANSFER_END, buf);', '/* Replaced */')
            c = c.replace('ClientPlayNetworking.send(NetworkPackets.SHARE_REQUEST, buf);', '/* Replaced */')
            c = c.replace('ClientPlayNetworking.send(NetworkPackets.SHARE_RESPONSE, buf);', '/* Replaced */')
            
            c = c.replace('PlayerSkinDrawer.draw(context, player.getSkinTexture(),', 'PlayerSkinDrawer.draw(context, player.getSkinTextures().texture(),') # 1.20.6 Skin changes
            
            c = c.replace('@Override\n    public void render(DrawContext context, int mouseX, int mouseY, float delta) {', 'public void render(DrawContext context, int mouseX, int mouseY, float delta) {')

            c = c.replace('(client, handler, buf, responseSender)', '(payload, context)')

            with open(path, 'w', encoding='utf-8') as f_out:
                f_out.write(c)

print("Java files patched.")
