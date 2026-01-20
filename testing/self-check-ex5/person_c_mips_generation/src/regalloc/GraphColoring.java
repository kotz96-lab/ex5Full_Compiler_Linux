package regalloc;

import temp.Temp;
import java.util.*;

/**
 * Graph Coloring for Register Allocation
 *
 * Assigns colors (registers) to nodes (temporaries) in the interference graph.
 * Goal: No two adjacent nodes have the same color.
 * Constraint: Use at most K colors (K=10 for $t0-$t9).
 *
 * Algorithm: Simplification-based coloring
 * 1. Repeatedly remove nodes with degree < K (push on stack)
 * 2. If all remaining nodes have degree >= K: FAILURE (spilling needed)
 * 3. If graph is empty: SUCCESS, proceed to color
 * 4. Pop nodes from stack and assign colors (always possible if we reached here)
 */
public class GraphColoring
{
    private static final int NUM_REGISTERS = 10; // $t0 through $t9
    private static final String[] REGISTER_NAMES = {
        "$t0", "$t1", "$t2", "$t3", "$t4",
        "$t5", "$t6", "$t7", "$t8", "$t9"
    };

    private InterferenceGraph graph;

    public GraphColoring(InterferenceGraph graph)
    {
        this.graph = graph;
    }

    /**
     * Perform graph coloring
     * @return RegisterAllocation result (success + color mapping)
     */
    public RegisterAllocation color()
    {
        // Make a copy of the graph (we'll modify it)
        InterferenceGraph workingGraph = graph.copy();

        // Stack to remember the order of removed nodes
        Stack<Temp> stack = new Stack<>();

        // Simplification phase
        boolean success = simplify(workingGraph, stack);

        if (!success) {
            // Coloring failed - need more than 10 registers
            return new RegisterAllocation(false, null);
        }

        // Coloring phase
        Map<Temp, String> colors = assignColors(stack);

        return new RegisterAllocation(true, colors);
    }

    /**
     * Simplification phase
     * Repeatedly remove nodes with degree < K
     *
     * @param graph working graph (will be modified)
     * @param stack stack to store removed nodes
     * @return true if successful, false if all nodes have degree >= K
     */
    private boolean simplify(InterferenceGraph graph, Stack<Temp> stack)
    {
        while (!graph.isEmpty()) {
            // Find a node with degree < K
            Temp lowDegreeNode = graph.findNodeWithDegreeLessThan(NUM_REGISTERS);

            if (lowDegreeNode != null) {
                // Found one! Remove it and push on stack
                stack.push(lowDegreeNode);
                graph.removeNode(lowDegreeNode);
            }
            else {
                // All remaining nodes have degree >= K
                // This means we can't color the graph with K colors
                // In a real compiler, we'd choose a node to spill to memory
                // For this exercise, we just fail
                return false;
            }
        }

        // Successfully simplified the entire graph
        return true;
    }

    /**
     * Coloring phase
     * Pop nodes from stack and assign colors
     *
     * Since we removed nodes with degree < K, we're guaranteed
     * that when we pop a node, it has < K neighbors, so we can
     * always find a free color.
     *
     * @param stack stack of nodes in reverse order
     * @return mapping from temp to register name
     */
    private Map<Temp, String> assignColors(Stack<Temp> stack)
    {
        Map<Temp, String> colors = new HashMap<>();
        Set<String> availableColors = new HashSet<>(Arrays.asList(REGISTER_NAMES));

        while (!stack.isEmpty()) {
            Temp node = stack.pop();

            // Find which colors are used by neighbors
            Set<String> usedColors = new HashSet<>();
            for (Temp neighbor : graph.getNeighbors(node)) {
                if (colors.containsKey(neighbor)) {
                    usedColors.add(colors.get(neighbor));
                }
            }

            // Find a free color
            String chosenColor = null;
            for (String color : availableColors) {
                if (!usedColors.contains(color)) {
                    chosenColor = color;
                    break;
                }
            }

            if (chosenColor == null) {
                // This should never happen if simplification succeeded
                System.err.println("ERROR: No free color found during coloring phase!");
                System.err.format("Node: Temp_%d, Neighbors: %d, Used colors: %s\n",
                    node.getSerialNumber(),
                    graph.getNeighbors(node).size(),
                    usedColors);
                return null;
            }

            colors.put(node, chosenColor);
        }

        return colors;
    }

    /**
     * Print coloring result for debugging
     */
    public static void printColoring(RegisterAllocation allocation)
    {
        if (!allocation.success) {
            System.out.println("=== REGISTER ALLOCATION FAILED ===");
            return;
        }

        System.out.println("=== REGISTER ALLOCATION ===");
        Map<Temp, String> colors = allocation.assignment;

        List<Temp> temps = new ArrayList<>(colors.keySet());
        temps.sort(Comparator.comparingInt(Temp::getSerialNumber));

        for (Temp t : temps) {
            System.out.format("Temp_%d → %s\n", t.getSerialNumber(), colors.get(t));
        }
    }
}
