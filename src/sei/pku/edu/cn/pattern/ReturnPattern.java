package sei.pku.edu.cn.pattern;

public class ReturnPattern extends StatementPattern {

	public ReturnPattern(int blockFlag) {
		super(Label.RETURN, PatternValue.RETURN_EXP, blockFlag);
	}

}
