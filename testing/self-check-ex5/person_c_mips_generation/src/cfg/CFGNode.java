package cfg;

import java.util.*;
import ir.*;

public class CFGNode {
    private static int nodeCounter = 0;

    private int nodeId;
    private IrCommand command;
    private Set<CFGNode> predecessors;
    private Set<CFGNode> successors;

    private Set<String> inSet;
    private Set<String> outSet;
    private Set<String> genSet;
    private Set<String> killSet;

    public CFGNode(IrCommand command) {
        this.nodeId = nodeCounter++;
        this.command = command;
        this.predecessors = new HashSet<>();
        this.successors = new HashSet<>();
        this.inSet = new HashSet<>();
        this.outSet = new HashSet<>();
        this.genSet = new HashSet<>();
        this.killSet = new HashSet<>();
        computeGenKillSets();
    }

    public int getNodeId() {
        return nodeId;
    }

    public IrCommand getCommand() {
        return command;
    }

    public Set<CFGNode> getPredecessors() {
        return predecessors;
    }

    public Set<CFGNode> getSuccessors() {
        return successors;
    }

    public Set<String> getInSet() {
        return new HashSet<>(inSet);
    }

    public Set<String> getOutSet() {
        return new HashSet<>(outSet);
    }

    public Set<String> getGenSet() {
        return new HashSet<>(genSet);
    }

    public Set<String> getKillSet() {
        return new HashSet<>(killSet);
    }

    public void setInSet(Set<String> inSet) {
        this.inSet = new HashSet<>(inSet);
    }

    public void setOutSet(Set<String> outSet) {
        this.outSet = new HashSet<>(outSet);
    }

    public void addSuccessor(CFGNode successor) {
        successors.add(successor);
        successor.predecessors.add(this);
    }

    public void addPredecessor(CFGNode predecessor) {
        predecessors.add(predecessor);
        predecessor.successors.add(this);
    }

    private void computeGenKillSets() {
        if (command instanceof IrCommandStore) {
            IrCommandStore store = (IrCommandStore) command;
        } else if (command instanceof IrCommandAllocate) {
            IrCommandAllocate allocate = (IrCommandAllocate) command;
        }
    }

    public Set<String> computeOut(Set<String> in) {
        Set<String> result = new HashSet<>(in);
        result.removeAll(killSet);
        result.addAll(genSet);
        return result;
    }
    
    public boolean isJumpTarget() {
        return command instanceof IrCommandLabel;
    }

    public boolean isConditionalJump() {
        return command instanceof IrCommandJumpIfEqToZero;
    }

    public boolean isUnconditionalJump() {
        return command instanceof IrCommandJumpLabel;
    }

    public String getJumpTarget() {
        if (command instanceof IrCommandJumpIfEqToZero) {
            return ((IrCommandJumpIfEqToZero) command).labelName;
        } else if (command instanceof IrCommandJumpLabel) {
            return ((IrCommandJumpLabel) command).labelName;
        }
        return null;
    }

    public String getLabelName() {
        if (command instanceof IrCommandLabel) {
            return ((IrCommandLabel) command).labelName;
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("CFGNode[%d]: %s", nodeId, command.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CFGNode cfgNode = (CFGNode) obj;
        return nodeId == cfgNode.nodeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }
}