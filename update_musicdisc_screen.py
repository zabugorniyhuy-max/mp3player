import re

file_path = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric\src\main\java\com\mp3player\client\gui\MusicDiscScreen.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(
    'import java.util.List;',
    'import java.util.List;\nimport com.mp3player.client.config.ModConfig;'
)

content = content.replace(
    'super(Text.literal("Выберите пластинку для записи"));',
    'super(Text.literal(DrawUtils.t("Choose disc for recording", "Выберите пластинку для записи")));'
)

content = content.replace(
    'ctx.fill(px, py, px + pw, py + ph, 0xFF1A1A1A);',
    'boolean dt = ModConfig.getInstance().darkTheme;\n        ctx.fill(px, py, px + pw, py + ph, dt ? 0xEE1a1a1a : 0xFFe8e8e8);'
)

content = content.replace(
    'ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, py + 10, 0xFFFFFF);',
    'ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, py + 10, dt ? 0xFFFFFFFF : 0xFF1a1a1a);'
)

content = content.replace(
    'ctx.drawCenteredTextWithShadow(this.textRenderer, "Нет пластинок в инвентаре", px + pw / 2, listY + 20, 0xAAAAAA);',
    'ctx.drawCenteredTextWithShadow(this.textRenderer, DrawUtils.t("No discs in inventory", "Нет пластинок в инвентаре"), px + pw / 2, listY + 20, dt ? 0xFFAAAAAA : 0xFF555555);'
)

content = content.replace(
    'ctx.fill(px + 10, y, px + pw - 10, y + 20, 0xFF333333);',
    'ctx.fill(px + 10, y, px + pw - 10, y + 20, dt ? 0xFF333333 : 0xFFcccccc);'
)

content = content.replace(
    'DrawUtils.drawText(ctx, this.textRenderer, displayName, px + 35, y + 6, hover ? 0xFFFF0055 : 0xAAAAAA, false);',
    'DrawUtils.drawText(ctx, this.textRenderer, displayName, px + 35, y + 6, hover ? 0xFFFF0055 : (dt ? 0xFFAAAAAA : 0xFF555555), false);'
)

content = content.replace(
    'ctx.drawCenteredTextWithShadow(this.textRenderer, "Отменить", px + pw - 40, y + 6, 0xFFFFFF);',
    'ctx.drawCenteredTextWithShadow(this.textRenderer, DrawUtils.t("Cancel", "Отменить"), px + pw - 40, y + 6, 0xFFFFFF);'
)

content = content.replace(
    'Text.literal("Записать песню на пластинку?")',
    'Text.literal(DrawUtils.t("Record song to disc?", "Записать песню на пластинку?"))'
)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("MusicDiscScreen updated successfully")
