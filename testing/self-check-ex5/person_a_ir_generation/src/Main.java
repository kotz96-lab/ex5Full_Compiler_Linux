import java.io.*;
import java.io.PrintWriter;
import java_cup.runtime.Symbol;
import ast.*;
import cfg.*;

public class Main
{
	static public void main(String argv[])
	{
		Lexer l;
		Parser p;
		Symbol s;
		AstProgram ast;
		FileReader fileReader;
		PrintWriter fileWriter;
		String inputFileName = argv[0];
		String outputFileName = argv[1];

		try
		{
			fileReader = new FileReader(inputFileName);
			FileOutputStream fos = new FileOutputStream(outputFileName);
			fileWriter = new PrintWriter(new OutputStreamWriter(fos, "UTF-8"));
			l = new Lexer(fileReader);
			p = new Parser(l);

			ast = (AstProgram) p.parse().value;
			ast.printMe();
            ast.semantMe();
			ast.irMe();
			ir.Ir.getInstance().printIR();

			Analyzer analyzer = new Analyzer();
			boolean analysisSuccess = analyzer.analyze(ir.Ir.getInstance());

			if (analysisSuccess) {
				analyzer.printReport();
				analyzer.exportCFGDotFile("cfg_output.dot");
				analyzer.exportAnalysisResults("analysis_results.txt");
				analyzer.writeOutputFile(fileWriter);
			} else {
				System.err.println("Static analysis failed");
				analyzer.printReport();
				fileWriter.write("!OK");
				fileWriter.flush();
			}

			fileWriter.close();
			AstGraphviz.getInstance().finalizeFile();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}


