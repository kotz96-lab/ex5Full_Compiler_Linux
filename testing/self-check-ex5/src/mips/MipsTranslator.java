package mips;

import ir.*;
import regalloc.*;
import temp.Temp;
import java.io.IOException;
import java.util.*;

/**
 * MIPS Translator
 *
 * Main class that translates IR commands to MIPS assembly.
 * Uses Person A's IR and Person B's register allocation.
 *
 * Usage:
 *   MipsTranslator translator = new MipsTranslator("output.s");
 *   translator.translate(irCommands, registerAllocation);
 *   translator.close();
 */
public class MipsTranslator
{
    private MipsGenerator gen;
    private RuntimeChecks checks;
    private SaturationArithmetic sat;
    private StringTable strings;
    private RegisterAllocation regAlloc;
    private Set<String> allocatedVars = new HashSet<>();

    // Stack-based locals support
    private Set<String> globalVars = new HashSet<>();
    private Map<String, Map<String, Integer>> functionLocals = new HashMap<>();  // func -> {varName -> offset}
    private String currentFunction = null;
    private int currentStackSize = 0;

    // Track which globals each function uses (for callee-save)
    private Map<String, Set<String>> functionGlobals = new HashMap<>();  // func -> set of global vars used

    // For lookahead in translateStore
    private List<IrCommand> currentCommands = null;
    private int currentCommandIndex = -1;

    // Track variables saved before store-then-call patterns
    private Stack<String> savedGlobalVars = new Stack<>();

    public MipsTranslator(String outputFile) throws IOException
    {
        this.gen = new MipsGenerator(outputFile);
        this.checks = new RuntimeChecks(gen);
        this.sat = new SaturationArithmetic(gen);
        this.strings = new StringTable();
    }

    /**
     * Helper: Get register for a temp, or $zero if null
     */
    private String getReg(Temp t)
    {
        if (t == null) {
            return "$zero";
        }
        String reg = regAlloc.getRegister(t);
        return (reg == null) ? "$zero" : reg;
    }

    /**
     * Main translation method
     */
    public void translate(List<IrCommand> commands, RegisterAllocation allocation)
    {
        this.regAlloc = allocation;
        this.currentCommands = commands;  // Store for lookahead in translateStore

        // Check if allocation succeeded
        if (!allocation.isSuccess()) {
            System.err.println("Register Allocation Failed");
            System.exit(1);
        }

        // Step 1: Collect all string literals and allocated variables
        collectStrings(commands);
        collectAllocatedVars(commands);

        // Step 2: Emit strings and variables to data section
        strings.emitAllStrings(gen);
        emitAllocatedVars();

        // Step 3: Emit entry point and global initialization
        gen.emitLabel("main");

        // Translate commands until we hit the first label (function or main)
        // This executes global initialization code
        int firstLabelIndex = -1;
        for (int i = 0; i < commands.size(); i++) {
            if (commands.get(i) instanceof IrCommandLabel) {
                firstLabelIndex = i;
                break;
            }
        }

        // Execute global init code (before first label)
        if (firstLabelIndex > 0) {
            for (int i = 0; i < firstLabelIndex; i++) {
                currentCommandIndex = i;
                translateCommand(commands.get(i));
            }
        }

        // Jump to __user_main after global init
        gen.emit("j __user_main", "Jump to main function");

        // Step 4: Translate remaining commands (functions and main)
        for (int i = firstLabelIndex >= 0 ? firstLabelIndex : 0; i < commands.size(); i++) {
            currentCommandIndex = i;
            translateCommand(commands.get(i));
        }

        // Add explicit exit after main completes (before error handlers)
        gen.emitComment("Exit program");
        gen.emit("li $v0, 10");
        gen.emit("syscall");
        gen.emitBlankLine();

        // Step 5: Emit error handlers and library functions
        gen.emitAllErrorHandlers();
        emitLibraryFunctions();
    }

    /**
     * Emit library function stubs (PrintInt, PrintString, etc.)
     */
    private void emitLibraryFunctions()
    {
        // PrintInt - prints integer and space
        gen.emitLabel("PrintInt");
        gen.emitComment("Print integer (expects value in p_10 variable)");
        gen.emit("lw $a0, p_10", "load value to print");
        gen.emit("li $v0, 1", "syscall: print_int");
        gen.emit("syscall");
        gen.emit("li $a0, 32", "print space");
        gen.emit("li $v0, 11", "syscall: print_char");
        gen.emit("syscall");
        gen.emit("jr $ra", "return");
        gen.emitBlankLine();

        // PrintString - prints string (expects value in p_10 variable)
        gen.emitLabel("PrintString");
        gen.emitComment("Print string (expects value in p_10 variable)");
        gen.emit("lw $a0, p_10", "load string address to print");
        gen.emit("li $v0, 4", "syscall: print_string");
        gen.emit("syscall");
        gen.emit("jr $ra", "return");
    }

    /**
     * Collect all string literals from IR
     */
    private void collectStrings(List<IrCommand> commands)
    {
        for (IrCommand cmd : commands) {
            if (cmd instanceof IrCommandConstString) {
                IrCommandConstString strCmd = (IrCommandConstString) cmd;
                strings.addString(strCmd.value);
            }
        }
    }

    /**
     * Collect all allocated variables from IR
     * Classify them as global or local (per-function)
     */
    private void collectAllocatedVars(List<IrCommand> commands)
    {
        String currentFunc = null;

        for (IrCommand cmd : commands) {
            // Track which function we're in
            // Only consider it a function if it's NOT a jump label (which start with "Label_")
            if (cmd instanceof IrCommandLabel) {
                IrCommandLabel labelCmd = (IrCommandLabel) cmd;
                String labelName = labelCmd.labelName;

                // Function labels: main, fib, BubbleSort, etc.
                // Jump labels: Label_1_end, Label_0_start, etc. (start with "Label_")
                if (!labelName.isEmpty() && !labelName.startsWith("Label_")) {
                    currentFunc = labelName;
                    if (!functionLocals.containsKey(currentFunc)) {
                        functionLocals.put(currentFunc, new HashMap<>());
                    }
                    if (!functionGlobals.containsKey(currentFunc)) {
                        functionGlobals.put(currentFunc, new HashSet<>());
                    }
                }
            }

            // Collect variable names
            if (cmd instanceof IrCommandAllocate) {
                IrCommandAllocate allocCmd = (IrCommandAllocate) cmd;
                allocatedVars.add(allocCmd.varName);

                // Variables starting with p_ are library function parameters (globals)
                // If we're inside a function and it's not a p_ variable, it's local
                if (currentFunc != null && !allocCmd.varName.startsWith("p_")) {
                    Map<String, Integer> locals = functionLocals.get(currentFunc);
                    if (!locals.containsKey(allocCmd.varName)) {
                        // Assign stack offset: -12, -16, -20, ...
                        // First two words are for $ra and $fp
                        int offset = -12 - (locals.size() * 4);
                        locals.put(allocCmd.varName, offset);
                    }
                } else {
                    globalVars.add(allocCmd.varName);
                }
            }
            else if (cmd instanceof IrCommandLoad) {
                IrCommandLoad loadCmd = (IrCommandLoad) cmd;
                allocatedVars.add(loadCmd.varName);

                // If not allocated yet, decide if global or local
                // p_ variables are always global
                if (currentFunc != null && !globalVars.contains(loadCmd.varName) && !loadCmd.varName.startsWith("p_")) {
                    Map<String, Integer> locals = functionLocals.get(currentFunc);
                    if (!locals.containsKey(loadCmd.varName)) {
                        int offset = -12 - (locals.size() * 4);
                        locals.put(loadCmd.varName, offset);
                    }
                } else if (currentFunc == null || loadCmd.varName.startsWith("p_")) {
                    globalVars.add(loadCmd.varName);
                }

                // Track global usage by function (excluding p_ which are for library calls)
                if (currentFunc != null && !loadCmd.varName.startsWith("p_")) {
                    functionGlobals.get(currentFunc).add(loadCmd.varName);
                }
            }
            else if (cmd instanceof IrCommandStore) {
                IrCommandStore storeCmd = (IrCommandStore) cmd;
                allocatedVars.add(storeCmd.varName);

                // If not allocated yet, decide if global or local
                // p_ variables are always global
                if (currentFunc != null && !globalVars.contains(storeCmd.varName) && !storeCmd.varName.startsWith("p_")) {
                    Map<String, Integer> locals = functionLocals.get(currentFunc);
                    if (!locals.containsKey(storeCmd.varName)) {
                        int offset = -12 - (locals.size() * 4);
                        locals.put(storeCmd.varName, offset);
                    }
                } else if (currentFunc == null || storeCmd.varName.startsWith("p_")) {
                    globalVars.add(storeCmd.varName);
                }

                // Track global usage by function (excluding p_ which are for library calls)
                if (currentFunc != null && !storeCmd.varName.startsWith("p_")) {
                    functionGlobals.get(currentFunc).add(storeCmd.varName);
                }
            }
        }
    }

    /**
     * Emit all allocated variables to .data section
     */
    private void emitAllocatedVars()
    {
        for (String varName : allocatedVars) {
            gen.emitData(varName + ": .word 0");
        }
    }

    /**
     * Translate a single IR command
     */
    private void translateCommand(IrCommand cmd)
    {
        // Don't emit comment for labels (to avoid SPIM parser issues)
        if (!(cmd instanceof IrCommandLabel)) {
            gen.emitComment(cmd.toString());
        }

        if (cmd instanceof IrCommandBinopAddIntegers) {
            translateAdd((IrCommandBinopAddIntegers) cmd);
        }
        else if (cmd instanceof IrCommandBinopSubIntegers) {
            translateSub((IrCommandBinopSubIntegers) cmd);
        }
        else if (cmd instanceof IrCommandBinopMulIntegers) {
            translateMul((IrCommandBinopMulIntegers) cmd);
        }
        else if (cmd instanceof IrCommandBinopDivIntegers) {
            translateDiv((IrCommandBinopDivIntegers) cmd);
        }
        else if (cmd instanceof IrCommandBinopEqIntegers) {
            translateEq((IrCommandBinopEqIntegers) cmd);
        }
        else if (cmd instanceof IrCommandBinopLtIntegers) {
            translateLt((IrCommandBinopLtIntegers) cmd);
        }
        else if (cmd instanceof IrCommandBinopMinusInteger) {
            translateNeg((IrCommandBinopMinusInteger) cmd);
        }
        else if (cmd instanceof IRcommandConstInt) {
            translateConstInt((IRcommandConstInt) cmd);
        }
        else if (cmd instanceof IrCommandConstString) {
            translateConstString((IrCommandConstString) cmd);
        }
        else if (cmd instanceof IrCommandNilConst) {
            translateNilConst((IrCommandNilConst) cmd);
        }
        else if (cmd instanceof IrCommandStringConcat) {
            translateStringConcat((IrCommandStringConcat) cmd);
        }
        else if (cmd instanceof IrCommandStringEqual) {
            translateStringEqual((IrCommandStringEqual) cmd);
        }
        else if (cmd instanceof IrCommandArrayAccess) {
            translateArrayAccess((IrCommandArrayAccess) cmd);
        }
        else if (cmd instanceof IrCommandArrayStore) {
            translateArrayStore((IrCommandArrayStore) cmd);
        }
        else if (cmd instanceof IrCommandArrayLength) {
            translateArrayLength((IrCommandArrayLength) cmd);
        }
        else if (cmd instanceof IrCommandNewArray) {
            translateNewArray((IrCommandNewArray) cmd);
        }
        else if (cmd instanceof IrCommandFieldAccess) {
            translateFieldAccess((IrCommandFieldAccess) cmd);
        }
        else if (cmd instanceof IrCommandFieldStore) {
            translateFieldStore((IrCommandFieldStore) cmd);
        }
        else if (cmd instanceof IrCommandNewObject) {
            translateNewObject((IrCommandNewObject) cmd);
        }
        else if (cmd instanceof IrCommandMethodCall) {
            translateMethodCall((IrCommandMethodCall) cmd);
        }
        else if (cmd instanceof IrCommandLabel) {
            translateLabel((IrCommandLabel) cmd);
        }
        else if (cmd instanceof IrCommandJumpLabel) {
            translateJump((IrCommandJumpLabel) cmd);
        }
        else if (cmd instanceof IrCommandJumpIfEqToZero) {
            translateJumpIfZero((IrCommandJumpIfEqToZero) cmd);
        }
        else if (cmd instanceof IrCommandReturn) {
            translateReturn((IrCommandReturn) cmd);
        }
        else if (cmd instanceof IrCommandReturnVoid) {
            translateReturnVoid((IrCommandReturnVoid) cmd);
        }
        else if (cmd instanceof IrCommandLoad) {
            translateLoad((IrCommandLoad) cmd);
        }
        else if (cmd instanceof IrCommandStore) {
            translateStore((IrCommandStore) cmd);
        }
        else if (cmd instanceof IrCommandCallFunc) {
            translateCallFunc((IrCommandCallFunc) cmd);
        }
        else if (cmd instanceof IrCommandAllocate) {
            translateAllocate((IrCommandAllocate) cmd);
        }

        gen.emitBlankLine();
    }

    // ==================== Integer Arithmetic ====================

    private void translateAdd(IrCommandBinopAddIntegers cmd)
    {
        String dst = getReg(cmd.dst);
        String src1 = getReg(cmd.t1);
        String src2 = getReg(cmd.t2);

        sat.emitSaturatedAdd(dst, src1, src2);
    }

    private void translateSub(IrCommandBinopSubIntegers cmd)
    {
        String dst = getReg(cmd.dst);
        String src1 = getReg(cmd.t1);
        String src2 = getReg(cmd.t2);

        sat.emitSaturatedSub(dst, src1, src2);
    }

    private void translateMul(IrCommandBinopMulIntegers cmd)
    {
        String dst = getReg(cmd.dst);
        String src1 = getReg(cmd.t1);
        String src2 = getReg(cmd.t2);

        sat.emitSaturatedMul(dst, src1, src2);
    }

    private void translateDiv(IrCommandBinopDivIntegers cmd)
    {
        String dst = getReg(cmd.dst);
        String src1 = getReg(cmd.t1);
        String src2 = getReg(cmd.t2);

        // Check division by zero
        checks.emitDivByZeroCheck(src2);

        sat.emitSaturatedDiv(dst, src1, src2);
    }

    private void translateEq(IrCommandBinopEqIntegers cmd)
    {
        String dst = getReg(cmd.dst);
        String src1 = getReg(cmd.t1);
        String src2 = getReg(cmd.t2);

        String labelEq = gen.getFreshLabel("eq");
        String labelDone = gen.getFreshLabel("eq_done");

        // If dst overlaps with src1 or src2, we need to save them first
        if (dst.equals(src1) || dst.equals(src2)) {
            // Use temporary register to avoid clobbering
            String temp1 = "$t8";
            String temp2 = "$t9";
            if (dst.equals(src1)) {
                gen.emit(String.format("move %s, %s", temp1, src1));
                src1 = temp1;
            }
            if (dst.equals(src2)) {
                gen.emit(String.format("move %s, %s", temp2, src2));
                src2 = temp2;
            }
        }

        gen.emit(String.format("li %s, 0", dst), "assume not equal");
        gen.emit(String.format("bne %s, %s, %s", src1, src2, labelDone));
        gen.emit(String.format("li %s, 1", dst), "they are equal");
        gen.emitLabel(labelDone);
    }

    private void translateLt(IrCommandBinopLtIntegers cmd)
    {
        String dst = getReg(cmd.dst);
        String src1 = getReg(cmd.t1);
        String src2 = getReg(cmd.t2);

        gen.emit(String.format("slt %s, %s, %s", dst, src1, src2));
    }

    private void translateNeg(IrCommandBinopMinusInteger cmd)
    {
        String dst = getReg(cmd.dst);
        String src = getReg(cmd.t);

        sat.emitSaturatedNeg(dst, src);
    }

    // ==================== Constants ====================

    private void translateConstInt(IRcommandConstInt cmd)
    {
        String dst = getReg(cmd.t);
        gen.emit(String.format("li %s, %d", dst, cmd.value));
    }

    private void translateConstString(IrCommandConstString cmd)
    {
        String dst = getReg(cmd.dst);
        String label = strings.getLabel(cmd.value);

        if (label == null) {
            // String not in table (shouldn't happen if collectStrings worked)
            label = strings.addString(cmd.value);
        }

        gen.emit(String.format("la %s, %s", dst, label));
    }

    private void translateNilConst(IrCommandNilConst cmd)
    {
        String dst = getReg(cmd.dst);
        gen.emit(String.format("li %s, 0", dst), "nil = 0");
    }

    // ==================== String Operations ====================

    private void translateStringConcat(IrCommandStringConcat cmd)
    {
        String dst = getReg(cmd.dst);
        String str1 = getReg(cmd.str1);
        String str2 = getReg(cmd.str2);

        // Use $s0-$s3 as scratch registers
        String len1 = "$s0";
        String len2 = "$s1";
        String ptr = "$s2";
        String str1Saved = "$s4";  // Save str1 in case dst clobbers it
        String str2Saved = "$s5";  // Save str2 in case dst clobbers it

        String labelLoop1 = gen.getFreshLabel("strlen1_loop");
        String labelLoop1Done = gen.getFreshLabel("strlen1_done");
        String labelLoop2 = gen.getFreshLabel("strlen2_loop");
        String labelLoop2Done = gen.getFreshLabel("strlen2_done");
        String labelCopy1 = gen.getFreshLabel("strcpy1_loop");
        String labelCopy1Done = gen.getFreshLabel("strcpy1_done");
        String labelCopy2 = gen.getFreshLabel("strcpy2_loop");
        String labelCopy2Done = gen.getFreshLabel("strcpy2_done");

        // Save str1 and str2 in case they get clobbered by dst assignment
        gen.emit(String.format("move %s, %s", str1Saved, str1));
        gen.emit(String.format("move %s, %s", str2Saved, str2));

        gen.emitComment("Calculate strlen(str1)");
        gen.emit(String.format("li %s, 0", len1));
        gen.emit(String.format("move %s, %s", ptr, str1Saved));
        gen.emitLabel(labelLoop1);
        gen.emit(String.format("lb $t8, 0(%s)", ptr));
        gen.emit(String.format("beq $t8, $zero, %s", labelLoop1Done));
        gen.emit(String.format("addi %s, %s, 1", len1, len1));
        gen.emit(String.format("addi %s, %s, 1", ptr, ptr));
        gen.emit(String.format("j %s", labelLoop1));
        gen.emitLabel(labelLoop1Done);

        gen.emitComment("Calculate strlen(str2)");
        gen.emit(String.format("li %s, 0", len2));
        gen.emit(String.format("move %s, %s", ptr, str2Saved));
        gen.emitLabel(labelLoop2);
        gen.emit(String.format("lb $t8, 0(%s)", ptr));
        gen.emit(String.format("beq $t8, $zero, %s", labelLoop2Done));
        gen.emit(String.format("addi %s, %s, 1", len2, len2));
        gen.emit(String.format("addi %s, %s, 1", ptr, ptr));
        gen.emit(String.format("j %s", labelLoop2));
        gen.emitLabel(labelLoop2Done);

        gen.emitComment("Allocate memory for concatenated string");
        gen.emit(String.format("add $a0, %s, %s", len1, len2));
        gen.emit("addi $a0, $a0, 1", "+1 for null terminator");
        gen.emit("li $v0, 9", "malloc syscall");
        gen.emit("syscall");
        gen.emit(String.format("move %s, $v0", dst));

        gen.emitComment("Copy str1");
        gen.emit(String.format("move %s, %s", ptr, dst));
        gen.emit(String.format("move $s3, %s", str1Saved));
        gen.emitLabel(labelCopy1);
        gen.emit("lb $t8, 0($s3)");
        gen.emit(String.format("beq $t8, $zero, %s", labelCopy1Done));
        gen.emit(String.format("sb $t8, 0(%s)", ptr));
        gen.emit(String.format("addi %s, %s, 1", ptr, ptr));
        gen.emit("addi $s3, $s3, 1");
        gen.emit(String.format("j %s", labelCopy1));
        gen.emitLabel(labelCopy1Done);

        gen.emitComment("Copy str2");
        gen.emit(String.format("move $s3, %s", str2Saved));
        gen.emitLabel(labelCopy2);
        gen.emit("lb $t8, 0($s3)");
        gen.emit(String.format("sb $t8, 0(%s)", ptr));
        gen.emit(String.format("beq $t8, $zero, %s", labelCopy2Done));
        gen.emit(String.format("addi %s, %s, 1", ptr, ptr));
        gen.emit("addi $s3, $s3, 1");
        gen.emit(String.format("j %s", labelCopy2));
        gen.emitLabel(labelCopy2Done);
    }

    private void translateStringEqual(IrCommandStringEqual cmd)
    {
        String dst = getReg(cmd.dst);
        String str1 = getReg(cmd.str1);
        String str2 = getReg(cmd.str2);

        String labelLoop = gen.getFreshLabel("streq_loop");
        String labelNotEq = gen.getFreshLabel("streq_ne");
        String labelEq = gen.getFreshLabel("streq_eq");
        String labelDone = gen.getFreshLabel("streq_done");

        gen.emit("move $s0, " + str1, "pointer to str1");
        gen.emit("move $s1, " + str2, "pointer to str2");

        gen.emitLabel(labelLoop);
        gen.emit("lb $s2, 0($s0)", "load char from str1");
        gen.emit("lb $s3, 0($s1)", "load char from str2");
        gen.emit(String.format("bne $s2, $s3, %s", labelNotEq), "chars differ");
        gen.emit(String.format("beq $s2, $zero, %s", labelEq), "both null = equal");
        gen.emit("addi $s0, $s0, 1");
        gen.emit("addi $s1, $s1, 1");
        gen.emit(String.format("j %s", labelLoop));

        gen.emitLabel(labelNotEq);
        gen.emit(String.format("li %s, 0", dst), "not equal");
        gen.emit(String.format("j %s", labelDone));

        gen.emitLabel(labelEq);
        gen.emit(String.format("li %s, 1", dst), "equal");

        gen.emitLabel(labelDone);
    }

    // ==================== Array Operations ====================

    private void translateNewArray(IrCommandNewArray cmd)
    {
        String dst = getReg(cmd.dst);
        String size = getReg(cmd.size);
        int elemSize = cmd.elementSize;

        gen.emitComment("Allocate array");
        gen.emit(String.format("li $s0, %d", elemSize));
        gen.emit(String.format("mul $s0, %s, $s0", size), "size * elemSize");
        gen.emit("addi $a0, $s0, 4", "+4 for length field");
        gen.emit("li $v0, 9", "malloc");
        gen.emit("syscall");
        gen.emit(String.format("sw %s, 0($v0)", size), "store length");
        gen.emit(String.format("move %s, $v0", dst));
    }

    private void translateArrayAccess(IrCommandArrayAccess cmd)
    {
        String dst = getReg(cmd.dst);
        String array = getReg(cmd.array);
        String index = getReg(cmd.index);

        checks.emitBoundsCheck(array, index, "$s0");

        gen.emit(String.format("sll $s0, %s, 2", index), "index * 4");
        gen.emit("addi $s0, $s0, 4", "+ 4 (skip length)");
        gen.emit(String.format("add $s0, %s, $s0", array));
        gen.emit(String.format("lw %s, 0($s0)", dst));
    }

    private void translateArrayStore(IrCommandArrayStore cmd)
    {
        String array = getReg(cmd.array);
        String index = getReg(cmd.index);
        String value = getReg(cmd.value);

        checks.emitBoundsCheck(array, index, "$s0");

        gen.emit(String.format("sll $s0, %s, 2", index));
        gen.emit("addi $s0, $s0, 4");
        gen.emit(String.format("add $s0, %s, $s0", array));
        gen.emit(String.format("sw %s, 0($s0)", value));
    }

    private void translateArrayLength(IrCommandArrayLength cmd)
    {
        String dst = getReg(cmd.dst);
        String array = getReg(cmd.array);

        checks.emitNullCheck(array);
        gen.emit(String.format("lw %s, 0(%s)", dst, array));
    }

    // ==================== Object Operations ====================

    private void translateNewObject(IrCommandNewObject cmd)
    {
        String dst = getReg(cmd.dst);
        int size = cmd.sizeInBytes;

        gen.emitComment(String.format("Allocate object: %s", cmd.className));
        gen.emit(String.format("li $a0, %d", size));
        gen.emit("li $v0, 9", "malloc");
        gen.emit("syscall");
        gen.emit(String.format("move %s, $v0", dst));

        // Initialize fields to zero
        for (int offset = 0; offset < size; offset += 4) {
            gen.emit(String.format("sw $zero, %d($v0)", offset));
        }
    }

    private void translateFieldAccess(IrCommandFieldAccess cmd)
    {
        String dst = getReg(cmd.dst);
        String object = getReg(cmd.object);
        int offset = cmd.fieldOffset;

        checks.emitFieldAccessCheck(object);
        gen.emit(String.format("lw %s, %d(%s)", dst, offset, object));
    }

    private void translateFieldStore(IrCommandFieldStore cmd)
    {
        String object = getReg(cmd.object);
        String value = getReg(cmd.value);
        int offset = cmd.fieldOffset;

        checks.emitFieldAccessCheck(object);
        gen.emit(String.format("sw %s, %d(%s)", value, offset, object));
    }

    private void translateMethodCall(IrCommandMethodCall cmd)
    {
        // Simplified: treat as function call
        // Full implementation would use vtable for dynamic dispatch

        String object = getReg(cmd.object);
        checks.emitNullCheck(object);

        // Push arguments (if any)
        // ...

        // Call method
        gen.emit(String.format("jal method_%s", cmd.methodName));

        // Get return value
        if (cmd.dst != null) {
            String dst = getReg(cmd.dst);
            gen.emit(String.format("move %s, $v0", dst));
        }
    }

    // ==================== Stack Frame Management ====================

    private void emitFunctionPrologue(String funcName)
    {
        // Stack frame: just $ra and $fp
        // NO callee-save for globals - we use caller-save instead
        currentStackSize = 8;

        gen.emitComment("Function prologue");
        gen.emit(String.format("addi $sp, $sp, -%d", currentStackSize), "allocate stack frame");
        gen.emit("sw $ra, 4($sp)", "save return address");
        gen.emit("sw $fp, 0($sp)", "save frame pointer");
        gen.emit(String.format("addi $fp, $sp, %d", currentStackSize), "set new frame pointer");
    }

    private void emitFunctionEpilogue()
    {
        // NO callee-save restoration - we use caller-save instead
        gen.emitComment("Function epilogue");
        gen.emit("lw $ra, 4($sp)", "restore return address");
        gen.emit("lw $fp, 0($sp)", "restore frame pointer");
        gen.emit(String.format("addi $sp, $sp, %d", currentStackSize), "deallocate stack frame");
    }

    // ==================== Control Flow ====================

    private void translateLabel(IrCommandLabel cmd)
    {
        // Check if this is a function entry label (has locals allocated)
        boolean isFunctionEntry = functionLocals.containsKey(cmd.labelName);

        if (isFunctionEntry) {
            // If we're already in a function, emit an implicit return (epilogue + jr $ra)
            // This handles void functions without explicit return statements
            if (currentFunction != null) {
                gen.emitComment("Implicit return for void function");
                emitFunctionEpilogue();
                gen.emit("jr $ra");
                gen.emitBlankLine();
            }

            // Update current function context
            currentFunction = cmd.labelName;

            // Rename the IR's "main" label to avoid conflict with SPIM's entry point
            if (cmd.labelName.equals("main")) {
                gen.emitLabel("__user_main");
            } else {
                gen.emitLabel(cmd.labelName);
            }

            // Emit function prologue only for function entry
            emitFunctionPrologue(currentFunction);
        } else {
            // Regular label (not a function entry)
            gen.emitLabel(cmd.labelName);
        }
    }

    private void translateJump(IrCommandJumpLabel cmd)
    {
        gen.emit(String.format("j %s", cmd.labelName));
    }

    private void translateJumpIfZero(IrCommandJumpIfEqToZero cmd)
    {
        // If temp is null, this is an unconditional jump (always false condition)
        // Just skip this instruction or treat it as never jumping
        if (cmd.t == null) {
            // Null temp means condition is always false, so never jump
            // Just emit a comment and continue
            gen.emitComment("Skipping jump with null condition");
            return;
        }

        String cond = getReg(cmd.t);
        gen.emit(String.format("beq %s, $zero, %s", cond, cmd.labelName));
    }

    private void translateReturn(IrCommandReturn cmd)
    {
        String value = getReg(cmd.returnValue);
        gen.emit(String.format("move $v0, %s", value), "return value");
        emitFunctionEpilogue();
        gen.emit("jr $ra");
    }

    private void translateReturnVoid(IrCommandReturnVoid cmd)
    {
        emitFunctionEpilogue();
        gen.emit("jr $ra");
    }

    // ==================== Memory Operations ====================

    private void translateLoad(IrCommandLoad cmd)
    {
        String dst = getReg(cmd.dst);
        String varName = cmd.varName;

        // All variables are global for now
        gen.emit(String.format("lw %s, %s", dst, varName));
    }

    private void translateStore(IrCommandStore cmd)
    {
        String varName = cmd.varName;
        String src;

        if (cmd.src == null) {
            // Storing null/nil - use $zero register
            src = "$zero";
        } else {
            src = getReg(cmd.src);
        }

        // CRITICAL FIX FOR RECURSION:
        // Check if next command is a function call
        // If so, save this global BEFORE we store to it
        boolean nextIsCall = false;
        if (currentCommands != null && currentCommandIndex + 1 < currentCommands.size()) {
            IrCommand nextCmd = currentCommands.get(currentCommandIndex + 1);
            if (nextCmd instanceof IrCommandCallFunc) {
                nextIsCall = true;
            }
        }

        if (nextIsCall && currentFunction != null) {
            // This store is immediately followed by a call
            // Save the CURRENT value of the global before we overwrite it
            Set<String> globalsSet = functionGlobals.getOrDefault(currentFunction, new HashSet<>());
            if (globalsSet.contains(varName)) {
                gen.emitComment("Save " + varName + " before store (recursive call ahead)");
                gen.emit("addi $sp, $sp, -4", "allocate space");
                gen.emit(String.format("lw $t8, %s", varName), "load current value");
                gen.emit("sw $t8, 0($sp)", "save on stack");

                // Track that we saved this variable
                savedGlobalVars.push(varName);
            }
        }

        // All variables are global for now
        gen.emit(String.format("sw %s, %s", src, varName));
    }

    private void translateAllocate(IrCommandAllocate cmd)
    {
        // IrCommandAllocate in ex4 only has varName, no dst or size
        // This command might not be used in the actual IR generation
        // Just emit a comment for now
        gen.emitComment("Allocate " + cmd.varName);
    }

    private void translateCallFunc(IrCommandCallFunc cmd)
    {
        // Determine which register will hold the return value (if any)
        String dstReg = null;
        int dstRegNum = -1;
        if (cmd.t != null) {
            dstReg = getReg(cmd.t);
            // Extract register number if it's a $t register
            if (dstReg.startsWith("$t") && dstReg.length() >= 3) {
                try {
                    dstRegNum = Integer.parseInt(dstReg.substring(2));
                } catch (NumberFormatException e) {
                    // Not a simple $tN register
                }
            }
        }

        // Save caller-saved registers ($t0-$t9) on stack before call
        // This is necessary because recursive calls will clobber these registers
        gen.emitComment("Save caller-saved registers");
        int numRegs = 10;  // $t0 through $t9
        gen.emit(String.format("addi $sp, $sp, -%d", numRegs * 4), "allocate space for $t0-$t9");
        for (int i = 0; i < numRegs; i++) {
            gen.emit(String.format("sw $t%d, %d($sp)", i, i * 4));
        }

        // Make the function call
        gen.emit(String.format("jal %s", cmd.name));

        // Move return value to destination FIRST
        if (cmd.t != null) {
            gen.emit(String.format("move %s, $v0", dstReg), "save return value");
        }

        // Restore caller-saved registers, EXCEPT the destination register
        gen.emitComment("Restore caller-saved registers");
        for (int i = 0; i < numRegs; i++) {
            // Skip restoring the destination register since it now holds the return value
            if (i != dstRegNum) {
                gen.emit(String.format("lw $t%d, %d($sp)", i, i * 4));
            }
        }
        gen.emit(String.format("addi $sp, $sp, %d", numRegs * 4), "deallocate register save area");

        // Restore any globals that were saved by translateStore
        // These are globals that were modified right before this call
        // NOW the stack pointer is back to where the global was saved
        if (!savedGlobalVars.isEmpty()) {
            String varName = savedGlobalVars.pop();
            gen.emitComment("Restore " + varName + " after call (from pre-store save)");
            gen.emit("lw $t8, 0($sp)", "load saved value");
            gen.emit(String.format("sw $t8, %s", varName), "restore global");
            gen.emit("addi $sp, $sp, 4", "deallocate global save space");
        }
    }

    /**
     * Close and finalize output
     */
    public void close()
    {
        gen.close();
    }
}
