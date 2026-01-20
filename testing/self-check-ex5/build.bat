@echo off
REM Complete L Compiler Build Script for Windows
REM Exercise 5 - All Three Persons' Work

echo ====================================
echo  Exercise 5 - L Compiler Build
echo ====================================
echo.

REM Step 0: Remove old compiler
echo [0] Removing old COMPILER.jar...
if exist COMPILER del COMPILER
echo.

REM Step 1: Clean old class files
echo [1] Cleaning old .class files and generated files...
if exist bin\*.class del /Q bin\*.class
if exist bin\ast\*.class del /Q bin\ast\*.class
if exist bin\ir\*.class del /Q bin\ir\*.class
if exist bin\regalloc\*.class del /Q bin\regalloc\*.class
if exist bin\mips\*.class del /Q bin\mips\*.class
if exist bin\types\*.class del /Q bin\types\*.class
if exist bin\symboltable\*.class del /Q bin\symboltable\*.class
if exist bin\cfg\*.class del /Q bin\cfg\*.class
if exist bin\temp\*.class del /Q bin\temp\*.class
if exist src\Lexer.java del src\Lexer.java
if exist src\Parser.java del src\Parser.java
if exist src\TokenNames.java del src\TokenNames.java
echo.

REM Step 2: Generate Lexer with JFlex
echo [2] Generating Lexer.java with JFlex...
jflex -q -d src jflex\LEX_FILE.lex
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: JFlex failed!
    pause
    exit /b 1
)
echo.

REM Step 3: Generate Parser with CUP
echo [3] Generating Parser.java and TokenNames.java with CUP...
java -jar external_jars\java-cup-11b.jar -nowarn -parser Parser -symbols TokenNames -destdir src cup\CUP_FILE.cup
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: CUP failed!
    pause
    exit /b 1
)
echo.

REM Step 4: Compile all Java files
echo [4] Compiling Java files...
if not exist bin mkdir bin
javac -cp external_jars\java-cup-11b-runtime.jar -d bin src\*.java src\ast\*.java src\ir\*.java src\regalloc\*.java src\mips\*.java src\types\*.java src\symboltable\*.java src\cfg\*.java src\temp\*.java
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)
echo.

REM Step 5: Create JAR file
echo [5] Creating COMPILER.jar...
jar cfm COMPILER manifest\MANIFEST.MF -C bin .
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: JAR creation failed!
    pause
    exit /b 1
)
echo.

echo ====================================
echo  Build Complete!
echo ====================================
echo.
echo COMPILER.jar created successfully.
echo.
echo To test: java -jar COMPILER input\Input.txt output\Output.s
echo.
pause
