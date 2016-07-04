package sei.pku.edu.cn.pattern;

public class ParamPattern extends StatementPattern{

	public ParamPattern(long varPos) {
		super(Label.PARAM, varPos, BlockLevel.BLOCK_NO);
	}

}
