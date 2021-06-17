import java.io.*;
import java.util.Scanner;

public class CacheSimulator {

    static int L1s = 0, L1E = 0, L1b = 0;
    static int L2s = 0, L2E = 0, L2b = 0;
    static int L1IHitCount = 0, L1IMissCount = 0, L1IEvictionCount = 0;
    static int L1DHitCount = 0, L1DMissCount = 0, L1DEvictionCount = 0;
    static int L2HitCount = 0, L2MissCount = 0, L2EvictionCount = 0;
    static int timeL1I = 0, timeL1D = 0, timeL2 = 0;
    static String traceFile;
    static Cache l1DataCache, l1InstructionCache, l2Cache;

    public static void main(String[] args) throws IOException {
        String[] input = new String[]{"-L1s", "4", "-L1E", "10", "-L1b", "4", "-L2s", "5", "-L2E", "10", "-L2b", "4",
                "-t", "test_medium.trace"};
        ParseInput(input);
        InitializeCaches();

        ReadTraceFile();


        System.out.println();

    }

    private static void Store(String address, String size, String data) throws IOException {
        int addressHex = Integer.parseInt(address, 16);

        String addressBin = Integer.toBinaryString(addressHex);
        addressBin = ZeroExtend(addressBin);
        String[] addressArrForL1 = ParseAddress(addressBin, L1s, L1b);
        String[] addressArrForL2 = ParseAddress(addressBin, L2s, L2b);


        String tag1 = addressArrForL1[0], setIndex1 = addressArrForL1[1], blockIndex1 = addressArrForL1[2];
        String tag2 = addressArrForL2[0], setIndex2 = addressArrForL2[1], blockIndex2 = addressArrForL2[2];
        int setI1, setI2;

        setI1 = BinaryStringToDecimal(setIndex1);
        setI2 = BinaryStringToDecimal(setIndex2);

        //this represents the hex strings to skip.
        int blockI1 = BinaryStringToDecimal(blockIndex1) * 2;
        int blockI2 = BinaryStringToDecimal(blockIndex2) * 2;

        int sizeI = Integer.parseInt(size) * 2;

        for (int i = 0; i < L1E; i++) {
            if (l1DataCache.getSets()[setI1].getLines()[i].isValid()) {
                if (l1DataCache.getSets()[setI1].getLines()[i].getTag().equals(tag1)) {
                    String temp = l1DataCache.getSets()[setI1].getLines()[i].getData();
                    temp = temp.substring(0, blockI1) + data + temp.substring(blockI1 + sizeI);
                    l1DataCache.getSets()[setI1].getLines()[i].setData(temp);
                    L1DHitCount++;
                    break;
                }
            } else {
                L1DMissCount++;
                break;
            }
        }

        for (int i = 0; i < L2E; i++) {
            if (l2Cache.getSets()[setI2].getLines()[i].isValid()) {
                if (l2Cache.getSets()[setI2].getLines()[i].getTag().equals(tag2)) {
                    String temp = l2Cache.getSets()[setI2].getLines()[i].getData();
                    temp = temp.substring(0, blockI2) + data + temp.substring(blockI2 + sizeI);
                    l2Cache.getSets()[setI2].getLines()[i].setData(temp);
                    L2HitCount++;
                    break;
                }
            } else {
                L2MissCount++;
                break;
            }
        }
        String dataTemp = ReadFromOffset("RAM.dat", addressHex);
        dataTemp = dataTemp.substring(0, blockI2) + data + dataTemp.substring(blockI2 + sizeI);
        WriteWithOffset(dataTemp, addressHex);
    }

    private static void Load(String address, String size, boolean mode) throws IOException {
        int addressHex = Integer.parseInt(address, 16);

        String addressBin = Integer.toBinaryString(addressHex);
        addressBin = ZeroExtend(addressBin);
        String[] addressArrForL1 = ParseAddress(addressBin, L1s, L1b);
        String[] addressArrForL2 = ParseAddress(addressBin, L2s, L2b);


        String tag1 = addressArrForL1[0], setIndex1 = addressArrForL1[1], blockIndex1 = addressArrForL1[2];
        String tag2 = addressArrForL2[0], setIndex2 = addressArrForL2[1], blockIndex2 = addressArrForL2[2];
        int setI1, setI2;

        setI1 = BinaryStringToDecimal(setIndex1);
        setI2 = BinaryStringToDecimal(setIndex2);

        String data = ReadFromOffset("RAM.dat", addressHex);

        //L1 Instruction Cache
        if (mode) {
            for (int i = 0; i < L1E; i++) {
                if (l1InstructionCache.getSets()[setI1].getLines()[i].isValid()) {
                    if (l1InstructionCache.getSets()[setI1].getLines()[i].getTag().equals(tag1)) {
                        //Look at Block
                        L1IHitCount++;
                        break;
                    }
                    if (i == L1E - 1) {
                        int minIndex = l1InstructionCache.getSets()[setI1].GetMinTime();
                        Line newLine = new Line(tag1, true, data, ++timeL1I);
                        l1InstructionCache.getSets()[setI1].setLinesIndex(newLine, minIndex);
                        L1IHitCount++;
                        L1IEvictionCount++;
                        break;
                    }
                } else {
                    //Load instructions from RAM.
                    Line newLine = new Line(tag1, true, data, ++timeL1I);
                    l1InstructionCache.getSets()[setI1].setLinesIndex(newLine, i);
                    L1IMissCount++;
                    break;
                }
            }
        } else {
            //todo l1Data cache
            for (int i = 0; i < L1E; i++) {
                if (l1DataCache.getSets()[setI1].getLines()[i].isValid()) {
                    if (l1DataCache.getSets()[setI1].getLines()[i].getTag().equals(tag1)) {
                        //Look at Block
                        L1DHitCount++;
                        break;
                    }
                    if (i == L1E - 1) {
                        int minIndex = l1DataCache.getSets()[setI1].GetMinTime();
                        Line newLine = new Line(tag1, true, data, ++timeL1D);
                        l1DataCache.getSets()[setI1].setLinesIndex(newLine, minIndex);
                        L1DHitCount++;
                        L1DEvictionCount++;
                        break;
                    }
                } else {
                    //Load instructions from RAM.
                    Line newLine = new Line(tag1, true, data, ++timeL1D);
                    l1DataCache.getSets()[setI1].setLinesIndex(newLine, i);
                    L1DMissCount++;
                    break;
                }
            }
        }
        //L2 Cache
        for (int i = 0; i < L2E; i++) {
            if (l2Cache.getSets()[setI2].getLines()[i].isValid()) {
                if (l2Cache.getSets()[setI2].getLines()[i].getTag().equals(tag2)) {
                    //Look at Block
                    L2HitCount++;
                    break;
                }
                if (i == L2E - 1) {
                    int minIndex = l2Cache.getSets()[setI2].GetMinTime();
                    Line newLine = new Line(tag2, true, data, ++timeL2);
                    l2Cache.getSets()[setI2].setLinesIndex(newLine, minIndex);
                    L2HitCount++;
                    L2EvictionCount++;
                    break;
                }
            } else {
                //Load instructions from RAM.
                Line newLine = new Line(tag2, true, data, ++timeL2);
                l2Cache.getSets()[setI2].setLinesIndex(newLine, i);
                L2MissCount++;
                break;
            }
        }
    }

    private static int BinaryStringToDecimal(String str) {
        if (str.equals(""))
            return 0;
        return Integer.parseInt(str, 2);
    }

    private static String[] ParseAddress(String addressBin, int cacheSetIndex, int cacheBlockIndex) {
        String[] addressArr = new String[3];

        addressArr[0] = addressBin.substring(0, addressBin.length() - (cacheBlockIndex + cacheSetIndex));
        addressArr[1] = addressBin.substring(addressArr[0].length(), addressArr[0].length() + cacheSetIndex);
        addressArr[2] = addressBin.substring(addressArr[0].length() + addressArr[1].length(), addressArr[0].length() + addressArr[1].length() + cacheBlockIndex);

        return addressArr;
    }

    private static String ZeroExtend(String address) {
        StringBuilder addressBuilder = new StringBuilder(address);
        for (int i = addressBuilder.length(); i < 32; i++) {
            addressBuilder.insert(0, "0");
        }
        return addressBuilder.toString();
    }

    private static void ReadTraceFile() throws IOException {
        try {
            Scanner sc = new Scanner(new File("traces/" + traceFile));
            while (sc.hasNextLine()) {
                char operation = sc.next().charAt(0);
                String line = sc.nextLine();
                String[] lineArr = line.split(", ");
                String address = lineArr[0].replaceAll("\\s+", "");
                String size = lineArr[1];

                switch (operation) {
                    case 'I' -> Load(address, size, true);
                    case 'L' -> Load(address, size, false);
                    case 'M' -> {

                    }
                    case 'S' -> {
                        String data = lineArr[2];
                        Store(address, size, data);
                    }
                }
            }
            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void WriteWithOffset(String data, int Position) throws IOException {
        RandomAccessFile file = new RandomAccessFile("RAM.dat", "rw");
        file.seek(Position);
        long data1 = Long.parseLong(data,16);
        file.writeLong(data1);
        file.close();
    }

    private static String ReadFromOffset(String FileName, int Position) throws IOException {
        RandomAccessFile file = new RandomAccessFile(FileName, "r");
        file.seek(Position);
        String x = Long.toHexString(file.readLong());
        file.close();
        return x;
    }

    private static void InitializeCaches() {
        int L1S = 1 << L1s;
        int L1B = 1 << L1b;

        int L2S = 1 << L2s;
        int L2B = 1 << L2b;

        l1DataCache = new Cache("L1 Data Cache");
        l1InstructionCache = new Cache("L1 Instruction Cache");
        l2Cache = new Cache("L2 Cache");

        l1DataCache.CreateSet(L1S);
        l1InstructionCache.CreateSet(L1S);
        for (int i = 0; i < L1S; i++) {
            l1DataCache.getSets()[i].CreateLine(L1E);
            l1InstructionCache.getSets()[i].CreateLine(L1E);
        }

        l2Cache.CreateSet(L2S);
        for (int i = 0; i < L2S; i++) {
            l2Cache.getSets()[i].CreateLine(L2E);
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
