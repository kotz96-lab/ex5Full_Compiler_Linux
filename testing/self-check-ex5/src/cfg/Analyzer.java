package cfg;

import java.util.*;
import java.io.*;
import ir.*;

public class Analyzer {
    private CFG cfg;
    private ChaoticIterations analysis;
    private boolean analysisCompleted;
    private List<String> warnings;
    private List<String> errors;

    public Analyzer() {
        this.analysisCompleted = false;
        this.warnings = new ArrayList<>();
        this.errors = new ArrayList<>();
    }

    public boolean analyze(Ir ir) {
        try {
            System.out.println("\n==================== STARTING STATIC ANALYSIS ====================");
            System.out.println("[1] Building Control Flow Graph...");
            cfg = CFG.buildFromIr(ir);

            if (cfg.getNodes().isEmpty()) {
                warnings.add("Empty IR - no CFG constructed");
                return true;
            }

            System.out.println("CFG constructed with " + cfg.getNodes().size() + " nodes");
            System.out.println("[2] Running Data Flow Analysis...");
            analysis = new ChaoticIterations(cfg);
            boolean fixedPointReached = analysis.runAnalysis();

            if (!fixedPointReached) {
                errors.add("Data flow analysis did not converge to fixed point");
                return false;
            }

            System.out.println("[3] Collecting Analysis Results...");
            collectAnalysisResults();

            analysisCompleted = true;
            System.out.println("Static analysis completed successfully!");
            System.out.println("===================================================================\n");

            return true;

        } catch (Exception e) {
            errors.add("Analysis failed with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void collectAnalysisResults() {
        if (analysis == null) {
            return;
        }

        List<String> uninitVars = analysis.getUninitializedVariables();
        for (String var : uninitVars) {
            warnings.add("Possibly uninitialized variable: " + var);
        }

        for (CFGNode node : cfg.getNodes()) {
            if (node != cfg.getEntryNode() && node.getPredecessors().isEmpty()) {
                warnings.add("Unreachable code detected at: " + node.getCommand().toString());
            }
        }

        detectInfiniteLoops();
    }

    private void detectInfiniteLoops() {
        Set<CFGNode> visited = new HashSet<>();
        Set<CFGNode> recursionStack = new HashSet<>();

        for (CFGNode node : cfg.getNodes()) {
            if (!visited.contains(node)) {
                if (hasCycleDFS(node, visited, recursionStack)) {
                    warnings.add("Potential infinite loop detected involving: " +
                                node.getCommand().toString());
                }
            }
        }
    }
    
    private boolean hasCycleDFS(CFGNode node, Set<CFGNode> visited, Set<CFGNode> recursionStack) {
        visited.add(node);
        recursionStack.add(node);

        for (CFGNode successor : node.getSuccessors()) {
            if (!visited.contains(successor)) {
                if (hasCycleDFS(successor, visited, recursionStack)) {
                    return true;
                }
            } else if (recursionStack.contains(successor)) {
                return true;
            }
        }

        recursionStack.remove(node);
        return false;
    }

    public CFG getCFG() {
        return cfg;
    }

    public ChaoticIterations getAnalysis() {
        return analysis;
    }

    public boolean isAnalysisCompleted() {
        return analysisCompleted;
    }

    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }

    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    public void printReport() {
        System.out.println("\n==================== ANALYSIS REPORT ====================");

        if (!analysisCompleted) {
            System.out.println("Analysis not completed or failed.");
            if (!errors.isEmpty()) {
                System.out.println("Errors:");
                for (String error : errors) {
                    System.out.println("  ERROR: " + error);
                }
            }
            System.out.println("=========================================================\n");
            return;
        }

        System.out.println("Analysis completed in " + analysis.getIterationCount() + " iterations");
        System.out.println("CFG has " + cfg.getNodes().size() + " nodes");
        System.out.println();

        if (errors.isEmpty() && warnings.isEmpty()) {
            System.out.println("No issues detected!");
        } else {
            if (!errors.isEmpty()) {
                System.out.println("Errors found:");
                for (String error : errors) {
                    System.out.println("  " + error);
                }
                System.out.println();
            }

            if (!warnings.isEmpty()) {
                System.out.println("Warnings:");
                for (String warning : warnings) {
                    System.out.println("  " + warning);
                }
                System.out.println();
            }
        }

        System.out.println("=========================================================\n");
    }

    public void writeOutputFile(PrintWriter writer) {
        if (!analysisCompleted || analysis == null) {
            writer.write("!OK");
            writer.flush();
            return;
        }

        List<String> uninitializedVars = analysis.getUninitializedVariables();

        if (uninitializedVars.isEmpty()) {
            writer.write("!OK");
        } else {
            Collections.sort(uninitializedVars);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < uninitializedVars.size(); i++) {
                sb.append(uninitializedVars.get(i));
                if (i < uninitializedVars.size() - 1) {
                    sb.append('\n');
                }
            }
            writer.write(sb.toString());
        }
        writer.flush();
    }

    public void exportCFGDotFile(String filename) {
        if (cfg != null) {
            cfg.saveDotGraphToFile(filename);
        }
    }

    public void exportAnalysisResults(String filename) {
        if (!analysisCompleted) {
            return;
        }

        try {
            PrintWriter writer = new PrintWriter(filename);
            writer.println("Static Analysis Results");
            writer.println("=======================");
            writer.println();

            writer.println("CFG Statistics:");
            writer.println("  Nodes: " + cfg.getNodes().size());
            writer.println("  Entry Node: " + (cfg.getEntryNode() != null ? cfg.getEntryNode().getNodeId() : "None"));
            writer.println("  Exit Nodes: " + cfg.getExitNodes().size());
            writer.println();

            writer.println("Analysis Statistics:");
            writer.println("  Iterations: " + analysis.getIterationCount());
            writer.println("  Errors: " + errors.size());
            writer.println("  Warnings: " + warnings.size());
            writer.println();

            if (!errors.isEmpty()) {
                writer.println("Errors:");
                for (String error : errors) {
                    writer.println("  - " + error);
                }
                writer.println();
            }

            if (!warnings.isEmpty()) {
                writer.println("Warnings:");
                for (String warning : warnings) {
                    writer.println("  - " + warning);
                }
                writer.println();
            }

            writer.close();
            System.out.println("Analysis results exported to: " + filename);
        } catch (IOException e) {
            System.err.println("Failed to export analysis results: " + e.getMessage());
        }
    }
}
