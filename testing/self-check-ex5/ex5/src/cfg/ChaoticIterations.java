package cfg;

import java.util.*;
import ir.*;
import temp.*;

public class ChaoticIterations {
    private CFG cfg;
    private Map<CFGNode, InitializationState> inStates;
    private Map<CFGNode, InitializationState> outStates;
    private Set<CFGNode> visited;
    private Set<String> uninitializedVariables;
    private int maxIterations;
    private int currentIteration;

    public ChaoticIterations(CFG cfg) {
        this.cfg = cfg;
        this.inStates = new HashMap<>();
        this.outStates = new HashMap<>();
        this.visited = new HashSet<>();
        this.uninitializedVariables = new HashSet<>();
        this.maxIterations = 1000;
        this.currentIteration = 0;
    }
    
    public boolean runAnalysis() {
        System.out.println("\n==================== CHAOTIC ITERATIONS ====================");

        initializeStates();

        if (cfg.getEntryNode() != null) {
            inStates.put(cfg.getEntryNode(), new InitializationState());
        }

        Queue<CFGNode> worklist = new LinkedList<>(cfg.getNodes());
        currentIteration = 0;

        while (!worklist.isEmpty() && currentIteration < maxIterations) {
            currentIteration++;
            CFGNode node = worklist.poll();
            InitializationState newInState = computeInState(node);

            inStates.put(node, newInState);
            InitializationState newOutState = computeOutState(node, newInState);
            InitializationState oldOutState = outStates.get(node);
            outStates.put(node, newOutState);

            visited.add(node);

            if (!newOutState.equals(oldOutState)) {
                for (CFGNode successor : node.getSuccessors()) {
                    if (!worklist.contains(successor)) {
                        worklist.add(successor);
                    }
                }
            }
        }

        System.out.println("Chaotic iterations completed in " + currentIteration + " iterations");
        System.out.println("=============================================================");

        return true;
    }

    private void initializeStates() {
        for (CFGNode node : cfg.getNodes()) {
            inStates.put(node, new InitializationState());
            outStates.put(node, new InitializationState());
        }
    }
    
    private InitializationState computeInState(CFGNode node) {
        if (node == cfg.getEntryNode()) {
            return new InitializationState();
        }

        InitializationState result = null;

        for (CFGNode predecessor : node.getPredecessors()) {
            if (!visited.contains(predecessor)) {
                continue;
            }

            InitializationState predOutState = outStates.get(predecessor);
            if (predOutState != null) {
                if (result == null) {
                    result = new InitializationState(predOutState);
                } else {
                    result = result.meet(predOutState);
                }
            }
        }

        return result != null ? result : new InitializationState();
    }

    private InitializationState computeOutState(CFGNode node, InitializationState inState) {
        return applyTransferFunction(node.getCommand(), new InitializationState(inState));
    }

    private InitializationState applyTransferFunction(IrCommand command, InitializationState state) {
        if (command instanceof IRcommandConstInt) {
            IRcommandConstInt cmd = (IRcommandConstInt) command;
            String tempName = "Temp_" + cmd.t.getSerialNumber();
            state.markInitialized(tempName);

        } else if (command instanceof IrCommandLoad) {
            IrCommandLoad cmd = (IrCommandLoad) command;
            String varName = cmd.varName;
            String tempName = "Temp_" + cmd.dst.getSerialNumber();

            if (!state.isInitialized(varName)) {
                uninitializedVariables.add(varName);
            }

            if (state.isInitialized(varName)) {
                state.markInitialized(tempName);
            }

        } else if (command instanceof IrCommandStore) {
            IrCommandStore cmd = (IrCommandStore) command;
            String varName = cmd.varName;
            state.markInitialized(varName);

        } else if (command instanceof IrCommandBinopAddIntegers) {
            IrCommandBinopAddIntegers cmd = (IrCommandBinopAddIntegers) command;
            handleBinop(cmd.dst, cmd.t1, cmd.t2, state);

        } else if (command instanceof IrCommandBinopSubIntegers) {
            IrCommandBinopSubIntegers cmd = (IrCommandBinopSubIntegers) command;
            handleBinop(cmd.dst, cmd.t1, cmd.t2, state);

        } else if (command instanceof IrCommandBinopMulIntegers) {
            IrCommandBinopMulIntegers cmd = (IrCommandBinopMulIntegers) command;
            handleBinop(cmd.dst, cmd.t1, cmd.t2, state);

        } else if (command instanceof IrCommandBinopDivIntegers) {
            IrCommandBinopDivIntegers cmd = (IrCommandBinopDivIntegers) command;
            handleBinop(cmd.dst, cmd.t1, cmd.t2, state);

        } else if (command instanceof IrCommandBinopLtIntegers) {
            IrCommandBinopLtIntegers cmd = (IrCommandBinopLtIntegers) command;
            handleBinop(cmd.dst, cmd.t1, cmd.t2, state);

        } else if (command instanceof IrCommandBinopEqIntegers) {
            IrCommandBinopEqIntegers cmd = (IrCommandBinopEqIntegers) command;
            handleBinop(cmd.dst, cmd.t1, cmd.t2, state);

        } else if (command instanceof IrCommandBinopMinusInteger) {
            IrCommandBinopMinusInteger cmd = (IrCommandBinopMinusInteger) command;
            String dstName = "Temp_" + cmd.dst.getSerialNumber();
            String srcName = "Temp_" + cmd.t.getSerialNumber();

            if (state.isInitialized(srcName)) {
                state.markInitialized(dstName);
            }

        } else if (command instanceof IrCommandCallFunc) {
            IrCommandCallFunc cmd = (IrCommandCallFunc) command;
            String tempName = "Temp_" + cmd.t.getSerialNumber();

            if (!state.isInitialized(tempName)) {
                uninitializedVariables.add(tempName);
            }

        } else if (command instanceof IrCommandJumpIfEqToZero) {
            IrCommandJumpIfEqToZero cmd = (IrCommandJumpIfEqToZero) command;
            String tempName = "Temp_" + cmd.t.getSerialNumber();

            if (!state.isInitialized(tempName)) {
                uninitializedVariables.add(tempName);
            }
        }

        return state;
    }

    private void handleBinop(Temp dst, Temp t1, Temp t2, InitializationState state) {
        String dstName = "Temp_" + dst.getSerialNumber();
        String t1Name = "Temp_" + t1.getSerialNumber();
        String t2Name = "Temp_" + t2.getSerialNumber();

        if (state.isInitialized(t1Name) && state.isInitialized(t2Name)) {
            state.markInitialized(dstName);
        }
    }
    
    public Map<CFGNode, InitializationState> getInStates() {
        return new HashMap<>(inStates);
    }

    public Map<CFGNode, InitializationState> getOutStates() {
        return new HashMap<>(outStates);
    }

    public int getIterationCount() {
        return currentIteration;
    }

    public void printAnalysisResults() {
        System.out.println("\n==================== ANALYSIS RESULTS ====================");

        for (CFGNode node : cfg.getNodes()) {
            System.out.println("Node " + node.getNodeId() + ": " + node.getCommand());
            System.out.println("  IN:  " + inStates.get(node));
            System.out.println("  OUT: " + outStates.get(node));
            System.out.println();
        }

        System.out.println("==========================================================");
    }

    public List<String> getUninitializedVariables() {
        Set<String> result = new HashSet<>();

        for (CFGNode node : cfg.getNodes()) {
            IrCommand cmd = node.getCommand();
            InitializationState inState = inStates.get(node);

            if (cmd instanceof IrCommandLoad) {
                IrCommandLoad load = (IrCommandLoad) cmd;
                if (!inState.isInitialized(load.varName)) {
                    if (!load.varName.startsWith("Temp_")) {
                        String cleanName = load.varName.replaceAll("_\\d+$", "");
                        result.add(cleanName);
                    }
                }
            } else if (cmd instanceof IrCommandCallFunc) {
                IrCommandCallFunc call = (IrCommandCallFunc) cmd;
                String tempName = "Temp_" + call.t.getSerialNumber();
                if (!inState.isInitialized(tempName)) {

                }
            } else if (cmd instanceof IrCommandJumpIfEqToZero) {
                IrCommandJumpIfEqToZero jump = (IrCommandJumpIfEqToZero) cmd;
                String tempName = "Temp_" + jump.t.getSerialNumber();
                if (!inState.isInitialized(tempName)) {

                }
            }
        }

        return new ArrayList<>(result);
    }
}