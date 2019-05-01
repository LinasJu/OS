package lt.mif.vu.RealMachine.Hardware;

import lt.mif.vu.Enums.RMRegistersEnum;
import lt.mif.vu.RealMachine.Model.Block;
import lt.mif.vu.RealMachine.Model.Word;
import lt.mif.vu.RealMachine.Model.Registers;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;

import static lt.mif.vu.Util.Constants.BLOCK_SIZE;
import static lt.mif.vu.Util.Constants.USER_MEMORY_SIZE;

public class RealMemory {
    private static BitSet bitSet = new BitSet(USER_MEMORY_SIZE);
    private static ArrayList<Block> realMem = new ArrayList<>();

    /**
     * Initialize virtualMemory
     */
    private RealMemory() {
    }

    public static void initRealMemory() {
        realMem.addAll(Collections.nCopies(USER_MEMORY_SIZE, null));

        double totalMemory = realMem.size() * BLOCK_SIZE * 4 / 1000;
        String initText = String.format("[Memory] %d pages, " +
                        "%d words per block (4 bytes each), total %1.2f kB",
                realMem.size(), BLOCK_SIZE, totalMemory);
        System.out.println(initText);
    }

    public static void clearMemory() {
        realMem.clear();
        bitSet.clear();
        bitSet = new BitSet(USER_MEMORY_SIZE);
        realMem = new ArrayList<>();
        realMem.addAll(Collections.nCopies(USER_MEMORY_SIZE, null));
    }

    /**
     * Checks page availability
     *
     * @param page Block number to check
     */
    private static void checkPage(int page) {
        if (page >= USER_MEMORY_SIZE)
            throw new Error("Block page out of range");

        if (bitSet.get(page))
            throw new Error("Block already allocated");
    }

    /**
     * Allocate a block of virtualMemory without data
     *
     * @param page Block number
     * @return New block
     */
    public static Block allocateBlock(int page) {
        checkPage(page);
        setAllocated(page);
        Block block = new Block(page);
        realMem.set(page, block);
        return block;
    }


    /**
     * Allocate a new block of virtualMemory with data
     *
     * @param page        Block number
     * @param virtualPage Virtual page index
     * @param data        Data to initialize with
     * @return Requested block
     */
    public static Block allocateBlock(int page, int virtualPage, String[] data) {
        checkPage(page);
        setAllocated(page);
        Block block = new Block(virtualPage, data);
        realMem.set(page, block);
        return block;
    }

    public static boolean isAllocated(int location) {
        return bitSet.get(location);
    }

    private static void setAllocated(int location) {
        bitSet.set(location);
    }

    public static void setNotAllocated(int location) {
        bitSet.set(location, false);
    }

    public static int unallocatedBlockCount() {
        return USER_MEMORY_SIZE - bitSet.cardinality();
    }

    /**
     * Get word based on its address
     *
     * @param address Lowest byte is address location, higher ones page number
     * @return Word in the requested address
     */
    public static Word getWord(int address) {
        int wordIndex = address & 0xF;
        int blockIndex = address >> 4;

        if (realMem.get(blockIndex) == null) {
            System.err.println("[Memory] !!! Requested unallocated block, probably a bug in the CPU microcode or the OS");
            Registers.set(RMRegistersEnum.PI, 1);
            // TODO: Initialize a new page here
            return new Word(-1);
        }

        return realMem.get(blockIndex).getBlock()[wordIndex];
    }
}