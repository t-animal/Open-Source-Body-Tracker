from __future__ import annotations

import re


SVG_NS = "http://www.w3.org/2000/svg"
INKSCAPE_NS = "http://www.inkscape.org/namespaces/inkscape"
ANDROID_NS = "http://schemas.android.com/apk/res/android"

INKSCAPE_LABEL = f"{{{INKSCAPE_NS}}}label"
NUMBER_RE = re.compile(r"[-+]?(?:\d*\.\d+|\d+\.?\d*)(?:[eE][-+]?\d+)?")
COMMAND_RE = re.compile(r"[MmLlHhVvCcSsQqTtZz]|[-+]?(?:\d*\.\d+|\d+\.?\d*)(?:[eE][-+]?\d+)?")
TRANSFORM_RE = re.compile(r"([A-Za-z]+)\s*\(([^)]*)\)")

PAGE_LABELS = {
    "female-circumference-front",
    "female-circumference-back",
    "male-circumference-front",
    "male-circumference-back",
    "female-skinfold-front",
    "female-skinfold-back",
    "male-skinfold-front",
    "male-skinfold-back",
}

HIGHLIGHT_LABELS = {
    "circumference_neck",
    "circumference_chest",
    "circumference_waist",
    "circumference_abdomen",
    "circumference_hip",
    "skinfold_chest",
    "skinfold_abdomen",
    "skinfold_thigh",
    "skinfold_triceps",
    "skinfold_suprailiac",
}

METRIC_SPECS = [
    ("weight", "circumference", None),
    ("body_fat", "circumference", None),
    ("neck_circumference", "circumference", "circumference_neck"),
    ("chest_circumference", "circumference", "circumference_chest"),
    ("waist_circumference", "circumference", "circumference_waist"),
    ("abdomen_circumference", "circumference", "circumference_abdomen"),
    ("hip_circumference", "circumference", "circumference_hip"),
    ("chest_skinfold", "skinfold", "skinfold_chest"),
    ("abdomen_skinfold", "skinfold", "skinfold_abdomen"),
    ("thigh_skinfold", "skinfold", "skinfold_thigh"),
    ("triceps_skinfold", "skinfold", "skinfold_triceps"),
    ("suprailiac_skinfold", "skinfold", "skinfold_suprailiac"),
]

COLOR_KEYWORDS = {
    "black": "#000000",
    "white": "#ffffff",
    "none": None,
    "transparent": None,
}

MAX_PATH_DATA_CHARS = 30000