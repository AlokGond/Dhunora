#!/usr/bin/env python3
"""Apply the reviewed Dhunora changes to pinned SimpMusic sources, idempotently."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
UPSTREAM = ROOT / 'upstream' / 'SimpMusic'
PINS = {
    UPSTREAM: 'f26622bdf48be734cbfb65c360124cfeed8907c5',
    UPSTREAM / 'core': '454213897e020e8b52097acde25fed3c43a12c73',
}

def git(path, *args, **kwargs):
    return subprocess.run(['git', '-C', str(path), *args], check=True, **kwargs)

if not (UPSTREAM / 'gradlew').exists():
    git(ROOT, 'submodule', 'update', '--init', '--recursive')
for path, expected in PINS.items():
    actual = git(path, 'rev-parse', 'HEAD', capture_output=True, text=True).stdout.strip()
    if actual != expected:
        raise SystemExit(f'{path.name}: expected {expected}, got {actual}. Refusing to patch an unknown version.')
for path, patch in [(UPSTREAM, ROOT/'patches/simpmusic.patch'), (UPSTREAM/'core', ROOT/'patches/core.patch')]:
    already = subprocess.run(['git', '-C', str(path), 'apply', '--reverse', '--check', str(patch)], capture_output=True).returncode == 0
    if not already:
        git(path, 'apply', '--check', str(patch))
        git(path, 'apply', str(patch))
print('Dhunora 1.0.0 sources are ready:', UPSTREAM)
