package sei.pku.edu.cn.pattern.struct;

public class BitVector {
	private byte[] bits;
	private int size;
	private int count = -1;

	public BitVector(int len) {
		size = len;
		// there are total size bits used to store info, the last
		// 8 bits are used to store the position for a pramater in
		// a method/constructor invocation
		bits = new byte[(size >> 3) + 2];
	}

	public int bitLength() {
		return this.size;
	}

	public final void set(int bit) {
		bits[bit >> 3] |= (1 << (bit & 7));
		count = -1;
	}

	public final void clear(int bit) {
		bits[bit >> 3] &= (~(1 << (bit & 7)));
		count = -1;
	}

	public final boolean get(int bit) {
		return (bits[bit >> 3] & (1 << (bit & 7))) != 0;
	}

	public BitVector and(BitVector bitVector) {
		BitVector vector = new BitVector(this.size);
		if (bitVector.bitLength() != this.size) {
			return vector;
		}
		byte[] vbits = bitVector.bits;
		int arrayLen = (size >> 3) + 2;
		for (int i = 0; i < arrayLen; i++) {
			for (int j = 0; j < 8; j++) {
				vector.bits[i] |= (bits[i] & (1 << j)) & (vbits[i] & (1 << j));
			}
		}
		return vector;
	}

	public BitVector or(BitVector bitVector) {
		BitVector vector = new BitVector(this.size);
		if (bitVector.bitLength() != this.size) {
			return vector;
		}
		byte[] vbits = bitVector.bits;
		int arrayLen = (size >> 3) + 2;
		for (int i = 0; i < arrayLen; i++) {
			for (int j = 0; j < 8; j++) {
				vector.bits[i] |= (bits[i] & (1 << j)) | (vbits[i] & (1 << j));
			}
		}
		return vector;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BitVector) {
			BitVector vector = (BitVector) obj;
			if (vector.bitLength() != this.size) {
				return false;
			}
			int arrayLen = (size >> 3) + 2;
			for (int i = 0; i < arrayLen; i++) {
				if (vector.bits[i] != this.bits[i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		String tmp = "";
		for (int i = bits.length - 1; i >= 0; i--) {
			tmp = Integer.toBinaryString(bits[i]);
			for(int j = tmp.length(); j < 8; j++){
				sb.append("0");
			}
			sb.append(tmp);
		}
		return sb.toString();

	}

	public final int count() {
		// if the vector has been modified
		if (count == -1) {
			int c = 0;
			int end = bits.length;
			for (int i = 0; i < end; i++)
				c += BYTE_COUNTS[bits[i] & 0xFF]; // sum bits per byte
			count = c;
		}
		return count;
	}

	private static final byte[] BYTE_COUNTS = { // table of bits/byte
			0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 1, 2, 2, 3,
			2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 1, 2, 2, 3, 2, 3, 3, 4,
			2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5,
			4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5,
			2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3, 4, 4, 5,
			4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6,
			4, 5, 5, 6, 5, 6, 6, 7, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 4, 5, 5, 6, 5, 6, 6, 7, 5, 6, 6, 7,
			6, 7, 7, 8 };
}
