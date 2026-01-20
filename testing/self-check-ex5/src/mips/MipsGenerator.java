package mips;

import java.io.*;
import java.util.*;

/**
 * MIPS Code Generator
 *
 * Manages MIPS assembly code generation:
 * - Data section (strings, error messages)
 * - Text section (code)
 * - Label generation
 * - Output file management
 *
 * Usage:
 *   MipsGenerator gen = new MipsGenerator("output.s");
 *   gen.emitDataString("hello", "string_0");
 *   gen.emitText("li $t0, 5");
 *   gen.close();
 */
public class MipsGenerator
{
    private PrintWriter writer;
    private StringBuilder dataSection;
    private StringBuilder textSection;
    private int labelCounter;

    public MipsGenerator(String outputFile) throws IOException
    {
        this.writer = new PrintWriter(new FileWriter(outputFile));
        this.dataSection = new StringBuilder();
        this.textSection = new StringBuilder();
        this.labelCounter = 0;

        initializeDataSection();
    }

    /**
     * Initialize data section with error messages
     */
    private void initializeDataSection()
    {
        dataSection.append(".data\n");

        // Error messages (required by spec)
        emitDataString("Illegal Division By Zero", "msg_div_zero");
        emitDataString("Invalid Pointer Dereference", "msg_null_ptr");
        emitDataString("Access Violation", "msg_bounds");
    }

    /**
     * Emit a string to the data section
     * @param value the string value (will be null-terminated)
     * @param label the label for this string
     */
    public void emitDataString(String value, String label)
    {
        dataSection.append(String.format("%s: .asciiz \"%s\"\n", label, escapeString(value)));
    }

    /**
     * Emit arbitrary data directive
     */
    public void emitData(String directive)
    {
        dataSection.append(directive).append("\n");
    }

    /**
     * Emit a label in the text section
     */
    public void emitLabel(String label)
    {
        textSection.append(label).append(":\n");
    }

    /**
     * Emit an instruction in the text section
     */
    public void emit(String instruction)
    {
        textSection.append("    ").append(instruction).append("\n");
    }

    /**
     * Emit an instruction with a comment
     */
    public void emit(String instruction, String comment)
    {
        textSection.append("    ").append(instruction);
        if (comment != null && !comment.isEmpty()) {
            textSection.append("    # ").append(comment);
        }
        textSection.append("\n");
    }

    /**
     * Emit a comment
     */
    public void emitComment(String comment)
    {
        textSection.append("    # ").append(comment).append("\n");
    }

    /**
     * Emit a blank line for readability
     */
    public void emitBlankLine()
    {
        textSection.append("\n");
    }

    /**
     * Generate a fresh label
     */
    public String getFreshLabel(String prefix)
    {
        return String.format("%s_%d", prefix, labelCounter++);
    }

    /**
     * Finalize and write the complete MIPS file
     */
    public void finalize()
    {
        // Write data section
        writer.print(dataSection.toString());
        writer.println();

        // Write text section header
        writer.println(".text");
        writer.println(".globl main");
        writer.println();

        // Write text section
        writer.print(textSection.toString());

        // Ensure program ends with exit
        writer.println();
        writer.println("# Program exit");
        writer.println("    li $v0, 10");
        writer.println("    syscall");
    }

    /**
     * Close the output file
     */
    public void close()
    {
        finalize();
        writer.close();
    }

    /**
     * Escape special characters in strings
     */
    private String escapeString(String str)
    {
        return str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\t", "\\t");
    }

    /**
     * Emit error handler for division by zero
     */
    public void emitDivByZeroHandler()
    {
        emitLabel("error_div_by_zero");
        emit("la $a0, msg_div_zero");
        emit("li $v0, 4");
        emit("syscall");
        emit("li $v0, 10");
        emit("syscall");
        emitBlankLine();
    }

    /**
     * Emit error handler for null pointer dereference
     */
    public void emitNullPointerHandler()
    {
        emitLabel("error_null_pointer");
        emit("la $a0, msg_null_ptr");
        emit("li $v0, 4");
        emit("syscall");
        emit("li $v0, 10");
        emit("syscall");
        emitBlankLine();
    }

    /**
     * Emit error handler for array bounds violation
     */
    public void emitBoundsViolationHandler()
    {
        emitLabel("error_bounds");
        emit("la $a0, msg_bounds");
        emit("li $v0, 4");
        emit("syscall");
        emit("li $v0, 10");
        emit("syscall");
        emitBlankLine();
    }

    /**
     * Emit all error handlers
     */
    public void emitAllErrorHandlers()
    {
        emitDivByZeroHandler();
        emitNullPointerHandler();
        emitBoundsViolationHandler();
    }
}
