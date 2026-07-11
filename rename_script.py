import os

def replace_in_file(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
            
        new_content = content.replace('unan', 'unan')
        new_content = new_content.replace('Unan', 'Unan')
        new_content = new_content.replace('nexxxup', 'nexxxup')
        new_content = new_content.replace('Nexxxup', 'Nexxxup')
        new_content = new_content.replace('Nexxxup', 'Nexxxup')
        new_content = new_content.replace('Nexxxup', 'Nexxxup')
        new_content = new_content.replace('Nexxxup', 'Nexxxup')
        
        if content != new_content:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(new_content)
    except Exception as e:
        print(f"Skipping {filepath}: {e}")

def main():
    # 1. Replace contents in all files
    for root, dirs, files in os.walk('.'):
        if '.git' in root or '.gradle' in root or 'build' in root:
            continue
        for file in files:
            if file.endswith('.png') or file.endswith('.jpg') or file.endswith('.jpeg') or file.endswith('.webp') or file.endswith('.jar'):
                continue
            filepath = os.path.join(root, file)
            replace_in_file(filepath)

    # 2. Rename directories bottom-up
    for root, dirs, files in os.walk('.', topdown=False):
        if '.git' in root or '.gradle' in root or 'build' in root:
            continue
        for d in dirs:
            new_d = d.replace('unan', 'unan').replace('nexxxup', 'nexxxup')
            if new_d != d:
                old_path = os.path.join(root, d)
                new_path = os.path.join(root, new_d)
                os.rename(old_path, new_path)
                print(f"Renamed {old_path} to {new_path}")

if __name__ == '__main__':
    main()
