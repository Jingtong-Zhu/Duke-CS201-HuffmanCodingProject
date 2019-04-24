import java.util.PriorityQueue;

//good video explaining huffman: https://www.youtube.com/watch?v=dM6us854Jk0
/**
 * Although this class has a history of several years,
 * it is starting from a blank-slate, new and clean implementation
 * as of Fall 2018.
 * <P>
 * Changes include relying solely on a tree for header information
 * and including debug and bits read/written information
 * 
 * @author Owen Astrachan
 */

public class HuffProcessor {

	public static final int BITS_PER_WORD = 8;
	public static final int BITS_PER_INT = 32;
	public static final int ALPH_SIZE = (1 << BITS_PER_WORD); 
	public static final int PSEUDO_EOF = ALPH_SIZE;
	public static final int HUFF_NUMBER = 0xface8200;
	public static final int HUFF_TREE  = HUFF_NUMBER | 1;

	private final int myDebugLevel;
	
	public static final int DEBUG_HIGH = 4;
	public static final int DEBUG_LOW = 1;
	
	public HuffProcessor() {
		this(0);
	}
	
	public HuffProcessor(int debug) {
		myDebugLevel = debug;
	}

	/**
	 * Compresses a file. Process must be reversible and loss-less.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be compressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	public void compress(BitInputStream in, BitOutputStream out){

		HuffNode node = treeMaker(in);
		String[] encodings  = new String[ALPH_SIZE + 1];
		codeWriter(node, encodings, "");
		out.writeBits(BITS_PER_INT, HUFF_TREE);
		writeTreeHeader(node, out);
		in.reset();
		writeCompressedBits(encodings, in, out);
		out.close();	
	}
	
	private HuffNode treeMaker(BitInputStream in){
		int[] counts = new int[ALPH_SIZE + 1];
		int bits = in.readBits(BITS_PER_WORD);
		
		while(bits != -1)
		{
			counts[bits]++;
			bits = in.readBits(BITS_PER_WORD);
		}
		counts[PSEUDO_EOF] = 1;
		PriorityQueue<HuffNode> pq = new PriorityQueue<>();
		for (int i = 0; i < counts.length; i++)
		{
			if (counts[i] > 0)
				pq.add(new HuffNode(i, counts[i], null, null));
		}
		while(pq.size() > 1)
		{
			HuffNode left = pq.remove();
			HuffNode right = pq.remove();
			HuffNode guy = new HuffNode(0, left.myWeight + right.myWeight, left, right);
			pq.add(guy);
		}
		return pq.remove();	
	}
	
	private void codeWriter(HuffNode node, String[] arr, String guy)
	{
		if (node.myRight == null && node.myLeft ==null)
		{
			arr[node.myValue] = guy;
			return;
		}
		codeWriter(node.myLeft, arr, guy + "0");
		codeWriter(node.myRight, arr, guy + "1");
	}
	/**
	 * Decompresses a file. Output file must be identical bit-by-bit to the
	 * original.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be decompressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	public void decompress(BitInputStream in, BitOutputStream out){

		int bits = in.readBits(BITS_PER_INT);
		if (bits !=HUFF_TREE)
			throw new HuffException("illegal header starts with "+bits);
		
		HuffNode node = readTreeHeader(in);
		readCompressedBits(node, in, out);
		out.close();
	}
	/**
	 * 
	 * @param in
	 * @return
	 */
	private HuffNode readTreeHeader(BitInputStream in) {
		int bit = in.readBits(1);
		if (bit ==-1)
			throw new HuffException("Reading bits has failed");
		if (bit == 0)
		{
			HuffNode left = readTreeHeader(in);
			HuffNode right = readTreeHeader(in);
			return new HuffNode(0, 0, left, right);
		}
		int value = in.readBits(1 + BITS_PER_WORD);
		return new HuffNode(value, 0, null, null);
	}
	
	private void writeTreeHeader(HuffNode node, BitOutputStream out)
	{
		if (node.myRight !=null || node.myLeft != null)
		{
			out.writeBits(1, 0);
			writeTreeHeader(node.myLeft,out);
			writeTreeHeader(node.myRight, out);
		}
		out.writeBits(1, 1);
		out.writeBits(BITS_PER_WORD + 1, node.myValue);
	}
	
	/**
	 * 
	 * @param root
	 * @param input
	 * @param out
	 */
	private void readCompressedBits(HuffNode root, BitInputStream input, BitOutputStream out)
	{
		HuffNode tracker = root;
		while(true)
		{
			int bits = input.readBits(1);
			if (bits == -1)
				throw new HuffException("Reading bits has failed");
			else
			{
				if (bits == 0)
					tracker = tracker.myLeft;
				else
					tracker = tracker.myRight;
				if(tracker.myRight == null && tracker.myLeft == null)
				{
					if (tracker.myValue == PSEUDO_EOF) 
						break;
					else {
						out.writeBits(BITS_PER_WORD, tracker.myValue);
						tracker = root;
					}
				}	
			}
		}
	}
	
	/**
	 * 
	 * @param encodings
	 * @param in
	 * @param out
	 */
	private void writeCompressedBits(String[] encodings, BitInputStream in, BitOutputStream out)
	{
		while(true) 
		{
			int bits = in.readBits(BITS_PER_WORD);
			if (bits == -1) break;

			String code = encodings[bits];
			if (code != null) {
				out.writeBits(code.length(), Integer.parseInt(code, 2)); 
			}
		}
		String eofcode = encodings[PSEUDO_EOF];
		out.writeBits(eofcode.length(), Integer.parseInt(eofcode,2));
	}
}