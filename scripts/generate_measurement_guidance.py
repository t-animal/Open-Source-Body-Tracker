#!/usr/bin/env python3

from __future__ import annotations

import sys

from measurement_guidance.cli import main


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:  # pragma: no cover - script error path
        print(f"error: {exc}", file=sys.stderr)
        raise SystemExit(1)
