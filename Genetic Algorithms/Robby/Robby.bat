@echo off

rem path %path%;%~dp0
java -jar %~dp0\..\dist\%~n0.jar %*
if not errorlevel 1	goto End

echo.
echo     [ OK - stiskni cokoli ]
pause >NUL

:End
