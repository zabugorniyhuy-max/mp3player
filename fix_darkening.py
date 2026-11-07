import os

directory = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric\src\main\java\com\mp3player\client\gui'

# Fix MainScreen.java - replace super.render with direct drawable rendering
file_path = os.path.join(directory, 'MainScreen.java')
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Remove the broken renderBackground overrides
old_bg = """    
    @Override
    public void renderBackground(DrawContext context) {
        // No darkening
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // No darkening
    }
    
    public void renderBackgroundTexture(DrawContext context) {
        // No darkening
    }
"""
content = content.replace(old_bg, '')

# Also try with \r\n
old_bg_crlf = old_bg.replace('\n', '\r\n')
content = content.replace(old_bg_crlf, '')

# Replace super.render with direct children rendering (no background)
content = content.replace(
    'super.render(ctx, mouseX, mouseY, delta);',
    '// Render children (buttons/widgets) without calling super.render to avoid darkening\n        for (net.minecraft.client.gui.Element child : this.children()) {\n            if (child instanceof net.minecraft.client.gui.Drawable drawable) {\n                drawable.render(ctx, mouseX, mouseY, delta);\n            }\n        }'
)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print(f"Fixed {file_path}")

# Fix ShareScreen.java
file_path = os.path.join(directory, 'ShareScreen.java')
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(old_bg, '')
content = content.replace(old_bg_crlf, '')

content = content.replace(
    'super.render(context, mouseX, mouseY, delta);',
    '// Render children (buttons/widgets) without calling super.render to avoid darkening\n        for (net.minecraft.client.gui.Element child : this.children()) {\n            if (child instanceof net.minecraft.client.gui.Drawable drawable) {\n                drawable.render(context, mouseX, mouseY, delta);\n            }\n        }'
)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print(f"Fixed {file_path}")

# Fix ShareRequestScreen.java
file_path = os.path.join(directory, 'ShareRequestScreen.java')
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(old_bg, '')
content = content.replace(old_bg_crlf, '')

content = content.replace(
    'super.render(context, mouseX, mouseY, delta);',
    '// Render children (buttons/widgets) without calling super.render to avoid darkening\n        for (net.minecraft.client.gui.Element child : this.children()) {\n            if (child instanceof net.minecraft.client.gui.Drawable drawable) {\n                drawable.render(context, mouseX, mouseY, delta);\n            }\n        }'
)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print(f"Fixed {file_path}")

# Fix MusicDiscScreen.java
file_path = os.path.join(directory, 'MusicDiscScreen.java')
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(old_bg, '')
content = content.replace(old_bg_crlf, '')

content = content.replace(
    'super.render(ctx, mouseX, mouseY, delta);',
    '// Render children (buttons/widgets) without calling super.render to avoid darkening\n        for (net.minecraft.client.gui.Element child : this.children()) {\n            if (child instanceof net.minecraft.client.gui.Drawable drawable) {\n                drawable.render(ctx, mouseX, mouseY, delta);\n            }\n        }'
)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print(f"Fixed {file_path}")

print("\nAll screens fixed! super.render() replaced with direct children rendering.")
