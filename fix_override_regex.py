import os
import re

def fix_override(path):
    with open(path, 'r', encoding='utf-8') as f:
        c = f.read()
    
    # Replace multiple @Override with single
    c = re.sub(r'(@Override\s*)+public void renderBackground', r'@Override\n    public void renderBackground', c)
    c = re.sub(r'(@Override\s*)+public void render\(', r'@Override\n    public void render(', c)
    
    with open(path, 'w', encoding='utf-8') as f:
        f.write(c)

for p in [r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric-1.20.6', r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric']:
    base = os.path.join(p, 'src', 'main', 'java', 'com', 'mp3player', 'client', 'gui')
    for f in ['MainScreen.java', 'MusicDiscScreen.java', 'ShareScreen.java', 'ShareRequestScreen.java', 'ConfirmScreen.java']:
        fp = os.path.join(base, f)
        if os.path.exists(fp):
            fix_override(fp)
