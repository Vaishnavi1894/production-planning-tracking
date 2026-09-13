@echo off
title Production Planning & Tracking - Backend (Micronaut)
echo ==========================================================
echo Starting Production Planning & Tracking Backend (Micronaut)
echo ==========================================================
cd /d "%~dp0backend"
set "JAVA_HOME=C:\Program Files\Java\jdk-25"
call gradlew.bat run
pause
