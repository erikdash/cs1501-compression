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
        double ratioRatio = 0;
        boolean ratioStarter = false;
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        int code = R+1;  // R is codeword for EOF
        
        while (input.length() > 0) {
            String s = st.longestPrefixOf(input); // Find max prefix match s.            
            uncomp += s.length() * 8;
            BinaryStdOut.write(st.get(s), W); // Print s's encoding.
            comp += W;
            int t = s.length();
            
            if (t < input.length() && code < L)    // Add s to symbol table.
            {
                st.put(input.substring(0, t + 1), code++);                
            }
            else if(t < input.length() && code == L && W < 16)
            {
                W++;
                L = (int)Math.pow(2, W);                
                st.put(input.substring(0, t + 1), code++);
            }
            else if(t < input.length() && code == L && W == 16)
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
                }
                else if(fullCodebook.equals("m"))
                {                  
                    if(ratioStarter == false)
                    {
                        System.err.println("Monitoring...");
                        oldRatio = uncomp/comp;
                        ratioStarter = true;
                    }
                    else if(ratioStarter == true)
                    {
                        newRatio = uncomp/comp;
                        ratioRatio = oldRatio/newRatio;
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
                            oldRatio = 0;
                            newRatio = 0;
                            ratioStarter = false;
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
        double ratioRatio = 0;
        boolean ratioStarter = false;
        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";                        // (unused) lookahead for EOF

        int codeword = BinaryStdIn.readInt(W);     
        //comp += W;
        if (codeword == R) return;           // expanded message is empty string        
        String val = st[codeword];

        while (true) {
            uncomp += val.length() * 8;
            comp += W;            
            if(i == L && W < 16)
            {
                W++;
                L = (int)Math.pow(2, W);
            }
            else if(i == L && W == 16)
            {
                if(fullCodebook.equals("n"))
                {
                    //do nothing
                }
                else if(fullCodebook.equals("r"))
                {
                    System.err.println("Resetting codebook...");
                    st = new String[65536]; //reset codebook array
                    for (i = 0; i < R; i++)
                    {
                        st[i] = "" + (char) i;
                    } //re-add all ASCII values to codebook array
                    W = 9; //reset W to 9
                    L = 512; //reset L to 2^9 (which is 512)
                }
                else if(fullCodebook.equals("m"))
                {
                    if(ratioStarter == false)
                    {
                        System.err.println("Monitoring...");
                        oldRatio = uncomp/comp;
                        ratioStarter = true;
                    }
                    else if(ratioStarter == true)
                    {
                        newRatio = uncomp/comp;
                        ratioRatio = oldRatio/newRatio;
                        if(ratioRatio > 1.1)//decide if need to reset or not
                        {
                            //RESET
                            System.err.println("Ratio of ratios:" + ratioRatio);
                            System.err.println("Resetting codebook...");
                            st = new String[65536]; //reset codebook array
                            for (i = 0; i < R; i++)
                            {
                                st[i] = "" + (char) i;
                            } //re-add all ASCII values to codebook array
                            W = 9; //reset W to 9
                            L = 512; //reset L to 2^9 (which is 512)
                            oldRatio = 0;
                            newRatio = 0;
                            ratioStarter = false;
                        }
                    }   
                }
            }
            BinaryStdOut.write(val);
            codeword = BinaryStdIn.readInt(W);
            if (codeword == R) break;
            String s;// = st[codeword];
            if (i == codeword) s = val + val.charAt(0); 
            else s = st[codeword];
            if (i < L) 
            {
                //System.err.println(i);
                //System.err.println("Here");
                st[i++] = val + s.charAt(0);
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
            System.err.println(fullCodebook + " mode");
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
