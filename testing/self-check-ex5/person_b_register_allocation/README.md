# Person B: Register Allocation - Work Plan

## Overview
Implement liveness analysis and register allocation to map IR temporaries to 10 physical MIPS registers ($t0-$t9).

## Responsibility
Take the IR from Person A and allocate all temporaries to physical registers using graph coloring.

## The Challenge
This is the **hardest part** of the entire compiler project because:
- Liveness analysis is a complex dataflow problem
- Graph coloring is NP-complete
- Must handle allocation failures gracefully
- Need to understand the entire IR structure

## What Person B Needs to Do

### 1. Liveness Analysis ⭐ (CRITICAL)
Determine which temporaries are "live" at each point in the program.

**Definition:** A temporary is "live" at a point if:
- It has been defined (assigned a value)
- It will be used later (before being redefined)

**Algorithm:** Backward dataflow analysis
- Start from end of each basic block
- Work backwards through instructions
- Track which temps are used (IN set) and defined (OUT set)
- Iterate until fixpoint is reached

**Example:**
```
1: Temp_1 := 5
2: Temp_2 := 10
3: Temp_3 := Temp_1 + Temp_2    // Temp_1 and Temp_2 are live here
4: Temp_4 := Temp_3 * 2         // Temp_3 is live, Temp_1 & Temp_2 are dead
```

### 2. Build Interference Graph ⭐
Create a graph where:
- **Nodes** = temporaries
- **Edges** = two temporaries are live at the same time (interfere)

If two temps interfere, they **cannot** be assigned the same register.

**Example:**
```
Temp_1 and Temp_2 are both live at line 3
→ Add edge between Temp_1 and Temp_2 in interference graph
→ They must get different registers
```

### 3. Graph Coloring ⭐ (HARD!)
Assign colors (registers) to nodes (temporaries) such that:
- No two adjacent nodes have the same color
- Use at most 10 colors ($t0 through $t9)

**Simplification Algorithm:**
1. Find a node with < 10 neighbors
2. Remove it from the graph (push on stack)
3. Repeat until graph is empty OR all nodes have ≥ 10 neighbors
4. If graph is empty: SUCCESS, proceed to step 5
5. If all nodes have ≥ 10 neighbors: FAILURE (allocation impossible)
6. Pop nodes from stack and assign colors

**Why it works:**
- A node with < 10 neighbors can always be colored (we have 10 colors)
- By removing it, we simplify the graph
- When we pop it back, we can find a free color

### 4. Handle Allocation Failure
If simplification fails (all nodes have ≥ 10 neighbors):
- Print "Register Allocation Failed"
- Terminate compilation

**Note:** We are NOT implementing spilling (saving to memory).

## Input from Person A

Person A provides:
- **IR** via `Ir.getInstance()` - linked list of IR commands
- **Temporaries** - all temps created via `TempFactory`

## Output to Person C

Person B must provide:
- **Register mapping** - Map from Temp → register name
  - Example: `Temp_5 → "$t3"`
  - Example: `Temp_12 → "$t7"`

Suggested format:
```java
Map<Temp, String> registerAllocation = new HashMap<>();
registerAllocation.put(temp5, "$t3");
registerAllocation.put(temp12, "$t7");
```

## Files to Create

### Core Algorithm Files
1. **LivenessAnalysis.java**
   - Compute live-in and live-out sets for each IR command
   - Use dataflow equations

2. **InterferenceGraph.java**
   - Build graph from liveness information
   - Nodes = temporaries
   - Edges = interference relationships

3. **GraphColoring.java**
   - Simplification-based coloring algorithm
   - Assign registers ($t0-$t9) to temporaries
   - Detect when allocation is impossible

4. **RegisterAllocator.java**
   - Main orchestrator
   - Calls liveness → interference → coloring
   - Returns final register mapping

### Helper Files
5. **BasicBlock.java** (optional but recommended)
   - Represent a basic block of IR commands
   - Makes liveness analysis easier

6. **LivenessInfo.java**
   - Store live-in/live-out sets for each IR command
   - Helper data structure

## Key Data Structures

### Liveness Sets
```java
// For each IR command
class LivenessInfo {
    Set<Temp> liveIn;   // Temps live before this command
    Set<Temp> liveOut;  // Temps live after this command
    Set<Temp> def;      // Temps defined by this command
    Set<Temp> use;      // Temps used by this command
}
```

### Interference Graph
```java
class InterferenceGraph {
    Map<Temp, Set<Temp>> adjacencyList;  // temp → set of interfering temps

    void addEdge(Temp t1, Temp t2) {
        adjacencyList.get(t1).add(t2);
        adjacencyList.get(t2).add(t1);
    }

    int degree(Temp t) {
        return adjacencyList.get(t).size();
    }
}
```

### Register Allocation Result
```java
class RegisterAllocation {
    Map<Temp, String> assignment;  // temp → register name ("$t0" .. "$t9")
    boolean success;                // false if allocation failed
}
```

## Algorithm Pseudocode

### Liveness Analysis
```
for each basic block:
    IN[exit] = ∅
    OUT[exit] = ∅

repeat until no changes:
    for each block (in reverse order):
        for each instruction i (in reverse order):
            IN[i] = USE[i] ∪ (OUT[i] - DEF[i])
            OUT[i] = ∪ IN[successor of i]
```

### Build Interference
```
for each instruction i:
    for each temp t in OUT[i]:
        for each temp t' in OUT[i]:
            if t != t':
                add edge (t, t') to interference graph
```

### Graph Coloring
```
stack = []
graph_copy = copy(interference_graph)

// Simplification
while graph_copy not empty:
    found = false
    for each node n in graph_copy:
        if degree(n) < 10:
            stack.push(n)
            graph_copy.remove(n)
            found = true
            break

    if not found:
        return FAILURE  // All nodes have degree >= 10

// Coloring
colors = {}
available_colors = {"$t0", "$t1", ..., "$t9"}

while stack not empty:
    node = stack.pop()
    used_colors = {colors[neighbor] | neighbor in neighbors(node) if neighbor in colors}
    free_colors = available_colors - used_colors
    colors[node] = free_colors.pick_any()

return SUCCESS, colors
```

## Testing Strategy

### Test 1: Simple Program
```
Temp_1 := 5
Temp_2 := 10
Temp_3 := Temp_1 + Temp_2
```
Expected:
- Temp_1 and Temp_2 interfere (both live at line 3)
- Need at least 2 registers
- Should succeed

### Test 2: Many Temporaries (Sequential)
```
Temp_1 := 1
Temp_2 := Temp_1 + 1
Temp_3 := Temp_2 + 1
...
Temp_20 := Temp_19 + 1
```
Expected:
- No interference (temporaries die before next is born)
- Need only 2 registers max
- Should succeed

### Test 3: Many Temporaries (Parallel)
```
Temp_1 := 1
Temp_2 := 2
...
Temp_15 := 15
Temp_result := Temp_1 + Temp_2 + ... + Temp_15
```
Expected:
- All temps 1-15 are live simultaneously
- Need 15 registers
- Should FAIL (only have 10)

### Test 4: Control Flow
```
Label_start:
Temp_1 := 5
if Temp_1 = 0 goto Label_end
Temp_2 := Temp_1 + 1
Label_end:
```
Expected:
- Temp_1 live across branches
- Should succeed

## Common Pitfalls

1. **Forgetting backward analysis** - Liveness is computed backwards!
2. **Not handling control flow** - Branches affect liveness
3. **Missing interference edges** - Two temps live at same time = must interfere
4. **Wrong simplification order** - Always remove lowest-degree node first
5. **Not detecting failure** - Must check if all nodes have degree ≥ 10

## Integration Points

### From Person A
```java
// Get IR commands
Ir ir = Ir.getInstance();
List<IrCommand> commands = ir.getCommands(); // May need to add this getter

// Iterate through commands
for (IrCommand cmd : commands) {
    // Analyze each command
}
```

### To Person C
```java
// Person C needs register mapping
Map<Temp, String> allocation = registerAllocator.allocate(ir);

// Person C uses it like:
String reg = allocation.get(temp);  // e.g., "$t5"
```

## Deliverables

1. **Working register allocator** that:
   - Takes IR as input
   - Performs liveness analysis
   - Builds interference graph
   - Colors the graph
   - Returns register mapping

2. **Documentation** explaining:
   - How liveness is computed
   - How interference is built
   - How coloring works
   - Example walkthrough

3. **Test cases** showing:
   - Successful allocation
   - Failed allocation
   - Edge cases (loops, branches)

## Estimated Effort
40-50 hours (the hardest part of the project!)

## Resources
- Dragon Book Chapter 9 (Liveness Analysis)
- Appel's Modern Compiler Implementation (Chapter 11)
- Course lecture notes on register allocation

---

**Status:** Ready to start
**Next:** Begin with liveness analysis implementation
