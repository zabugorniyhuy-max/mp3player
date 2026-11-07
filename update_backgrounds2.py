import os
import re

directory = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric\src\main\java\com\mp3player\client\gui'
files_to_patch = ['MainScreen.java', 'ShareScreen.java', 'ShareRequestScreen.java', 'MusicDiscScreen.java']

pattern = re.compile(r'@Override\s+public void renderBackground\(DrawContext context\)\s*\{\s*// No darkening\s*\}')

correct_signatures = """
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

for filename in files_to_patch:
    file_path = os.path.join(directory, filename)
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Replace the existing single override with the multiple overrides
    content = pattern.sub(correct_signatures, content)
        
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

print("Screens updated successfully with all possible background overrides!")
