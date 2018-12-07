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
	private int magic;//

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
		int [] counts = readForCounts(in);//Determine the frequency of every 
		//eight-bit character/chunk in the file being compressed. I used "in" as a variable but it is the text or the file to be compressed
		HuffNode root = makeTreeFromCounts(counts);//from the frequency of eight bit of characters in the "in"
		//make a HuffMan tree
		String[] codings = makeCodingsFromTree(root);//from the graph it created on the previous tree,
		//create an encoding for each eigth-bit character chunk interms of 0 and one. Basiclly it codes the apth from the root to the leaf.
		
		out.writeBits(BITS_PER_INT, HUFF_TREE);// we write bits for the huff-tree and 
		writeHeader(root, out);//Write the magic number and the tree to the beginning/header of the compressed file 
		
		
		in.reset();//This method repositions the “cursor” to the beginning of the input file.

		writeCompressedBits(codings, in, out);//do the same by deleting 
		//the eight characters that are visited
		out.close();

	}
	
	private int[] readForCounts(BitInputStream in) {
		int[] abc = new int[ALPH_SIZE + 1];//create an array of size 257. 256 for the bit and the extra one is for PSEUDO_EOF
		
		while(true) {
		int bits = in.readBits(BITS_PER_WORD);//reads the first bit in the "in" and remove it	
		if(bits==-1) break;//if the bits are done after it traverse through all the bits, it will get -1 and break the traversal
		abc[bits] +=1;	//when we get the same characters repeated it increment the number stored in the array.
		 
		
		}
	abc[PSEUDO_EOF] = 1 ;//it gives one for PSEUDO_EOF
	// TODO Auto-generated method stub
	return abc;
}
	
	private HuffNode makeTreeFromCounts(int[] abc) {//the ideas is to create the huffman tree based on the
		// number of frequncey of the characters
		
		PriorityQueue<HuffNode> pq = new PriorityQueue<>();//we use priority queue to priotitize 
		//the adding part by the number stored in the array named abc
		for(int k=0; k < abc.length; k++) {
			if(abc[k] > 0) {//if the character in the "in" file exist, it has a frequency number greater than 0
				 pq.add(new HuffNode(k,abc[k],null,null));//create a single node
			}//by the end of this loop we will have node of the characters with their frequncies marked 
			//in them and left and right node are null
			
		   
		}

		while (pq.size() > 1) {// finally we will get the head of the hufman tree
		    HuffNode left = pq.remove();// pick the first and assign it to be left
		    HuffNode right = pq.remove();// pick the the first after one befrore it is removed  and assigne it as a right node
		    HuffNode poop = new HuffNode(0,left.myWeight+right.myWeight,left,right); // creates a node which has 0 as a value, left.myWeight+right.myWeight as a //
		    //wiegh and the left and the right node as a child
		    // left.weight+right.weight and left, right subtrees
		    pq.add(poop); // add the new node to the priorityque.
		}
		HuffNode head = pq.remove(); // in the end we have one tree. from that one tree we get the root.

	// TODO Auto-generated method stub
	return head;//it is the head of the huffman tree
}
	
	private String[] makeCodingsFromTree(HuffNode head) {
		 String[] encodings = new String[ALPH_SIZE + 1];//creates string array of size 257
		 codingHelper(head,"",encodings);//

		 
	return encodings;//returns the String array which stores the path that defines in terms of 0 and one
}
	
	private void codingHelper(HuffNode root, String path, String[] encodings) {
		if (root.myRight == null && root.myLeft== null) {
	        encodings[root.myValue] = path;
	        return;
	   }
		if(root.myLeft != null) {
			codingHelper(root.myLeft, path+"0",encodings );//defines the left traversal  for the eigth bit character
		}
		if(root.myLeft != null) {
			codingHelper(root.myRight,path+"1",encodings );	//	defines the right traversal  for the eigth bit character
		}
		

	}
	
	
	private void writeHeader(HuffNode root, BitOutputStream out) {//writting the tree
		// TODO Auto-generated method stub
		
			if(root.myLeft != null || root.myRight != null) {//b
				out.writeBits(1, 0);//write a single bit of zero. 1 determines the number of zero is one.
				writeHeader(root.myLeft,out);//recursive call
				writeHeader(root.myRight,out);//recursive call
				
			}
			else {
				out.writeBits(1, 1);//if root is leaf, put one 1.
				out.writeBits(BITS_PER_WORD + 1, root.myValue);//then next to the 1, put the nine characters
				//which defines the character stored in the in the root.myValue
			}
	}
	
	
	
	
	
	
	private void writeCompressedBits(String[] codings, BitInputStream in, BitOutputStream out) {
	
		while (true) {
			int bits = in.readBits(BITS_PER_WORD);//gets the first 8 bits
			if (bits == -1) 
				break;
			String code = codings[bits];//we determine the path of the bits we get the above line
			out.writeBits(code.length(), Integer.parseInt(code,2));//changes the code in the string form to the sequence of strings
		}
		
		String code = codings[PSEUDO_EOF];
		out.writeBits(code.length(), Integer.parseInt(code,2));// changes the code in the string form to the sequence of strings;
		
		
		
	// TODO Auto-generated method stub
	
}	
//String path="";
	
	/**
	 * Decompresses a file. Output file must be identical bit-by-bit to the
	 * original.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be decompressed. it is the already compressed file to be decompressed and turn in to the original file
	 * @param out
	 *            Buffered bit stream writing to the output file. It is the output and it will have the decompressed version 
	 *            //of the compressed file.The out put will be either text or image depending the file we compressed in the in
	 */
	public void decompress(BitInputStream in, BitOutputStream out){
		//the "out" will have sequence of characters after this method decompresses the  "in"
		
		int bits = in.readBits(BITS_PER_INT);// Reads and returns the next single bit of input from this stream.
		
		if(bits != HUFF_TREE) {//if the bit is not a hufftree
			throw new HuffException("Invalid header"+bits);
		} //exception thrown when file of compressed bits does not start with 32 bit value.
     if(bits==-1) throw new HuffException("Invalid header" + bits);
		
		
		HuffNode head=readTreeHeader(in);//defining the head or the root of the compressed file that will be stored in the out.
		//we define the head/root by using the readTreeHeader method created above.
		//
		readCompressedBits(head, in, out);//using the root we get from the previous line of code, we read the compressed file by 
		//traversing through the tree of the compressed(0) file and by mimicking the arrangement of the tree, it creates a tree for the decomprssed file
		//and store it in the out. Thus the 
		out.close();//This closes this stream for reading otherwise throws error, if we cannot 
		// close the out
	}
	/**
	 * Basiclly, this method reads the bit sequence representing tree or "in"
	 * @param in, which is the tree which coded character in terms of path defined by 0 and one
	 * 
	 * @return tree which holds all the characters stored in the "in" in a form of tree.
	 */
	
    private HuffNode readTreeHeader(BitInputStream in) {//in is a compressed file in 0 and 1 and we are trying to
    	//obtain the information endocode in it. The characters are coded next to 1 which is at the leaf

    	
    	int bits = in.readBits(1);//it reads the first bit of the group of zeros and ones
    	//this method move to the right whenever it's called and does not read the same bit again.
    	//it reads one bit and return the first bit it reads and give it to bits variable
     
    	if(bits ==-1) {//if there is no bit it will return -1, else it will continue
       	 throw new HuffException("illegal bit");
        }
	//do a preorder traversal of the tree.
    // First it check if the node of the "in" which is left after the first bit is removed, is either an internal or a leaf node
    // if the bits is zero, the traversal does not reach at the leaf node in the tree encoded by "in".
    	if(bits == 0) {
    		HuffNode left = readTreeHeader(in);//the sequence in the in is reducing whenever the readBits is called
    		HuffNode right = readTreeHeader(in);
    		return new HuffNode(0,0,left,right);//// it creates new node and 
    		//put the 0 values and 0 weight for intermediate or internal nodes and connect it to the left and right
    	}
    	
    	//if the bit is 1, that means the character next to it is nine bit character. Thus we reached at the leaf
    	//it then reads the next nine bits, which is the character stored at the leaf
    	else {
    		int val = in.readBits(BITS_PER_WORD + 1);//read the first nine characters stored in the "in". Assume the val 
    		//represented the ascii number
    		return new HuffNode(val,0,null,null);//now we put that ASCII code in the leaf.
    		
    	}

} 
  private void readCompressedBits(HuffNode root,BitInputStream in,BitOutputStream out) {

    	HuffNode current =  root; // the root node
    	while (true) {
    		int bits = in.readBits(1);//,reads and return the first bit
    		if (bits == -1) {
    			throw new HuffException("bad input, no PSEUDO_EOF");
    		}
    		else { 
    			if (bits == 0) {//if bit is 0, that means the node in the "in" is not a leaf node
    				current = current.myLeft; //current get the left node, note 0 means turn to the left else to the right
    			}
    			else {
    				current = current.myRight; //if bits is 1, go to the right
    			}
    			

    			if (current.myLeft == null && current.myRight == null) {//if we reach at the leaf, we get the value in terms bits.
    				// Then we write the value in the "out" in-terms of eight bits

    				if (current.myValue == PSEUDO_EOF) 
    					break;
    				// out of loop
    				else {
    					out.writeBits(BITS_PER_WORD, current.myValue);//
    					current = root; //start from the root again and do the traversal in the while loop
    					//}
    				}
    			}
    		}//return current;
    	}

	
    }	
}
