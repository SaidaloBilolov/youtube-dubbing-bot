"""Synthesize a dubbed audio track from a translated SRT, aligned to timestamps.

Default engine is Edge-TTS (free, supports Uzbek voices). Each cue is synthesized
separately and placed on a silent timeline at its start time, so the dubbed audio
stays in sync with the original video.

Usage:
    python tts_synthesize.py --input translated.srt --output dubbed.wav \
        --voice uz-UZ-MadinaNeural

Dependencies: edge-tts, pydub (see requirements.txt). FFmpeg must be installed.
"""
from __future__ import annotations

import argparse
import asyncio
import os
import sys
import tempfile

import srt_utils

try:
    import edge_tts
    from pydub import AudioSegment
except ImportError as exc:  # pragma: no cover - surfaced to the Java layer via stderr
    print(f"Missing TTS dependency: {exc}. Run pip install -r requirements.txt",
          file=sys.stderr)
    sys.exit(4)


async def _synth_cue(text: str, voice: str, out_path: str) -> None:
    communicate = edge_tts.Communicate(text, voice)
    await communicate.save(out_path)


def synthesize(input_path: str, output_path: str, voice: str) -> None:
    cues = srt_utils.parse(input_path)
    if not cues:
        print("No cues parsed from translated subtitle", file=sys.stderr)
        sys.exit(3)

    total_ms = max(c.end_ms for c in cues)
    timeline = AudioSegment.silent(duration=total_ms + 1000)

    with tempfile.TemporaryDirectory() as tmp:
        for cue in cues:
            if not cue.text.strip():
                continue
            seg_path = os.path.join(tmp, f"seg_{cue.index}.mp3")
            try:
                asyncio.run(_synth_cue(cue.text, voice, seg_path))
            except Exception as exc:  # noqa: BLE001 - log and skip a single bad cue
                print(f"TTS failed for cue {cue.index}: {exc}", file=sys.stderr)
                continue
            segment = AudioSegment.from_file(seg_path)
            timeline = timeline.overlay(segment, position=cue.start_ms)

    timeline.export(output_path, format="wav")
    print(f"Synthesized {len(cues)} cues -> {output_path}")


def main() -> None:
    parser = argparse.ArgumentParser(description="Synthesize dubbed audio from SRT")
    parser.add_argument("--input", required=True)
    parser.add_argument("--output", required=True)
    parser.add_argument("--voice", default="uz-UZ-MadinaNeural")
    args = parser.parse_args()
    synthesize(args.input, args.output, args.voice)


if __name__ == "__main__":
    main()
