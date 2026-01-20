import os
import csv
import shutil
import subprocess
import pathlib
import collections
import zipfile
import socket

SCRIPT_DIR = pathlib.Path(__file__).parent
EXERCISE_DIR = SCRIPT_DIR / 'ex5'

INPUT_DIR = SCRIPT_DIR / 'tests'
EXPECTED_OUTPUT_DIR = SCRIPT_DIR / 'expected_output'
SELF_CHECK_OUTPUT_DIR = SCRIPT_DIR / 'self_check_output'
MAKEFILE_PATH = EXERCISE_DIR / 'Makefile'
IDS_FILE_PATH = SCRIPT_DIR / 'ids.txt' 
EXECUTABLE_NAME = "COMPILER"

HOST_NAME = "nova"

def remove_if_exists(path_obj):
    try:
        if path_obj.exists():
            if path_obj.is_dir():
                shutil.rmtree(str(path_obj), ignore_errors=True)
            else:
                os.remove(str(path_obj))
    except OSError as e:
        print(f"Error removing {path_obj.name}: {e}")


def ensure_exists(path_obj, is_file=True):
    if not path_obj.exists():
        if is_file:
            print(f"FATAL ERROR: Required file not found: {path_obj.name}")
            raise RuntimeError("Missing required file/directory.")
        else:
            print(f"FATAL ERROR: Required directory not found: {path_obj.name}")
            raise RuntimeError("Missing required file/directory.")


def unzip_single_archive():
    zip_files = [f for f in SCRIPT_DIR.glob("*.zip") if f.stem.isdigit()]
    if len(zip_files) == 0:
        raise RuntimeError("No zip archive found in the directory.")
    elif len(zip_files) > 1:
        raise RuntimeError("More than one zip archive found. Ensure only one archive is present.")
    zip_file = zip_files[0]
    print(f"Unzipping archive: {zip_file.name}")
    with zipfile.ZipFile(zip_file, 'r') as zip_ref:
        zip_ref.extractall(SCRIPT_DIR)
        print("Unzip complete.")

 
def setup_self_check():
    print("--- 1. Validating project structure ---")
    ensure_exists(MAKEFILE_PATH)
    ensure_exists(IDS_FILE_PATH)
    ensure_exists(INPUT_DIR, is_file=False)
    ensure_exists(EXPECTED_OUTPUT_DIR, is_file=False)
    print("Project structure OK.")
    print("--- 2. Running 'make' ---")
    process = subprocess.Popen(
        ["make"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        universal_newlines=True,
        cwd=str(EXERCISE_DIR) 
    ) 
    stdout, stderr = process.communicate()
    if process.returncode != 0:
        print("--- MAKE FAILED! ---")
        print("--- STDOUT: ---")
        print(stdout)
        print("--- STDERR: ---")
        print(stderr)
        print("--------------------")
        raise RuntimeError("Make command failed. See output above.")
    
    print("--- 'make' successful ---")
    
    executable_file = EXERCISE_DIR / EXECUTABLE_NAME
    ensure_exists(executable_file)
    
    remove_if_exists(SELF_CHECK_OUTPUT_DIR)
    os.makedirs(str(SELF_CHECK_OUTPUT_DIR), exist_ok=True)
    
    return SELF_CHECK_OUTPUT_DIR, executable_file


def run_test(test_path, output_dir, executable_file):
    output_path = output_dir / test_path.name
    
    try:
        process = subprocess.run(
            ["java", "-jar", str(executable_file.resolve()), str(test_path.resolve()), str(output_path)],
            stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=10
        )
    
    except subprocess.TimeoutExpired:
        print("FAILED (Timeout)")
        raise RuntimeError("Timed out.")

    if not output_path.exists():
        print(f"FAILED (Output file missing)")
        raise RuntimeError("Output file was not created.")

    expected_output_path = EXPECTED_OUTPUT_DIR / f"{test_path.stem}_Expected_Output.txt"
    if not expected_output_path.exists():
        print(f"FAILED (Expected output file missing)")
        raise RuntimeError(f"Expected output file not found")
       
        
    with open(str(output_path), 'r') as f1, open(str(expected_output_path), 'r') as f2:
        content1 = f1.read()
        
        if content1 != "Register Allocation Failed":
            try:
                spim_result = subprocess.run(
                    ["spim", "-file", str(output_path)],
                    stdout=subprocess.PIPE,
                    stderr=subprocess.PIPE,
                    text=True,
                    timeout=10
                )
                if spim_result.returncode != 0:
                    print(f"FAILED (SPIM runtime error)")
                    raise RuntimeError("SPIM runtime error.")

                content1 = spim_result.stdout
            
            except subprocess.TimeoutExpired:
                print("FAILED (SPIM Timeout)")
                raise RuntimeError("SPIM Timed out.")        
        
        content2 = f2.read()
        if content1 == content2:
            print("OK")
        else:
            print("FAILED")
            raise RuntimeError("Test failed") 


def run_all_tests(output_dir, executable_file):
    print("--- 3. Running all tests ---")
    failed = []

    for test_path in sorted(INPUT_DIR.glob("*.txt")):
        print(f"Running {test_path.stem} ... ", end="")
        try:
            run_test(test_path, output_dir, executable_file)
        except RuntimeError as e:
            failed.append((test_path.stem, str(e)))

    print("--- 4. Test summary ---")
    total = len(list(INPUT_DIR.glob("*.txt")))
    passed = total - len(failed)
    print(f"Total tests: {total}")
    print(f"Passed: {passed}")
    print(f"Failed: {len(failed)}")
    if failed:
        print("Failed test details:")
        for name, reason in failed:
            print(f"  {name}: {reason}")
        raise RuntimeError(f"{len(failed)} test(s) failed.")

    print("All tests passed")


def main():
    print("--- Starting Student Self-Check ---")
    
    try:
        hostname = socket.gethostname()
        if hostname != HOST_NAME:
            print(f"FATAL ERROR: This script must be run on 'nova'.")
            raise RuntimeError("Invalid host machine.")
    
        unzip_single_archive()
        output_dir, executable_file = setup_self_check()
        run_all_tests(output_dir, executable_file)

    except Exception as e:
        print("--- SELF-CHECK FAILED ---")
        print(f"ERROR: {e}")
        return
    
    print("--- Self-Check Finished ---")
    print("OK to submit :)")
    
if __name__ == "__main__":
    main()