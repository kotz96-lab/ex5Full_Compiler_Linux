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

    public MipsTranslator(String outputFile) throws IOException
    {
        this.gen = new MipsGenerator(outputFile);
        this.checks = new RuntimeChecks(gen);
        this.sat = new SaturationArithmetic(gen);
        this.strings = new StringTable();
    }

    /**
     * Main translation method
     */
    public void translate(List<IrCommand> commands, RegisterAllocation allocation)
    {
        this.regAlloc = allocation;

        // Check if allocation succeeded
        if (!allocation.isSuccess()) {
            System.err.println("Register Allocation Failed");
            System.exit(1);
        }

        // Step 1: Collect all string literals
        collectStrings(commands);

        // Step 2: Emit strings to data section
        strings.emitAllStrings(gen);

        // Step 3: Emit main label
        gen.emitLabel("main");

        // Step 4: Translate each IR command
        for (IrCommand cmd : commands) {
            translateCommand(cmd);
        }

        // Step 5: Emit error handlers
        gen.emitAllErrorHandlers();
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
     * Translate a single IR command
     */
    private void translateCommand(IrCommand cmd)
    {
        gen.emitComment(cmd.toString());

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
        String dst = regAlloc.getRegister(cmd.dst);
        String src1 = regAlloc.getRegister(cmd.t1);
        String src2 = regAlloc.getRegister(cmd.t2);

        sat.emitSaturatedAdd(dst, src1, src2);
    }

    private void translateSub(IrCommandBinopSubIntegers cmd)
    {
        String dst = regAlloc.getRegister(cmd.dst);
        String src1 = regAlloc.getRegister(cmd.t1);
        String src2 = regAlloc.getRegister(cmd.t2);

        sat.emitSaturatedSub(dst, src1, src2);
    }

    private void translateMul(IrCommandBinopMulIntegers cmd)
    {
        String dst = regAlloc.getRegister(cmd.dst);
        String src1 = regAlloc.getRegister(cmd.t1);
        String src2 = regAlloc.getRegister(cmd.t2);

        sat.emitSaturatedMul(dst, src1, src2);
    }

    private void translateDiv(IrCommandBinopDivIntegers cmd)
    {
        String dst = regAlloc.getRegister(cmd.dst);
        String src1 = regAlloc.getRegister(cmd.t1);
        String src2 = regAlloc.getRegister(cmd.t2);

        // Check division by zero
        checks.emitDivByZeroCheck(src2);

        sat.emitSaturatedDiv(dst, src1, src2);
    }

    private void translateEq(IrCommandBinopEqIntegers cmd)
    {
        String dst = regAlloc.getRegister(cmd.dst);
        String src1 = regAlloc.getRegister(cmd.t1);
        String src2 = regAlloc.getRegister(cmd.t2);

        String labelEq = gen.getFreshLabel("eq");
        String labelDone = gen.getFreshLabel("eq_done");

        gen.emit(String.format("li %s, 0", dst), "assume not equal");
        gen.emit(String.format("bne %s, %s, %s", src1, src2, labelDone));
        gen.emit(String.format("li %s, 1", dst), "they are equal");
        gen.emitLabel(labelDone);
    }

    private void translateLt(IrCommandBinopLtIntegers cmd)
    {
        String dst = regAlloc.getRegister(cmd.dst);
        String src1 = regAlloc.getRegister(cmd.t1);
        String src2 = regAlloc.getRegister(cmd.t2);

        gen.emit(String.format("slt %s, %s, %s", dst, src1, src2));
    }

    private void translateNeg(IrCommandBinopMinusInteger cmd)
    {
        String dst = regAlloc.getRegister(cmd.dst);
        String src = regAlloc.getRegister(cmd.t1);

        sat.emitSaturatedNeg(dst, src);
    }

    // ==================== Constants ====================

    private void translateConstInt(IRcommandConstInt cmd)
    {
        String dst = regAlloc.getRegister(cmd.dst);
        gen.emit(String.format("li %s, %d", dst, cmd.value));
    }

    private void translateConstString(IrCommandConstString cmd)
    {
        String dst = regAlloc.getRegister(cmd.dst);
        String label = strings.getLabel(cmd.value);

        if (label == null) {
            // String not in table (shouldn't happen if collectStrings worked)
            label = strings.addString(cmd.value);
        }

        gen.emit(String.format("la %s, %s", dst, label));
    }

    private void translateNilConst(IrCommandNilConst cmd)
    {
        String dst = regAlloc.getRegister(cmd.dst);
        gen.emit(String.format("li %s, 0", dst), "nil = 0");
    }

    // ==================== String Operations ====================

    private void translateStringConcat(IrCommandStringConcat cmd)
    {
        String dst = regAlloc.getRegister(cmd.dst);
        String str1 = regAlloc.getRegister(cmd.str1);
        String str2 = regAlloc.getRegister(cmd.str2);

        // Use $s0-$s3 as scratch registers
        String len1 = "$s0";
        String len2 = "$s1";
        String ptr = "$s2";

        String labelLoop1 = gen.getFreshLabel("strlen1_loop");
        String labelLoop1Done = gen.getFreshLabel("strlen1_done");
        String labelLoop2 = gen.getFreshLabel("strlen2_loop");
        String labelLoop2Done = gen.getFreshLabel("strlen2_done");
        String labelCopy1 = gen.getFreshLabel("strcpy1_loop");
        String labelCopy1Done = gen.getFreshLabel("strcpy1_done");
        String labelCopy2 = gen.getFreshLabel("strcpy2_loop");
        String labelCopy2Done = gen.getFreshLabel("strcpy2_done");

        gen.emitComment("Calculate strlen(str1)");
        gen.emit(String.format("li %s, 0", len1));
        gen.emit(String.format("move %s, %s", ptr, str1));
        gen.emitLabel(labelLoop1);
        gen.emit(String.format("lb $at, 0(%s)", ptr));
        gen.emit(String.format("beq $at, $zero, %s", labelLoop1Done));
        gen.emit(String.format("addi %s, %s, 1", len1, len1));
        gen.emit(String.format("addi %s, %s, 1", ptr, ptr));
        gen.emit(String.format("j %s", labelLoop1));
        gen.emitLabel(labelLoop1Done);

        gen.emitComment("Calculate strlen(str2)");
        gen.emit(String.format("li %s, 0", len2));
        gen.emit(String.format("move %s, %s", ptr, str2));
        gen.emitLabel(labelLoop2);
        gen.emit(String.format("lb $at, 0(%s)", ptr));
        gen.emit(String.format("beq $at, $zero, %s", labelLoop2Done));
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
        gen.emit(String.format("move $s3, %s", str1));
        gen.emitLabel(labelCopy1);
        gen.emit("lb $at, 0($s3)");
        gen.emit(String.format("beq $at, $zero, %s", labelCopy1Done));
        gen.emit(String.format("sb $at, 0(%s)", ptr));
        gen.emit(String.format("addi %s, %s, 1", ptr, ptr));
        gen.emit("addi $s3, $s3, 1");
        gen.emit(String.format("j %s", labelCopy1));
        gen.emitLabel(labelCopy1Done);

        gen.emitComment("Copy str2");
        gen.emit(String.format("move $s3, %s", str2));
        gen.emitLabel(labelCopy2);
        gen.emit("lb $at, 0($s3)");
        gen.emit(String.format("sb $at, 0(%s)", ptr));
        gen.emit(String.format("beq $at, $zero, %s", labelCopy2Done));
        gen.emit(String.format("addi %s, %s, 1", ptr, ptr));
        gen.emit("addi $s3, $s3, 1");
        gen.emit(String.format("j %s", labelCopy2));
        gen.emitLabel(labelCopy2Done);
    }

    private void translateStringEqual(IrCommandStringEqual cmd)
    {
        String dst = regAlloc.getRegister(cmd.dst);
        String str1 = regAlloc.getRegister(cmd.str1);
        String str2 = regAlloc.getRegister(cmd.str2);

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
        String dst = regAlloc.getRegister(cmd.dst);
        String size = regAlloc.getRegister(cmd.size);
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
        String dst = regAlloc.getRegister(cmd.dst);
        String array = regAlloc.getRegister(cmd.array);
        String index = regAlloc.getRegister(cmd.index);

        checks.emitBoundsCheck(array, index, "$s0");

        gen.emit(String.format("sll $s0, %s, 2", index), "index * 4");
        gen.emit("addi $s0, $s0, 4", "+ 4 (skip length)");
        gen.emit(String.format("add $s0, %s, $s0", array));
        gen.emit(String.format("lw %s, 0($s0)", dst));
    }

    private void translateArrayStore(IrCommandArrayStore cmd)
    {
        String array = regAlloc.getRegister(cmd.array);
        String index = regAlloc.getRegister(cmd.index);
        String value = regAlloc.getRegister(cmd.value);

        checks.emitBoundsCheck(array, index, "$s0");

        gen.emit(String.format("sll $s0, %s, 2", index));
        gen.emit("addi $s0, $s0, 4");
        gen.emit(String.format("add $s0, %s, $s0", array));
        gen.emit(String.format("sw %s, 0($s0)", value));
    }

    private void translateArrayLength(IrCommandArrayLength cmd)
    {
        String dst = regAlloc.getRegister(cmd.dst);
        String array = regAlloc.getRegister(cmd.array);

        checks.emitNullCheck(array);
        gen.emit(String.format("lw %s, 0(%s)", dst, array));
    }

    // ==================== Object Operations ====================

    private void translateNewObject(IrCommandNewObject cmd)
    {
        String dst = regAlloc.getRegister(cmd.dst);
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
        String dst = regAlloc.getRegister(cmd.dst);
        String object = regAlloc.getRegister(cmd.object);
        int offset = cmd.fieldOffset;

        checks.emitFieldAccessCheck(object);
        gen.emit(String.format("lw %s, %d(%s)", dst, offset, object));
    }

    private void translateFieldStore(IrCommandFieldStore cmd)
    {
        String object = regAlloc.getRegister(cmd.object);
        String value = regAlloc.getRegister(cmd.value);
        int offset = cmd.fieldOffset;

        checks.emitFieldAccessCheck(object);
        gen.emit(String.format("sw %s, %d(%s)", value, offset, object));
    }

    private void translateMethodCall(IrCommandMethodCall cmd)
    {
        // Simplified: treat as function call
        // Full implementation would use vtable for dynamic dispatch

        String object = regAlloc.getRegister(cmd.object);
        checks.emitNullCheck(object);

        // Push arguments (if any)
        // ...

        // Call method
        gen.emit(String.format("jal method_%s", cmd.methodName));

        // Get return value
        if (cmd.dst != null) {
            String dst = regAlloc.getRegister(cmd.dst);
            gen.emit(String.format("move %s, $v0", dst));
        }
    }

    // ==================== Control Flow ====================

    private void translateLabel(IrCommandLabel cmd)
    {
        gen.emitLabel(cmd.label);
    }

    private void translateJump(IrCommandJumpLabel cmd)
    {
        gen.emit(String.format("j %s", cmd.label));
    }

    private void translateJumpIfZero(IrCommandJumpIfEqToZero cmd)
    {
        String cond = regAlloc.getRegister(cmd.t1);
        gen.emit(String.format("beq %s, $zero, %s", cond, cmd.label));
    }

    private void translateReturn(IrCommandReturn cmd)
    {
        String value = regAlloc.getRegister(cmd.returnValue);
        gen.emit(String.format("move $v0, %s", value), "return value");
        gen.emit("jr $ra");
    }

    private void translateReturnVoid(IrCommandReturnVoid cmd)
    {
        gen.emit("jr $ra");
    }

    // ==================== Memory Operations ====================

    private void translateLoad(IrCommandLoad cmd)
    {
        String dst = regAlloc.getRegister(cmd.dst);
        String varName = cmd.var_name;

        gen.emit(String.format("lw %s, %s", dst, varName));
    }

    private void translateStore(IrCommandStore cmd)
    {
        String src = regAlloc.getRegister(cmd.src);
        String varName = cmd.var_name;

        gen.emit(String.format("sw %s, %s", src, varName));
    }

    private void translateAllocate(IrCommandAllocate cmd)
    {
        String dst = regAlloc.getRegister(cmd.dst);
        int size = cmd.sizeInBytes;

        gen.emit(String.format("li $a0, %d", size));
        gen.emit("li $v0, 9", "malloc");
        gen.emit("syscall");
        gen.emit(String.format("move %s, $v0", dst));
    }

    private void translateCallFunc(IrCommandCallFunc cmd)
    {
        // Simplified function call
        // Real implementation would handle argument passing

        gen.emit(String.format("jal %s", cmd.label));

        if (cmd.dst != null) {
            String dst = regAlloc.getRegister(cmd.dst);
            gen.emit(String.format("move %s, $v0", dst));
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
