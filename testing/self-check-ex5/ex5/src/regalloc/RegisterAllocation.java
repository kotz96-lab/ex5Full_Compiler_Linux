package regalloc;

import temp.Temp;
import java.util.Map;

/**
 * Register Allocation Result
 *
 * Contains the result of register allocation:
 * - success: whether allocation succeeded
 * - assignment: mapping from temporary to register name
 *
 * If success = false, allocation failed (too many simultaneous live temps).
 * If success = true, assignment contains the register for each temp.
 */
public class RegisterAllocation
{
    public boolean success;
    public Map<Temp, String> assignment;  // temp → register name (e.g., "$t5")

    public RegisterAllocation(boolean success, Map<Temp, String> assignment)
    {
        this.success = success;
        this.assignment = assignment;
    }

    /**
     * Get the register assigned to a temporary
     * @param t the temporary
     * @return register name (e.g., "$t5"), or null if not allocated
     */
    public String getRegister(Temp t)
    {
        if (assignment == null) {
            return null;
        }
        return assignment.get(t);
    }

    /**
     * Check if allocation was successful
     */
    public boolean isSuccess()
    {
        return success;
    }
}
