import re

file_path = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric\src\main\java\com\mp3player\client\gui\MainScreen.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Revert Colors
content = re.sub(r'private int bgPanel\(\) \{ return darkTheme \? 0xFF121212 : 0xFFe8e8e8; \}',
                 r'private int bgPanel() { return darkTheme ? 0xEE1a1a1a : 0xFFe8e8e8; }', content)
content = re.sub(r'private int bgDark\(\) \{ return darkTheme \? 0xFF0A0A0A : 0xFFffffff; \}',
                 r'private int bgDark() { return darkTheme ? 0xFF151515 : 0xFFffffff; }', content)
content = re.sub(r'private int bgButton\(\) \{ return darkTheme \? 0xFF1f1f1f : 0xFF9a9a9a; \}',
                 r'private int bgButton() { return darkTheme ? 0xFF2a2a2a : 0xFF9a9a9a; }', content)
content = re.sub(r'private int bgHover\(\) \{ return darkTheme \? 0xFF2f2f2f : 0xFF7a7a7a; \}',
                 r'private int bgHover() { return darkTheme ? 0xFF3a3a3a : 0xFF7a7a7a; }', content)
content = re.sub(r'private int textSecondary\(\) \{ return darkTheme \? 0xFFAAAAAA : 0xFF555555; \}',
                 r'private int textSecondary() { return darkTheme ? 0xFF888888 : 0xFF3a3a3a; }', content)

# 2. Revert Emojis in Buttons and Tabs
content = content.replace('drawButton(ctx, mouseX, mouseY, panelX + 4, panelY + 4, 24, 14, darkTheme ? t("Drk", "Тем") : t("Lgt", "Св"), 200);',
                          'drawButton(ctx, mouseX, mouseY, panelX + 4, panelY + 4, 16, 14, darkTheme ? "☾" : "☀", 200);')

content = content.replace('int tab2W = this.textRenderer.getWidth(t("Fav", "Избр")) + 20;',
                          'int tab2W = this.textRenderer.getWidth("⭐") + 20;')

content = content.replace('drawTab(ctx, mouseX, mouseY, panelX + 10 + tab1W + 5, panelY - 20, t("Fav", "Избр"), 1, t("Favorites", "Любимые песни"));',
                          'drawTab(ctx, mouseX, mouseY, panelX + 10 + tab1W + 5, panelY - 20, "⭐", 1, t("Favorites", "Любимые песни"));')

content = content.replace('drawButton(ctx, mouseX, mouseY, panelX + panelW - 65, panelY + 24, 55, 14, t("Speaker", "Колонка"), 202, sm ? accent() : textSecondary());',
                          'drawButton(ctx, mouseX, mouseY, panelX + panelW - 65, panelY + 24, 55, 14, t("🌍 Speaker", "🌍 Колонка"), 202, sm ? accent() : textSecondary());')

content = content.replace('drawButton(ctx, mouseX, mouseY, panelX + panelW - 90, panelY + 24, 20, 14, mg ? t("Mut", "Мут") : t("Vol", "Зв"), 203, mg ? 0xFFFF5555 : textSecondary());',
                          'drawButton(ctx, mouseX, mouseY, panelX + panelW - 85, panelY + 24, 16, 14, mg ? "🔇" : "🔊", 203, mg ? 0xFFFF5555 : textSecondary());')

content = content.replace('drawButton(ctx, mouseX, mouseY, px + 10, topY - 2, 24, 14, t("Upd", "Обн"), 110);',
                          'drawButton(ctx, mouseX, mouseY, px + 10, topY - 2, 20, 14, "🔄", 110);')

content = content.replace('drawButton(ctx, mouseX, mouseY, px + 38, topY - 2, 24, 14, t("Dir", "Пап"), 111);',
                          'drawButton(ctx, mouseX, mouseY, px + 35, topY - 2, 20, 14, "📁", 111);')

# 3. Revert Track list emojis
content = content.replace('String prefix = current ? "> " : (isFav ? "★ " : "  ");',
                          'String prefix = current ? "▶ " : (isFav ? "♥ " : "  ");')

content = content.replace('String playIcon = current ? "||" : ">";',
                          'String playIcon = current ? "⏸" : "▶";')

content = content.replace('DrawUtils.drawText(ctx, this.textRenderer, "...", px + pw - 18, tY + 2, hoverDots ? accent() : textSecondary(), false);',
                          'DrawUtils.drawText(ctx, this.textRenderer, "⋮", px + pw - 18, tY + 2, hoverDots ? accent() : textSecondary(), false);')

# 4. Revert Track Menu emojis
content = content.replace('String[] opts = {t("★ Favorite", "★ Нравится"), t("O Repeat", "O Повтор"), t("O Make Disc", "O На пластинку"), t("i Info", "i Сведения"), t("^ Share", "^ Поделиться"), t("X Delete", "X Удалить")};',
                          'String[] opts = {t("♥ Favorite", "♥ Нравится"), t("🔁 Repeat", "🔁 Повтор"), t("💿 Make Disc", "💿 На пластинку"), t("i Info", "i Сведения"), t("📤 Share", "📤 Поделиться"), t("✖ Delete", "✖ Удалить")};')

# 5. Revert Player Bar Emojis
content = content.replace('drawButton(ctx, mouseX, mouseY, btnX + btnS + gap + btnW_10 + gap, btnY, btnS, btnS, playing ? "||" : ">", 52, accent());',
                          'drawButton(ctx, mouseX, mouseY, btnX + btnS + gap + btnW_10 + gap, btnY, btnS, btnS, playing ? "⏸" : "▶", 52, accent());')
content = content.replace('drawButton(ctx, mouseX, mouseY, btnX + btnS + gap + btnW_10 + gap, btnY, btnS, btnS, playing ? "||" : "▶", 52, accent());',
                          'drawButton(ctx, mouseX, mouseY, btnX + btnS + gap + btnW_10 + gap, btnY, btnS, btnS, playing ? "⏸" : "▶", 52, accent());')

# Ensure the title doesn't overlap the new EN/RU button which is at X=24
# The EN/RU button is currently at: panelX + 32, with width 24 (ends at 56).
# In the patched version we changed it to:
content = content.replace('drawButton(ctx, mouseX, mouseY, panelX + 32, panelY + 4, 24, 14, ModConfig.getInstance().isEnglish ? "EN" : "RU", 204);',
                          'drawButton(ctx, mouseX, mouseY, panelX + 24, panelY + 4, 20, 14, ModConfig.getInstance().isEnglish ? "EN" : "RU", 204);')

# Adjust mouseClicked for the translated EN/RU button
content = content.replace('if (isInside(mx, my, px + 32, py + 4, 24, 14)) {',
                          'if (isInside(mx, my, px + 24, py + 4, 20, 14)) {')

# The title is currently at panelX + 65. Let's make it panelX + 50 to be tighter, since buttons end at 44.
content = content.replace('DrawUtils.drawText(ctx, this.textRenderer, title, panelX + 65, panelY + 7, accent(), true);',
                          'DrawUtils.drawText(ctx, this.textRenderer, title, panelX + 50, panelY + 7, accent(), true);')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Patch script completed")
