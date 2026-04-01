import os
import re

base_dir = "/home/ajay/Ajay/Personal/SwiftTrack/backend/services"

files_updated = 0
for root, dirs, files in os.walk(base_dir):
    if "src/main/resources" in root and "application.yaml" in files:
        filepath = os.path.join(root, "application.yaml")
        with open(filepath, "r") as f:
            content = f.read()
            
        original_content = content
        
        # If maximum-pool-size already exists, replace to 2
        if "maximum-pool-size" in content:
            content = re.sub(r'maximum-pool-size:\s*\d+', 'maximum-pool-size: 2', content)
        elif "hikari:" in content:
            content = re.sub(r'(hikari:\s*\n)', r'\1      maximum-pool-size: 2\n', content)
        else:
            # If there's no hikari block at all, append it under database standard properties
            content = re.sub(r'(driver-class-name:.*)', r'\1\n    hikari:\n      maximum-pool-size: 2', content)
            
        if content != original_content:
            with open(filepath, "w") as f:
                f.write(content)
            print(f"Updated Connection Pool Limit to 2 in: {filepath}")
            files_updated += 1

print(f"Successfully optimized exactly {files_updated} microservices to sip connections!")
