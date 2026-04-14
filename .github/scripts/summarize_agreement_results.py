#!/usr/bin/env python3
"""
Reads agreements-shard-*.json from downloaded artifacts and appends a GitHub Actions job summary.
"""
from __future__ import annotations

import json
import os
import pathlib
import sys


def iter_result_files(root: pathlib.Path):
    if not root.is_dir():
        return
    yield from sorted(root.rglob("agreements-shard-*.json"))


def parse_file(path: pathlib.Path):
    run_meta: dict = {}
    agreements: list[dict] = []
    failures: list[dict] = []
    with path.open(encoding="utf-8") as f:
        data = json.load(f)
    if not isinstance(data, list):
        return run_meta, agreements, failures
    for item in data:
        if not isinstance(item, dict):
            continue
        if item.get("type") == "run" and isinstance(item.get("data"), dict):
            run_meta.update(item["data"])
        elif item.get("type") == "failure" and isinstance(item.get("data"), dict):
            failures.append(item["data"])
        elif item.get("status") == "SUCCESS":
            agreements.append(item)
    return run_meta, agreements, failures


def esc(s: object) -> str:
    if s is None:
        return ""
    return str(s).replace("|", "\\|").replace("\n", " ")


def main() -> int:
    root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else "downloaded")
    out_path = os.environ.get("GITHUB_STEP_SUMMARY")
    lines: list[str] = []

    lines.append("## Agreement E2E — combined results")
    lines.append("")

    all_agreements: list[dict] = []
    all_failures: list[dict] = []
    run_meta: dict = {}

    files = list(iter_result_files(root))
    if not files:
        lines.append("_No result JSON files were found (tests may have failed before writing results)._")
    for path in files:
        rm, ag, fl = parse_file(path)
        run_meta.update(rm)
        all_agreements.extend(ag)
        all_failures.extend(fl)

    all_agreements.sort(key=lambda r: (r.get("agreementIndex", 0), r.get("agreementOrdinal", 0)))

    if run_meta.get("eapiBaseUrl"):
        lines.append(f"- **eAPI:** `{esc(run_meta.get('eapiBaseUrl'))}`")
    if run_meta.get("environmentProfile"):
        lines.append(f"- **Profile:** `{esc(run_meta.get('environmentProfile'))}`")
    if run_meta.get("clubNumber"):
        lines.append(f"- **Club:** `{esc(run_meta.get('clubNumber'))}`")
    if run_meta.get("paymentPlan"):
        lines.append(f"- **Plan:** `{esc(run_meta.get('paymentPlan'))}`")
    if run_meta.get("totalAgreements") is not None:
        lines.append(f"- **Total requested:** `{esc(run_meta.get('totalAgreements'))}`")
    srv = run_meta.get("githubServerUrl") or ""
    repo = run_meta.get("githubRepository") or ""
    rid = run_meta.get("githubRunId") or ""
    if srv and repo and rid:
        url = f"{str(srv).rstrip('/')}/{repo}/actions/runs/{rid}"
        lines.append(f"- **Workflow run:** [{url}]({url})")
    lines.append("")

    if all_failures:
        lines.append("### Failures")
        for f in all_failures:
            lines.append(f"- **FAILED:** {esc(f.get('message'))}")
        lines.append("")

    if all_agreements:
        lines.append("### Agreements created")
        lines.append("")
        lines.append("| Slot | Agreement # | Member ID | Barcode | Name |")
        lines.append("|---:|---|---|---|---|")
        for r in all_agreements:
            lines.append(
                "| {slot} | `{an}` | `{mid}` | `{bc}` | {nm} |".format(
                    slot=esc(r.get("agreementOrdinal")),
                    an=esc(r.get("agreementNumber")),
                    mid=esc(r.get("memberId")),
                    bc=esc(r.get("barcode")),
                    nm=esc(r.get("memberName")),
                )
            )
        lines.append("")
        lines.append(f"**Count:** {len(all_agreements)} agreement(s) with SUCCESS in result files.")
        lines.append("")

    text = "\n".join(lines) + "\n"
    if out_path:
        with open(out_path, "a", encoding="utf-8") as out:
            out.write(text)
    else:
        print(text)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
