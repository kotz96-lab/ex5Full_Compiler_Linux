package regalloc;

import ir.*;
import temp.Temp;
import java.util.*;

/**
 * Liveness Analysis
 *
 * Computes which temporaries are "live" at each point in the program.
 * A temporary is live if it holds a value that will be used in the future.
 *
 * Algorithm: Backward dataflow analysis
 * 1. Initialize all IN/OUT sets to empty
 * 2. Compute USE and DEF sets for each instruction
 * 3. Iterate backwards through instructions updating IN/OUT until fixpoint
 *
 * Dataflow equations:
 * IN[i] = USE[i] ∪ (OUT[i] - DEF[i])
 * OUT[i] = ∪ IN[successor of i]
 */
public class LivenessAnalysis
{
    private List<IrCommand> commands;
    private Map<IrCommand, LivenessInfo> livenessMap;

    public LivenessAnalysis(List<IrCommand> commands)
    {
        this.commands = commands;
        this.livenessMap = new HashMap<>();

        // Initialize liveness info for each command
        for (IrCommand cmd : commands) {
            livenessMap.put(cmd, new LivenessInfo());
        }
    }

    /**
     * Perform liveness analysis
     * @return map from IR command to liveness information
     */
    public Map<IrCommand, LivenessInfo> analyze()
    {
        // Step 1: Compute USE and DEF sets for each instruction
        computeUseDefSets();

        // Step 2: Iterate until fixpoint
        boolean changed = true;
        int iterations = 0;
        int maxIterations = 1000; // Safety limit

        while (changed && iterations < maxIterations) {
            changed = false;
            iterations++;

            // Process commands in REVERSE order (backwards analysis)
            for (int i = commands.size() - 1; i >= 0; i--) {
                IrCommand cmd = commands.get(i);
                LivenessInfo info = livenessMap.get(cmd);

                // Update OUT[i] = ∪ IN[successor of i]
                Set<Temp> newOut = new HashSet<>();

                // Get successors (next instruction, or jump targets)
                List<IrCommand> successors = getSuccessors(i);
                for (IrCommand succ : successors) {
                    LivenessInfo succInfo = livenessMap.get(succ);
                    newOut.addAll(succInfo.in);
                }

                if (!newOut.equals(info.out)) {
                    info.out = newOut;
                    changed = true;
                }

                // Update IN[i] = USE[i] ∪ (OUT[i] - DEF[i])
                if (info.updateIn()) {
                    changed = true;
                }
            }
        }

        if (iterations >= maxIterations) {
            System.err.println("Warning: Liveness analysis did not converge after " + maxIterations + " iterations");
        }

        return livenessMap;
    }

    /**
     * Compute USE and DEF sets for each IR command
     */
    private void computeUseDefSets()
    {
        for (IrCommand cmd : commands) {
            LivenessInfo info = livenessMap.get(cmd);

            // Extract USE and DEF based on command type
            if (cmd instanceof IrCommandBinopAddIntegers) {
                IrCommandBinopAddIntegers binop = (IrCommandBinopAddIntegers) cmd;
                info.use.add(binop.t1);
                info.use.add(binop.t2);
                info.def.add(binop.dst);
            }
            else if (cmd instanceof IrCommandBinopSubIntegers) {
                IrCommandBinopSubIntegers binop = (IrCommandBinopSubIntegers) cmd;
                info.use.add(binop.t1);
                info.use.add(binop.t2);
                info.def.add(binop.dst);
            }
            else if (cmd instanceof IrCommandBinopMulIntegers) {
                IrCommandBinopMulIntegers binop = (IrCommandBinopMulIntegers) cmd;
                info.use.add(binop.t1);
                info.use.add(binop.t2);
                info.def.add(binop.dst);
            }
            else if (cmd instanceof IrCommandBinopDivIntegers) {
                IrCommandBinopDivIntegers binop = (IrCommandBinopDivIntegers) cmd;
                info.use.add(binop.t1);
                info.use.add(binop.t2);
                info.def.add(binop.dst);
            }
            else if (cmd instanceof IrCommandBinopEqIntegers) {
                IrCommandBinopEqIntegers binop = (IrCommandBinopEqIntegers) cmd;
                info.use.add(binop.t1);
                info.use.add(binop.t2);
                info.def.add(binop.dst);
            }
            else if (cmd instanceof IrCommandBinopLtIntegers) {
                IrCommandBinopLtIntegers binop = (IrCommandBinopLtIntegers) cmd;
                info.use.add(binop.t1);
                info.use.add(binop.t2);
                info.def.add(binop.dst);
            }
            else if (cmd instanceof IRcommandConstInt) {
                IRcommandConstInt constCmd = (IRcommandConstInt) cmd;
                info.def.add(constCmd.dst);
            }
            else if (cmd instanceof IrCommandConstString) {
                IrCommandConstString constCmd = (IrCommandConstString) cmd;
                info.def.add(constCmd.dst);
            }
            else if (cmd instanceof IrCommandNilConst) {
                IrCommandNilConst constCmd = (IrCommandNilConst) cmd;
                info.def.add(constCmd.dst);
            }
            else if (cmd instanceof IrCommandStringConcat) {
                IrCommandStringConcat strCmd = (IrCommandStringConcat) cmd;
                info.use.add(strCmd.str1);
                info.use.add(strCmd.str2);
                info.def.add(strCmd.dst);
            }
            else if (cmd instanceof IrCommandStringEqual) {
                IrCommandStringEqual strCmd = (IrCommandStringEqual) cmd;
                info.use.add(strCmd.str1);
                info.use.add(strCmd.str2);
                info.def.add(strCmd.dst);
            }
            else if (cmd instanceof IrCommandArrayAccess) {
                IrCommandArrayAccess arrCmd = (IrCommandArrayAccess) cmd;
                info.use.add(arrCmd.array);
                info.use.add(arrCmd.index);
                info.def.add(arrCmd.dst);
            }
            else if (cmd instanceof IrCommandArrayStore) {
                IrCommandArrayStore arrCmd = (IrCommandArrayStore) cmd;
                info.use.add(arrCmd.array);
                info.use.add(arrCmd.index);
                info.use.add(arrCmd.value);
                // No DEF (store doesn't define a new temp)
            }
            else if (cmd instanceof IrCommandArrayLength) {
                IrCommandArrayLength arrCmd = (IrCommandArrayLength) cmd;
                info.use.add(arrCmd.array);
                info.def.add(arrCmd.dst);
            }
            else if (cmd instanceof IrCommandNewArray) {
                IrCommandNewArray arrCmd = (IrCommandNewArray) cmd;
                info.use.add(arrCmd.size);
                info.def.add(arrCmd.dst);
            }
            else if (cmd instanceof IrCommandFieldAccess) {
                IrCommandFieldAccess fieldCmd = (IrCommandFieldAccess) cmd;
                info.use.add(fieldCmd.object);
                info.def.add(fieldCmd.dst);
            }
            else if (cmd instanceof IrCommandFieldStore) {
                IrCommandFieldStore fieldCmd = (IrCommandFieldStore) cmd;
                info.use.add(fieldCmd.object);
                info.use.add(fieldCmd.value);
                // No DEF
            }
            else if (cmd instanceof IrCommandNewObject) {
                IrCommandNewObject objCmd = (IrCommandNewObject) cmd;
                info.def.add(objCmd.dst);
            }
            else if (cmd instanceof IrCommandJumpIfEqToZero) {
                IrCommandJumpIfEqToZero jumpCmd = (IrCommandJumpIfEqToZero) cmd;
                info.use.add(jumpCmd.t1);
                // No DEF
            }
            else if (cmd instanceof IrCommandReturn) {
                IrCommandReturn retCmd = (IrCommandReturn) cmd;
                info.use.add(retCmd.returnValue);
                // No DEF
            }
            // IrCommandLabel, IrCommandJumpLabel, IrCommandReturnVoid have no USE/DEF
        }
    }

    /**
     * Get successors of a command
     * For most commands, it's just the next command
     * For jumps, it's the jump target AND the next command (conditional jump)
     * For unconditional jumps and returns, it's just the jump target or exit
     */
    private List<IrCommand> getSuccessors(int index)
    {
        List<IrCommand> successors = new ArrayList<>();
        IrCommand cmd = commands.get(index);

        // Check if this is a jump
        if (cmd instanceof IrCommandJumpLabel) {
            // Unconditional jump: only successor is the target
            IrCommandJumpLabel jump = (IrCommandJumpLabel) cmd;
            IrCommand target = findLabelTarget(jump.label);
            if (target != null) {
                successors.add(target);
            }
        }
        else if (cmd instanceof IrCommandJumpIfEqToZero) {
            // Conditional jump: successors are BOTH next instruction AND jump target
            IrCommandJumpIfEqToZero jump = (IrCommandJumpIfEqToZero) cmd;

            // Add next instruction
            if (index + 1 < commands.size()) {
                successors.add(commands.get(index + 1));
            }

            // Add jump target
            IrCommand target = findLabelTarget(jump.label);
            if (target != null) {
                successors.add(target);
            }
        }
        else if (cmd instanceof IrCommandReturn || cmd instanceof IrCommandReturnVoid) {
            // Return has no successors (exits function)
            // Leave empty
        }
        else {
            // Normal command: successor is next instruction
            if (index + 1 < commands.size()) {
                successors.add(commands.get(index + 1));
            }
        }

        return successors;
    }

    /**
     * Find the IR command that corresponds to a label
     */
    private IrCommand findLabelTarget(String labelName)
    {
        for (IrCommand cmd : commands) {
            if (cmd instanceof IrCommandLabel) {
                IrCommandLabel label = (IrCommandLabel) cmd;
                if (label.label.equals(labelName)) {
                    return cmd;
                }
            }
        }
        return null;
    }

    /**
     * Print liveness information for debugging
     */
    public void printLiveness()
    {
        System.out.println("=== LIVENESS ANALYSIS ===");
        for (int i = 0; i < commands.size(); i++) {
            IrCommand cmd = commands.get(i);
            LivenessInfo info = livenessMap.get(cmd);
            System.out.format("[%3d] %-50s %s\n", i, cmd.toString(), info.toString());
        }
    }
}
