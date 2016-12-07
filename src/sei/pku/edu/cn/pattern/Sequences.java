package sei.pku.edu.cn.pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sound.midi.Sequence;

import org.eclipse.jdt.core.dom.Type;

public class Sequences {
	
	private Map<String, List<StatementPattern>> sequenceMap = new HashMap<>();
	
	private int filterLength = 3;
	
	public Sequences(){
		
	}
	/**
	 * set the length to filter sequence
	 * @param n
	 */
	public void setFilterLength(int n){
		filterLength = n;
	}
	
	public int oneDiffStatementPattern(Sequences sequences){
		if(this.sequenceMap.size() != 1 || sequences.sequenceMap.size() != 1){
			System.out.println("Get sequence different number error, the length is not 1.");
			return -1;
		}
		List<StatementPattern> self = getStatementPatterns();
		List<StatementPattern> other = sequences.getAllStatementPatterns();
		int size = self.size();
		if(size != other.size()){
			System.out.println("Get sequence different number error, sequences' lengths are not equal.");
			return -1;
		}
		boolean oneDiff = false;
		// diffType : 1,should add a statement; 2,should delete a statement; 3, should change a statement
		int diffType = -1;
		for(int i = 0, j = 0; i < size && j < size;){
			if(self.get(i).getHexPosition() != other.get(j).getHexPosition()){
				if(oneDiff){
					return -1;
				} else {
					if(i + 1 == size || j + 1 == size){
						diffType = 3;
						return diffType;
					}
					if(self.get(i).getHexPosition() == other.get(j+1).getHexPosition()){
						oneDiff = true;
						diffType = 1;
						i ++;
						j += 2;
					} else if(self.get(i+1).getHexPosition() == other.get(j).getHexPosition()){
						oneDiff = true;
						diffType = 2;
						i += 2;
						j++;
					} else {
						oneDiff = true;
						diffType = 3;
						i ++;
						j ++;
					}
				}
			} else {
				i ++;
				j ++;
			}
		}
		return diffType;
	}
	
	/**
	 * obtain all {@code StatementPattern}s from current sequences, 
	 * TODO need to be refactored, the structure of the sequence should be split
	 * into two classes, and one holds a single sequence, the other holds the 
	 * sequence list
	 * 
	 * using it to get a sequence of {@code StatementPattern} temporary, only used 
	 * in query process
	 * @return
	 */
	public List<StatementPattern> getStatementPatterns(){
		List<StatementPattern> statements = new ArrayList<>();
		for(Entry<String, List<StatementPattern>> entry : sequenceMap.entrySet()){
			List<StatementPattern> list = entry.getValue();
			for(StatementPattern statementPattern : list){
				statements.add(statementPattern);
			}
		}
		return statements;
	}

	/**
	 * add new variable statement
	 * @param var : variable name
	 * @param type : variable type
	 * @param pattern : statement pattern
	 */
	public void addStatementPattern(String var, Type type, StatementPattern pattern){
		String variable = var + ":" + type;
		addStatementPattern(variable, pattern);
	}
	/**
	 * add new variable statement
	 * @param variableWithType : variable with type information, format as var:type
	 * @param pattern : statement pattern
	 */
	public void addStatementPattern(String variableWithType, StatementPattern pattern){
		if(containVariable(variableWithType)){
			sequenceMap.get(variableWithType).add(pattern);
		} else {
			List<StatementPattern> patternList = new ArrayList<>();
			patternList.add(pattern);
			sequenceMap.put(variableWithType, patternList);
		}
	}
	
	
	public List<Sequences> sliceSequence(int sliceLength){
		List<Sequences> result = new ArrayList<>();
		for(Entry<String, List<StatementPattern>> entry : sequenceMap.entrySet()){
			String variableWithType = entry.getKey();
			List<StatementPattern> statementPatterns = entry.getValue();
			int len = statementPatterns.size();
			for(int i = 0; i < len - sliceLength; i++){
				Sequences sequence = new Sequences();
				for(int j = i; j < i + sliceLength; j++){
					sequence.addStatementPattern(variableWithType, statementPatterns.get(j));
				}
				result.add(sequence);
			}
		}
		return result;
	}
	
	public long getHexValueRepresent(){
		long hex = 0;
		for(Entry<String, List<StatementPattern>> entry : sequenceMap.entrySet()){
			List<StatementPattern> patterns = entry.getValue();
			for(StatementPattern statementPattern : patterns){
				hex |= statementPattern.hexPosition;
			}
		}
		return hex;
	}
	/**
	 * generate a new sequence with the lengths of all variables' sequence 
	 * are no shorter than {@code filterLength}
	 * @return
	 */
	public Sequences lengthFilter(){
		Sequences sequence = new Sequences();
		for(Entry<String, List<StatementPattern>> entry : sequenceMap.entrySet()){
			if(entry.getValue().size() < filterLength){
				continue;
			}
			sequence.sequenceMap.put(entry.getKey(), entry.getValue());
		}
		return sequence;
	}
	/**
	 * query variable {@code var} with the type of {@code type}
	 * @param var : variable name
	 * @param type : varibable's type
	 * @return true if this sequence contains the variable, otherwise false.
	 */
	public boolean containVariable(String var, Type type){
		return containVariable(var + ":" + type);
	}
	/**
	 * query variable 
	 * @param variableWithType
	 * @return
	 */
	private boolean containVariable(String variableWithType){
		return sequenceMap.containsKey(variableWithType);
	}
	/**
	 * get statement sequence for specific variable
	 * @param variable
	 * @return
	 */
	public List<StatementPattern> getStatementPatternList(String variableWithType){
		return sequenceMap.get(variableWithType);
	}
	public List<StatementPattern> getAllStatementPatterns(){
		List<StatementPattern> result = new ArrayList<>();
		for(Entry<String, List<StatementPattern>> entry : sequenceMap.entrySet()){
			result.addAll(entry.getValue());
		}
		return result;
	}
	/**
	 * get all variables' sequences for specific type
	 * @param type
	 * @return
	 */
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
	public boolean equals(Object obj) {
		if(obj instanceof Sequences){
			Sequences comp = (Sequences) obj;
			if(sequenceMap.size() != 1 || comp.sequenceMap.size() != 1){
				return false;
			} else {
				List<StatementPattern> self = getAllStatementPatterns();
				List<StatementPattern> other = comp.getAllStatementPatterns();
				if(self.size() != other.size()){
					return false;
				} else {
					for(int i = 0; i < self.size(); i++){
						if(self.get(i).getLabel() != other.get(i).getLabel()){
							return false;
						}
					}
					return true;
				}
			}
		} else {
			return false;
		}
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
	
	public String filterString(){
		StringBuffer stringBuffer = new StringBuffer();
		for(Entry<String, List<StatementPattern>> entry : sequenceMap.entrySet()){
			if(entry.getValue().size() < filterLength){
				continue;
			}
			stringBuffer.append(entry.getKey() + " : ");
			stringBuffer.append(entry.getValue().toString()+"\n");
		}
		return stringBuffer.toString();
	}
}
