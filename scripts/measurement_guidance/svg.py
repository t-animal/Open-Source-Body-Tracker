from __future__ import annotations

import math
import xml.etree.ElementTree as ET

from .constants import (
    COLOR_KEYWORDS,
    COMMAND_RE,
    HIGHLIGHT_LABELS,
    INKSCAPE_LABEL,
    MAX_PATH_DATA_CHARS,
    NUMBER_RE,
    PAGE_LABELS,
    TRANSFORM_RE,
)
from .models import BBox, Command, IDENTITY, Matrix, Page, Point, RenderedPath
from .utils import fmt_pair_path, local_name, parse_float


def multiply(m1: Matrix, m2: Matrix) -> Matrix:
    a1, b1, c1, d1, e1, f1 = m1
    a2, b2, c2, d2, e2, f2 = m2
    return (
        a1 * a2 + c1 * b2,
        b1 * a2 + d1 * b2,
        a1 * c2 + c1 * d2,
        b1 * c2 + d1 * d2,
        a1 * e2 + c1 * f2 + e1,
        b1 * e2 + d1 * f2 + f1,
    )


def apply_matrix(matrix: Matrix, point: Point) -> Point:
    a, b, c, d, e, f = matrix
    x, y = point
    return (a * x + c * y + e, b * x + d * y + f)


def parse_transform(value: str | None) -> Matrix:
    if not value:
        return IDENTITY

    matrix = IDENTITY
    for name, arg_text in TRANSFORM_RE.findall(value):
        numbers = [float(item) for item in NUMBER_RE.findall(arg_text)]
        if name == "matrix":
            if len(numbers) != 6:
                raise ValueError(f"Unsupported matrix transform: {value}")
            next_matrix: Matrix = tuple(numbers)  # type: ignore[assignment]
        elif name == "translate":
            tx = numbers[0]
            ty = numbers[1] if len(numbers) > 1 else 0.0
            next_matrix = (1.0, 0.0, 0.0, 1.0, tx, ty)
        elif name == "scale":
            sx = numbers[0]
            sy = numbers[1] if len(numbers) > 1 else sx
            next_matrix = (sx, 0.0, 0.0, sy, 0.0, 0.0)
        elif name == "rotate":
            angle = math.radians(numbers[0])
            cos_angle = math.cos(angle)
            sin_angle = math.sin(angle)
            rotate_matrix: Matrix = (cos_angle, sin_angle, -sin_angle, cos_angle, 0.0, 0.0)
            if len(numbers) == 3:
                cx, cy = numbers[1], numbers[2]
                next_matrix = multiply(
                    multiply((1.0, 0.0, 0.0, 1.0, cx, cy), rotate_matrix),
                    (1.0, 0.0, 0.0, 1.0, -cx, -cy),
                )
            else:
                next_matrix = rotate_matrix
        elif name == "skewX":
            angle = math.radians(numbers[0])
            next_matrix = (1.0, 0.0, math.tan(angle), 1.0, 0.0, 0.0)
        elif name == "skewY":
            angle = math.radians(numbers[0])
            next_matrix = (1.0, math.tan(angle), 0.0, 1.0, 0.0, 0.0)
        else:
            raise ValueError(f"Unsupported transform '{name}'")
        matrix = multiply(matrix, next_matrix)

    return matrix


def parse_style(element: ET.Element) -> dict[str, str]:
    style: dict[str, str] = {}
    for chunk in (element.get("style") or "").split(";"):
        if ":" not in chunk:
            continue
        key, value = chunk.split(":", 1)
        style[key.strip()] = value.strip()

    for key in (
        "fill",
        "fill-opacity",
        "fill-rule",
        "stroke",
        "stroke-opacity",
        "stroke-width",
        "stroke-linecap",
        "stroke-linejoin",
        "opacity",
        "display",
        "visibility",
    ):
        if key in element.attrib:
            style[key] = element.attrib[key]

    return style


def normalize_color(value: str | None) -> str | None:
    if value is None:
        return None

    lowered = value.strip().lower()
    if lowered in COLOR_KEYWORDS:
        return COLOR_KEYWORDS[lowered]
    if lowered.startswith("#"):
        if len(lowered) == 4:
            return "#" + "".join(ch * 2 for ch in lowered[1:])
        if len(lowered) == 7:
            return lowered
    if lowered.startswith("rgb(") and lowered.endswith(")"):
        parts = [part.strip() for part in lowered[4:-1].split(",")]
        if len(parts) == 3:
            channels: list[int] = []
            for part in parts:
                if part.endswith("%"):
                    channels.append(round(float(part[:-1]) * 2.55))
                else:
                    channels.append(int(float(part)))
            return "#" + "".join(f"{max(0, min(255, channel)):02x}" for channel in channels)

    raise ValueError(f"Unsupported color value: {value}")


def parse_alpha(value: str | None, default: float = 1.0) -> float:
    if value is None or value == "":
        return default
    return float(value)


def stroke_scale(matrix: Matrix) -> float:
    a, b, c, d, _, _ = matrix
    sx = math.hypot(a, b)
    sy = math.hypot(c, d)
    if sx == 0 and sy == 0:
        return 1.0
    if sx == 0:
        return sy
    if sy == 0:
        return sx
    return (sx + sy) / 2.0


def to_android_cap(value: str | None) -> str | None:
    if value is None:
        return None
    return {"round": "round", "square": "square", "butt": "butt"}.get(value)


def to_android_join(value: str | None) -> str | None:
    if value is None:
        return None
    return {"round": "round", "bevel": "bevel", "miter": "miter"}.get(value)


def reflect(point: Point, control: Point | None) -> Point:
    if control is None:
        return point
    return (2 * point[0] - control[0], 2 * point[1] - control[1])


def parse_path_commands(data: str) -> list[Command]:
    tokens = COMMAND_RE.findall(data)
    commands: list[Command] = []
    index = 0
    command = ""
    current: Point = (0.0, 0.0)
    start: Point = (0.0, 0.0)
    previous_cubic: Point | None = None
    previous_quadratic: Point | None = None
    previous_command = ""

    def is_command(token: str) -> bool:
        return len(token) == 1 and token.isalpha()

    def read_number() -> float:
        nonlocal index
        value = float(tokens[index])
        index += 1
        return value

    while index < len(tokens):
        token = tokens[index]
        if is_command(token):
            command = token
            index += 1
        if not command:
            raise ValueError("Path data is missing an initial command")

        if command in "Mm":
            first = True
            while index < len(tokens) and not is_command(tokens[index]):
                x = read_number()
                y = read_number()
                current = (current[0] + x, current[1] + y) if command == "m" else (x, y)
                if first:
                    start = current
                    commands.append(("M", (current,)))
                    first = False
                else:
                    commands.append(("L", (current,)))
                previous_cubic = None
                previous_quadratic = None
            previous_command = "L" if not first else "M"
            command = "l" if command == "m" else "L"
            continue

        if command in "Ll":
            while index < len(tokens) and not is_command(tokens[index]):
                x = read_number()
                y = read_number()
                current = (current[0] + x, current[1] + y) if command == "l" else (x, y)
                commands.append(("L", (current,)))
                previous_cubic = None
                previous_quadratic = None
                previous_command = "L"
            continue

        if command in "Hh":
            while index < len(tokens) and not is_command(tokens[index]):
                x = read_number()
                current = (current[0] + x, current[1]) if command == "h" else (x, current[1])
                commands.append(("L", (current,)))
                previous_cubic = None
                previous_quadratic = None
                previous_command = "L"
            continue

        if command in "Vv":
            while index < len(tokens) and not is_command(tokens[index]):
                y = read_number()
                current = (current[0], current[1] + y) if command == "v" else (current[0], y)
                commands.append(("L", (current,)))
                previous_cubic = None
                previous_quadratic = None
                previous_command = "L"
            continue

        if command in "Cc":
            while index < len(tokens) and not is_command(tokens[index]):
                x1 = read_number()
                y1 = read_number()
                x2 = read_number()
                y2 = read_number()
                x = read_number()
                y = read_number()
                if command == "c":
                    control1 = (current[0] + x1, current[1] + y1)
                    control2 = (current[0] + x2, current[1] + y2)
                    end = (current[0] + x, current[1] + y)
                else:
                    control1 = (x1, y1)
                    control2 = (x2, y2)
                    end = (x, y)
                commands.append(("C", (control1, control2, end)))
                current = end
                previous_cubic = control2
                previous_quadratic = None
                previous_command = "C"
            continue

        if command in "Ss":
            while index < len(tokens) and not is_command(tokens[index]):
                x2 = read_number()
                y2 = read_number()
                x = read_number()
                y = read_number()
                control1 = reflect(current, previous_cubic if previous_command == "C" else None)
                if command == "s":
                    control2 = (current[0] + x2, current[1] + y2)
                    end = (current[0] + x, current[1] + y)
                else:
                    control2 = (x2, y2)
                    end = (x, y)
                commands.append(("C", (control1, control2, end)))
                current = end
                previous_cubic = control2
                previous_quadratic = None
                previous_command = "C"
            continue

        if command in "Qq":
            while index < len(tokens) and not is_command(tokens[index]):
                x1 = read_number()
                y1 = read_number()
                x = read_number()
                y = read_number()
                if command == "q":
                    control = (current[0] + x1, current[1] + y1)
                    end = (current[0] + x, current[1] + y)
                else:
                    control = (x1, y1)
                    end = (x, y)
                commands.append(("Q", (control, end)))
                current = end
                previous_quadratic = control
                previous_cubic = None
                previous_command = "Q"
            continue

        if command in "Tt":
            while index < len(tokens) and not is_command(tokens[index]):
                x = read_number()
                y = read_number()
                control = reflect(current, previous_quadratic if previous_command == "Q" else None)
                end = (current[0] + x, current[1] + y) if command == "t" else (x, y)
                commands.append(("Q", (control, end)))
                current = end
                previous_quadratic = control
                previous_cubic = None
                previous_command = "Q"
            continue

        if command in "Zz":
            commands.append(("Z", ()))
            current = start
            previous_cubic = None
            previous_quadratic = None
            previous_command = "Z"
            command = ""
            continue

        raise ValueError(f"Unsupported path command: {command}")

    return commands


def commands_from_rect(element: ET.Element) -> list[Command]:
    x = parse_float(element.get("x"))
    y = parse_float(element.get("y"))
    width = parse_float(element.get("width"))
    height = parse_float(element.get("height"))
    rx = parse_float(element.get("rx"))
    ry = parse_float(element.get("ry"))
    if rx == 0 and ry == 0:
        return [
            ("M", ((x, y),)),
            ("L", ((x + width, y),)),
            ("L", ((x + width, y + height),)),
            ("L", ((x, y + height),)),
            ("Z", ()),
        ]

    rx = min(rx or ry, width / 2)
    ry = min(ry or rx, height / 2)
    curve_factor = 0.5522847498307936
    return [
        ("M", ((x + rx, y),)),
        ("L", ((x + width - rx, y),)),
        (
            "C",
            (
                (x + width - rx + rx * curve_factor, y),
                (x + width, y + ry - ry * curve_factor),
                (x + width, y + ry),
            ),
        ),
        ("L", ((x + width, y + height - ry),)),
        (
            "C",
            (
                (x + width, y + height - ry + ry * curve_factor),
                (x + width - rx + rx * curve_factor, y + height),
                (x + width - rx, y + height),
            ),
        ),
        ("L", ((x + rx, y + height),)),
        (
            "C",
            (
                (x + rx - rx * curve_factor, y + height),
                (x, y + height - ry + ry * curve_factor),
                (x, y + height - ry),
            ),
        ),
        ("L", ((x, y + ry),)),
        (
            "C",
            (
                (x, y + ry - ry * curve_factor),
                (x + rx - rx * curve_factor, y),
                (x + rx, y),
            ),
        ),
        ("Z", ()),
    ]


def commands_from_ellipse(element: ET.Element) -> list[Command]:
    cx = parse_float(element.get("cx"))
    cy = parse_float(element.get("cy"))
    rx = parse_float(element.get("rx"), parse_float(element.get("r")))
    ry = parse_float(element.get("ry"), parse_float(element.get("r")))
    curve_factor = 0.5522847498307936
    return [
        ("M", ((cx + rx, cy),)),
        ("C", ((cx + rx, cy + ry * curve_factor), (cx + rx * curve_factor, cy + ry), (cx, cy + ry))),
        ("C", ((cx - rx * curve_factor, cy + ry), (cx - rx, cy + ry * curve_factor), (cx - rx, cy))),
        ("C", ((cx - rx, cy - ry * curve_factor), (cx - rx * curve_factor, cy - ry), (cx, cy - ry))),
        ("C", ((cx + rx * curve_factor, cy - ry), (cx + rx, cy - ry * curve_factor), (cx + rx, cy))),
        ("Z", ()),
    ]


def commands_from_line(element: ET.Element) -> list[Command]:
    x1 = parse_float(element.get("x1"))
    y1 = parse_float(element.get("y1"))
    x2 = parse_float(element.get("x2"))
    y2 = parse_float(element.get("y2"))
    return [("M", ((x1, y1),)), ("L", ((x2, y2),))]


def commands_from_points(element: ET.Element, close: bool) -> list[Command]:
    raw_points = [float(value) for value in NUMBER_RE.findall(element.get("points") or "")]
    if len(raw_points) < 2:
        return []

    pairs = list(zip(raw_points[::2], raw_points[1::2], strict=False))
    commands: list[Command] = [("M", (pairs[0],))]
    for pair in pairs[1:]:
        commands.append(("L", (pair,)))
    if close:
        commands.append(("Z", ()))
    return commands


def commands_for_element(element: ET.Element) -> list[Command]:
    tag = local_name(element.tag)
    if tag == "path":
        return parse_path_commands(element.get("d") or "")
    if tag == "rect":
        return commands_from_rect(element)
    if tag in {"ellipse", "circle"}:
        return commands_from_ellipse(element)
    if tag == "line":
        return commands_from_line(element)
    if tag == "polygon":
        return commands_from_points(element, close=True)
    if tag == "polyline":
        return commands_from_points(element, close=False)
    return []


def transform_commands(commands: list[Command], matrix: Matrix) -> tuple[list[str], BBox]:
    bbox = BBox()
    serialized: list[str] = []
    start: Point | None = None

    for command, points in commands:
        if command == "M":
            transformed = apply_matrix(matrix, points[0])
            start = transformed
            bbox.include(transformed)
            serialized.append(f"M{fmt_pair_path(transformed)}")
        elif command == "L":
            transformed = apply_matrix(matrix, points[0])
            bbox.include(transformed)
            serialized.append(f"L{fmt_pair_path(transformed)}")
        elif command == "Q":
            control = apply_matrix(matrix, points[0])
            end = apply_matrix(matrix, points[1])
            bbox.include_many((control, end))
            serialized.append(f"Q{fmt_pair_path(control)} {fmt_pair_path(end)}")
        elif command == "C":
            control1 = apply_matrix(matrix, points[0])
            control2 = apply_matrix(matrix, points[1])
            end = apply_matrix(matrix, points[2])
            bbox.include_many((control1, control2, end))
            serialized.append(f"C{fmt_pair_path(control1)} {fmt_pair_path(control2)} {fmt_pair_path(end)}")
        elif command == "Z":
            if start is not None:
                bbox.include(start)
            serialized.append("Z")
        else:
            raise ValueError(f"Unsupported command for serialization: {command}")

    return serialized, bbox


def copy_bbox(bbox: BBox) -> BBox:
    return BBox(bbox.min_x, bbox.min_y, bbox.max_x, bbox.max_y)


def merge_bbox(target: BBox, source: BBox) -> None:
    target.min_x = min(target.min_x, source.min_x)
    target.min_y = min(target.min_y, source.min_y)
    target.max_x = max(target.max_x, source.max_x)
    target.max_y = max(target.max_y, source.max_y)


def split_subpaths(commands: list[Command]) -> list[list[Command]]:
    subpaths: list[list[Command]] = []
    current: list[Command] = []
    for command in commands:
        if command[0] == "M" and current:
            subpaths.append(current)
            current = []
        current.append(command)
    if current:
        subpaths.append(current)
    return subpaths


def collect_visible_subpaths(commands: list[Command], matrix: Matrix, viewport: BBox) -> list[tuple[str, BBox]]:
    visible_subpaths: list[tuple[str, BBox]] = []
    for subpath in split_subpaths(commands):
        subpath_parts, subpath_bbox = transform_commands(subpath, matrix)
        if not subpath_bbox.is_valid() or not subpath_bbox.intersects(viewport):
            continue
        visible_subpaths.append((" ".join(subpath_parts), subpath_bbox))
    return visible_subpaths


def chunk_path_data(serialized_subpaths: list[tuple[str, BBox]], max_path_data_chars: int) -> list[tuple[str, BBox]]:
    chunks: list[tuple[str, BBox]] = []
    current_parts: list[str] = []
    current_bbox: BBox | None = None
    current_length = 0

    for subpath_data, subpath_bbox in serialized_subpaths:
        subpath_length = len(subpath_data)
        if subpath_length > max_path_data_chars:
            raise ValueError(
                f"Single SVG subpath serialized to {subpath_length} characters, "
                f"which exceeds the safe VectorDrawable limit of {max_path_data_chars}."
            )

        next_length = subpath_length if not current_parts else current_length + 1 + subpath_length
        if current_parts and next_length > max_path_data_chars:
            if current_bbox is None:
                raise ValueError("Path chunk unexpectedly missing a bounding box")
            chunks.append((" ".join(current_parts), current_bbox))
            current_parts = [subpath_data]
            current_bbox = copy_bbox(subpath_bbox)
            current_length = subpath_length
            continue

        current_parts.append(subpath_data)
        current_length = next_length
        if current_bbox is None:
            current_bbox = copy_bbox(subpath_bbox)
        else:
            merge_bbox(current_bbox, subpath_bbox)

    if current_parts:
        if current_bbox is None:
            raise ValueError("Path chunk unexpectedly missing a bounding box")
        chunks.append((" ".join(current_parts), current_bbox))

    return chunks


def build_rendered_paths(
    element: ET.Element,
    matrix: Matrix,
    viewport: BBox,
    night_fill: str,
    dark_mode: bool,
) -> list[RenderedPath]:
    commands = commands_for_element(element)
    if not commands:
        return []

    style = parse_style(element)
    if style.get("display") == "none" or style.get("visibility") == "hidden":
        return []

    opacity = parse_alpha(style.get("opacity"), 1.0)
    fill = normalize_color(style.get("fill", "#000000"))
    stroke = normalize_color(style.get("stroke"))
    fill_alpha = parse_alpha(style.get("fill-opacity"), 1.0) * opacity if fill else None
    stroke_alpha = parse_alpha(style.get("stroke-opacity"), 1.0) * opacity if stroke else None

    stroke_width = None
    stroke_width_value = style.get("stroke-width")
    if stroke and stroke_width_value is not None:
        stroke_width = parse_float(stroke_width_value) * stroke_scale(matrix)

    if fill == "#000000" and dark_mode:
        fill = night_fill.lower()
    if fill is None and stroke is None:
        return []

    fill_type = "evenOdd" if style.get("fill-rule") == "evenodd" else None
    visible_subpaths = collect_visible_subpaths(commands, matrix, viewport)
    if not visible_subpaths:
        return []

    rendered_paths: list[RenderedPath] = []
    for path_data, bbox in chunk_path_data(visible_subpaths, MAX_PATH_DATA_CHARS):
        rendered_paths.append(
            RenderedPath(
                path_data=path_data,
                bbox=bbox,
                fill=fill,
                fill_alpha=fill_alpha,
                stroke=stroke,
                stroke_alpha=stroke_alpha,
                stroke_width=stroke_width,
                line_cap=to_android_cap(style.get("stroke-linecap")),
                line_join=to_android_join(style.get("stroke-linejoin")),
                fill_type=fill_type,
            )
        )
    return rendered_paths


def page_viewport(page: Page) -> BBox:
    return BBox(0.0, 0.0, page.width, page.height)


def collect_paths(
    node: ET.Element,
    inherited_matrix: Matrix,
    viewport: BBox,
    results: list[RenderedPath],
    night_fill: str,
    dark_mode: bool,
) -> None:
    current_matrix = multiply(inherited_matrix, parse_transform(node.get("transform")))
    if local_name(node.tag) == "g":
        style = parse_style(node)
        if style.get("display") == "none" or style.get("visibility") == "hidden":
            return
        for child in list(node):
            collect_paths(child, current_matrix, viewport, results, night_fill, dark_mode)
        return

    label = node.get(INKSCAPE_LABEL)
    if local_name(node.tag) == "rect" and label in PAGE_LABELS:
        return

    rendered_paths = build_rendered_paths(node, current_matrix, viewport, night_fill, dark_mode)
    for rendered in rendered_paths:
        if rendered.bbox.intersects(viewport):
            results.append(rendered)


def load_pages(background_layer: ET.Element) -> dict[str, Page]:
    pages: dict[str, Page] = {}
    for node in background_layer.iter():
        if local_name(node.tag) != "rect":
            continue
        label = node.get(INKSCAPE_LABEL)
        if label not in PAGE_LABELS:
            continue
        pages[label] = Page(
            label=label,
            x=parse_float(node.get("x")),
            y=parse_float(node.get("y")),
            width=parse_float(node.get("width")),
            height=parse_float(node.get("height")),
        )
    return pages


def find_layers(root: ET.Element) -> tuple[ET.Element, dict[str, ET.Element]]:
    background_layer: ET.Element | None = None
    highlight_layers: dict[str, ET.Element] = {}
    for child in list(root):
        if local_name(child.tag) != "g":
            continue
        label = child.get(INKSCAPE_LABEL)
        if label == "background":
            background_layer = child
        elif label in HIGHLIGHT_LABELS:
            highlight_layers[label] = child

    if background_layer is None:
        raise ValueError("Could not find the background layer in the SVG")

    missing = HIGHLIGHT_LABELS.difference(highlight_layers)
    if missing:
        raise ValueError(f"Missing highlight layers: {sorted(missing)}")

    return background_layer, highlight_layers