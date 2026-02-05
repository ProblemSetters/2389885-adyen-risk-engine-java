#!/bin/bash

# Script to merge multiple JUnit XML test reports into a single XML file

REPORTS_DIR="target/surefire-reports"
OUTPUT_FILE="reports/test_result.xml"

# Create reports directory if it doesn't exist
mkdir -p reports

# Find all TEST-*.xml files (must exist before we run the loop)
if ! find "$REPORTS_DIR" -name "TEST-*.xml" -print 2>/dev/null | grep -q .; then
    echo "No test report files found in $REPORTS_DIR"
    exit 1
fi

# Start the merged XML file with testsuites root element
cat > "$OUTPUT_FILE" << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<testsuites>
EOF

# Variables to aggregate totals
TOTAL_TESTS=0
TOTAL_FAILURES=0
TOTAL_ERRORS=0
TOTAL_SKIPPED=0
TOTAL_TIME=0.0

# Process each XML file (portable: works on macOS and Linux)
while IFS= read -r XML_FILE; do
    if [ -f "$XML_FILE" ]; then
        # Extract the testsuite element (everything between <testsuite and </testsuite>)
        # Remove XML declaration and any outer wrapper if present
        TESTSUITE_CONTENT=$(sed -n '/<testsuite/,/<\/testsuite>/p' "$XML_FILE")
        
        if [ -n "$TESTSUITE_CONTENT" ]; then
            # Add the testsuite content to the merged file
            echo "$TESTSUITE_CONTENT" >> "$OUTPUT_FILE"
            
            # Extract numeric values using grep and sed
            TESTS=$(grep -o 'tests="[0-9]*"' "$XML_FILE" | grep -o '[0-9]*' | head -1 || echo "0")
            FAILURES=$(grep -o 'failures="[0-9]*"' "$XML_FILE" | grep -o '[0-9]*' | head -1 || echo "0")
            ERRORS=$(grep -o 'errors="[0-9]*"' "$XML_FILE" | grep -o '[0-9]*' | head -1 || echo "0")
            SKIPPED=$(grep -o 'skipped="[0-9]*"' "$XML_FILE" | grep -o '[0-9]*' | head -1 || echo "0")
            TIME_STR=$(grep -o 'time="[0-9.]*"' "$XML_FILE" | grep -o '[0-9.]*' | head -1 || echo "0")
            
            # Add to totals (using awk for floating point arithmetic)
            TOTAL_TESTS=$((TOTAL_TESTS + TESTS))
            TOTAL_FAILURES=$((TOTAL_FAILURES + FAILURES))
            TOTAL_ERRORS=$((TOTAL_ERRORS + ERRORS))
            TOTAL_SKIPPED=$((TOTAL_SKIPPED + SKIPPED))
            TOTAL_TIME=$(awk "BEGIN {print $TOTAL_TIME + $TIME_STR}")
        fi
    fi
done < <(find "$REPORTS_DIR" -name "TEST-*.xml" 2>/dev/null | sort)

# Close testsuites element
echo '</testsuites>' >> "$OUTPUT_FILE"

echo "Merged test reports into $OUTPUT_FILE"
echo "Total: tests=$TOTAL_TESTS, failures=$TOTAL_FAILURES, errors=$TOTAL_ERRORS, skipped=$TOTAL_SKIPPED, time=$TOTAL_TIME"
