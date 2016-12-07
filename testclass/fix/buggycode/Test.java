package testclass;

public class Test {
	private int a;
	private String s;
	
	public Test(){
		
	}
	
	public void func(int a, int c){
		int b = 0;
		if(b > 5){
			return;
		}
		Test test = new Test();
		Miniclass miniclass  = test.new Miniclass();
		
		c = miniclass.test(b, b);
		
		switch(b){
		case 1: b++; break;
		case 2: b++; break;
		default: b--;
		}
		
		c = c > 4 ? c : c + 4;
		
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
		
		public int test(int a, int b){
			return a;
		}
		
		public void test2(){
			int m, n;
		}
	}
}
