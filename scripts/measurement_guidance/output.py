from __future__ import annotations

from pathlib import Path
import xml.etree.ElementTree as ET

from .constants import METRIC_SPECS
from .models import Page, RenderedPath
from .svg import collect_paths, page_viewport
from .utils import fmt, log


def build_vector_xml(page: Page, paths: list[RenderedPath]) -> str:
    lines = [
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>",
        "<vector xmlns:android=\"http://schemas.android.com/apk/res/android\"",
        f"    android:width=\"{fmt(page.width)}dp\"",
        f"    android:height=\"{fmt(page.height)}dp\"",
        f"    android:viewportWidth=\"{fmt(page.width)}\"",
        f"    android:viewportHeight=\"{fmt(page.height)}\">",
    ]
    for rendered in paths:
        lines.append("    <path")
        lines.append(f"        android:pathData=\"{rendered.path_data}\"")
        if rendered.fill:
            lines.append(f"        android:fillColor=\"{rendered.fill}\"")
        if rendered.fill_alpha is not None and rendered.fill_alpha < 0.999:
            lines.append(f"        android:fillAlpha=\"{fmt(rendered.fill_alpha)}\"")
        if rendered.fill_type:
            lines.append(f"        android:fillType=\"{rendered.fill_type}\"")
        if rendered.stroke:
            lines.append(f"        android:strokeColor=\"{rendered.stroke}\"")
        if rendered.stroke_alpha is not None and rendered.stroke_alpha < 0.999:
            lines.append(f"        android:strokeAlpha=\"{fmt(rendered.stroke_alpha)}\"")
        if rendered.stroke_width is not None:
            lines.append(f"        android:strokeWidth=\"{fmt(rendered.stroke_width)}\"")
        if rendered.line_cap:
            lines.append(f"        android:strokeLineCap=\"{rendered.line_cap}\"")
        if rendered.line_join:
            lines.append(f"        android:strokeLineJoin=\"{rendered.line_join}\"")
        lines[-1] = lines[-1] + " />"
    lines.append("</vector>")
    return "\n".join(lines) + "\n"


def write_output(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def build_outputs(
    pages: dict[str, Page],
    background_layer: ET.Element,
    highlight_layers: dict[str, ET.Element],
    light_dir: Path,
    night_dir: Path,
    night_fill: str,
) -> int:
    file_count = 0
    for metric_name, page_group, highlight in METRIC_SPECS:
        for sex in ("male", "female"):
            for side in ("front", "back"):
                page_label = f"{sex}-{page_group}-{side}"
                page = pages[page_label]
                viewport = page_viewport(page)
                page_offset = (1.0, 0.0, 0.0, 1.0, -page.x, -page.y)

                for dark_mode, target_dir in ((False, light_dir), (True, night_dir)):
                    rendered_paths: list[RenderedPath] = []
                    collect_paths(
                        background_layer,
                        page_offset,
                        viewport,
                        rendered_paths,
                        night_fill,
                        dark_mode,
                    )
                    if highlight:
                        collect_paths(
                            highlight_layers[highlight],
                            page_offset,
                            viewport,
                            rendered_paths,
                            night_fill,
                            dark_mode,
                        )

                    file_name = f"measurement_guidance_{metric_name}_{sex}_{side}.xml"
                    output_path = target_dir / file_name
                    write_output(output_path, build_vector_xml(page, rendered_paths))
                    file_count += 1

                    variant = "night" if dark_mode else "light"
                    log(
                        f"wrote {variant} drawable {output_path} "
                        f"with {len(rendered_paths)} paths"
                    )

    return file_count