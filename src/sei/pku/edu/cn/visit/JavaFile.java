package sei.pku.edu.cn.visit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import sei.pku.edu.cn.pattern.ArrayAccessPattern;
import sei.pku.edu.cn.pattern.AssignPattern;
import sei.pku.edu.cn.pattern.ForPattern;
import sei.pku.edu.cn.pattern.IfPattern;
import sei.pku.edu.cn.pattern.PatternValue;
import sei.pku.edu.cn.pattern.Sequences;
import sei.pku.edu.cn.pattern.VariableDefPattern;
import sei.pku.edu.cn.pattern.struct.Pair;
import sei.pku.edu.cn.query.NGram;

public class JavaFile {
	
	Map<String, List<Sequences>> sequences = new HashMap<>();
	
	
	String resultFileName = "result.txt";
	
	public JavaFile(String Path) {
		if (Path == null)
			return;
		File file = new File(Path);
		ergodic(file);
		
		File file2 = new File(resultFileName);
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file2, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<Sequences> sList = new ArrayList<>();
		for(Entry<String, List<Sequences>> entry : sequences.entrySet()){
			try {
				fileWriter.write(entry.getKey() + "\n");
				fileWriter.write(entry.getValue() + "\n\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			sList.addAll(entry.getValue());
			NGram nGram = new NGram(entry.getValue());
			List<Pair<Long, Sequences>> list = nGram.slicing();
			for(Pair<Long, Sequences> pair : list){
				System.out.print(Long.toBinaryString(pair.getFirst()) + " : ");
				System.out.println(pair.getSecond().toString());
			}
			
		}
		try {
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		NGram nGram = new NGram(sList);
		nGram.slicing();
		Sequences query = new Sequences();
		// 1000000001000000000 : k:int : [<FOR, 512, 0>, <ARRAYACCESS, 262144, 4>]
		query.addStatementPattern("k:int", new ForPattern(PatternValue.FOR_UPDATE, 0));
		query.addStatementPattern("k:int", new ArrayAccessPattern(PatternValue.ARRAY_ACC_INDEX, 4));
		
		// 10010000 : a:int : [<ASSIGN, 16, 4>, <FOR, 128, 4>]
//		query.addStatementPattern("a:int", new AssignPattern(PatternValue.ASSIGN_RIGHT, 4));
//		query.addStatementPattern("a:int", new ForPattern(PatternValue.FOR_INIT, 4));
		
		// 1000000000000000000000100000 : a:int : [<VARDEF, 134217728, -1>, <IF, 32, 0>]
//		query.addStatementPattern("a:int", new VariableDefPattern());
//		query.addStatementPattern("a:int", new IfPattern(0));
		
		System.out.println("\n\nHere is a test query======\n");
		System.out.println(Long.toBinaryString(query.getHexValueRepresent()) + " : " + query);
		System.out.println("result------\n");
		for(Pair<Sequences, Double> sequences : nGram.query(query)){
			System.out.println(sequences.getFirst());
		}
		
	}

	private void ergodic(File file) {
		File[] files = file.listFiles();
		if (files == null)
			return;
		else {
			for (File f : files) {
				if (f.isDirectory()) {
					ergodic(f);
				} else if (f.getName().endsWith(".java")) {
					System.out.println("collect java file : " + f.getPath());
					List<Sequences> sequence = new ArrayList<>();
					TypingInfo.resetAll();
					CompilationUnit compilationUnit = parse(readFileToString(f));
					compilationUnit.accept(new TypingVisitor());
//					NormalizeVisitor normalizeVisitor = new NormalizeVisitor(compilationUnit);
//					compilationUnit.accept(normalizeVisitor);
//					System.out.println(normalizeVisitor.getCU());
//					CompilationUnit normalizedICU = normalizeVisitor.getCU();
					compilationUnit.accept(new CollectVisitor(sequence));
					
					sequences.put(f.getPath(), sequence);
					
				}
			}
		}
	}

	private String readFileToString(File file) {

		StringBuilder fileData = new StringBuilder(1000);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		char[] buf = new char[10];
		int numRead = 0;
		try {
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			}

			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fileData.toString();
	}

	private CompilationUnit parse(String str) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		Map<?, ?> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		parser.setSource(str.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		return (CompilationUnit) parser.createAST(null);
	}

}
