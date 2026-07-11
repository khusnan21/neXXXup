import os
import re

files_to_fix = [
    'app/src/main/java/com/unan/nexxxup/AdultProvider/Asian/Jav141Provider.kt',
    'app/src/main/java/com/unan/nexxxup/AdultProvider/Asian/UncenXProvider.kt',
    'app/src/main/java/com/unan/nexxxup/AdultProvider/Asian/Jav98Provider.kt'
]

pattern = re.compile(r'newExtractorLink\(\s*name,\s*linkName,\s*href,\s*"",\s*0,\s*false\s*\)', re.MULTILINE)
replacement = r'newExtractorLink(source = name, name = linkName, url = href, type = com.unan.nexxxup.utils.ExtractorLinkType.VIDEO)'

for f_path in files_to_fix:
    full_path = f'/storage/emulated/0/Download/nexxxup (1)/{f_path}'
    if os.path.exists(full_path):
        with open(full_path, 'r') as f:
            content = f.read()
        
        # fix newExtractorLink
        content = pattern.sub(replacement, content)
        
        # fix Actor = it
        content = content.replace('ActorData(Actor = it)', 'ActorData(actor = it)')
        
        with open(full_path, 'w') as f:
            f.write(content)

# Fix CloudStreamApp
settings_dir = '/storage/emulated/0/Download/nexxxup (1)/app/src/main/java/com/unan/nexxxup/ui/settings'
for root, dirs, files in os.walk(settings_dir):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            content = content.replace('CloudStreamApp', 'NexxxupApp')
            content = content.replace('import com.unan.nexxxup.NexxxupApp', 'import com.unan.nexxxup.NexxxupApp')
            
            with open(path, 'w') as f:
                f.write(content)

print("Fixed.")
