from __future__ import annotations

import sys

from .models import Point


def log(message: str) -> None:
    print(f"[generate_measurement_guidance] {message}", file=sys.stderr)


def local_name(tag: str) -> str:
    if "}" in tag:
        return tag.split("}", 1)[1]
    return tag


def parse_float(value: str | None, default: float = 0.0) -> float:
    if value is None or value == "":
        return default
    return float(value)


def fmt(value: float) -> str:
    rounded = round(value, 3)
    if abs(rounded) < 1e-9:
        rounded = 0.0
    text = f"{rounded:.3f}".rstrip("0").rstrip(".")
    return text or "0"


def fmt_pair(point: Point) -> str:
    return f"{fmt(point[0])},{fmt(point[1])}"


def fmt_path(value: float) -> str:
    rounded = round(value, 1)
    if abs(rounded) < 1e-9:
        rounded = 0.0
    text = f"{rounded:.1f}".rstrip("0").rstrip(".")
    return text or "0"


def fmt_pair_path(point: Point) -> str:
    return f"{fmt_path(point[0])},{fmt_path(point[1])}"