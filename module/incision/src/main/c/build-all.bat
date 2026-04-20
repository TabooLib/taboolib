@echo off
setlocal

set "CDIR=%~dp0"
set "SRC=%CDIR%incision_jvmti.c"
set "RES=%CDIR%..\resources\native"

:: JNI headers - use JAVA_HOME if set, otherwise try D:\Java\jdk-21
if defined JAVA_HOME (
    set "JNI=%JAVA_HOME%\include"
) else (
    set "JNI=D:\Java\jdk-21\include"
)

if not exist "%JNI%\jni.h" (
    echo ERROR: jni.h not found at %JNI%
    echo Set JAVA_HOME or edit this script
    exit /b 1
)

echo === Building Incision JVMTI native for all platforms ===
echo Using JNI headers: %JNI%
echo.

echo [1/6] Windows x64 ...
zig cc -shared -O2 -I"%JNI%" -I"%JNI%\win32" -target x86_64-windows-gnu "%SRC%" -o "%RES%\windows\x64\incision-jvmti.dll"
if %errorlevel% neq 0 ( echo   FAILED ) else ( echo   OK )

echo [2/6] Windows arm64 ...
zig cc -shared -O2 -I"%JNI%" -I"%JNI%\win32" -target aarch64-windows-gnu "%SRC%" -o "%RES%\windows\arm64\incision-jvmti.dll"
if %errorlevel% neq 0 ( echo   FAILED ) else ( echo   OK )

echo [3/6] Linux x64 ...
zig cc -shared -fPIC -O2 -I"%JNI%" -I"%CDIR%include\linux" -target x86_64-linux-gnu "%SRC%" -o "%RES%\linux\x64\libincision-jvmti.so"
if %errorlevel% neq 0 ( echo   FAILED ) else ( echo   OK )

echo [4/6] Linux arm64 ...
zig cc -shared -fPIC -O2 -I"%JNI%" -I"%CDIR%include\linux" -target aarch64-linux-gnu "%SRC%" -o "%RES%\linux\arm64\libincision-jvmti.so"
if %errorlevel% neq 0 ( echo   FAILED ) else ( echo   OK )

echo [5/6] macOS x64 ...
zig cc -shared -fPIC -O2 -I"%JNI%" -I"%CDIR%include\darwin" -target x86_64-macos-none "%SRC%" -o "%RES%\macos\x64\libincision-jvmti.dylib"
if %errorlevel% neq 0 ( echo   FAILED ) else ( echo   OK )

echo [6/6] macOS arm64 ...
zig cc -shared -fPIC -O2 -I"%JNI%" -I"%CDIR%include\darwin" -target aarch64-macos-none "%SRC%" -o "%RES%\macos\arm64\libincision-jvmti.dylib"
if %errorlevel% neq 0 ( echo   FAILED ) else ( echo   OK )

echo.
echo === Done ===
endlocal
