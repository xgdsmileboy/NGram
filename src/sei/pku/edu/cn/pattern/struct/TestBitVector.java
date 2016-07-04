package sei.pku.edu.cn.pattern.struct;

public class TestBitVector {
	public static void main(String[] args) {
		BitVector bitVector = new BitVector(10);
		System.out.println(bitVector.bitLength());
		bitVector.set(4);
		System.out.println(bitVector.get(4));
		System.out.println(bitVector.toString());
		System.out.println(bitVector.get(5));
		bitVector.clear(4);
		System.out.println(bitVector.get(4));
		
		bitVector.set(5);
		bitVector.set(9);
		System.out.println(bitVector.count());
		System.out.println(bitVector.toString());
		
		BitVector bitVector2 = new BitVector(5);
		System.out.println(bitVector.equals(bitVector2));
		System.out.println(bitVector.and(bitVector2).toString());

	}
}
