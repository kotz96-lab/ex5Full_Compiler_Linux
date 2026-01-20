package regalloc;

import ir.*;
import temp.Temp;
import java.util.*;

/**
 * Register Allocator
 *
 * Main orchestrator for register allocation.
 * Performs the following steps:
 * 1. Liveness analysis on IR
 * 2. Build interference graph
 * 3. Color the graph (assign registers)
 * 4. Return register mapping or failure
 *
 * Usage:
 *   RegisterAllocator allocator = new RegisterAllocator();
 *   RegisterAllocation result = allocator.allocate(irCommands);
 *   if (!result.success) {
 *       System.out.println("Register Allocation Failed");
 *   } else {
 *       String reg = result.getRegister(someTemp);
 *   }
 */
public class RegisterAllocator
{
    private boolean verbose = false;  // Set to true for debug output

    public RegisterAllocator()
    {
    }

    public RegisterAllocator(boolean verbose)
    {
        this.verbose = verbose;
    }

    /**
     * Perform register allocation
     *
     * @param commands list of IR commands
     * @return RegisterAllocation result
     */
    public RegisterAllocation allocate(List<IrCommand> commands)
    {
        if (commands == null || commands.isEmpty()) {
            return new RegisterAllocation(true, new HashMap<>());
        }

        // Step 1: Liveness Analysis
        if (verbose) System.out.println("\n=== STEP 1: LIVENESS ANALYSIS ===");
        LivenessAnalysis liveness = new LivenessAnalysis(commands);
        Map<IrCommand, LivenessInfo> livenessMap = liveness.analyze();

        if (verbose) liveness.printLiveness();

        // Step 2: Build Interference Graph
        if (verbose) System.out.println("\n=== STEP 2: BUILD INTERFERENCE GRAPH ===");
        InterferenceGraph interferenceGraph = buildInterferenceGraph(commands, livenessMap);

        if (verbose) interferenceGraph.printGraph();

        // Step 3: Graph Coloring
        if (verbose) System.out.println("\n=== STEP 3: GRAPH COLORING ===");
        GraphColoring coloring = new GraphColoring(interferenceGraph);
        RegisterAllocation result = coloring.color();

        if (verbose) GraphColoring.printColoring(result);

        return result;
    }

    /**
     * Build interference graph from liveness information
     *
     * Two temporaries interfere if they are both live at the same time.
     * Strategy: For each instruction, all temps in OUT[i] interfere with each other.
     *
     * @param commands list of IR commands
     * @param livenessMap liveness information for each command
     * @return interference graph
     */
    private InterferenceGraph buildInterferenceGraph(
        List<IrCommand> commands,
        Map<IrCommand, LivenessInfo> livenessMap)
    {
        InterferenceGraph graph = new InterferenceGraph();

        // Add all temporaries as nodes
        Set<Temp> allTemps = new HashSet<>();
        for (LivenessInfo info : livenessMap.values()) {
            allTemps.addAll(info.use);
            allTemps.addAll(info.def);
        }

        for (Temp t : allTemps) {
            graph.addNode(t);
        }

        // Add interference edges
        for (IrCommand cmd : commands) {
            LivenessInfo info = livenessMap.get(cmd);

            // All temps in OUT[i] are live simultaneously
            // Therefore, they all interfere with each other
            List<Temp> liveTemps = new ArrayList<>(info.out);

            for (int i = 0; i < liveTemps.size(); i++) {
                for (int j = i + 1; j < liveTemps.size(); j++) {
                    graph.addEdge(liveTemps.get(i), liveTemps.get(j));
                }
            }

            // Special case: If this instruction defines a temp,
            // that temp interferes with all temps in OUT (except itself)
            // This handles the case where a temp is defined but immediately dies
            if (!info.def.isEmpty()) {
                for (Temp defTemp : info.def) {
                    for (Temp outTemp : info.out) {
                        if (!defTemp.equals(outTemp)) {
                            graph.addEdge(defTemp, outTemp);
                        }
                    }
                }
            }
        }

        return graph;
    }

    /**
     * Convenience method to allocate from Ir singleton
     */
    public RegisterAllocation allocate(Ir ir)
    {
        // Ir class needs a method to get commands list
        // For now, we'll need to modify Ir to add this
        // or extract commands manually
        throw new UnsupportedOperationException(
            "Need to add getCommands() method to Ir class");
    }
}
