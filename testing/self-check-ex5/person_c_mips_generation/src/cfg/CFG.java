package cfg;

import java.util.*;
import ir.*;

public class CFG {
    
    private List<CFGNode> nodes;
    private CFGNode entryNode;
    private Set<CFGNode> exitNodes;
    private Map<String, CFGNode> labelToNodeMap;
    
    
    public CFG() {
        this.nodes = new ArrayList<>();
        this.exitNodes = new HashSet<>();
        this.labelToNodeMap = new HashMap<>();
    }
    
    
    public static CFG buildFromIr(Ir ir) {
        CFG cfg = new CFG();
        
        
        List<IrCommand> commands = cfg.extractCommandsFromIr(ir);
        
        if (commands.isEmpty()) {
            return cfg;
        }
        
        
        cfg.createNodesFromCommands(commands);
        
        
        cfg.buildLabelMapping();
        
        
        cfg.connectNodes();
        
        
        cfg.setEntryAndExitNodes();
        
        return cfg;
    }
    
    
    private List<IrCommand> extractCommandsFromIr(Ir ir) {
        List<IrCommand> commands = new ArrayList<>();
        
        try {
            // Use reflection to access the head and tail fields of Ir
            java.lang.reflect.Field headField = Ir.class.getDeclaredField("head");
            java.lang.reflect.Field tailField = Ir.class.getDeclaredField("tail");
            headField.setAccessible(true);
            tailField.setAccessible(true);
            
            IrCommand head = (IrCommand) headField.get(ir);
            IrCommandList tail = (IrCommandList) tailField.get(ir);
            
            // Add head command
            if (head != null) {
                commands.add(head);
            }
            
            // Add tail commands
            IrCommandList current = tail;
            while (current != null) {
                if (current.head != null) {
                    commands.add(current.head);
                }
                current = current.tail;
            }
        } catch (Exception e) {
            System.err.println("Error extracting IR commands: " + e.getMessage());
        }
        
        return commands;
    }
    
    // Create nodes from commands
    private void createNodesFromCommands(List<IrCommand> commands) {
        for (IrCommand command : commands) {
            CFGNode node = new CFGNode(command);
            nodes.add(node);
        }
    }
    
    // Build label mapping
    private void buildLabelMapping() {
        for (CFGNode node : nodes) {
            if (node.isJumpTarget()) {
                String labelName = node.getLabelName();
                if (labelName != null) {
                    labelToNodeMap.put(labelName, node);
                }
            }
        }
    }
    
    // Connect nodes
    private void connectNodes() {
        for (int i = 0; i < nodes.size(); i++) {
            CFGNode currentNode = nodes.get(i);
            
            if (currentNode.isUnconditionalJump()) {
                // Unconditional jump: connect to target label only
                String targetLabel = currentNode.getJumpTarget();
                CFGNode targetNode = labelToNodeMap.get(targetLabel);
                if (targetNode != null) {
                    currentNode.addSuccessor(targetNode);
                }
            } else if (currentNode.isConditionalJump()) {
                // Conditional jump: connect to both target and next instruction
                String targetLabel = currentNode.getJumpTarget();
                CFGNode targetNode = labelToNodeMap.get(targetLabel);
                if (targetNode != null) {
                    currentNode.addSuccessor(targetNode);
                }
                
                // Also connect to next instruction (fall-through)
                if (i + 1 < nodes.size()) {
                    currentNode.addSuccessor(nodes.get(i + 1));
                }
            } else {
                // Regular instruction: connect to next instruction
                if (i + 1 < nodes.size()) {
                    // Don't connect if the next instruction is a label that's not the immediate next
                    CFGNode nextNode = nodes.get(i + 1);
                    currentNode.addSuccessor(nextNode);
                }
            }
        }
    }
    
    // Set entry and exit nodes
    private void setEntryAndExitNodes() {
        if (!nodes.isEmpty()) {
            entryNode = nodes.get(0);
        }
        
        // Exit nodes are those with no successors
        for (CFGNode node : nodes) {
            if (node.getSuccessors().isEmpty()) {
                exitNodes.add(node);
            }
        }
    }
    
    // Getters
    public List<CFGNode> getNodes() {
        return new ArrayList<>(nodes);
    }
    
    public CFGNode getEntryNode() {
        return entryNode;
    }
    
    public Set<CFGNode> getExitNodes() {
        return new HashSet<>(exitNodes);
    }
    
    public CFGNode getLabelNode(String labelName) {
        return labelToNodeMap.get(labelName);
    }
    
    // Utility methods
    public void printCFG() {
        System.out.println("\n==================== CONTROL FLOW GRAPH ====================");
        System.out.println("Entry Node: " + (entryNode != null ? entryNode.toString() : "None"));
        System.out.println("Exit Nodes: " + exitNodes.size());
        System.out.println("Total Nodes: " + nodes.size());
        System.out.println();
        
        for (CFGNode node : nodes) {
            System.out.println(node.toString());
            System.out.println("  Successors: ");
            for (CFGNode successor : node.getSuccessors()) {
                System.out.println("    -> " + successor.toString());
            }
            System.out.println("  Predecessors: ");
            for (CFGNode predecessor : node.getPredecessors()) {
                System.out.println("    <- " + predecessor.toString());
            }
            System.out.println();
        }
        System.out.println("===========================================================");
    }
    
    // DOT graph generation
    public String toDotGraph() {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph CFG {\n");
        dot.append("  rankdir=TB;\n");
        dot.append("  node [shape=box];\n");
        
        // Add nodes
        for (CFGNode node : nodes) {
            String label = node.getCommand().toString().replace("\"", "\\\"");
            String shape = "box";
            String color = "black";
            
            if (node == entryNode) {
                color = "green";
            } else if (exitNodes.contains(node)) {
                color = "red";
            }
            
            dot.append(String.format("  node%d [label=\"%s\" color=%s];\n", 
                      node.getNodeId(), label, color));
        }
        
        // Add edges
        for (CFGNode node : nodes) {
            for (CFGNode successor : node.getSuccessors()) {
                dot.append(String.format("  node%d -> node%d;\n", 
                          node.getNodeId(), successor.getNodeId()));
            }
        }
        
        dot.append("}\n");
        return dot.toString();
    }
    
    // Save DOT graph to file
    public void saveDotGraphToFile(String filename) {
        try {
            java.io.PrintWriter writer = new java.io.PrintWriter(filename);
            writer.println(toDotGraph());
            writer.close();
            System.out.println("CFG dot graph saved to: " + filename);
        } catch (Exception e) {
            System.err.println("Error saving CFG dot graph: " + e.getMessage());
        }
    }
}