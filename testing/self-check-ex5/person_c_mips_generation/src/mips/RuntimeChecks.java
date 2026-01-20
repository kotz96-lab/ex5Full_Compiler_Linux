package mips;

/**
 * Runtime Checks for MIPS Generation
 *
 * Generates MIPS code for runtime safety checks:
 * 1. Division by zero
 * 2. Null pointer dereference
 * 3. Array bounds checking
 */
public class RuntimeChecks
{
    private MipsGenerator gen;

    public RuntimeChecks(MipsGenerator gen)
    {
        this.gen = gen;
    }

    /**
     * Emit division by zero check
     *
     * Checks if divisor is zero before division.
     * If zero, jumps to error handler.
     *
     * @param divisorReg register containing divisor
     */
    public void emitDivByZeroCheck(String divisorReg)
    {
        gen.emitComment("Check division by zero");
        gen.emit(String.format("beq %s, $zero, error_div_by_zero", divisorReg));
    }

    /**
     * Emit null pointer check
     *
     * Checks if pointer is null before dereferencing.
     * If null, jumps to error handler.
     *
     * @param pointerReg register containing pointer
     */
    public void emitNullCheck(String pointerReg)
    {
        gen.emitComment("Check null pointer");
        gen.emit(String.format("beq %s, $zero, error_null_pointer", pointerReg));
    }

    /**
     * Emit array bounds check
     *
     * Checks:
     * 1. Array pointer is not null
     * 2. Index >= 0
     * 3. Index < array.length
     *
     * If any check fails, jumps to error handler.
     *
     * @param arrayReg register containing array address
     * @param indexReg register containing index
     * @param tempReg temporary register for length
     */
    public void emitBoundsCheck(String arrayReg, String indexReg, String tempReg)
    {
        gen.emitComment("Check array bounds");

        // Check null pointer
        gen.emit(String.format("beq %s, $zero, error_null_pointer", arrayReg));

        // Check index < 0
        gen.emit(String.format("bltz %s, error_bounds", indexReg));

        // Load array length and check index < length
        gen.emit(String.format("lw %s, 0(%s)", tempReg, arrayReg), "load array length");
        gen.emit(String.format("bge %s, %s, error_bounds", indexReg, tempReg));
    }

    /**
     * Emit field access null check
     *
     * Checks if object pointer is null before accessing field.
     *
     * @param objectReg register containing object address
     */
    public void emitFieldAccessCheck(String objectReg)
    {
        emitNullCheck(objectReg);
    }
}
