package sei.pku.edu.cn.visit;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.ClasspathNormalizer;

public class TypingVisitor extends ASTVisitor {

	public boolean visit(TypeDeclaration node) {

		FieldDeclaration fields[] = node.getFields();
		for (FieldDeclaration f : fields) {
			for (Object o : f.fragments()) {
				VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
				TypingInfo.addFieldType(vdf.getName().toString(), f.getType());
			}
		}
		return true;
	}

	public boolean visit(MethodDeclaration node) {

		String methodName = node.getName().toString();
		String params = "";
		for(Object obj : node.parameters()){
			SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) obj;
			params += ","+singleVariableDeclaration.getType().toString();
		}
		methodName += params;
		Map<String, Type> map = new HashMap<>();
		for (Object o : node.parameters()) {
			SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
			TypingInfo.addMethodVariableType(methodName, svd.getName().toString(), svd.getType());
		}

		MethodVisitor mv = new MethodVisitor();
		node.accept(mv);

		for (Entry<String, Type> entry : mv.getVarMap().entrySet()) {
			TypingInfo.addMethodVariableType(methodName, entry.getKey(), entry.getValue());
		}

		// System.out.println("MethodDeclaration = " + node);
		return true;
	}

	class MethodVisitor extends ASTVisitor {

		Map<String, Type> map = new HashMap<>();

		public Map<String, Type> getVarMap() {
			return map;
		}

		public boolean visit(ConditionalExpression node) {

//			System.out.println("ConditionalExpression -->" + node);
			return true;
		}

		public boolean visit(InfixExpression node) {
//			System.out.println("InfixExpression -->" + node);
			return true;
		}

		public boolean visit(InstanceofExpression node) {
//			System.out.println("InstanceofExpression -->" + node);
			return true;
		}

		public boolean visit(MethodInvocation node) {
//			System.out.println("MethodInvocation -->" + node);
			return true;
		}

		public boolean visit(Name node) {
//			System.out.println("Name -->" + node);
			return true;
		}

		public boolean visit(ParenthesizedExpression node) {
//			System.out.println("ParenthesizedExpression -->" + node);
			return true;
		}

		public boolean visit(PostfixExpression node) {
//			System.out.println("PostfixExpression -->" + node);
			return true;
		}

		public boolean visit(PrefixExpression node) {
//			System.out.println("PrefixExpression -->" + node);
			return true;
		}

		public boolean visit(TypeLiteral node) {
//			System.out.println("TypeLiteral -->" + node);
			return true;
		}

		public boolean visit(VariableDeclarationStatement node) {
//			Class<?> clazz = Utils.convert2Class(node.getType());
			for (Object o : node.fragments()) {
				VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
//				System.out.println(vdf.getName());
				map.put(vdf.getName().toString(), node.getType());
			}
//			System.out.println("VariableDeclarationStatement -->" + node);
			return true;
		}

		public boolean visit(VariableDeclarationExpression node) {
//			Class<?> clazz = Utils.convert2Class(node.getType());
			for (Object o : node.fragments()) {
				VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
//				System.out.println(vdf.getName());
				map.put(vdf.getName().toString(), node.getType());
			}
//			System.out.println("VariableDeclarationExpression -->" + node);
			return true;
		}
		
		public boolean visit(SingleVariableDeclaration node){
//			Class<?> clazz = Utils.convert2Class(node.getType());
			map.put(node.getName().toString(), node.getType());
//			System.out.println("SingleVariableDeclaration -->" + node);
			return true;
		}
	}

}
