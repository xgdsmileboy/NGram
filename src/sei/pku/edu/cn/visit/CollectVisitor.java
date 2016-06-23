package sei.pku.edu.cn.visit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import sei.pku.edu.cn.pattern.ArrayAccessPattern;
import sei.pku.edu.cn.pattern.ArrayCreatePattern;
import sei.pku.edu.cn.pattern.BlockLevel;
import sei.pku.edu.cn.pattern.BreakPattern;
import sei.pku.edu.cn.pattern.ContinuePatter;
import sei.pku.edu.cn.pattern.EForPattern;
import sei.pku.edu.cn.pattern.ForPattern;
import sei.pku.edu.cn.pattern.IfPattern;
import sei.pku.edu.cn.pattern.PatternValue;
import sei.pku.edu.cn.pattern.ReturnPattern;
import sei.pku.edu.cn.pattern.Sequence;
import sei.pku.edu.cn.pattern.StatementPattern;
import sei.pku.edu.cn.pattern.SwitchPattern;
import sei.pku.edu.cn.pattern.VariableDefPattern;
import sei.pku.edu.cn.pattern.WhilePattern;

public class CollectVisitor extends ASTVisitor {

	Map<String, Class<?>> typeMapping = new HashMap<>();
	Stack<String> variableStackForBreakStatement = new Stack<>();
	
	Sequence sequence;
	
	String methodName;
	
	public CollectVisitor(Sequence sequence) {
		this.sequence = sequence;
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		methodName = node.getName().toString();
		Block block = node.getBody();
		@SuppressWarnings("unchecked")
		List<Statement> statementlist = block.statements();
		for(Statement statement : statementlist){
			processStatement(statement, BlockLevel.BLOCK_NO);
		}
		return true;
	}
	
	private void processStatement(Statement statement, int blockFlag){
		
		if(statement instanceof Block){
			Block block = (Block) statement;
			for(Object s : block.statements()){
				Statement statement2 = (Statement) s;
				processStatement(statement2, blockFlag);
			}
		} else if(statement instanceof IfStatement){
			// print for debugging
			System.out.println("If Statement "+statement);
			
			IfStatement ifStatement = (IfStatement) statement;
			
			VariableVisitor variableVisitor = new VariableVisitor();
			ifStatement.getExpression().accept(variableVisitor);
			
			String variables = "$$";
			for(String variable : variableVisitor.getVariables()){
				StatementPattern statementPattern = new IfPattern(blockFlag);
				Type clazz = Utils.getVariableType(methodName, variable);
				sequence.addStatementPattern(variable, clazz, statementPattern);
				variables += "," + variable + ":" + clazz;
			}
			// record condition variables which may be related to a break statement
			variableStackForBreakStatement.push(variables);
			
			Statement thenblock = ifStatement.getThenStatement();
			processStatement(thenblock, BlockLevel.BLOCK_THEN);
			Statement elseblock = ifStatement.getElseStatement();
			processStatement(elseblock, BlockLevel.BLOCK_ELSE);
			
			// delete variables at the top of the stack, since the if statement is completed
			variableStackForBreakStatement.pop();
			
		} else if(statement instanceof ForStatement){
			// print for debugging
			System.out.println("For Statement "+statement);
			
			ForStatement forStatement = (ForStatement) statement;

			for(Object init : forStatement.initializers()){
				Expression initStatement = (Expression) init;
				VariableVisitor varVisitor = new VariableVisitor();
				initStatement.accept(varVisitor);
				for(String variable : varVisitor.getVariables()){
					Type clazz = Utils.getVariableType(methodName, variable);
					StatementPattern statementPattern = new ForPattern(PatternValue.FOR_INIT, blockFlag);
					sequence.addStatementPattern(variable, clazz, statementPattern);
				}
			}
			VariableVisitor variableVisitor = new VariableVisitor();
			forStatement.getExpression().accept(variableVisitor);
			for(String variable : variableVisitor.getVariables()){
				Type clazz = Utils.getVariableType(methodName, variable);
				StatementPattern statementPattern = new ForPattern(PatternValue.FOR_COND, blockFlag);
				sequence.addStatementPattern(variable, clazz, statementPattern);
			}
			
			for(Object update : forStatement.updaters()){
				Expression updateStatement = (Expression) update;
				VariableVisitor varVisitor = new VariableVisitor();
				updateStatement.accept(varVisitor);
				for(String variable : varVisitor.getVariables()){
					Type clazz = Utils.getVariableType(methodName, variable);
					StatementPattern statementPattern = new ForPattern(PatternValue.FOR_UPDATE, blockFlag);
					sequence.addStatementPattern(variable, clazz, statementPattern);
				}
			}
			
			Statement bodyStatement = forStatement.getBody();
			processStatement(bodyStatement, BlockLevel.BLOCK_LOOP);
			
		} else if(statement instanceof WhileStatement){
			// print for debugging
			System.out.println("While Statement "+statement);
			
			WhileStatement whileStatement = (WhileStatement) statement;
			
			VariableVisitor variableVisitor = new VariableVisitor();
			whileStatement.getExpression().accept(variableVisitor);
			for(String variable : variableVisitor.getVariables()){
				Type clazz = Utils.getVariableType(methodName, variable);
				StatementPattern statementPattern = new WhilePattern(blockFlag);
				sequence.addStatementPattern(variable, clazz, statementPattern);
			}
			
			Statement bodyStatement = whileStatement.getBody();
			processStatement(bodyStatement, BlockLevel.BLOCK_LOOP);
			
		} else if(statement instanceof DoStatement){
			// print for debugging
			System.out.println("Do Statement "+statement);
			
			DoStatement doStatement = (DoStatement) statement;
			
			VariableVisitor variableVisitor = new VariableVisitor();
			doStatement.getExpression().accept(variableVisitor);
			for(String variable : variableVisitor.getVariables()){
				Type clazz = Utils.getVariableType(methodName, variable);
				StatementPattern statementPattern = new WhilePattern(blockFlag);
				sequence.addStatementPattern(variable, clazz, statementPattern);
			}
			
			Statement bodyStatement = doStatement.getBody();
			processStatement(bodyStatement, BlockLevel.BLOCK_LOOP);
			
		} else if(statement instanceof EnhancedForStatement){
			// print for debugging
			System.out.println("EnhancedFor Statement "+statement);
			
			EnhancedForStatement enhancedForStatement = (EnhancedForStatement) statement;
			
			VariableVisitor variableVisitor = new VariableVisitor();
			enhancedForStatement.getParameter().accept(variableVisitor);
			for(String variable : variableVisitor.getVariables()){
				Type clazz = Utils.getVariableType(methodName, variable);
				StatementPattern statementPattern = new EForPattern(PatternValue.EFOR_DEF, blockFlag);
				sequence.addStatementPattern(variable, clazz, statementPattern);
			}
			
			variableVisitor = new VariableVisitor();
			enhancedForStatement.getExpression().accept(variableVisitor);
			for(String variable : variableVisitor.getVariables()){
				Type clazz = Utils.getVariableType(methodName, variable);
				StatementPattern statementPattern = new EForPattern(PatternValue.EFOR_ARRAY, blockFlag);
				sequence.addStatementPattern(variable, clazz, statementPattern);
			}
			
			Statement bodyStatement = enhancedForStatement.getBody();
			processStatement(bodyStatement, BlockLevel.BLOCK_LOOP);
			
		} else if(statement instanceof SwitchStatement){
			// print for debugging
			System.out.println("Switch Statement "+statement);
			SwitchStatement switchStatement = (SwitchStatement) statement;
			
			VariableVisitor variableVisitor = new VariableVisitor();
			switchStatement.getExpression().accept(variableVisitor);
			String variables = "$$";
			for(String variable : variableVisitor.getVariables()){
				Type clazz = Utils.getVariableType(methodName, variable);
				StatementPattern statementPattern = new SwitchPattern(blockFlag);
				sequence.addStatementPattern(variable, clazz, statementPattern);
				variables += "," + variable + ":" + clazz;
			}
			
			variableStackForBreakStatement.push(variables);
			
			// traverse each switch case statement
			for(Object object : switchStatement.statements()){
				Statement s = (Statement) object;
				processStatement(s, BlockLevel.BLOCK_SWITCH);
			}
			variableStackForBreakStatement.pop();
			
		} else if(statement instanceof BreakStatement){
			// print for debugging
			System.out.println("Break Statement "+statement);
			String[] variables = variableStackForBreakStatement.peek().split(",");
			for(int i = 1; i < variables.length; i++){
				String var = variables[i];
				StatementPattern statementPattern = new BreakPattern(blockFlag);
				sequence.addStatementPattern(var, statementPattern);
			}
			
		} else if(statement instanceof ContinueStatement){
			// print for debugging
			System.out.println("Continue Statement "+statement);
			String[] variables = variableStackForBreakStatement.peek().split(",");
			for(int i = 1; i < variables.length; i++){
				String var = variables[i];
				StatementPattern statementPattern = new ContinuePatter(blockFlag);
				sequence.addStatementPattern(var, statementPattern);
			}
			
		} else if(statement instanceof ReturnStatement){
			// print for debugging
			System.out.println("Return Statement "+statement);
			
			ReturnStatement returnStatement = (ReturnStatement) statement;
			VariableVisitor variableVisitor = new VariableVisitor();
			if(null == returnStatement.getExpression()){
				return;
			}
			returnStatement.getExpression().accept(variableVisitor);
			
			for(String variable : variableVisitor.getVariables()){
				Type type = Utils.getVariableType(methodName, variable);
				StatementPattern statementPattern = new ReturnPattern(blockFlag);
				sequence.addStatementPattern(variable, type, statementPattern);
			}
			
		} else if(statement instanceof VariableDeclarationStatement){
			// print for debugging
			System.out.println("VariableDeclaration Statement "+statement);
			
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statement;
			Type type = variableDeclarationStatement.getType();
			@SuppressWarnings("unchecked")
			List<VariableDeclarationFragment> expList = variableDeclarationStatement.fragments();
			for(VariableDeclarationFragment exp : expList){
				StatementPattern statementPattern = new VariableDefPattern();
				sequence.addStatementPattern(exp.getName().toString(), type, statementPattern);
				processExpression(exp.getInitializer(), blockFlag);
			}
			
		} else if(statement instanceof ExpressionStatement){
			ExpressionStatement expressionStatement = (ExpressionStatement) statement;
			processExpression(expressionStatement.getExpression(), blockFlag);
		} else {
			
		}
	}
	
	private void processExpression(Expression expression, int blockFlag){
		if(expression instanceof ArrayAccess){
			// print for debugging
			System.out.println("ArrayAccess Expression "+ expression);
			
			ArrayAccess arrayAccess = (ArrayAccess) expression;
			
			VariableVisitor variableVisitor = new VariableVisitor();
			arrayAccess.getArray().accept(variableVisitor);
			for(String variable : variableVisitor.getVariables()){
				Type type = Utils.getVariableType(methodName, variable);
				StatementPattern statementPattern = new ArrayAccessPattern(PatternValue.ARRAY_ACC_OBJ, blockFlag);
				sequence.addStatementPattern(variable, type, statementPattern);
			}
			
			variableVisitor = new VariableVisitor();
			arrayAccess.getIndex().accept(variableVisitor);
			for(String variable : variableVisitor.getVariables()){
				Type type = Utils.getVariableType(methodName, variable);
				StatementPattern statementPattern = new ArrayAccessPattern(PatternValue.ARRAY_ACC_INDEX, blockFlag);
				sequence.addStatementPattern(variable, type, statementPattern);
			}
			
		} else if(expression instanceof ArrayCreation){
			// print for debugging
			System.out.println("ArrayCreation Expression "+ expression);
			
			ArrayCreation arrayCreation = (ArrayCreation) expression;
			VariableVisitor variableVisitor = new VariableVisitor();
			arrayCreation.accept(variableVisitor);
			for(String variable : variableVisitor.getVariables()){
				Type type = Utils.getVariableType(methodName, variable);
				StatementPattern statementPattern = new ArrayCreatePattern(blockFlag);
				sequence.addStatementPattern(variable, type, statementPattern);
			}
			
		} else if(expression instanceof ArrayInitializer){
			// print for debugging
			System.out.println("ArrayInitializer Expression "+ expression);
			
		} else if(expression instanceof Assignment){
			// print for debugging
			System.out.println("Assignment Expression "+ expression);
			
		} else if(expression instanceof ClassInstanceCreation){
			// print for debugging
			System.out.println("ClassInstanceCreation Expression "+ expression);
			
		} else if(expression instanceof ConditionalExpression){
			// print for debugging
			System.out.println("Conditional Expression "+ expression);
			
		} else if(expression instanceof MethodInvocation){
			// print for debugging
			System.out.println("MethodInvocation Expression "+ expression);
			
		} else if(expression instanceof PostfixExpression){
			// print for debugging
			System.out.println("PostfixExpression Expression "+ expression);
			
		} else if(expression instanceof InfixExpression){
			// print for debugging
			System.out.println("InfixExpression Expression "+ expression);
			
		} else {
			
		}
	}
	
	class VariableVisitor extends ASTVisitor{
		private List<String> variables = new ArrayList<>();
		
		@Override
		public boolean visit(SimpleName node) {
			String name = node.toString();
			if(null == Utils.getVariableType(methodName, node.toString())){
				return true;
			}
			variables.add(node.toString());
			return true;
		}
		
		public List<String> getVariables(){
			return variables;
		}
	}
	
}
