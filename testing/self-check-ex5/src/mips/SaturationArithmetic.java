package mips;

/**
 * Saturation Arithmetic for MIPS Generation
 *
 * Implements saturation arithmetic for integers bounded to [-32768, 32767].
 * Results exceeding the range are "clamped" to the nearest boundary.
 *
 * For each operation ⊙ ∈ {+, −, *, /}:
 * result = clamp(a ⊙ b, -32768, 32767)
 */
public class SaturationArithmetic
{
    private MipsGenerator gen;
    private static final int MAX_VALUE = 32767;   // 2^15 - 1
    private static final int MIN_VALUE = -32768;  // -2^15

    public SaturationArithmetic(MipsGenerator gen)
    {
        this.gen = gen;
    }

    /**
     * Emit saturated addition
     *
     * dst = saturate(src1 + src2)
     *
     * @param dst destination register
     * @param src1 first operand register
     * @param src2 second operand register
     */
    public void emitSaturatedAdd(String dst, String src1, String src2)
    {
        String labelMax = gen.getFreshLabel("saturate_add_max");
        String labelMin = gen.getFreshLabel("saturate_add_min");
        String labelDone = gen.getFreshLabel("saturate_add_done");

        gen.emitComment("Saturated addition");

        // Perform addition
        gen.emit(String.format("add %s, %s, %s", dst, src1, src2));

        // Check overflow (result > MAX_VALUE)
        gen.emit(String.format("li $t9, %d", MAX_VALUE));
        gen.emit(String.format("bgt %s, $t9, %s", dst, labelMax));

        // Check underflow (result < MIN_VALUE)
        gen.emit(String.format("li $t9, %d", MIN_VALUE));
        gen.emit(String.format("blt %s, $t9, %s", dst, labelMin));

        // Normal case - no saturation needed
        gen.emit(String.format("j %s", labelDone));

        // Saturate to MAX
        gen.emitLabel(labelMax);
        gen.emit(String.format("li %s, %d", dst, MAX_VALUE));
        gen.emit(String.format("j %s", labelDone));

        // Saturate to MIN
        gen.emitLabel(labelMin);
        gen.emit(String.format("li %s, %d", dst, MIN_VALUE));

        gen.emitLabel(labelDone);
    }

    /**
     * Emit saturated subtraction
     *
     * dst = saturate(src1 - src2)
     */
    public void emitSaturatedSub(String dst, String src1, String src2)
    {
        String labelMax = gen.getFreshLabel("saturate_sub_max");
        String labelMin = gen.getFreshLabel("saturate_sub_min");
        String labelDone = gen.getFreshLabel("saturate_sub_done");

        gen.emitComment("Saturated subtraction");

        // Perform subtraction
        gen.emit(String.format("sub %s, %s, %s", dst, src1, src2));

        // Check overflow
        gen.emit(String.format("li $t9, %d", MAX_VALUE));
        gen.emit(String.format("bgt %s, $t9, %s", dst, labelMax));

        // Check underflow
        gen.emit(String.format("li $t9, %d", MIN_VALUE));
        gen.emit(String.format("blt %s, $t9, %s", dst, labelMin));

        // Normal case
        gen.emit(String.format("j %s", labelDone));

        // Saturate to MAX
        gen.emitLabel(labelMax);
        gen.emit(String.format("li %s, %d", dst, MAX_VALUE));
        gen.emit(String.format("j %s", labelDone));

        // Saturate to MIN
        gen.emitLabel(labelMin);
        gen.emit(String.format("li %s, %d", dst, MIN_VALUE));

        gen.emitLabel(labelDone);
    }

    /**
     * Emit saturated multiplication
     *
     * dst = saturate(src1 * src2)
     */
    public void emitSaturatedMul(String dst, String src1, String src2)
    {
        String labelMax = gen.getFreshLabel("saturate_mul_max");
        String labelMin = gen.getFreshLabel("saturate_mul_min");
        String labelDone = gen.getFreshLabel("saturate_mul_done");

        gen.emitComment("Saturated multiplication");

        // Perform multiplication
        gen.emit(String.format("mul %s, %s, %s", dst, src1, src2));

        // Check overflow
        gen.emit(String.format("li $t9, %d", MAX_VALUE));
        gen.emit(String.format("bgt %s, $t9, %s", dst, labelMax));

        // Check underflow
        gen.emit(String.format("li $t9, %d", MIN_VALUE));
        gen.emit(String.format("blt %s, $t9, %s", dst, labelMin));

        // Normal case
        gen.emit(String.format("j %s", labelDone));

        // Saturate to MAX
        gen.emitLabel(labelMax);
        gen.emit(String.format("li %s, %d", dst, MAX_VALUE));
        gen.emit(String.format("j %s", labelDone));

        // Saturate to MIN
        gen.emitLabel(labelMin);
        gen.emit(String.format("li %s, %d", dst, MIN_VALUE));

        gen.emitLabel(labelDone);
    }

    /**
     * Emit saturated division
     *
     * dst = saturate(src1 / src2)
     * Note: Division by zero is checked separately
     */
    public void emitSaturatedDiv(String dst, String src1, String src2)
    {
        String labelMax = gen.getFreshLabel("saturate_div_max");
        String labelMin = gen.getFreshLabel("saturate_div_min");
        String labelDone = gen.getFreshLabel("saturate_div_done");

        gen.emitComment("Saturated division");

        // Perform division (assume div-by-zero already checked)
        gen.emit(String.format("div %s, %s, %s", dst, src1, src2));

        // Check overflow (rare, but can happen: -32768 / -1 = 32768)
        gen.emit(String.format("li $t9, %d", MAX_VALUE));
        gen.emit(String.format("bgt %s, $t9, %s", dst, labelMax));

        // Check underflow
        gen.emit(String.format("li $t9, %d", MIN_VALUE));
        gen.emit(String.format("blt %s, $t9, %s", dst, labelMin));

        // Normal case
        gen.emit(String.format("j %s", labelDone));

        // Saturate to MAX
        gen.emitLabel(labelMax);
        gen.emit(String.format("li %s, %d", dst, MAX_VALUE));
        gen.emit(String.format("j %s", labelDone));

        // Saturate to MIN
        gen.emitLabel(labelMin);
        gen.emit(String.format("li %s, %d", dst, MIN_VALUE));

        gen.emitLabel(labelDone);
    }

    /**
     * Emit unary negation with saturation
     *
     * dst = saturate(-src)
     * Special case: -(-32768) = 32767 (saturates)
     */
    public void emitSaturatedNeg(String dst, String src)
    {
        String labelSaturate = gen.getFreshLabel("saturate_neg");
        String labelDone = gen.getFreshLabel("saturate_neg_done");

        gen.emitComment("Saturated negation");

        // Check if src == MIN_VALUE
        gen.emit(String.format("li $t9, %d", MIN_VALUE));
        gen.emit(String.format("beq %s, $t9, %s", src, labelSaturate));

        // Normal case: just negate
        gen.emit(String.format("neg %s, %s", dst, src));
        gen.emit(String.format("j %s", labelDone));

        // Special case: -MIN_VALUE saturates to MAX_VALUE
        gen.emitLabel(labelSaturate);
        gen.emit(String.format("li %s, %d", dst, MAX_VALUE));

        gen.emitLabel(labelDone);
    }
}
