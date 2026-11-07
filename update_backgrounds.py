import os
import re

directory = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric\src\main\java\com\mp3player\client\gui'
files_to_patch = ['MainScreen.java', 'ShareScreen.java', 'ShareRequestScreen.java', 'MusicDiscScreen.java']

wrong_signature_pattern = re.compile(r'public void renderBackground\(DrawContext \w+, int \w+, int \w+, float \w+\) \{\s*// Removed 0x44000000 darkening\s*\}')

correct_signature = """
    @Override
    public void renderBackground(DrawContext context) {
        // No darkening
    }
"""

for filename in files_to_patch:
    file_path = os.path.join(directory, filename)
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Remove the wrong signature if it exists
    content = wrong_signature_pattern.sub('', content)

    # Remove this manual call if it exists
    content = content.replace('this.renderBackground(context, mouseX, mouseY, delta);', '')
    content = content.replace('this.renderBackground(ctx, mouseX, mouseY, delta);', '')

    # Insert the correct signature before the render method
    # Since render might use ctx or context, we look for public void render(DrawContext
    render_idx = content.find('public void render(DrawContext')
    if render_idx != -1 and 'public void renderBackground(DrawContext context)' not in content:
        # Find the `@Override` right before render if it exists
        override_idx = content.rfind('@Override', 0, render_idx)
        insert_idx = render_idx
        if override_idx != -1 and (render_idx - override_idx) < 30:
            insert_idx = override_idx
            
        content = content[:insert_idx] + correct_signature + content[insert_idx:]
        
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

print("Screens updated successfully!")
