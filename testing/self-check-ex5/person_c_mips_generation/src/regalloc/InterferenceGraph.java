package regalloc;

import temp.Temp;
import java.util.*;

/**
 * Interference Graph
 *
 * Represents which temporaries interfere with each other.
 * Two temporaries interfere if they are both live at the same time.
 *
 * Graph structure:
 * - Nodes: temporaries
 * - Edges: interference relationships (undirected)
 *
 * If two temps interfere, they CANNOT be assigned the same register.
 */
public class InterferenceGraph
{
    // Adjacency list: temp → set of temps it interferes with
    private Map<Temp, Set<Temp>> adjacencyList;

    public InterferenceGraph()
    {
        this.adjacencyList = new HashMap<>();
    }

    /**
     * Add a node (temporary) to the graph
     */
    public void addNode(Temp t)
    {
        if (!adjacencyList.containsKey(t)) {
            adjacencyList.put(t, new HashSet<>());
        }
    }

    /**
     * Add an edge between two temporaries (they interfere)
     * Undirected edge: add both t1→t2 and t2→t1
     */
    public void addEdge(Temp t1, Temp t2)
    {
        if (t1.equals(t2)) return; // No self-edges

        addNode(t1);
        addNode(t2);

        adjacencyList.get(t1).add(t2);
        adjacencyList.get(t2).add(t1);
    }

    /**
     * Get the degree of a node (number of neighbors)
     */
    public int getDegree(Temp t)
    {
        if (!adjacencyList.containsKey(t)) {
            return 0;
        }
        return adjacencyList.get(t).size();
    }

    /**
     * Get neighbors of a temporary
     */
    public Set<Temp> getNeighbors(Temp t)
    {
        if (!adjacencyList.containsKey(t)) {
            return new HashSet<>();
        }
        return new HashSet<>(adjacencyList.get(t));
    }

    /**
     * Get all nodes (temporaries) in the graph
     */
    public Set<Temp> getNodes()
    {
        return new HashSet<>(adjacencyList.keySet());
    }

    /**
     * Remove a node from the graph
     * Also removes all edges to/from this node
     */
    public void removeNode(Temp t)
    {
        if (!adjacencyList.containsKey(t)) {
            return;
        }

        // Remove edges from neighbors to this node
        for (Temp neighbor : adjacencyList.get(t)) {
            adjacencyList.get(neighbor).remove(t);
        }

        // Remove the node itself
        adjacencyList.remove(t);
    }

    /**
     * Check if the graph is empty
     */
    public boolean isEmpty()
    {
        return adjacencyList.isEmpty();
    }

    /**
     * Get the number of nodes
     */
    public int size()
    {
        return adjacencyList.size();
    }

    /**
     * Find a node with degree < k
     * Returns null if no such node exists
     */
    public Temp findNodeWithDegreeLessThan(int k)
    {
        for (Temp t : adjacencyList.keySet()) {
            if (getDegree(t) < k) {
                return t;
            }
        }
        return null;
    }

    /**
     * Print the interference graph for debugging
     */
    public void printGraph()
    {
        System.out.println("=== INTERFERENCE GRAPH ===");
        System.out.println("Nodes: " + adjacencyList.size());

        List<Temp> temps = new ArrayList<>(adjacencyList.keySet());
        temps.sort(Comparator.comparingInt(Temp::getSerialNumber));

        for (Temp t : temps) {
            System.out.format("Temp_%d (degree=%d): ", t.getSerialNumber(), getDegree(t));

            List<Temp> neighbors = new ArrayList<>(adjacencyList.get(t));
            neighbors.sort(Comparator.comparingInt(Temp::getSerialNumber));

            boolean first = true;
            for (Temp neighbor : neighbors) {
                if (!first) System.out.print(", ");
                System.out.format("Temp_%d", neighbor.getSerialNumber());
                first = false;
            }
            System.out.println();
        }
    }

    /**
     * Create a copy of this graph
     */
    public InterferenceGraph copy()
    {
        InterferenceGraph copy = new InterferenceGraph();

        for (Temp t : adjacencyList.keySet()) {
            copy.addNode(t);
        }

        for (Temp t : adjacencyList.keySet()) {
            for (Temp neighbor : adjacencyList.get(t)) {
                // Only add once (avoid duplicating edges)
                if (t.getSerialNumber() < neighbor.getSerialNumber()) {
                    copy.addEdge(t, neighbor);
                }
            }
        }

        return copy;
    }
}
