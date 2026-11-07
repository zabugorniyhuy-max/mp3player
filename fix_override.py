import os
import re

def fix_override(path):
    with open(path, 'r', encoding='utf-8') as f:
        c = f.read()
    c = c.replace('@Override\n    @Override\n    public void renderBackground', '@Override\n    public void renderBackground')
    # add back Override to render
    c = c.replace('}\n\npublic void render(', '}\n\n    @Override\n    public void render(')
    c = c.replace('}\n\n    public void render(', '}\n\n    @Override\n    public void render(')
    with open(path, 'w', encoding='utf-8') as f:
        f.write(c)

for p in [r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric-1.20.6', r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric']:
    base = os.path.join(p, 'src', 'main', 'java', 'com', 'mp3player', 'client', 'gui')
    for f in ['MainScreen.java', 'MusicDiscScreen.java', 'ShareScreen.java', 'ShareRequestScreen.java', 'ConfirmScreen.java']:
        fp = os.path.join(base, f)
        if os.path.exists(fp):
            fix_override(fp)
