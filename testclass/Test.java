package testclass;

public class Test {
	private int a;
	private String s;
	
	public Test(){
		
	}
	
	public void func(int a){
		int b = 0, c;
		if(b > 5){
			return;
		}
		
		Miniclass miniclass  = new Miniclass();
		
		switch(b){
		case 1: b++; break;
		case 2: b++; break;
		default: b--;
		}
		
		while(a < 3){
			a ++;
		}
		int i = 0;
		for(; i < 6; i++){
			for(int j = 0; j < 5; j++){
				j ++;
			}
		}
		
		int[] array = new int[a];
		
		array[4] = 4;
		for (int data : array) {
			System.out.println(data);
		}
	}
	
	class Miniclass {
		
	}
}
