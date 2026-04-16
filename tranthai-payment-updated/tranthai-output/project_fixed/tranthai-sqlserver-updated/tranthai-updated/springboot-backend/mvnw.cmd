@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set MAVEN_OPTS=-Xmx512m

where mvn >nul 2>nul
if %ERRORLEVEL% == 0 (
    mvn %*
    goto end
)

echo ERROR: Maven (mvn) khong tim thay trong PATH.
echo Vui long cai Maven hoac them vao PATH.
echo Huong dan: https://maven.apache.org/install.html
exit /b 1

:end
endlocal
