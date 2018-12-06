import java.util.PriorityQueue;

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
	int magic;

	private final int myDebugLevel;
	
	public static final int DEBUG_HIGH = 4;
	public static final int DEBUG_LOW = 1;
	
	public HuffProcessor() {
		this(0);
	}
	
	public HuffProcessor(int debug) {
		myDebugLevel = debug;
	}
//FARZEENNAJAMA
	/**
	 * Compresses a file. Process must be reversible and loss-less.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be compressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	public void compress(BitInputStream in, BitOutputStream out){
		int [] counts = readForCounts(in);
		HuffNode root = makeTreeFromCounts(counts);
		String[] codings = makeCodingsFromTree(root);
		out.writeBits(BITS_PER_INT, HUFF_TREE);
		WriteHeader(root, out);
		
		in.reset();
		writeCompressedBits(codings, in, out);
		while (true){
			int val = in.readBits(BITS_PER_WORD);
			if (val == -1) break;
			out.writeBits(BITS_PER_WORD, val);
		}
		out.close();
	}
	
	
	private void writeCompressedBits(String[] codings, BitInputStream in, BitOutputStream out) {
		
		int read = in.readBits(BITS_PER_WORD + 1);
		readForCounts(in);
		 
		
		 out.writeBits(codings[read].length(), Integer.parseInt(codings[read],2));
		 read = in.readBits(BITS_PER_WORD + 1);
		 out.writeBits(codings[PSEUDO_EOF].length(), Integer.parseInt(codings[PSEUDO_EOF],2));
	}
	private void WriteHeader(HuffNode root, BitOutputStream out) {
		out.writeBits(BITS_PER_INT, HUFF_TREE);
		writeTree(root,out);
	// TODO Auto-generated method stub
	
}

	private void  writeTree(HuffNode root, BitOutputStream out) {
		if(root.myLeft == null && root.myRight== null) {
			out.writeBits(1, 1);
			out.writeBits(BITS_PER_WORD + 1, root.myValue);
		}
		
		if(root.myLeft !=null && root.myRight != null) {
			out.writeBits(1, 0);
			 writeTree(root.myRight,out);
			 writeTree(root.myLeft, out);
			
			
			
		}
		
	}

	private String[] makeCodingsFromTree(HuffNode root) {
		 String[] encodings = new String[ALPH_SIZE + 1];
		 codingHelper(root,"",encodings);

		 
	return encodings;
}
//String path="";
	private void codingHelper(HuffNode root, String string, String[] encodings) {
		if (root.myRight == null && root.myLeft== null) {
	        encodings[root.myValue] = string;
	        return;
	   }
		
		
		if(root.myLeft != null) {
			
			codingHelper(root.myLeft, string+"0",encodings );
			
			
		}
		if(root.myRight != null) {
			codingHelper(root.myRight, string+"1",encodings );		
		}

	}

	private HuffNode makeTreeFromCounts(int[] counts) {
		PriorityQueue<HuffNode> pq = new PriorityQueue<>();


		for(int k=0; k < counts.length; k++) {
			if(counts[k] > 0) {
				 pq.add(new HuffNode(k,counts[k],null,null));
			}
			
		   
		}

		while (pq.size() > 1) {
		    HuffNode left = pq.remove();// pick the left one
		    HuffNode right = pq.remove();// pick the right one
		    HuffNode poop = new HuffNode(-1,left.myWeight+right.myWeight,left,right); //-1 starts from the end, adds the weights and then make left and right sub trees.
		    // create new HuffNode t with weight from
		    // left.weight+right.weight and left, right subtrees
		    pq.add(poop); // combine both of them and add to the pq
		}
		HuffNode root = pq.remove(); // in the end we have one tree. from that one tree we get the root.

	// TODO Auto-generated method stub
	return root;
}

	private int[] readForCounts(BitInputStream in) {
		int[] abc = new int[ALPH_SIZE + 1];
		int bits = in.readBits(BITS_PER_WORD);
		while(bits> 0) {
		abc[bits] +=1;	
		abc[PSEUDO_EOF] = 1 ; //write it once
		 bits = in.readBits(BITS_PER_WORD);
		
		}
		
	// TODO Auto-generated method stub
	return abc;
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
		
		if(bits != HUFF_TREE) {
			throw new HuffException("illegal header starts with" + bits);
		} //exception thrown when file of compressed bits does not start with 32 bit value.
     
		HuffNode head=readTreeHeader(in);
		readCompressedBits(in,  head, out);
		out.close();
	}
	
    private HuffNode readTreeHeader(BitInputStream in) {

    	
    	int bits = in.readBits(1);
     
    	if(bits ==-1) {
       	 throw new HuffException("illegal header starts with" + bits);
        }
	//do a preorder traversal of the tree.
    	if(bits == 0) {
    		HuffNode left = readTreeHeader(in);
    		HuffNode right = readTreeHeader(in);
    		return new HuffNode(0,0,left,right);
    	}
    	else {
    		int val = in.readBits(BITS_PER_WORD + 1);
    		HuffNode ans = new HuffNode(val,0,null,null);
    		return ans;
    	}
    	

	
}
    
    
    private void readCompressedBits(BitInputStream in,HuffNode root,BitOutputStream out) {
    	
		HuffNode current =  root; 
    	   while (true) {
    	       int bits = in.readBits(1);
    	       if (bits == -1) {
    	           throw new HuffException("bad input, no PSEUDO_EOF");
    	       }
    	       else { 
    	           if (bits == 0) current = current.myLeft; //0 left
    	        	
    	        	   
    	      else current = current.myRight; //right--1

    	          // if (current.myValue == 1) {
    	        	   
    	        	   if (current.myLeft == null && current.myRight == null) {
    	               if (current.myValue == PSEUDO_EOF) 
    	                  return;
    	        	   }// out of loop
    	               else {
    	                   out.writeBits(BITS_PER_WORD, current.myValue);//current.myValue=bits;
    	                   current = root; // start back after leaf
    	               //}
    	           }
    	       }
    	   }
    	
		//return current;
    	
    }
	
	
	
}