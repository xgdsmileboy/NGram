package sei.pku.edu.cn.pattern;

public class BreakPattern extends StatementPattern {
	public BreakPattern(int blockFlag) {
		super(Label.BREAK, PatternValue.BREAK_EXP, blockFlag);
	}
}
