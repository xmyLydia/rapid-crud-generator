#!/bin/bash


# 切换到脚本所在目录（项目根目录）
cd "$(dirname "$0")"

echo "📦 Building Vue frontend..."
cd frontend || exit 1
npm run build || exit 1

echo "🧹 Cleaning old static files in Spring Boot..."
rm -rf ../backend/src/main/resources/static/*

echo "📂 Copying built frontend files to Spring Boot static/..."
cp -r dist/* ../backend/src/main/resources/static/

echo "✅ Done. Frontend is now integrated into Spring Boot!"
