@rem Utility script to call patchxml with the given arguments
@echo off
IF DEFINED JAVA_HOME (
%JAVA_HOME%\bin\java.exe -cp "%~dp0\lib\diffxml.jar" org.diffxml.patchxml.PatchXML %*
) ELSE (
java -cp "%~dp0\lib\diffxml.jar" org.diffxml.patchxml.PatchXML %*
)
