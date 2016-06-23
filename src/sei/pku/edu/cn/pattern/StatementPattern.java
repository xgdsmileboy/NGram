package sei.pku.edu.cn.pattern;

/**
 * @author Jiajun
 * 
	## All patterns defined as follows:
	**flag**: marking the level of current statement.
			0:in no block; 1: in if block; 2: in else block; 3: in loop block
	-----------------------------------------------------------------------------
	* var : <assign, -1, flag>				: var = XXX;
	* var : <assign, 0, flag>				: XXX = var-expression,(could be infix/postfix/prefix expression);
	* var : <if, _, flag>					: if(var-expression) Statement;
	* var : <while, _, flag>				: while(var-expression) Statement; do Statement while(var-expression);
	* var : <for, 0, flag>					: for(var-expression; _ ; _) Statement;
	* var : <for, 1, flag>					: for(_ ; var-expression; _) Statement;
	* var : <for, 2, flag>					: for(_ ; _ ; var-expression) Statement;
	* var : <efor, 0, flag>					: for(var-expression : _) Statement;
	* var : <efor, 1, flag>					: for(_ : var-expression) Statement;
	* var : <invoke, ret/-1, flag> 			: var = a.m();
	* var : <invoke, 0, flag>				: XXX = var.m();
	* var : <invoke, k, flag>				: XXX = a.m(p1, ... var, ...);
	* var : <break, _, flag>				: relate to the nearest block, such as: if,for,while...
	* var : <continue, _, flag>				: same as break
	* var : <arrac, 0, flag>				: var[_];
	* var : <arrac, 1, flag>				: _[var];
	* var : <arrcr, _, flag>				: new XXX[var];
	* var : <newInstance, -1, flag>			: var = _.new XXX();
	* var : <newInstance, 0, flag>			: _ = var.new XXX();
	* var : <newInstance, k, flag>			: _ = _.new XXX(p1,...,var,...,pn);
	* var : <conditional, 0, flag>			: var ? exp : exp;
	* var : <conditional, 1, flag>			: exp ? var : exp;
	* var : <conditional, 2, flag>			: exp ? exp : var;
	* var : <return, _, flag>				: return var;
	* var : <vardef, _, flag>				: XXX var;
	* var : <switch, _, flag> 				: switch(var) {}
	-----------------------------------------------------------------------------
 */

public abstract class StatementPattern {
	protected Label label;
	protected int blockFlag;
	protected long hexPosition; 
	
	public StatementPattern(Label label, long varPos, int blockFlag){
		this.label = label;
		this.hexPosition = varPos;
		this.blockFlag = blockFlag;
	}
	
	@Override
	public String toString() {
		return "<" + this.label + ", " + this.hexPosition + ", " + this.blockFlag + ">";
	}
}
