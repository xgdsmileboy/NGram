package sei.pku.edu.cn.pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.Type;

public class Sequence {
	
	private Map<String, List<StatementPattern>> sequenceMap = new HashMap<>();
	
	public Sequence(){
		
	}
	
	public void addStatementPattern(String var, Type type, StatementPattern pattern){
		String variable = var + ":" + type;
		addStatementPattern(variable, pattern);
	}
	
	public void addStatementPattern(String variableWithClass, StatementPattern pattern){
		if(containVariable(variableWithClass)){
			sequenceMap.get(variableWithClass).add(pattern);
		} else {
			List<StatementPattern> patternList = new ArrayList<>();
			patternList.add(pattern);
			sequenceMap.put(variableWithClass, patternList);
		}
	}
	
	public boolean containVariable(String var, Type type){
		String variable = var + ":" + type;
		return sequenceMap.containsKey(variable);
	}
	
	private boolean containVariable(String variableWithType){
		return sequenceMap.containsKey(variableWithType);
	}
	
	public List<StatementPattern> getStatementPatternList(String variable){
		return sequenceMap.get(variable);
	}
	
	public List<List<StatementPattern>> getRelateStatementListByType(Type type){
		List<List<StatementPattern>> result = new ArrayList<>();
		for(String key : sequenceMap.keySet()){
			String[] words = key.split(":");
			if(words[1].equalsIgnoreCase(type.toString())){
				result.add(sequenceMap.get(key));
			}
		}
		return result;
	}
	
	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		for(Entry<String, List<StatementPattern>> entry : sequenceMap.entrySet()){
			stringBuffer.append(entry.getKey() + " : ");
			stringBuffer.append(entry.getValue().toString()+"\n");
		}
		return stringBuffer.toString();
	}
}
