import os
import re

base = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric-1.20.6\src\main\java\com\mp3player\client\gui'

def ensure_render_background(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    if 'public void renderBackground(' not in content:
        # Add renderBackground to the class
        rb = '\n    @Override\n    public void renderBackground(net.minecraft.client.gui.DrawContext ctx, int mouseX, int mouseY, float delta) {\n        // Prevents default Minecraft darkening\n    }\n\n'
        # find where to insert it. Before `public void render(`
        content = content.replace('public void render(net.minecraft.client.gui.DrawContext', rb + 'public void render(net.minecraft.client.gui.DrawContext')
        content = content.replace('public void render(DrawContext context', rb + 'public void render(DrawContext context')
        content = content.replace('public void render(DrawContext ctx', rb + 'public void render(DrawContext ctx')
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

for f in ['MainScreen.java', 'MusicDiscScreen.java', 'ShareScreen.java', 'ShareRequestScreen.java', ]:
    ensure_render_background(os.path.join(base, f))

# Fix ShareRequestScreen translations
share_req = os.path.join(base, 'ShareRequestScreen.java')
with open(share_req, 'r', encoding='utf-8') as f:
    sr = f.read()
sr = sr.replace('Text.literal("Запрос на получение песни")', 'Text.literal(DrawUtils.t("Song Request", "Запрос на получение песни"))')
sr = sr.replace('Text.literal("Принять")', 'Text.literal(DrawUtils.t("Accept", "Принять"))')
sr = sr.replace('Text.literal("Отклонить")', 'Text.literal(DrawUtils.t("Decline", "Отклонить"))')
sr = sr.replace('context.drawCenteredTextWithShadow(this.textRenderer, "От: " + senderName,', 'context.drawCenteredTextWithShadow(this.textRenderer, DrawUtils.t("From: ", "От: ") + senderName,')
sr = sr.replace('context.drawCenteredTextWithShadow(this.textRenderer, "Хочет поделиться песней:",', 'context.drawCenteredTextWithShadow(this.textRenderer, DrawUtils.t("Wants to share a song:", "Хочет поделиться песней:"),')
with open(share_req, 'w', encoding='utf-8') as f:
    f.write(sr)

# Fix MainScreen ... and ||
main_scr = os.path.join(base, 'MainScreen.java')
with open(main_scr, 'r', encoding='utf-8') as f:
    ms = f.read()
ms = ms.replace('trackInfo.substring(0, 28) + ".."', 'trackInfo.substring(0, 28) + "..."')
ms = ms.replace('String playIcon = current ? "||" : "▶";', 'String playIcon = current ? "⏸" : "▶";')
# Fix missing Make Disc translation in options menu
ms = ms.replace('t("💿 Make Disc", "💿 На пластинку")', 't("💿 Make Disc", "💿 Сделать пластинкой")')
ms = ms.replace('t("✖ Delete", "✖ Удалить")', 't("✖ Delete", "✖ Удалить песню")')
with open(main_scr, 'w', encoding='utf-8') as f:
    f.write(ms)

# Fix MusicDiscScreen .. 
md_scr = os.path.join(base, 'MusicDiscScreen.java')
with open(md_scr, 'r', encoding='utf-8') as f:
    md = f.read()
md = md.replace('displayName.substring(0, 33) + ".."', 'displayName.substring(0, 33) + "..."')
with open(md_scr, 'w', encoding='utf-8') as f:
    f.write(md)

print("UI fixed")
