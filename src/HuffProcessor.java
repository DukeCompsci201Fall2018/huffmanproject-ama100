
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

		while (true){
			int val = in.readBits(BITS_PER_WORD);
			if (val == -1) break;
			out.writeBits(BITS_PER_WORD, val);
		}
		out.close();
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
		
     
     
		while (true){
			int val = in.readBits(BITS_PER_WORD);
			if (val == -1) break;
			out.writeBits(BITS_PER_WORD, val);
			
		}
		out.close();
	}
	
    private HuffNode ReadTreeHeader(BitInputStream in) {

    	
    	int magic = in.readBits(BITS_PER_INT);
		
		if(magic != HUFF_TREE) {
			throw new HuffException("illegal header starts with" + magic);
		} //exception thrown when file of compressed bits does not start with 32 bit value.
     
    	if(magic < 0) {
       	 throw new HuffException("illegal header starts with" + magic);
        }
	//do a preorder traversal of the tree.
    	if(magic == 0) {
    		HuffNode left = ReadTreeHeader(in);
    		HuffNode right = ReadTreeHeader(in);
    		return new HuffNode(0,0,left,right);
    	}
    	else {
    		int val = in.readBits(BITS_PER_WORD + 1);
    		HuffNode ans = new HuffNode(val,0,null,null);
    		return ans;
    	}
    	

	
}
    private HuffNode readCompressedBits(BitInputStream in,HuffNode root,BitOutputStream out) {
    	
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
    	                   break;
    	        	   }// out of loop
    	               else {
    	                   out.writeBits(bits, current.myValue);//current.myValue=bits;
    	                   current = root; // start back after leaf
    	               //}
    	           }
    	       }
    	   }
    	
		return current;
    	
    }
	
	
	
}