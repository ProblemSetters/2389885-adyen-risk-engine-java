#!/usr/bin/env bash
set +e
if [ "$#" -eq 0 ]; then
  echo "Usage: $0 '<test command>'" >&2
  exit 2
fi
rm -f unit.xml
bash -lc "$*"
status=$?
python3 .hackerrank/merge_surefire_to_unit_xml.py
merge_status=$?
if [ "$status" -eq 0 ] && [ "$merge_status" -ne 0 ]; then
  exit "$merge_status"
fi
exit "$status"
