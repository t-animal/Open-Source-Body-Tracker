from __future__ import annotations

import math
from dataclasses import dataclass
from typing import Iterable


Matrix = tuple[float, float, float, float, float, float]
Point = tuple[float, float]
Command = tuple[str, tuple[Point, ...]]

IDENTITY: Matrix = (1.0, 0.0, 0.0, 1.0, 0.0, 0.0)


@dataclass(frozen=True)
class Page:
    label: str
    x: float
    y: float
    width: float
    height: float


@dataclass
class BBox:
    min_x: float = math.inf
    min_y: float = math.inf
    max_x: float = -math.inf
    max_y: float = -math.inf

    def include(self, point: Point) -> None:
        x, y = point
        self.min_x = min(self.min_x, x)
        self.min_y = min(self.min_y, y)
        self.max_x = max(self.max_x, x)
        self.max_y = max(self.max_y, y)

    def include_many(self, points: Iterable[Point]) -> None:
        for point in points:
            self.include(point)

    def is_valid(self) -> bool:
        return self.min_x <= self.max_x and self.min_y <= self.max_y

    def intersects(self, other: "BBox") -> bool:
        return not (
            self.max_x < other.min_x
            or self.min_x > other.max_x
            or self.max_y < other.min_y
            or self.min_y > other.max_y
        )


@dataclass
class RenderedPath:
    path_data: str
    bbox: BBox
    fill: str | None
    fill_alpha: float | None
    stroke: str | None
    stroke_alpha: float | None
    stroke_width: float | None
    line_cap: str | None
    line_join: str | None
    fill_type: str | None