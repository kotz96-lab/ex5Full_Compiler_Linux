#!/bin/bash

# VM Setup Script for Ex5 Compiler Testing
# Run this first in your VM

echo "=== Ex5 Compiler VM Setup ==="
echo ""

# Install required packages
echo "[1/3] Installing Java, SPIM, and build tools..."
sudo apt update -qq
sudo apt install -y openjdk-11-jdk spim make unzip

echo ""
echo "[2/3] Extracting project files..."
tar -xzf ex5_for_vm.tar.gz

echo ""
echo "[3/3] Setting up..."
cd ex5
chmod +x self-check-ex5.zip

echo ""
echo "=== Setup Complete! ==="
echo ""
echo "Next steps:"
echo "1. Read VM_INSTRUCTIONS.md for detailed guide"
echo "2. cd ex5"
echo "3. make compile"
echo "4. Test with: echo 'void main() { PrintInt(42); }' > test.txt"
echo "5.           java -jar COMPILER test.txt test.s"
echo "6.           spim -file test.s"
echo ""
echo "Good luck!"
