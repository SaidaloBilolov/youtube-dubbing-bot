"""Translate an SRT subtitle file with Google Gemini, preserving timestamps.

The script never changes cue timings; it only replaces the text of each cue with
its translation. Cues are batched into a single prompt (indexed) so the model
keeps segment boundaries aligned with the original timing.

Usage:
    python translate_gemini.py --input in.srt --output out.srt --target-lang uz \
        --model gemini-2.5-flash

The API key is read from the GEMINI_API_KEY environment variable.
"""
from __future__ import annotations

import argparse
import json
import os
import sys
import urllib.request
import urllib.error

import srt_utils

GEMINI_ENDPOINT = (
    "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent"
)
BATCH_SIZE = 80


def _build_prompt(target_lang: str, segments: list[tuple[int, str]]) -> str:
    numbered = "\n".join(f"{idx}\t{text}" for idx, text in segments)
    return (
        f"You are a professional subtitle translator. Translate each numbered line "
        f"into {target_lang}. Keep the SAME number of lines and the SAME leading "
        f"index and tab. Do not merge or split lines. Return ONLY the translated "
        f"lines, nothing else.\n\n{numbered}"
    )


def _call_gemini(model: str, api_key: str, prompt: str) -> str:
    url = GEMINI_ENDPOINT.format(model=model) + f"?key={api_key}"
    payload = {
        "contents": [{"parts": [{"text": prompt}]}],
        "generationConfig": {"temperature": 0.2},
    }
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(
        url, data=data, headers={"Content-Type": "application/json"}, method="POST"
    )
    with urllib.request.urlopen(req, timeout=120) as resp:
        body = json.loads(resp.read().decode("utf-8"))
    return body["candidates"][0]["content"]["parts"][0]["text"]


def _parse_response(text: str) -> dict[int, str]:
    result: dict[int, str] = {}
    for line in text.splitlines():
        if "\t" not in line:
            continue
        idx_str, _, translated = line.partition("\t")
        idx_str = idx_str.strip()
        if idx_str.isdigit():
            result[int(idx_str)] = translated.strip()
    return result


def translate(input_path: str, output_path: str, target_lang: str, model: str) -> None:
    api_key = os.environ.get("GEMINI_API_KEY")
    if not api_key:
        print("GEMINI_API_KEY is not set", file=sys.stderr)
        sys.exit(2)

    cues = srt_utils.parse(input_path)
    if not cues:
        print("No cues parsed from input subtitle", file=sys.stderr)
        sys.exit(3)

    for start in range(0, len(cues), BATCH_SIZE):
        batch = cues[start:start + BATCH_SIZE]
        segments = [(c.index, c.text) for c in batch]
        prompt = _build_prompt(target_lang, segments)
        try:
            raw = _call_gemini(model, api_key, prompt)
        except urllib.error.HTTPError as exc:
            print(f"Gemini HTTP error: {exc.code} {exc.read().decode('utf-8', 'ignore')}",
                  file=sys.stderr)
            raise
        translations = _parse_response(raw)
        for cue in batch:
            if cue.index in translations:
                cue.text = translations[cue.index]

    srt_utils.serialize(cues, output_path)
    print(f"Translated {len(cues)} cues -> {output_path}")


def main() -> None:
    parser = argparse.ArgumentParser(description="Translate SRT with Gemini")
    parser.add_argument("--input", required=True)
    parser.add_argument("--output", required=True)
    parser.add_argument("--target-lang", required=True)
    parser.add_argument("--model", default="gemini-2.5-flash")
    args = parser.parse_args()
    translate(args.input, args.output, args.target_lang, args.model)


if __name__ == "__main__":
    main()
