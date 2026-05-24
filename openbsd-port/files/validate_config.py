#!/usr/local/bin/python3
# $OpenBSD$
#
# Helper script to validate Reticulum configuration before starting rnsd.

import sys
import os
import yaml

def validate_config(config_path):
    if not os.path.exists(config_path):
        print(f"Error: Config file not found at {config_path}", file=sys.stderr)
        return False
        
    try:
        with open(config_path, 'r', encoding='utf-8') as f:
            config = yaml.safe_load(f)
    except yaml.YAMLError as e:
        print(f"Error: Invalid YAML syntax in {config_path}:\n{e}", file=sys.stderr)
        return False
    except Exception as e:
        print(f"Error: Failed to read {config_path}: {e}", file=sys.stderr)
        return False
        
    if not isinstance(config, dict):
        print(f"Error: Configuration must be a dictionary at the top level.", file=sys.stderr)
        return False

    errors = 0

    # 1. Check sharing_scope
    sharing_scope = config.get('reticulum', {}).get('sharing_scope', None)
    if sharing_scope is None:
        # Check top level as fallback if people misconfigure
        sharing_scope = config.get('sharing_scope', None)
        
    if sharing_scope != 'system':
        print("Warning: 'sharing_scope' should be 'system' for rc.d status integration to work properly.", file=sys.stderr)

    # 2. Check storage_path
    storage_path = config.get('reticulum', {}).get('storage_path', config.get('storage_path', None))
    if storage_path:
        if not os.path.isabs(storage_path):
            print(f"Error: 'storage_path' must be an absolute path (found: {storage_path})", file=sys.stderr)
            errors += 1
            
        parent_dir = os.path.dirname(storage_path)
        if not os.path.exists(parent_dir):
            print(f"Error: The parent directory for storage_path ({parent_dir}) does not exist.", file=sys.stderr)
            errors += 1

    # 3. Check interfaces
    interfaces = config.get('interfaces', [])
    if not isinstance(interfaces, list):
        print("Error: 'interfaces' must be a list of interface configurations.", file=sys.stderr)
        errors += 1
    else:
        for idx, iface in enumerate(interfaces):
            if not isinstance(iface, dict):
                print(f"Error: Interface #{idx} is not a valid dictionary.", file=sys.stderr)
                errors += 1
                continue
            
            if 'name' not in iface:
                print(f"Warning: Interface #{idx} is missing a 'name'.", file=sys.stderr)
            if 'type' not in iface:
                print(f"Error: Interface '{iface.get('name', f'#{idx}')}' is missing a 'type'.", file=sys.stderr)
                errors += 1

    if errors > 0:
        print(f"Validation failed with {errors} errors.", file=sys.stderr)
        return False

    return True

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(f"Usage: {sys.argv[0]} <path_to_config>", file=sys.stderr)
        sys.exit(1)
        
    if validate_config(sys.argv[1]):
        sys.exit(0)
    else:
        sys.exit(1)
