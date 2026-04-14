#!/usr/bin/env python3
"""Resolve eAPI hostname from environments.json (diagnostic for GitHub Actions)."""
from __future__ import annotations

import json
import os
import pathlib
import socket
import sys
import urllib.parse


def main() -> int:
    profile = (os.environ.get("E2E_ENV_PROFILE") or "qa-eapi-dev").strip()
    path = pathlib.Path("src/main/resources/e2e/environments.json")
    if not path.is_file():
        print(f"::notice::No {path} — skip eAPI DNS check")
        return 0
    data = json.loads(path.read_text(encoding="utf-8"))
    profiles = data.get("profiles") or {}
    if profile not in profiles:
        print(f"::warning::Unknown profile {profile!r} — known: {list(profiles)}")
        return 0
    url = (profiles[profile] or {}).get("eapiBaseUrl") or ""
    host = urllib.parse.urlparse(url).hostname
    if not host:
        print(f"::warning::No eapiBaseUrl host for profile {profile!r}")
        return 0
    print(f"eAPI DNS check: profile={profile} host={host!r}")
    try:
        socket.getaddrinfo(host, 443, type=socket.SOCK_STREAM)
    except OSError as e:
        print(
            f"::error::Cannot resolve eAPI host {host!r} ({e}). "
            "GitHub-hosted runners use the public internet — internal-only hostnames fail here. "
            "Use a self-hosted runner on your corporate network, or an eAPI endpoint/DNS your org exposes to the internet, "
            "or run this E2E from your laptop / internal CI."
        )
        return 1
    print(f"::notice::DNS OK for {host!r}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
