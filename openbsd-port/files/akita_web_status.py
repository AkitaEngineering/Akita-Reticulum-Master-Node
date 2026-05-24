#!/usr/local/bin/python3
# $OpenBSD$
#
# Lightweight web dashboard serving rnstatus output

import sys
import os
import subprocess
from http.server import BaseHTTPRequestHandler, HTTPServer
import ctypes

def apply_pledge():
    try:
        from ctypes.util import find_library
        libc_path = find_library('c')
        if not libc_path:
            libc_path = 'libc.so'
        libc = ctypes.CDLL(libc_path, use_errno=True)
        # pledge(promises, execpromises)
        promises = b"stdio rpath proc exec unix inet"
        if hasattr(libc, 'pledge'):
            if libc.pledge(promises, None) != 0:
                print("Warning: pledge() failed", file=sys.stderr)
            else:
                print("Successfully applied pledge: " + promises.decode(), file=sys.stderr)
    except Exception as e:
        print(f"Warning: Could not apply pledge: {e}", file=sys.stderr)

class StatusHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path != '/':
            self.send_response(404)
            self.end_headers()
            self.wfile.write(b"Not Found")
            return
            
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()
        
        try:
            # Execute rnstatus. Note: Requires sharing_scope: system
            result = subprocess.run(['/usr/local/bin/rnstatus'], capture_output=True, text=True, timeout=10)
            status_out = result.stdout
            if result.stderr:
                status_out += "\nErrors:\n" + result.stderr
        except Exception as e:
            status_out = f"Failed to execute rnstatus: {str(e)}"
            
        # Basic terminal-like styling
        html = f"""<!DOCTYPE html>
<html>
<head>
    <title>Akita Reticulum Node Status</title>
    <style>
        body {{ font-family: Consolas, monospace; background: #000; color: #00FF00; padding: 20px; margin: 0; }}
        pre {{ background: #111; padding: 15px; border: 1px solid #333; border-radius: 4px; overflow-x: auto; white-space: pre-wrap; }}
        h1 {{ color: #FFF; font-size: 1.5rem; }}
        .header {{ border-bottom: 1px solid #333; padding-bottom: 10px; margin-bottom: 20px; }}
    </style>
    <meta http-equiv="refresh" content="30">
</head>
<body>
    <div class="header">
        <h1>Akita Reticulum Node Status</h1>
        <small>Auto-refreshing every 30 seconds. Powered by OpenBSD.</small>
    </div>
    <pre>{status_out}</pre>
</body>
</html>"""
        self.wfile.write(html.encode('utf-8'))

if __name__ == '__main__':
    apply_pledge()
    port = 8080
    host = '127.0.0.1'
    server_address = (host, port)
    
    try:
        httpd = HTTPServer(server_address, StatusHandler)
        print(f"Starting web status server on http://{host}:{port}/...")
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nShutting down web server.")
        sys.exit(0)
    except OSError as e:
        print(f"Failed to start server: {e}", file=sys.stderr)
        sys.exit(1)
