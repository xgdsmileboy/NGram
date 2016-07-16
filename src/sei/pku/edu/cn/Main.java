package sei.pku.edu.cn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import sei.pku.edu.cn.pattern.Sequences;
import sei.pku.edu.cn.pattern.StatementPattern;
import sei.pku.edu.cn.pattern.struct.Pair;
import sei.pku.edu.cn.query.NGram;
import sei.pku.edu.cn.visit.CollectVisitor2;
import sei.pku.edu.cn.visit.JavaFile;
import sei.pku.edu.cn.visit.TypingInfo;
import sei.pku.edu.cn.visit.TypingVisitor;

public class Main {
	public static void main(String[] args) {
		JavaFile javaFile = new JavaFile("testclass/train");
		List<Sequences> sequences = javaFile.getTrainSequences();
		testNGram(sequences);
	}
	
	public static void testNGram(List<Sequences> list){
		
		NGram nGram = new NGram(list);
		nGram.setLength(3);
		List<Pair<Long, Sequences>> sequences = nGram.slicing();
		List<String> to_be_fix = new ArrayList<>();
		
		File configureFile = new File("testclass/fix/instruction.txt");
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(configureFile));
			String line = null;
			while((line = bufferedReader.readLine()) != null){
				to_be_fix.add(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		File fixPath = new File("testclass/fix");
		
		for(String string : to_be_fix){
			String[] strings = string.split("\\s+");
			if(strings.length < 3){
				System.out.println("File format is illegal and skipped : " + string);
				System.out.println("Correct format should be as \"ClassName MethodName variableName:type,VariableName:type\"");
				continue;
			}
			String clazz = strings[0].trim();
			String method = strings[1].trim();
			String variables = strings[2].trim();
			
			File file = JavaFile.findFile(fixPath, clazz+".java");
			
			List<Sequences> sequence = new ArrayList<>();
			TypingInfo.resetAll();
			CompilationUnit compilationUnit = parse(readFileToString(file));
			compilationUnit.accept(new TypingVisitor());
			compilationUnit.accept(new CollectVisitor2(sequence, method));
			
			if(sequence.size() < 1){
				continue;
			}
			
			for(String variable : variables.split(",")){
				List<StatementPattern> seq = sequence.get(0).getStatementPatternList(variable);
				Sequences querySeq = new Sequences();
				for(StatementPattern statementPattern : seq){
					querySeq.addStatementPattern(variable, statementPattern);
				}
				List<Sequences> querys = querySeq.sliceSequence(3);
				
				System.out.println("\n\nHere is a test query======\n");
				System.out.println("Class : " + clazz + "method : " + method + "\n");
				for(Sequences s : querys){
					System.out.println("Current variable : " + variable + ", Current query seqence : \n");
					System.out.println(Long.toBinaryString(s.getHexValueRepresent()) + " : " + s);
					System.out.println("result------\n");
					for(Pair<Sequences, Pair<Double, Double> > seqPair : nGram.query(s)){
						System.out.println(seqPair.getFirst() + ":" + seqPair.getSecond().getFirst() + ":" + seqPair.getSecond().getSecond());
					}
				}
			}
			
			
		}
		
	}
	
	private static String readFileToString(File file) {

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

	private static CompilationUnit parse(String str) {
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
