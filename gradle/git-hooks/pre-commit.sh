#!/bin/bash

echo "🔍 Pre-commit checks..."

# Проверка jOOQ
if [ ! -d "build/generated-src/jooq/main" ] || [ -z "$(ls -A build/generated-src/jooq/main 2>/dev/null)" ]; then
    echo "❌ jOOQ not generated!"
    echo "💡 Run: ./gradlew setup"
    exit 1
fi

# Проверка Docker
if ! docker ps | grep -q "user_service_postgres"; then
    echo "⚠️  PostgreSQL not running"
    read -p "Start Docker? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker-compose up -d
        sleep 3
    fi
fi

echo "✅ Pre-commit checks passed"
exit 0