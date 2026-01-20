package regalloc;

import temp.Temp;
import java.util.HashSet;
import java.util.Set;

/**
 * Liveness Information for a single IR command
 *
 * Stores the following sets:
 * - USE: temporaries used (read) by this command
 * - DEF: temporaries defined (written) by this command
 * - IN: temporaries live before this command executes
 * - OUT: temporaries live after this command executes
 *
 * Dataflow equations:
 * IN[i] = USE[i] ∪ (OUT[i] - DEF[i])
 * OUT[i] = ∪ IN[successor of i]
 */
public class LivenessInfo
{
    // Sets computed once (don't change during iteration)
    public Set<Temp> use;  // Temps read by this instruction
    public Set<Temp> def;  // Temps written by this instruction

    // Sets computed iteratively (change until fixpoint)
    public Set<Temp> in;   // Temps live before this instruction
    public Set<Temp> out;  // Temps live after this instruction

    public LivenessInfo()
    {
        this.use = new HashSet<>();
        this.def = new HashSet<>();
        this.in = new HashSet<>();
        this.out = new HashSet<>();
    }

    /**
     * Update IN set using dataflow equation:
     * IN[i] = USE[i] ∪ (OUT[i] - DEF[i])
     *
     * @return true if IN set changed
     */
    public boolean updateIn()
    {
        Set<Temp> newIn = new HashSet<>(use);

        // Add OUT[i] - DEF[i]
        for (Temp t : out) {
            if (!def.contains(t)) {
                newIn.add(t);
            }
        }

        boolean changed = !newIn.equals(in);
        in = newIn;
        return changed;
    }

    @Override
    public String toString()
    {
        return String.format("USE=%s DEF=%s IN=%s OUT=%s",
            tempSetToString(use),
            tempSetToString(def),
            tempSetToString(in),
            tempSetToString(out));
    }

    private String tempSetToString(Set<Temp> temps)
    {
        if (temps.isEmpty()) return "{}";

        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Temp t : temps) {
            if (!first) sb.append(", ");
            sb.append("Temp_").append(t.getSerialNumber());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
