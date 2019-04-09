package lt.mif.vu;

public abstract class Constants {
    public static final String EMPTY_WORD = "    ";
    public static final int USER_MEMORY_SIZE = 100; // 100 blocks, 0-99
    public static final int VIRTUAL_MACHINE_MEMORY_SIZE = 16; // 16 blocks, 0-15
    public static final int BLOCK_SIZE = 16; // 16 WORDS
    public static final int WORD_SIZE = 4;
    public static final int PROGRAM_SIZE = 16; // 16 BLOCKS
    public static final int KEYBOARD_BUFFER_SIZE = 16; // size in Words
    public static final int TIMER = 30;

    /* End of data segment in virtual machine virtualMemory */
    public static final int DATA_END = 7 * BLOCK_SIZE;
    /* End of code segment in virtual machine virtualMemory */
    public static final int CODE_END = DATA_END + 7 * BLOCK_SIZE;
    /* End of stack in virtual machine virtualMemory */
    public static final int STACK_END = CODE_END + 2 * BLOCK_SIZE;
    /* Size of the program in words */
    public static final int PROGRAM_SIZE_WORDS = PROGRAM_SIZE * BLOCK_SIZE;

    /* MODE Register value for supervisor mode */
    public static final int SUPERVISOR = 1;
    /* MODE Register value for user mode */
    public static final int USER = 0;

    private Constants() {

    }
}
