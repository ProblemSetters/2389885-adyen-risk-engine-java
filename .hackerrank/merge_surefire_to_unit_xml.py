#!/usr/bin/env python3
from __future__ import annotations

import glob
import shutil
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def as_int(value: str | None) -> int:
    try:
        return int(value or "0")
    except ValueError:
        return 0


def as_float(value: str | None) -> float:
    try:
        return float(value or "0")
    except ValueError:
        return 0.0


def empty_report(message: str) -> ET.ElementTree:
    suites = ET.Element("testsuites", {"name": "maven", "tests": "1", "failures": "0", "errors": "1", "skipped": "0"})
    suite = ET.SubElement(suites, "testsuite", {"name": "report-generation", "tests": "1", "failures": "0", "errors": "1", "skipped": "0"})
    case = ET.SubElement(suite, "testcase", {"classname": "report-generation", "name": "surefire-report"})
    ET.SubElement(case, "error", {"message": message}).text = message
    return ET.ElementTree(suites)


def main() -> int:
    output = Path("unit.xml")
    reports = sorted(Path(".").glob("target/surefire-reports/TEST-*.xml"))
    reports += sorted(Path(".").glob("target/failsafe-reports/TEST-*.xml"))
    valid = []
    for report in reports:
        try:
            tree = ET.parse(report)
        except ET.ParseError:
            continue
        root = tree.getroot()
        if root.tag in {"testsuite", "testsuites"}:
            valid.append((report, root))

    if not valid:
        tree = empty_report("No Maven Surefire/Failsafe XML reports were generated.")
        tree.write(output, encoding="utf-8", xml_declaration=True)
        return 1

    if len(valid) == 1 and valid[0][1].tag == "testsuite":
        shutil.copyfile(valid[0][0], output)
        return 0

    suites = ET.Element("testsuites", {"name": "maven"})
    totals = {"tests": 0, "failures": 0, "errors": 0, "skipped": 0}
    total_time = 0.0
    for _, root in valid:
        children = [root] if root.tag == "testsuite" else list(root.findall("testsuite"))
        for suite in children:
            totals["tests"] += as_int(suite.get("tests"))
            totals["failures"] += as_int(suite.get("failures"))
            totals["errors"] += as_int(suite.get("errors"))
            totals["skipped"] += as_int(suite.get("skipped"))
            total_time += as_float(suite.get("time"))
            suites.append(suite)
    for key, value in totals.items():
        suites.set(key, str(value))
    suites.set("time", f"{total_time:.3f}")
    ET.ElementTree(suites).write(output, encoding="utf-8", xml_declaration=True)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
