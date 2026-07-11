import os
import shutil

src_kt = '/storage/emulated/0/Download/minimalist-uploader-1/app/src/main/java/com/lagradost/cloudstream3/ui/settings'
dst_kt = '/storage/emulated/0/Download/nexxxup (1)/app/src/main/java/com/unan/nexxxup/ui/settings'
src_layout = '/storage/emulated/0/Download/minimalist-uploader-1/app/src/main/res/layout'
dst_layout = '/storage/emulated/0/Download/nexxxup (1)/app/src/main/res/layout'
src_xml = '/storage/emulated/0/Download/minimalist-uploader-1/app/src/main/res/xml'
dst_xml = '/storage/emulated/0/Download/nexxxup (1)/app/src/main/res/xml'

def replace_namespaces(content):
    content = content.replace('com.lagradost.cloudstream3', 'com.unan.nexxxup')
    content = content.replace('lagradost', 'unan')
    return content

# 1. Copy Kotlin files
if os.path.exists(dst_kt):
    shutil.rmtree(dst_kt)
shutil.copytree(src_kt, dst_kt)

for root, dirs, files in os.walk(dst_kt):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            content = replace_namespaces(content)
            with open(path, 'w') as f:
                f.write(content)

# 2. Copy specific layouts
layouts = [
    'main_settings.xml',
    'fragment_settings_account.xml',
    'fragment_settings_backup.xml',
    'fragment_settings_general.xml',
    'fragment_settings_player.xml',
    'fragment_settings_ui.xml',
    'custom_preference_category_material.xml',
    'custom_preference_material.xml',
    'custom_preference_widget_seekbar.xml',
    'settings_title_top.xml',
    'chromecast_subtitle_settings.xml',
    'subtitle_settings.xml',
    'subtitle_settings_dialog.xml'
]
for l in layouts:
    if os.path.exists(os.path.join(src_layout, l)):
        with open(os.path.join(src_layout, l), 'r') as f:
            content = f.read()
        content = replace_namespaces(content)
        with open(os.path.join(dst_layout, l), 'w') as f:
            f.write(content)

# 3. Copy xml files
xml_files = [
    'settings_account.xml',
    'settings_backup.xml',
    'settings_general.xml',
    'settings_player.xml',
    'settings_ui.xml'
]
for x in xml_files:
    if os.path.exists(os.path.join(src_xml, x)):
        with open(os.path.join(src_xml, x), 'r') as f:
            content = f.read()
        content = replace_namespaces(content)
        with open(os.path.join(dst_xml, x), 'w') as f:
            f.write(content)

print("Done copying")
