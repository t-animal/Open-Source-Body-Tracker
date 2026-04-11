from __future__ import annotations

import argparse
from pathlib import Path
import xml.etree.ElementTree as ET

from .constants import PAGE_LABELS
from .output import build_outputs
from .svg import find_layers, load_pages, normalize_color
from .utils import log


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Generate measurement guidance VectorDrawables from assets/body-drawing.svg.",
    )
    parser.add_argument("--svg", required=True, type=Path)
    parser.add_argument("--light-dir", required=True, type=Path)
    parser.add_argument("--night-dir", required=True, type=Path)
    parser.add_argument("--night-fill", default="#F2F2F2")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    log(f"reading SVG from {args.svg}")
    root = ET.parse(args.svg).getroot()

    background_layer, highlight_layers = find_layers(root)
    log(f"found background layer and {len(highlight_layers)} highlight layers")

    pages = load_pages(background_layer)
    missing_pages = PAGE_LABELS.difference(pages)
    if missing_pages:
        raise ValueError(f"Missing page rectangles: {sorted(missing_pages)}")
    log(f"loaded {len(pages)} page rectangles")

    night_fill = normalize_color(args.night_fill) or "#f2f2f2"
    log(
        f"generating drawables into {args.light_dir} and {args.night_dir} "
        f"with night fill {night_fill}"
    )

    file_count = build_outputs(
        pages,
        background_layer,
        highlight_layers,
        args.light_dir,
        args.night_dir,
        night_fill,
    )
    log(f"finished writing {file_count} drawable files")
    return 0