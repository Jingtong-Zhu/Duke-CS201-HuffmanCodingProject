import java.util.*;

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

	public void compress(BitInputStream in, BitOutputStream out)
	{
		int[] counts = new int[ALPH_SIZE + 1];
		
		int bits = in.readBits(BITS_PER_WORD);

		while (bits != -1) 
		{
			counts[bits]++;
			bits = in.readBits(BITS_PER_WORD);
		}

		counts[PSEUDO_EOF] = 1;

		PriorityQueue<HuffNode> prioriguy = new PriorityQueue<>();
		
		for (int i = 0; i < counts.length; i++) 
		{
			if (counts[i] > 0)
				prioriguy.add(new HuffNode(i, counts[i], null, null));
		}

		while (prioriguy.size() > 1) 
		{ 
			HuffNode left = prioriguy.remove();
			HuffNode right = prioriguy.remove();

			HuffNode newTree = new HuffNode(0, left.myWeight + right.myWeight, left, right);
			prioriguy.add(newTree);
		}

		HuffNode node = prioriguy.remove();

		String[] codings = new String[ALPH_SIZE + 1];
		makeCodingsFromTree(node,"",codings);

		out.writeBits(BITS_PER_INT, HUFF_TREE);
		writeHeader(node, out);

		in.reset();
		
		while (true) 
		{
			int newbits = in.readBits(BITS_PER_WORD);
			if (newbits == -1) break;

			String code = codings[newbits];
			if (code != null)
				out.writeBits(code.length(), Integer.parseInt(code, 2));
		}

		String pseudo = codings[PSEUDO_EOF]; 
		out.writeBits(pseudo.length(), Integer.parseInt(pseudo,2)); 
		
		out.close();
	}

	private void makeCodingsFromTree(HuffNode node, String guy, String[] encodings) {
		if (node.myRight == null && node.myLeft == null) {
			encodings[node.myValue] = guy; 
			return;
		}
		makeCodingsFromTree(node.myLeft, guy + "0", encodings);
		makeCodingsFromTree(node.myRight, guy + "1", encodings);
	}


	private void writeHeader(HuffNode root, BitOutputStream out) 
	{
		if (root.myRight != null || root.myLeft != null) {
			out.writeBits(1,0);
			writeHeader(root.myLeft, out);
			writeHeader(root.myRight, out);
		}
		else 
		{
			out.writeBits(1,1); 
			out.writeBits(BITS_PER_WORD + 1, root.myValue);
		}
	}

	public void decompress(BitInputStream in, BitOutputStream out)
	{
		int bits = in.readBits(BITS_PER_INT);
		
		if (bits == -1) throw new HuffException("illegal header starts with " + bits);
		if (bits != HUFF_TREE) throw new HuffException("illegal header starts with " + bits);
		
		HuffNode root = readTreeHeader(in);

		HuffNode tracker = root;

		while (true) 
		{
			int newbits = in.readBits(1);

			if (newbits == -1) throw new HuffException("reading bits has failed");

			else 
			{ 
				if (newbits == 0) tracker = tracker.myLeft;

				else tracker = tracker.myRight;

				if (tracker.myRight == null && tracker.myLeft == null) { 
					if (tracker.myValue == PSEUDO_EOF) break;

					else 
					{ 
						out.writeBits(BITS_PER_WORD, tracker.myValue);
						tracker = root;
					}
				}
			}
		}
		out.close();
	}

	private HuffNode readTreeHeader(BitInputStream in) 
	{
		int bits = in.readBits(1);

		if (bits == -1) throw new HuffException("reading bits has failed");

		if (bits == 0) 
		{ 
			HuffNode left = readTreeHeader(in);
			HuffNode right = readTreeHeader(in);
			return new HuffNode(0, 0, left, right);
		}
		else 
			return new HuffNode((in.readBits(BITS_PER_WORD + 1)), 0, null, null);

	}
}