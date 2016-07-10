package sei.pku.edu.cn.query;

import java.util.ArrayList;
import java.util.List;

import sei.pku.edu.cn.pattern.Sequences;
import sei.pku.edu.cn.pattern.StatementPattern;
import sei.pku.edu.cn.pattern.struct.Pair;

public class NGram {
	private List<Sequences> sequences;
	private int gramLengh = 3;
	private List<Pair<Long, Sequences>> nGramSequences = new ArrayList<>();
	
	public NGram(List<Sequences> sequences){
		this.sequences = sequences;
	}
	
	public void setLength(int n){
		this.gramLengh = n;
	}
	
	public List<Pair<Long, Sequences>> slicing(){
		for(Sequences sequence : sequences){
			List<Sequences> nSequences = sequence.sliceSequence(gramLengh);
			for(Sequences nseq : nSequences){
				nGramSequences.add(new Pair<Long, Sequences>(nseq.getHexValueRepresent(), nseq));
			}
		}
		return nGramSequences;
	}
	
	public List<Sequences> query(Sequences sequences){
		List<Sequences> result = new ArrayList<>();
		long represent = sequences.getHexValueRepresent();
		for(Pair<Long, Sequences> pair : nGramSequences){
			if((pair.getFirst() & represent) != represent){
				continue;
			}
			result.add(pair.getSecond());
		}
		List<Sequences> preciseResult = new ArrayList<>();
		List<StatementPattern> queryPatterns = sequences.getAllStatementPatterns();
		int threshold = gramLengh / 2 + 1;
		for(Sequences seq : result){
			List<StatementPattern> matchPatterns = seq.getAllStatementPatterns();
			boolean flag = true;
			for(int i = 0; i < threshold; i++){
				if((matchPatterns.get(i).getHexPosition() & queryPatterns.get(i).getHexPosition()) == 0){
					flag = false;
					break;
				}
			}
			if(flag){
				preciseResult.add(seq);
			}
		}
		
		return preciseResult;
	}
	
}
