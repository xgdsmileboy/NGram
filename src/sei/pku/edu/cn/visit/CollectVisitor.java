package sei.pku.edu.cn.visit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
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
import org.eclipse.jdt.core.dom.PrefixExpression;
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
import sei.pku.edu.cn.pattern.AssignPattern;
import sei.pku.edu.cn.pattern.BlockLevel;
import sei.pku.edu.cn.pattern.BreakPattern;
import sei.pku.edu.cn.pattern.ConditionalPattern;
import sei.pku.edu.cn.pattern.ContinuePatter;
import sei.pku.edu.cn.pattern.EForPattern;
import sei.pku.edu.cn.pattern.ForPattern;
import sei.pku.edu.cn.pattern.IfPattern;
import sei.pku.edu.cn.pattern.InvokePattern;
import sei.pku.edu.cn.pattern.NewInstancePattern;
import sei.pku.edu.cn.pattern.ParamPattern;
import sei.pku.edu.cn.pattern.PatternValue;
import sei.pku.edu.cn.pattern.ReturnPattern;
import sei.pku.edu.cn.pattern.Sequence;
import sei.pku.edu.cn.pattern.SimplePattern;
import sei.pku.edu.cn.pattern.StatementPattern;
import sei.pku.edu.cn.pattern.SwitchPattern;
import sei.pku.edu.cn.pattern.VariableDefPattern;
import sei.pku.edu.cn.pattern.WhilePattern;

public class CollectVisitor extends ASTVisitor {

	Map<String, Class<?>> typeMapping = new HashMap<>();
	Stack<String> variableStackForBreakStatement = new Stack<>();
	
	List<Sequence> sequences;
	Sequence sequence;
	
	String methodName;
	
	public CollectVisitor(List<Sequence> sequence) {
		this.sequences = sequence;
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		sequence = new Sequence();
		methodName = node.getName().toString();
		
		List<ASTNode> parameters = node.parameters();
		for(int i = 1; i <= parameters.size(); i++){
			ASTNode param = parameters.get(i-1);
			VariableVisitor variableVisitor = new VariableVisitor();
			param.accept(variableVisitor);
			List<String> variables = variableVisitor.getVariables();
			for(String variable : variables){
				Type type = Utils.getVariableType(methodName, variable);
				StatementPattern statementPattern = new ParamPattern((PatternValue.METHOD_PARAM | (long)i));
				sequence.addStatementPattern(variable, type, statementPattern);
			}
		}
		
		Block block = node.getBody();
		if(block == null){
			return true;
		}
		@SuppressWarnings("unchecked")
		List<Statement> statementlist = block.statements();
		for(Statement statement : statementlist){
			processStatement(statement, BlockLevel.BLOCK_NO);
		}
		sequences.add(sequence);
		return true;
	}
	
	private void processStatement(Statement statement, int blockFlag){
		if(statement == null){
			return;
		}
		if(statement instanceof Block){
			Block block = (Block) statement;
			for(Object s : block.statements()){
				Statement statement2 = (Statement) s;
				processStatement(statement2, blockFlag);
			}
		} else if(statement instanceof IfStatement){
			// print for debugging
//			System.out.println("If Statement "+statement);
			
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
//			System.out.println("For Statement "+statement);
			
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
//			System.out.println("While Statement "+statement);
			
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
//			System.out.println("Do Statement "+statement);
			
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
//			System.out.println("EnhancedFor Statement "+statement);
			
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
//			System.out.println("Switch Statement "+statement);
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
//			System.out.println("Break Statement "+statement);
			if(variableStackForBreakStatement.isEmpty()){
				return;
			}
			String[] variables = variableStackForBreakStatement.peek().split(",");
			for(int i = 1; i < variables.length; i++){
				String var = variables[i];
				StatementPattern statementPattern = new BreakPattern(blockFlag);
				sequence.addStatementPattern(var, statementPattern);
			}
			
		} else if(statement instanceof ContinueStatement){
			// print for debugging
//			System.out.println("Continue Statement "+statement);
			if(variableStackForBreakStatement.isEmpty()){
				return;
			}
			String[] variables = variableStackForBreakStatement.peek().split(",");
			for(int i = 1; i < variables.length; i++){
				String var = variables[i];
				StatementPattern statementPattern = new ContinuePatter(blockFlag);
				sequence.addStatementPattern(var, statementPattern);
			}
			
		} else if(statement instanceof ReturnStatement){
			// print for debugging
//			System.out.println("Return Statement "+statement);
			
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
//			System.out.println("VariableDeclaration Statement "+statement);
			
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statement;
			Type type = variableDeclarationStatement.getType();
			@SuppressWarnings("unchecked")
			List<VariableDeclarationFragment> expList = variableDeclarationStatement.fragments();
			for(VariableDeclarationFragment exp : expList){
				StatementPattern statementPattern = null;
				
				Expression expression = exp.getInitializer();
				if(expression instanceof ClassInstanceCreation){
					statementPattern = new NewInstancePattern(PatternValue.NEWINSTANCE_RET, blockFlag);
				} else if(expression instanceof MethodInvocation){
					statementPattern = new InvokePattern(PatternValue.INVOKE_RET, blockFlag);
				} else {
					statementPattern = new VariableDefPattern();
				}
				sequence.addStatementPattern(exp.getName().toString(), type, statementPattern);
				processExpression(exp.getInitializer(), blockFlag);
			}
			
		} else if(statement instanceof ExpressionStatement){
			ExpressionStatement expressionStatement = (ExpressionStatement) statement;
			processExpression(expressionStatement.getExpression(), blockFlag);
		} else {
			System.out.println("Unknown statement while process : " + statement);
		}
	}
	
	private void processExpression(Expression expression, int blockFlag){
		if(expression == null){
			return;
		}
		if(expression instanceof ArrayAccess){
			// print for debugging
//			System.out.println("ArrayAccess Expression "+ expression);
			
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
//			System.out.println("ArrayCreation Expression "+ expression);
			
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
//			System.out.println("ArrayInitializer Expression "+ expression);
			
		} else if(expression instanceof Assignment){
			// print for debugging
//			System.out.println("Assignment Expression "+ expression);
			
			Assignment assignment = (Assignment) expression;
			Expression lExpression = assignment.getLeftHandSide();
			Expression rExpression = assignment.getRightHandSide();
			VariableVisitor variableVisitor = new VariableVisitor();
			
			if(rExpression instanceof MethodInvocation){
				lExpression.accept(variableVisitor);
				// handle the returned value
				for(String variable : variableVisitor.getVariables()){
					Type type = Utils.getVariableType(methodName, variable);
					StatementPattern statementPattern = new InvokePattern(PatternValue.INVOKE_RET, blockFlag);
					sequence.addStatementPattern(variable, type, statementPattern);
				}
				
				MethodInvocation methodInvocation = (MethodInvocation) rExpression;
				//handle the invoke object
				Expression object = methodInvocation.getExpression();
				if(object != null){
					variableVisitor = new VariableVisitor();
					object.accept(variableVisitor);
					for(String variable : variableVisitor.getVariables()){
						Type type = Utils.getVariableType(methodName, variable);
						StatementPattern statementPattern = new InvokePattern(PatternValue.INVOKE_OBJ, blockFlag);
						sequence.addStatementPattern(variable, type, statementPattern);
					}
				}
				//handle the method invocation parameters
				List<ASTNode> params = methodInvocation.arguments();
				for(int i = 1; i <= params.size(); i++){
					ASTNode param = params.get(i-1);
					variableVisitor = new VariableVisitor();
					param.accept(variableVisitor);
					for(String variable : variableVisitor.getVariables()){
						Type type = Utils.getVariableType(methodName, variable);
						StatementPattern statementPattern = new InvokePattern((PatternValue.INVOKE_PARAM | (long)i), blockFlag);
						sequence.addStatementPattern(variable, type, statementPattern);
					}
				}
				
			} else if(rExpression instanceof ClassInstanceCreation){
				lExpression.accept(variableVisitor);
				// handle the returned value
				for(String variable : variableVisitor.getVariables()){
					Type type = Utils.getVariableType(methodName, variable);
					StatementPattern statementPattern = new NewInstancePattern(PatternValue.NEWINSTANCE_RET, blockFlag);
					sequence.addStatementPattern(variable, type, statementPattern);
				}
				
				ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) rExpression;
				//handle the invoke object
				Expression object = classInstanceCreation.getExpression();
				if(object != null){
					variableVisitor = new VariableVisitor();
					object.accept(variableVisitor);
					for(String variable : variableVisitor.getVariables()){
						Type type = Utils.getVariableType(methodName, variable);
						StatementPattern statementPattern = new NewInstancePattern(PatternValue.INVOKE_OBJ, blockFlag);
						sequence.addStatementPattern(variable, type, statementPattern);
					}
				}
				//handle the new class instance parameters
				List<ASTNode> params = classInstanceCreation.arguments();
				for(int i = 1; i <= params.size(); i++){
					ASTNode param = params.get(i-1);
					variableVisitor = new VariableVisitor();
					param.accept(variableVisitor);
					for(String variable : variableVisitor.getVariables()){
						Type type = Utils.getVariableType(methodName, variable);
						StatementPattern statementPattern = new NewInstancePattern((PatternValue.NEWINSTANCE_PARAN | (long)i), blockFlag);
						sequence.addStatementPattern(variable, type, statementPattern);
					}
				}
				
				
			} else{
				
				lExpression.accept(variableVisitor);
				for(String variable : variableVisitor.getVariables()){
					Type type = Utils.getVariableType(methodName, variable);
					StatementPattern statementPattern = new AssignPattern(PatternValue.ASSIGN_LEFT, blockFlag);
					sequence.addStatementPattern(variable, type, statementPattern);
				}
				
				if(rExpression instanceof InfixExpression || rExpression instanceof PostfixExpression
					|| rExpression instanceof PrefixExpression){
					variableVisitor = new VariableVisitor();
					rExpression.accept(variableVisitor);
					for(String variable : variableVisitor.getVariables()){
						Type type = Utils.getVariableType(methodName, variable);
						StatementPattern statementPattern = new AssignPattern(PatternValue.ASSIGN_RIGHT, blockFlag);
						sequence.addStatementPattern(variable, type, statementPattern);
					}
				} else {
					processExpression(rExpression, blockFlag);
				}
			}
			
		} else if(expression instanceof ClassInstanceCreation){
			// print for debugging
//			System.out.println("ClassInstanceCreation Expression "+ expression);
			
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
			VariableVisitor variableVisitor = null;
			
			//handle the class instance object
			Expression object = classInstanceCreation.getExpression();
			if(object != null){
				variableVisitor = new VariableVisitor();
				object.accept(variableVisitor);
				for(String variable : variableVisitor.getVariables()){
					Type type = Utils.getVariableType(methodName, variable);
					StatementPattern statementPattern = new NewInstancePattern(PatternValue.NEWINSTANCE_OBJ, blockFlag);
					sequence.addStatementPattern(variable, type, statementPattern);
				}
			}
			//handle class instance parameters
			List<Expression> args = classInstanceCreation.arguments();
			for(int i =  1; i <= args.size(); i++){
				Expression exp = args.get(i-1);
				variableVisitor = new VariableVisitor();
				exp.accept(variableVisitor);
				for(String variable : variableVisitor.getVariables()){
					Type type = Utils.getVariableType(methodName, variable);
					StatementPattern statementPattern = new NewInstancePattern((PatternValue.NEWINSTANCE_PARAN | (long)i), blockFlag);
					sequence.addStatementPattern(variable, type, statementPattern);
				}
			}
			
		} else if(expression instanceof ConditionalExpression){
			// print for debugging
//			System.out.println("Conditional Expression "+ expression);
			
			ConditionalExpression conditionalExpression = (ConditionalExpression) expression;
			Expression condExp = conditionalExpression.getExpression();
			VariableVisitor variableVisitor = new VariableVisitor();
			condExp.accept(variableVisitor);
			for(String variable : variableVisitor.getVariables()){
				Type type = Utils.getVariableType(methodName, variable);
				StatementPattern statementPattern = new ConditionalPattern(PatternValue.CONDITIONAL_COND, blockFlag);
				sequence.addStatementPattern(variable, type, statementPattern);
			}
			
			Expression lExp = conditionalExpression.getThenExpression();
			variableVisitor = new VariableVisitor();
			lExp.accept(variableVisitor);
			for(String variable : variableVisitor.getVariables()){
				Type type = Utils.getVariableType(methodName, variable);
				StatementPattern statementPattern = new ConditionalPattern(PatternValue.CONDITIONAL_LEXP, blockFlag);
				sequence.addStatementPattern(variable, type, statementPattern);
			}
			
			Expression rExp = conditionalExpression.getElseExpression();
			variableVisitor = new VariableVisitor();
			rExp.accept(variableVisitor);
			for(String variable : variableVisitor.getVariables()){
				Type type = Utils.getVariableType(methodName, variable);
				StatementPattern statementPattern = new ConditionalPattern(PatternValue.CONDITIONAL_REXP, blockFlag);
				sequence.addStatementPattern(variable, type, statementPattern);
			}
			
		} else if(expression instanceof MethodInvocation){
			// print for debugging
//			System.out.println("MethodInvocation Expression "+ expression);
			
		} else if(expression instanceof PostfixExpression || expression instanceof InfixExpression){
			// print for debugging
//			System.out.println("PostfixExpression/InfixExpression Expression "+ expression);
			VariableVisitor variableVisitor = new VariableVisitor();
			expression.accept(variableVisitor);
			for(String variable : variableVisitor.getVariables()){
				Type type = Utils.getVariableType(methodName, variable);
				StatementPattern statementPattern = new SimplePattern(PatternValue.SIMPLE_EXP, blockFlag);
				sequence.addStatementPattern(variable, type, statementPattern);
			}
			
		} else {
			System.out.println("Unknown expresion while process : "+expression);
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
