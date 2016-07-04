package sei.pku.edu.cn.visit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import sei.pku.edu.cn.pattern.Sequence;

public class JavaFile {

	List<Sequence> sequences = new ArrayList<>();
	
	public JavaFile(String Path) {
		if (Path == null)
			return;
		File file = new File(Path);
		ergodic(file);
		
		for(Sequence sequence : sequences){
			System.out.println(sequence.toString());
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
					CompilationUnit compilationUnit = parse(readFileToString(f));
					compilationUnit.accept(new TypeMappingVisitor());
					compilationUnit.accept(new CollectVisitor(sequences));
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
		return (CompilationUnit) parser.createAST(null);
	}

}
