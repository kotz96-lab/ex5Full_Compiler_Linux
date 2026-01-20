import java.io.*;
import ast.*;
import ir.*;
import regalloc.*;
import mips.*;
import java.util.List;

/**
 * Main Compiler Entry Point
 *
 * Complete L Compiler Pipeline:
 * 1. Parse L source code to AST
 * 2. Generate IR (Person A)
 * 3. Register Allocation (Person B)
 * 4. MIPS Generation (Person C)
 */
public class Main
{
    public static void main(String argv[])
    {
        if (argv.length != 2) {
            System.err.println("Usage: java Main <input.txt> <output.s>");
            System.exit(1);
        }

        String inputFileName = argv[0];
        String outputFileName = argv[1];

        try {
            // Step 1: Parse L source to AST (from ex4)
            FileReader fileReader = new FileReader(inputFileName);
            Lexer lexer = new Lexer(fileReader);
            Parser parser = new Parser(lexer);

            AstProgram ast = (AstProgram) parser.parse().value;

            // Step 2: Semantic analysis (from ex4)
            ast.semantMe();

            // Step 3: Person A - Generate IR
            ast.irMe();
            Ir ir = Ir.getInstance();
            List<IrCommand> commands = ir.getCommands();

            System.out.println("[Person A] IR Generation: " + commands.size() + " commands");

            // Step 4: Person B - Register Allocation
            RegisterAllocator allocator = new RegisterAllocator(true);
            RegisterAllocation allocation = allocator.allocate(commands);

            if (!allocation.isSuccess()) {
                // Register allocation failed - write failure message
                FileWriter fw = new FileWriter(outputFileName);
                fw.write("Register Allocation Failed");
                fw.close();
                System.out.println("[Person B] Register Allocation: FAILED (too many live temporaries)");
                return;
            }

            System.out.println("[Person B] Register Allocation: SUCCESS");

            // Step 5: Person C - MIPS Generation
            MipsTranslator translator = new MipsTranslator(outputFileName);
            translator.translate(commands, allocation);
            translator.close();

            System.out.println("[Person C] MIPS Generation: SUCCESS");
            System.out.println("Compilation complete: " + outputFileName);

        } catch (SemanticException e) {
            System.err.println("Semantic Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);

        } catch (Exception e) {
            System.err.println("Compilation Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
