#!/bin/bash

CSV_FILE="target/site/jacoco/jacoco.csv"

if [ ! -f "$CSV_FILE" ]; then
    echo "❌ Brak pliku jacoco.csv!"
    echo "Uruchom najpierw: ./mvnw clean test"
    exit 1
fi

echo "========================================"
echo "  RAPORT POKRYCIA KODU - PAKIET SERVICE"
echo "========================================"
echo ""
SERVICE_DATA=$(grep "org.example.lab01.service" "$CSV_FILE")

if [ -z "$SERVICE_DATA" ]; then
    echo "❌ Brak danych dla pakietu org.example.lab01.service"
    exit 1
fi
TOTAL_MISSED=0
TOTAL_COVERED=0

echo "Szczegóły pokrycia dla każdej klasy:"
echo "----------------------------------------"
while IFS=',' read -r group package class inst_missed inst_covered branch_missed branch_covered line_missed line_covered rest; do
    if [[ "$package" == "org.example.lab01.service" ]]; then
        TOTAL=$((line_missed + line_covered))
        COVERAGE=$(awk "BEGIN {printf \"%.2f\", ($line_covered / $TOTAL) * 100}")
        
        printf "%-20s: %3d/%3d linii = %6s%%\n" "$class" "$line_covered" "$TOTAL" "$COVERAGE"
        
        TOTAL_MISSED=$((TOTAL_MISSED + line_missed))
        TOTAL_COVERED=$((TOTAL_COVERED + line_covered))
    fi
done < <(tail -n +2 "$CSV_FILE")

echo ""
echo "========================================"
GRAND_TOTAL=$((TOTAL_MISSED + TOTAL_COVERED))
TOTAL_COVERAGE=$(awk "BEGIN {printf \"%.2f\", ($TOTAL_COVERED / $GRAND_TOTAL) * 100}")

echo "CAŁKOWITE POKRYCIE: $TOTAL_COVERED/$GRAND_TOTAL linii = $TOTAL_COVERAGE%"
echo ""
REQUIRED=70
if (( $(echo "$TOTAL_COVERAGE >= $REQUIRED" | bc -l) )); then
    echo "✅ SUKCES! Pokrycie $TOTAL_COVERAGE% >= $REQUIRED%"
    echo "========================================"
    exit 0
else
    echo "❌ BŁĄD! Pokrycie $TOTAL_COVERAGE% < $REQUIRED%"
    echo "========================================"
    exit 1
fi
