import zipfile
import json
import os
import glob
import shutil

input_jar = 'build/libs/mp3player-1.0.0.jar'
output_jar = 'build/libs/mp3player-1.0.0-final.jar'
temp_dir = 'build/tmp/jar_extract'

if os.path.exists(temp_dir):
    shutil.rmtree(temp_dir)
os.makedirs(temp_dir)

# Найти пути в кэше
mcef_paths = glob.glob(r'C:\Users\Владимир\.gradle\caches\modules-2\files-2.1\maven.modrinth\mcef-keksuccino\**\mcef-keksuccino-*.jar', recursive=True)
gson_paths = glob.glob(r'C:\Users\Владимир\.gradle\caches\modules-2\files-2.1\com.google.code.gson\gson\**\gson-*.jar', recursive=True)

mcef_path = next((p for p in mcef_paths if 'sources' not in p and 'javadoc' not in p), None)
gson_path = next((p for p in gson_paths if 'sources' not in p and 'javadoc' not in p), None)

with zipfile.ZipFile(input_jar, 'r') as zin:
    zin.extractall(temp_dir)

# Edit fabric.mod.json
mod_json_path = os.path.join(temp_dir, 'fabric.mod.json')
with open(mod_json_path, 'r', encoding='utf-8') as f:
    mod_json = json.load(f)

mod_json['jars'] = [
    {'file': 'META-INF/jars/mcef-keksuccino-2.2.0-1.21.1-fabric.jar'},
    {'file': 'META-INF/jars/gson-2.10.1.jar'}
]

with open(mod_json_path, 'w', encoding='utf-8') as f:
    json.dump(mod_json, f, indent=4)

# Copy nested jars
jars_dir = os.path.join(temp_dir, 'META-INF', 'jars')
os.makedirs(jars_dir, exist_ok=True)
shutil.copy(mcef_path, os.path.join(jars_dir, 'mcef-keksuccino-2.2.0-1.21.1-fabric.jar'))
shutil.copy(gson_path, os.path.join(jars_dir, 'gson-2.10.1.jar'))

# Zip it back up
with zipfile.ZipFile(output_jar, 'w', zipfile.ZIP_DEFLATED) as zout:
    for root, dirs, files in os.walk(temp_dir):
        for file in files:
            file_path = os.path.join(root, file)
            arcname = os.path.relpath(file_path, temp_dir)
            zout.write(file_path, arcname)

os.remove(input_jar)
os.rename(output_jar, input_jar)
shutil.rmtree(temp_dir)
print('Successfully bundled MCEF and Gson into the jar safely!')
