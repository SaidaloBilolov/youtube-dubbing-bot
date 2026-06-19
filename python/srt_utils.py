"""Minimal SRT parsing/serialization utilities (no external deps).

A subtitle cue keeps its index, start/end timestamps and text so that downstream
steps (translation, TTS) can preserve the original timing.
"""
from __future__ import annotations

import re
from dataclasses import dataclass

_TIME_RE = re.compile(
    r"(\d{2}):(\d{2}):(\d{2})[,.](\d{3})\s*-->\s*(\d{2}):(\d{2}):(\d{2})[,.](\d{3})"
)


@dataclass
class Cue:
    index: int
    start_ms: int
    end_ms: int
    text: str


def _to_ms(h: str, m: str, s: str, ms: str) -> int:
    return ((int(h) * 60 + int(m)) * 60 + int(s)) * 1000 + int(ms)


def _fmt_ts(ms: int) -> str:
    h, ms = divmod(ms, 3_600_000)
    m, ms = divmod(ms, 60_000)
    s, ms = divmod(ms, 1000)
    return f"{h:02d}:{m:02d}:{s:02d},{ms:03d}"


def parse(path: str) -> list[Cue]:
    with open(path, "r", encoding="utf-8-sig") as fh:
        content = fh.read()

    cues: list[Cue] = []
    blocks = re.split(r"\n\s*\n", content.strip())
    for block in blocks:
        lines = [ln for ln in block.splitlines() if ln.strip() != ""]
        if not lines:
            continue
        time_line_idx = 0
        if lines[0].strip().isdigit():
            time_line_idx = 1
        if time_line_idx >= len(lines):
            continue
        match = _TIME_RE.search(lines[time_line_idx])
        if not match:
            continue
        start_ms = _to_ms(*match.group(1, 2, 3, 4))
        end_ms = _to_ms(*match.group(5, 6, 7, 8))
        text = " ".join(lines[time_line_idx + 1:]).strip()
        cues.append(Cue(index=len(cues) + 1, start_ms=start_ms, end_ms=end_ms, text=text))
    return cues


def serialize(cues: list[Cue], path: str) -> None:
    with open(path, "w", encoding="utf-8") as fh:
        for i, cue in enumerate(cues, start=1):
            fh.write(f"{i}\n")
            fh.write(f"{_fmt_ts(cue.start_ms)} --> {_fmt_ts(cue.end_ms)}\n")
            fh.write(f"{cue.text}\n\n")
