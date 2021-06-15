import java.io.*;
import java.util.Scanner;

public class CacheSimulator {

    static int L1s = 0, L1E = 0, L1b = 0;
    static int L2s = 0, L2E = 0, L2b = 0;
    static String traceFile;
    static Cache l1DataCache, l1InstructionCache, l2Cache;

    public static void main(String[] args) throws IOException {
        String[] input = new String[]{"-L1s", "0", "-L1E", "2", "-L1b", "3", "-L2s", "1", "-L2E", "2", "-L2b", "3", "-t", "traces/test.trace"};
        ParseInput(input);
//        InitializeCaches();

        DataInputStream ram = new DataInputStream(new FileInputStream("RAM.dat"));

        for (int i = 0; i < 20; i++) {

            //x should have a hex value
            String x =  Long.toHexString(ram.readLong());
            System.out.println(x);
        }
        try {
            ram.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void InitializeCaches() {
        int L1S = 1 << L1s;
        int L1B = 1 << L1b;

        int L2S = 1 << L2s;
        int L2B = 1 << L2b;

        l1DataCache = new Cache("L1 Data Cache");
        l1InstructionCache = new Cache("L1 Instruction Cache");
        l2Cache = new Cache("L2 Cache");
        for (int j = 0; j < L1S; j++) {
            l1DataCache.Append(new Set(j));
            l1InstructionCache.Append(new Set(j));
            for (int i = 0; i < L1E; i++) {
                l1DataCache.getSets().get(j).Append(null);
                l1InstructionCache.getSets().get(j).Append(null);
            }
        }
        for (int j = 0; j < L2S; j++) {
            l2Cache.Append(new Set(j));
            for (int i = 0; i < L2E; i++) {
                l2Cache.getSets().get(j).Append(null);
            }
        }
    }

    private static void ParseInput(String[] args) {
        if (args[0].equals("-L1s") && args[2].equals("-L1E") && args[4].equals("-L1b")) {
            L1s = Integer.parseInt(args[1]);
            L1E = Integer.parseInt(args[3]);
            L1b = Integer.parseInt(args[5]);
        } else {
            FalseInput();
        }
        if (args[6].equals("-L2s") && args[8].equals("-L2E") && args[10].equals("-L2b")) {
            L2s = Integer.parseInt(args[7]);
            L2E = Integer.parseInt(args[9]);
            L2b = Integer.parseInt(args[11]);
        } else {
            FalseInput();
        }
        if (args[12].equals("-t")) {
            traceFile = args[13];
        } else {
            FalseInput();
        }
    }

    public static void FalseInput() {
        String inputFormat = """
                -L1s <L1s> -L1E <L1E> -L1b <L1b>
                -L2s <L2s> -L2E <L2E> -L2b <L2b>
                -t <traceFile>
                """;
        System.out.println("Please enter according to the following input format: \n" + inputFormat);
        System.exit(0);
    }
}

