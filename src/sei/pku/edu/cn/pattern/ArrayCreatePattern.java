package sei.pku.edu.cn.pattern;


public class ArrayCreatePattern extends StatementPattern {

	public ArrayCreatePattern(int blockFlag) {
		super(Label.ARRAYCREATE, PatternValue.ANY_POSITION, blockFlag);
	}

}
