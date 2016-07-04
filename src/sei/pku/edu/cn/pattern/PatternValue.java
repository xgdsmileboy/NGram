package sei.pku.edu.cn.pattern;
/**
 * 
 * @author Jiajun
 * contains all the statement type info
 */
public abstract class PatternValue {
	/*
	 * the first seven value (0x00000001-0x00000007) are used to denote the 
	 * index for a parameter in a method/constructor declaration
	 */
	public static final long PARAM_FILTER		= 0xFFFFFFF8;
	public static final long ASSIGN_LEFT 		= 0x00000008;
	public static final long ASSIGN_RIGHT 		= 0x00000010;
	public static final long IF_EXP 			= 0x00000020;
	public static final long WHILE_EXP 			= 0x00000040;
	public static final long FOR_INIT 			= 0x00000080;
	public static final long FOR_COND 			= 0x00000100;
	public static final long FOR_UPDATE 		= 0x00000200;
	public static final long EFOR_DEF 			= 0x00000400;
	public static final long EFOR_ARRAY 		= 0x00000800;
	public static final long INVOKE_RET 		= 0x00001000;
	public static final long INVOKE_OBJ 		= 0x00002000;
	public static final long INVOKE_PARAM 		= 0x00004000;
	public static final long BREAK_EXP 			= 0x00008000;
	public static final long CONTINUE_EXP 		= 0x00010000;
	public static final long ARRAY_ACC_OBJ 		= 0x00020000;
	public static final long ARRAY_ACC_INDEX 	= 0x00040000;
	public static final long ARRAY_CREATE	 	= 0x00080000;
	public static final long NEWINSTANCE_RET 	= 0x00100000;
	public static final long NEWINSTANCE_OBJ 	= 0x00200000;
	public static final long NEWINSTANCE_PARAN 	= 0x00400000;
	public static final long CONDITIONAL_COND 	= 0x00800000;
	public static final long CONDITIONAL_LEXP 	= 0x01000000;
	public static final long CONDITIONAL_REXP 	= 0x02000000;
	public static final long RETURN_EXP 		= 0x04000000;
	public static final long VARDEF_EXP 		= 0x08000000;
	public static final long ANY_POSITION 		= 0x10000000;
	public static final long SWITCH_EXP 		= 0x20000000;
	public static final long METHOD_PARAM		= 0x40000000;
	public static final long SIMPLE_EXP			= 0x80000000;
}
