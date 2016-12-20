import java.io.*;
/*************************************************************************
 *  Compilation:  javac LZW.java
 *  Execution:    java LZW - < input.txt   (compress)
 *  Execution:    java LZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STARTING WITH ORACLE JAVA 6, UPDATE 7 the SUBSTRING
 *  METHOD TAKES TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 *************************************************************************/

public class MyLZW {
    private static final int R = 256;        // number of input chars
    private static int L = 512;       // number of codewords = 2^W
    private static int W = 9;         // codeword width
    private static String fullCodebook;
    public static void compress() { 
        String input = BinaryStdIn.readString();
        TST<Integer> st = new TST<Integer>();
        double uncomp = 0;
        double comp = 0;
        double oldRatio = 0;
        double newRatio = 0;
        double ratioRatio;
        int ratioStarter = 0;
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        int code = R+1;  // R is codeword for EOF
        
        while (input.length() > 0) {
            String s = st.longestPrefixOf(input); // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W); // Print s's encoding.
            comp += W;
            int t = s.length();
            uncomp += t * 8;
            if (t < input.length() && code < L)    // Add s to symbol table.
            {
                st.put(input.substring(0, t + 1), code++);
            }
            else if(t < input.length() && code >= L && W != 16)
            {
                W++;
                L = (int)Math.pow(2, W);                
                st.put(input.substring(0, t + 1), code++);
                //System.err.println("New W:" + W + " New L:" + L);
            }
            else if(t < input.length() && code >= L && W == 16)
            {
                if(fullCodebook.equals("n"))
                {
                    //Do nothing
                }
                else if(fullCodebook.equals("r"))
                {
                    System.err.println("Resetting codebook...");
                    st = new TST<Integer>(); //re-initialize st
                    for (int i = 0; i < R; i++)
                    {
                        st.put("" + (char) i, i); //add ASCII values to st
                    }
                    code = R+1; //reset code value
                    W = 9; //reset W to 9
                    L = 512; //reset L to 2^9 (which is 512)
                    
                    st.put(input.substring(0, t+1), code++);
                }
                else if(fullCodebook.equals("m"))
                {
                    //System.err.println("Monitoring...");
                    //"Monitor" mode
                    //set current ratio to old ratio                    
                    if(ratioStarter == 0)
                    {
                        oldRatio = uncomp/comp;
                        newRatio = uncomp/comp;
                        ratioStarter++;
                    }
                    else
                    {
                        oldRatio = newRatio;
                        newRatio = uncomp/comp;//Calculate new ratio
                        ratioRatio = oldRatio/newRatio;//calculate ratio ratio                        
                        if(ratioRatio > 1.1)//decide if need to reset or not
                        {
                            //RESET
                            System.err.println("Ratio of ratios:" + ratioRatio);
                            System.err.println("Resetting codebook...");
                            st = new TST<Integer>(); //re-initialize st
                            for (int i = 0; i < R; i++)
                            {
                                st.put("" + (char) i, i); //add ASCII values to st
                            }
                            code = R+1; //reset code value
                            W = 9; //reset W to 9
                            L = 512; //reset L to 2^9 (which is 512)
                    
                            st.put(input.substring(0, t+1), code++);
                        }
                    }                    
                }
            }
            input = input.substring(t);            // Scan past s in input.
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 
    
    public static void expand() {
        String[] st = new String[65536];
        int i; // next available codeword value
        double comp = 0;
        double uncomp = 0;
        double oldRatio = 0;
        double newRatio = 0;
        double ratioRatio;
        int ratioStarter = 0;
        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";                        // (unused) lookahead for EOF

        int codeword = BinaryStdIn.readInt(W);
        comp += W;
        if (codeword == R) return;           // expanded message is empty string
        String val = st[codeword];

        while (true) {
            BinaryStdOut.write(val);
            uncomp += val.length() * 8;
            codeword = BinaryStdIn.readInt(W);
            //comp += W;
            if (codeword == R) break;
            String s = st[codeword];
            if (i == codeword) s = val + val.charAt(0);   // special case hack
            if (i < L-1) 
            {
                st[i++] = val + s.charAt(0);
            }
            else if(i >= L-1 && W != 16)
            {
                W++;
                L = (int)Math.pow(2, W);
                st[i++] = val + s.charAt(0);
                //System.err.println("New W:" + W + " New L:" + L);
            }
            else if(i >= L-1 && W == 16)
            {
                if(fullCodebook.equals("n"))
                {
                    
                }
                else if(fullCodebook.equals("r"))
                {
                    System.err.println("Resetting codebook...");
                    st = new String[65536]; //reset codebook array
                    for (i = 0; i < R; i++)
                    {
                        st[i] = "" + (char) i;
                    } //re-add all ASCII values to codebook array
                    //st[i++] = "";  //NEW LINE
                    //i = R+1; //set i to first value after ASCII values
                    W = 9; //reset W to 9
                    L = 512; //reset L to 2^9 (which is 512)
                    
                    st[i++] = val + s.charAt(0);
                }
                else if(fullCodebook.equals("m"))
                {
                    //"Monitor" mode
                    System.err.println("Monitoring...");
                    if(ratioStarter == 0)
                    {
                        oldRatio = uncomp/comp;
                        newRatio = uncomp/comp;
                        ratioStarter++;
                    }
                    else
                    {
                        oldRatio = newRatio;
                        newRatio = uncomp/comp;//Calculate new ratio
                        ratioRatio = oldRatio/newRatio;//calculate ratio ratio  
                        System.err.println("Ratio of ratios:" + ratioRatio);                        
                        if(ratioRatio > 1.1)//decide if need to reset or not
                        {
                            //RESET
                            //System.err.println("Ratio of ratios:" + ratioRatio);
                            System.err.println("Resetting codebook...");
                            st = new String[65536]; //reset codebook array
                            for (i = 0; i < R; i++)
                            {
                                st[i] = "" + (char) i;
                            } //re-add all ASCII values to codebook array
                            //st[i++] = "";  //NEW LINE
                            //i = R+1; //set i to first value after ASCII values
                            W = 9; //reset W to 9
                            L = 512; //reset L to 2^9 (which is 512)
                    
                            st[i++] = val + s.charAt(0);
                        }
                    }
                }
            }
            val = s;
        }
        BinaryStdOut.close();
    }

    public static void main(String[] args) throws IOException
    {
        if (args[0].equals("-")) 
        {
            BufferedWriter output = null;             
            File file = new File("fullCodebook.txt");
            output = new BufferedWriter(new FileWriter(file));
            output.write(args[1]);             
            output.close();
            if((args[1] == null) || (!(args[1].equals("n")) && !(args[1].equals("r")) && !(args[1].equals("m"))))
            {
                throw new IllegalArgumentException("Must enter a full codebook mode choice");
            }
            else
            {
                fullCodebook = args[1];
                compress();
            }
        }
        else if (args[0].equals("+")) 
        {
            BufferedReader br = new BufferedReader(new FileReader("fullCodebook.txt"));            
            String line = br.readLine();
            fullCodebook = line.toString();
            br.close();
            System.err.println(fullCodebook);
            if((fullCodebook == null) || (!(fullCodebook.equals("n")) && !(fullCodebook.equals("r")) && !(fullCodebook.equals("m"))))
            {
                throw new IllegalArgumentException("Must enter a full codebook mode choice");
            }
            else
            {
                expand();
            }
        }
        else 
        {
            throw new IllegalArgumentException("Illegal command line argument");
        }
    }

}
