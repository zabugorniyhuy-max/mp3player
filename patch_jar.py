import sys
import zipfile
import os
import shutil

def patch_jar(jar_path):
    print(f"Patching {jar_path}...")
    temp_jar = jar_path + ".tmp"
    patched_any = False
    
    with zipfile.ZipFile(jar_path, 'r') as zin, zipfile.ZipFile(temp_jar, 'w', compression=zipfile.ZIP_DEFLATED) as zout:
        for item in zin.infolist():
            data = zin.read(item.filename)
            if item.filename == 'windows/x64/org/lwjgl/nanovg/lwjgl_nanovg.dll':
                old_pat = b'\x41\xb8\x03\x19\x00\x00'
                new_pat = b'\x41\xb8\x29\x82\x00\x00'
                if old_pat in data:
                    data = data.replace(old_pat, new_pat, 1)
                    patched_any = True
                    print(f"  Successfully patched {item.filename} (GL_RED -> GL_R8 for OpenGL 3.3 Core Profile)!")
            zout.writestr(item, data)
            
    if patched_any:
        shutil.move(temp_jar, jar_path)
        print(f"Patched JAR saved: {jar_path}")
    else:
        if os.path.exists(temp_jar):
            os.remove(temp_jar)
        print("Warning: Pattern not found or already patched!")

if __name__ == '__main__':
    target = sys.argv[1] if len(sys.argv) > 1 else 'build/libs/mp3player-26.3.jar'
    patch_jar(target)

