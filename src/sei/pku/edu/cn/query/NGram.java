package sei.pku.edu.cn.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sei.pku.edu.cn.pattern.Sequences;
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
	/**
	 * This method is used for query a sequence from the whole, 
	 * return a list of statements that have a similar structure 
	 * with the queried one with possibilities for each.  
	 */
	public List<Pair<Sequences, Pair<Double, Double> >> query(Sequences sequences){
		
		List<Sequences> result_maybeAdd = new ArrayList<>();
		List<Sequences> result_maybeDelete = new ArrayList<>();
		List<Sequences> result_maybeChange = new ArrayList<>();
		
		for(Pair<Long, Sequences> pair : nGramSequences){
			int diffType = sequences.oneDiffStatementPattern(pair.getSecond());
			if(diffType == -1){
				continue;
			} else if(diffType == 1){
				result_maybeAdd.add(pair.getSecond());
			} else if(diffType == 2){
				result_maybeDelete.add(pair.getSecond());
			} else if(diffType == 3){
				result_maybeChange.add(pair.getSecond());
			} else {
				
			}
		}
		
		List<Pair<Sequences, Integer>> sorted_add = sortSequences(result_maybeAdd);
		List<Pair<Sequences, Integer>> sorted_delete = sortSequences(result_maybeDelete);
		List<Pair<Sequences, Integer>> sorted_change = sortSequences(result_maybeChange);
		float size_add = 0;
		for(Pair<Sequences, Integer> pair : sorted_add){
			size_add += pair.getSecond();
		}
		float size_delete = 0;
		for(Pair<Sequences, Integer> pair : sorted_delete){
			size_delete += pair.getSecond();
		}
		float size_change = 0;
		for(Pair<Sequences, Integer> pair : sorted_change){
			size_change += pair.getSecond();
		}
		float size_all = size_add + size_delete + size_change;
		List<Pair<Sequences, Pair<Double, Double> >> result = new ArrayList<>();
		int guard = sorted_add.size() > 5 ? 5 : sorted_add.size();
		// return first five
		for(int i = 0; i < guard; i++){
			int count = sorted_add.get(i).getSecond();
			Pair<Double, Double> statistic = new Pair<Double, Double>(Double.valueOf(count/size_add), Double.valueOf(count/size_all));
			result.add(new Pair<Sequences, Pair<Double, Double> >(sorted_add.get(i).getFirst(), statistic));
		}
		
		guard = sorted_delete.size() > 5 ? 5 : sorted_delete.size();
		for(int i = 0; i < guard; i++){
			int count = sorted_delete.get(i).getSecond();
			Pair<Double, Double> statistic = new Pair<Double, Double>(Double.valueOf(count/size_delete), Double.valueOf(count/size_all));
			result.add(new Pair<Sequences, Pair<Double, Double> >(sorted_delete.get(i).getFirst(), statistic));
		}
		
		guard = sorted_change.size() > 5 ? 5 : sorted_change.size();
		for(int i = 0; i < guard; i++){
			int count = sorted_change.get(i).getSecond();
			Pair<Double, Double> statistic = new Pair<Double, Double>(Double.valueOf(count/size_change), Double.valueOf(count/size_all));
			result.add(new Pair<Sequences, Pair<Double, Double> >(sorted_change.get(i).getFirst(), statistic));
		}
		
		return result;
		
//		List<Sequences> result_maybeAdd = new ArrayList<>();
//		List<Sequences> result_maybeDelete = new ArrayList<>();
//		List<Sequences> result_not_include_but_one_diff = new ArrayList<>();
//		long represent = sequences.getHexValueRepresent();
//		//using the encoding information to filter
//		for(Pair<Long, Sequences> pair : nGramSequences){
//			Long other = pair.getFirst();
//			//maybe some structure should be added to the original statement sequence
//			if((other & represent) == represent){
//				result_maybeAdd.add(pair.getSecond());
//			} else if((other & represent) == other){ // maybe some structure should be deleted from original statement sequence
//				result_maybeDelete.add(pair.getSecond());
//			} else { // two statement sequences have no including relation
//				int diff_count = 0;
//				List<StatementPattern> to_be_matched_statements = sequences.getAllStatementPatterns();
//				List<StatementPattern> statement_in_dataset = pair.getSecond().getAllStatementPatterns();
//				int size = to_be_matched_statements.size();
//				size = size > statement_in_dataset.size() ? statement_in_dataset.size() : size;
//				for(int i = 0; i < size; i++){
//					if(statement_in_dataset.get(i).getLabel() != to_be_matched_statements.get(i).getLabel()) {
//						diff_count ++;
//					}
//				}
//				if(diff_count <= 1){
//					result_not_include_but_one_diff.add(pair.getSecond());
//				}
//			}
//		}
//		
//		List<Sequences> filtered_add = processAdd(result_maybeAdd, sequences);
//		List<Sequences> filtered_delete = processDelete(result_maybeDelete, sequences);
//		List<Sequences> filtered_diff = processDifference(result_not_include_but_one_diff, sequences);
//		
//		List<Sequences> preciseResult = new ArrayList<>();
//		List<StatementPattern> queryPatterns = sequences.getAllStatementPatterns();
//		int threshold = gramLengh / 2 + 1;
//		for(Sequences seq : result_maybeAdd){
//			List<StatementPattern> matchPatterns = seq.getAllStatementPatterns();
//			boolean flag = true;
//			for(int i = 0; i < threshold; i++){
//				if((matchPatterns.get(i).getHexPosition() & queryPatterns.get(i).getHexPosition()) == 0){
//					flag = false;
//					break;
//				}
//			}
//			if(flag){
//				preciseResult.add(seq);
//			}
//		}
//		List<Pair<Sequences, Double>> sortSequences = statistic(preciseResult);
//		return sortSequences;
	}
	
	
	private List<Pair<Sequences, Integer>> sortSequences(List<Sequences> list){
		List<Pair<Sequences, Integer>> result = new ArrayList<>();
		for(Sequences sequences : list){
			int i = 0;
			for(i = 0; i < result.size(); i++){
				if(result.get(i).getFirst().equals(sequences)){
					break;
				}
			}
			if(i == result.size()){
				result.add(new Pair<Sequences, Integer>(sequences, Integer.valueOf(1)));
			} else {
				result.get(i).setSecond(result.get(i).getSecond()+1);
			}
		}
		
		Collections.sort(result, new Comparator<Pair<Sequences, Integer>>() {
			@Override
			public int compare(Pair<Sequences, Integer> o1, Pair<Sequences, Integer> o2) {
				return o1.getSecond() - o2.getSecond();
			}
		});
		return result;
	}
	
	
	private List<Pair<Sequences, Double>> statistic(List<Sequences> sequences){
		List<Pair<Sequences, Double>> sortSequences = new ArrayList<>();
		for(Sequences sequences2 : sequences){
			sortSequences.add(new Pair<Sequences, Double>(sequences2, Double.valueOf(1.0)));
		}
		return sortSequences;
	}
	
}
