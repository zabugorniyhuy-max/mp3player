import os

project_dir = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric-1.20.6'

# 1. Update build.gradle
build_gradle_path = os.path.join(project_dir, 'build.gradle')
with open(build_gradle_path, 'r', encoding='utf-8') as f:
    bg = f.read()

bg = bg.replace("minecraft \"com.mojang:minecraft:1.20.1\"", "minecraft \"com.mojang:minecraft:1.20.6\"")
bg = bg.replace("mappings \"net.fabricmc:yarn:1.20.1+build.10:v2\"", "mappings \"net.fabricmc:yarn:1.20.6+build.3:v2\"")
bg = bg.replace("modImplementation \"net.fabricmc:fabric-loader:0.14.22\"", "modImplementation \"net.fabricmc:fabric-loader:0.15.11\"")
bg = bg.replace("modImplementation \"net.fabricmc.fabric-api:fabric-api:0.83.0+1.20.1\"", "modImplementation \"net.fabricmc.fabric-api:fabric-api:0.100.3+1.20.6\"")
bg = bg.replace("it.options.release = 17", "it.options.release = 21")
bg = bg.replace("sourceCompatibility = JavaVersion.VERSION_17", "sourceCompatibility = JavaVersion.VERSION_21")
bg = bg.replace("targetCompatibility = JavaVersion.VERSION_17", "targetCompatibility = JavaVersion.VERSION_21")

with open(build_gradle_path, 'w', encoding='utf-8') as f:
    f.write(bg)

# 2. Update fabric.mod.json
fabric_mod_json_path = os.path.join(project_dir, r'src\main\resources\fabric.mod.json')
with open(fabric_mod_json_path, 'r', encoding='utf-8') as f:
    fm = f.read()

fm = fm.replace('"java": ">=17"', '"java": ">=21"')
fm = fm.replace('"minecraft": ">=1.20.1 <=1.21.5"', '"minecraft": ">=1.20.6 <=1.21.5"')

with open(fabric_mod_json_path, 'w', encoding='utf-8') as f:
    f.write(fm)

print("Updated build.gradle and fabric.mod.json")
