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

import sei.pku.edu.cn.normalize.Normalizer;
import sei.pku.edu.cn.normalize.visitor.NormalizeVisitor;
import sei.pku.edu.cn.pattern.Sequence;

public class JavaFile {
	
	Map<String, List<Sequence>> sequences = new HashMap<>();
	
	
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
		
		for(Entry<String, List<Sequence>> entry : sequences.entrySet()){
			try {
				fileWriter.write(entry.getKey() + "\n");
				fileWriter.write(entry.getValue() + "\n\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		try {
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
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
					List<Sequence> sequence = new ArrayList<>();
					Utils.resetAll();
					CompilationUnit compilationUnit = parse(readFileToString(f));
					compilationUnit.accept(new TypeMappingVisitor());
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
