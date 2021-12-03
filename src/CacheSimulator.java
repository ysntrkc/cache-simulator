// Yasin TARAKÇI 150118055
// Ahmet Emre SAĞCAN 150119042
// Yusuf Taha ATALAY 150119040
package src;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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

        // We copied original RAM file to ModifiedRAM file in order to use the original
        // one as much as we want without modifying it.
        Files.deleteIfExists(Paths.get("ModifiedRAM.dat"));
        File source = new File("RAM.dat");
        File dest = new File("ModifiedRAM.dat");
        Files.copy(source.toPath(), dest.toPath());

        ParseInput(args);
        InitializeCaches();

        ReadTraceFile();

        PrintScores();

        WriteCaches(l1InstructionCache);
        WriteCaches(l1DataCache);
        WriteCaches(l2Cache);
    }

    // Store function handles the S operation by writing the given data to given
    // address adn checks if any of our
    // 2 caches has that value already and arranges the miss&hit count accordingly
    private static void Store(String address, String size, String data) throws IOException {
        // converts address to integer value in order to gather the tag,set and block
        // bits out of it
        int addressHex = Integer.parseInt(address, 16);

        String addressBin = Integer.toBinaryString(addressHex);
        addressBin = ZeroExtend(addressBin, 32);
        String[] addressArrForL1 = ParseAddress(addressBin, L1s, L1b);
        String[] addressArrForL2 = ParseAddress(addressBin, L2s, L2b);
        // Next 6 lines is for extracting the tag set and bit values out of the binary
        // value of the address
        String tag1 = addressArrForL1[0], blockIndex1 = addressArrForL1[2];
        String tag2 = addressArrForL2[0], blockIndex2 = addressArrForL2[2];

        int setI1 = BinaryStringToDecimal(addressArrForL1[1]);
        int setI2 = BinaryStringToDecimal(addressArrForL2[1]);

        // this represents the hex strings to skip.
        int blockI1 = BinaryStringToDecimal(blockIndex1) * 2;
        int blockI2 = BinaryStringToDecimal(blockIndex2) * 2;

        int sizeI = Integer.parseInt(size) * 2;
        // Making some control like , is the line has valid bit set ? or the tag value
        // of the data inside of the line matches with the one that we are try to store
        // and set the counters accordingly
        for (int i = 0; i < L1E; i++) {
            if (l1DataCache.getSets()[setI1].getLines()[i].isValid()) {
                if (l1DataCache.getSets()[setI1].getLines()[i].getTag().equals(tag1)) {
                    String temp = l1DataCache.getSets()[setI1].getLines()[i].getData();
                    temp = temp.substring(0, blockI1) + data + temp.substring(blockI1 + sizeI);
                    l1DataCache.getSets()[setI1].getLines()[i].setData(temp);
                    L1DHitCount++;
                    break;
                }
                if (i == L1E - 1) {
                    L1DMissCount++;
                    break;
                }
            } else {
                L1DMissCount++;
                break;
            }
        }
        // Do the same for the L2 cache
        for (int i = 0; i < L2E; i++) {
            if (l2Cache.getSets()[setI2].getLines()[i].isValid()) {
                if (l2Cache.getSets()[setI2].getLines()[i].getTag().equals(tag2)) {
                    String temp = l2Cache.getSets()[setI2].getLines()[i].getData();
                    temp = temp.substring(0, blockI2) + data + temp.substring(blockI2 + sizeI);
                    l2Cache.getSets()[setI2].getLines()[i].setData(temp);
                    L2HitCount++;
                    break;
                }
                if (i == L2E - 1) {
                    L2MissCount++;
                    break;
                }
            } else {
                L2MissCount++;
                break;
            }
        }
        // Write the results to the corresponding files
        String dataTemp = ReadFromOffset(addressHex);
        dataTemp = dataTemp.substring(0, blockI2) + data + dataTemp.substring(blockI2 + sizeI);
        WriteWithOffset(dataTemp, addressHex);
    }

    // Load function handles both instruction and data loading operations by
    // managing the function with the "mode" parameter
    // if the mode parameter set to false it handles Instruction Load operation and
    // vice versa.
    private static void Load(String address, boolean mode) throws IOException {
        int addressHex = Integer.parseInt(address, 16);

        String addressBin = Integer.toBinaryString(addressHex);
        // Zero extend the address for block alignment purposes
        addressBin = ZeroExtend(addressBin, 32);
        String[] addressArrForL1 = ParseAddress(addressBin, L1s, L1b);
        String[] addressArrForL2 = ParseAddress(addressBin, L2s, L2b);
        // Gathering the tag and set bits from the address value
        String tag1 = addressArrForL1[0], tag2 = addressArrForL2[0];

        int setI1 = BinaryStringToDecimal(addressArrForL1[1]);
        int setI2 = BinaryStringToDecimal(addressArrForL2[1]);

        String data = ReadFromOffset(addressHex);

        // Load instruction for Instruction Cache
        if (mode) {
            for (int i = 0; i < L1E; i++) {
                if (l1InstructionCache.getSets()[setI1].getLines()[i].isValid()) {
                    if (l1InstructionCache.getSets()[setI1].getLines()[i].getTag().equals(tag1)) {
                        // Look at Block
                        L1IHitCount++;
                        break;
                    }
                    if (i == L1E - 1) {
                        int minIndex = l1InstructionCache.getSets()[setI1].GetMinTime();
                        Line newLine = new Line(tag1, true, data, ++timeL1I);
                        l1InstructionCache.getSets()[setI1].setLinesIndex(newLine, minIndex);
                        L1IMissCount++;
                        L1IEvictionCount++;
                        break;
                    }
                } else {
                    // Load instructions from RAM.
                    Line newLine = new Line(tag1, true, data, ++timeL1I);
                    l1InstructionCache.getSets()[setI1].setLinesIndex(newLine, i);
                    L1IMissCount++;
                    break;
                }
            }
        }
        // Load instruction for Data Cache
        else {
            for (int i = 0; i < L1E; i++) {
                if (l1DataCache.getSets()[setI1].getLines()[i].isValid()) {
                    if (l1DataCache.getSets()[setI1].getLines()[i].getTag().equals(tag1)) {
                        // Look at Block
                        L1DHitCount++;
                        break;
                    }
                    if (i == L1E - 1) {
                        int minIndex = l1DataCache.getSets()[setI1].GetMinTime();
                        Line newLine = new Line(tag1, true, data, ++timeL1D);
                        l1DataCache.getSets()[setI1].setLinesIndex(newLine, minIndex);
                        L1DMissCount++;
                        L1DEvictionCount++;
                        break;
                    }
                } else {
                    // Load instructions from RAM.
                    Line newLine = new Line(tag1, true, data, ++timeL1D);
                    l1DataCache.getSets()[setI1].setLinesIndex(newLine, i);
                    L1DMissCount++;
                    break;
                }
            }
        }
        // Since L2 cache is unified there is no segregation for Load Operation for L2
        // cache
        for (int i = 0; i < L2E; i++) {
            if (l2Cache.getSets()[setI2].getLines()[i].isValid()) {
                if (l2Cache.getSets()[setI2].getLines()[i].getTag().equals(tag2)) {
                    // Look at Block
                    L2HitCount++;
                    break;
                }
                if (i == L2E - 1) {
                    int minIndex = l2Cache.getSets()[setI2].GetMinTime();
                    Line newLine = new Line(tag2, true, data, ++timeL2);
                    l2Cache.getSets()[setI2].setLinesIndex(newLine, minIndex);
                    L2MissCount++;
                    L2EvictionCount++;
                    break;
                }
            } else {
                // Load instructions from RAM.
                Line newLine = new Line(tag2, true, data, ++timeL2);
                l2Cache.getSets()[setI2].setLinesIndex(newLine, i);
                L2MissCount++;
                break;
            }
        }
    }

    // ReadTraceFile read the given .trace files line by line and for each line it
    // makes the program behave accordingly.
    // This function also creates and fills the tracelog file
    private static void ReadTraceFile() throws IOException {
        Scanner sc = new Scanner(new File(traceFile));
        // Create the log file at the same level with the source code
        FileWriter fileWriter = new FileWriter("tracesLogs.txt");
        PrintWriter printWriter = new PrintWriter(fileWriter);
        // For every line...
        while (sc.hasNextLine()) {

            char operation = sc.next().charAt(0);
            String line = sc.nextLine();
            String[] lineArr = line.split(", ");
            String address = lineArr[0].replaceAll("\\s+", "");
            String size = lineArr[1];
            String secondLine = "", thirdLine = "", fourthLine = "", fifthLine = "";

            printWriter.print(operation + " " + line + "\n\t");
            // First character of every line denotes the operation
            switch (operation) {
                // Instruction loading done here
                case 'I' -> {
                    // Hold the current Miss amount and compare it after the loading done to see if
                    // it changes
                    int previousMissCountI = L1IMissCount;
                    int previousMissCount2 = L2MissCount;
                    Load(address, true);

                    if (previousMissCountI == L1IMissCount) {
                        secondLine += "L1I hit, ";
                    } else {
                        secondLine += "L1I miss, ";
                    }

                    if (previousMissCount2 == L2MissCount) {
                        secondLine += "L2 hit\n\t";
                    } else {
                        secondLine += "L2 miss\n\t";
                    }
                    int addressHex = Integer.parseInt(address, 16);

                    String addressBin = Integer.toBinaryString(addressHex);
                    addressBin = ZeroExtend(addressBin, 32);
                    String[] addressArrForL1 = ParseAddress(addressBin, L1s, L1b);
                    String[] addressArrForL2 = ParseAddress(addressBin, L2s, L2b);
                    // Shape the string and then write it to log file
                    int setI1 = BinaryStringToDecimal(addressArrForL1[1]);
                    int setI2 = BinaryStringToDecimal(addressArrForL2[1]);
                    if (L2s == 0) {
                        thirdLine += "Place in L2 ,";
                    } else {
                        thirdLine += "Place in L2 set " + setI2;
                    }
                    if (L1s == 0) {
                        thirdLine += ", L1I\n";
                    } else {
                        thirdLine += ", L1I set " + setI1 + "\n";
                    }
                }
                case 'L' -> {
                    // Data loading done here
                    int previousMissCountD = L1DMissCount;
                    int previousMissCount2 = L2MissCount;
                    Load(address, false);

                    if (previousMissCountD == L1DMissCount) {
                        secondLine += "L1D hit, ";
                    } else {
                        secondLine += "L1D miss, ";
                    }

                    if (previousMissCount2 == L2MissCount) {
                        secondLine += "L2 hit\n\t";
                    } else {
                        secondLine += "L2 miss\n\t";
                    }
                    int addressHex = Integer.parseInt(address, 16);

                    String addressBin = Integer.toBinaryString(addressHex);
                    addressBin = ZeroExtend(addressBin, 32);
                    String[] addressArrForL1 = ParseAddress(addressBin, L1s, L1b);
                    String[] addressArrForL2 = ParseAddress(addressBin, L2s, L2b);

                    int setI1 = BinaryStringToDecimal(addressArrForL1[1]);
                    int setI2 = BinaryStringToDecimal(addressArrForL2[1]);
                    if (L2s == 0) {
                        thirdLine += "Place in L2 ,";
                    } else {
                        thirdLine += "Place in L2 set " + setI2;
                    }
                    if (L1s == 0) {
                        thirdLine += ", L1D\n";
                    } else {
                        thirdLine += ", L1D set " + setI1 + "\n";
                    }

                }
                case 'M' -> {
                    // Modify operation simply calls Load and Store functions in order
                    String data = lineArr[2];
                    int previousMissCountD = L1DMissCount;
                    int previousMissCount2 = L2MissCount;
                    Load(address, false);

                    if (previousMissCountD == L1DMissCount) {
                        secondLine += "L1D hit, ";
                    } else {
                        secondLine += "L1D miss, ";
                    }

                    if (previousMissCount2 == L2MissCount) {
                        secondLine += "L2 hit\n\t";
                    } else {
                        secondLine += "L2 miss\n\t";
                    }
                    int addressHex = Integer.parseInt(address, 16);

                    String addressBin = Integer.toBinaryString(addressHex);
                    addressBin = ZeroExtend(addressBin, 32);
                    String[] addressArrForL1 = ParseAddress(addressBin, L1s, L1b);
                    String[] addressArrForL2 = ParseAddress(addressBin, L2s, L2b);

                    int setI1 = BinaryStringToDecimal(addressArrForL1[1]);
                    int setI2 = BinaryStringToDecimal(addressArrForL2[1]);

                    if (L2s == 0) {
                        thirdLine += "Place in L2 ,";
                    } else {
                        thirdLine += "Place in L2 set " + setI2;
                    }
                    if (L1s == 0) {
                        thirdLine += ", L1D\n\t";
                    } else {
                        thirdLine += ", L1D set " + setI1 + "\n\t";
                    }

                    previousMissCountD = L1DMissCount;
                    previousMissCount2 = L2MissCount;
                    Store(address, size, data);
                    fifthLine += "Store in ";
                    if (previousMissCountD == L1DMissCount) {
                        fourthLine += "L1D hit, ";
                        fifthLine += "L1D, ";
                    } else {
                        fourthLine += "L1D miss, ";
                    }

                    if (previousMissCount2 == L2MissCount) {
                        fourthLine += "L2 hit\n\t";
                        fifthLine += "L2, ";
                    } else {
                        fourthLine += "L2 miss\n\t";
                    }
                    fifthLine += "RAM\n";
                }
                case 'S' -> {
                    // Storing operation done here
                    int previousMissCountD = L1DMissCount;
                    int previousMissCount2 = L2MissCount;
                    String data = lineArr[2];
                    Store(address, size, data);
                    thirdLine += "Store in ";
                    if (previousMissCountD == L1DMissCount) {
                        secondLine += "L1D hit, ";
                        thirdLine += "L1D, ";
                    } else {
                        secondLine += "L1D miss, ";
                    }

                    if (previousMissCount2 == L2MissCount) {
                        secondLine += "L2 hit\n\t";
                        thirdLine += "L2, ";
                    } else {
                        secondLine += "L2 miss\n\t";
                    }
                    thirdLine += "RAM\n";
                }
            }
            printWriter.print(secondLine);
            printWriter.print(thirdLine);
            if (operation == 'M') {
                printWriter.print(fourthLine);
                printWriter.print(fifthLine);
            }
        }
        printWriter.close();
        sc.close();
    }

    // This method adds 0 to the beginning of the given string until the size of the
    // string reaches the given length.
    private static String ZeroExtend(String str, int length) {
        StringBuilder addressBuilder = new StringBuilder(str);
        for (int i = addressBuilder.length(); i < length; i++) {
            addressBuilder.insert(0, "0");
        }
        return addressBuilder.toString();
    }

    // PrintScore function prints the Miss, Hit and Eviction counts for every cache
    // to terminal
    private static void PrintScores() {
        System.out.printf("L1I-hits: %d  L1I-misses: %d  L1I-evictions: %d\n", L1IHitCount, L1IMissCount,
                L1IEvictionCount);
        System.out.printf("L1D-hits: %d  L1D-misses: %d  L1D-evictions: %d\n", L1DHitCount, L1DMissCount,
                L1DEvictionCount);
        System.out.printf("L2-hits: %d  L2-misses: %d  L2-evictions: %d\n", L2HitCount, L2MissCount, L2EvictionCount);
        System.out.println(
                "ModifiedRAM.dat, L1_Data_Cache.txt, L1_Instruction_Cache.txt, L2_Cache.txt and tracesLogs.txt created.");
    }

    // BinaryStringToHexString converts given binary string to its Hex form after
    // zero extension.
    private static String BinaryStringToHexString(String binStr) {
        long number = Long.parseLong(binStr, 2);
        return ZeroExtend(Long.toHexString(number), 8);
    }

    // This method converts the binary string to decimal value. If string is empty
    // method will return 0.
    private static int BinaryStringToDecimal(String str) {
        if (str.equals(""))
            return 0;
        return Integer.parseInt(str, 2);
    }

    // This method parses the address' binary format with respect to entered command
    // line arguments.
    private static String[] ParseAddress(String addressBin, int cacheSetIndex, int cacheBlockIndex) {
        String[] addressArr = new String[3];

        addressArr[0] = addressBin.substring(0, addressBin.length() - (cacheBlockIndex + cacheSetIndex));
        addressArr[1] = addressBin.substring(addressArr[0].length(), addressArr[0].length() + cacheSetIndex);
        addressArr[2] = addressBin.substring(addressArr[0].length() + addressArr[1].length(),
                addressArr[0].length() + addressArr[1].length() + cacheBlockIndex);

        addressArr[0] = BinaryStringToHexString(addressArr[0]);
        return addressArr;
    }

    // This method writes given data to RAM file to the specified position.
    private static void WriteWithOffset(String data, int Position) throws IOException {
        RandomAccessFile file = new RandomAccessFile("ModifiedRAM.dat", "rw");
        file.seek(Position);

        StringBuilder dataHolder = new StringBuilder();
        char[] charArray = data.toCharArray();
        for (int i = 0; i < charArray.length; i = i + 2) {
            String str = "" + charArray[i] + "" + charArray[i + 1];
            char ch = (char) Integer.parseInt(str, 16);
            dataHolder.append(ch);
        }

        file.writeBytes(dataHolder.toString());
        file.close();
    }

    // This method reads 16 digits from RAM file starting from the specified
    // position.
    private static String ReadFromOffset(int Position) throws IOException {
        RandomAccessFile file = new RandomAccessFile("ModifiedRAM.dat", "r");
        file.seek(Position);
        String x = Long.toHexString(file.readLong());
        file.close();
        return ZeroExtend(x, 16);
    }

    // This method writes caches to txt files with calling their toString methods.
    private static void WriteCaches(Cache cache) throws IOException {
        File theDir = new File("caches");
        if (!theDir.exists()) {
            theDir.mkdirs();
        }
        PrintWriter printWriter = new PrintWriter(new FileWriter("caches/" + cache.getType() + ".txt"));
        printWriter.print(cache);
        printWriter.close();
    }

    // This method creates caches and initializes them with respect to entered
    // values.
    // At first, lines are empty. When we read the trace files, we will fill them.
    private static void InitializeCaches() {
        int L1S = 1 << L1s;
        int L2S = 1 << L2s;

        l1DataCache = new Cache("L1_Data_Cache");
        l1InstructionCache = new Cache("L1_Instruction_Cache");
        l2Cache = new Cache("L2_Cache");

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

    // This method parses the command line argument and assigns the values to global
    // variables.
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

    // If the input format is false, the program throws this message.
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
