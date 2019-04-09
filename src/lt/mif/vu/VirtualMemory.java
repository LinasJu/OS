package lt.mif.vu;

import java.util.ArrayList;

public class VirtualMemory {
    private ArrayList<Integer> allCode; //visa programa 256Mb = 2^28 = 268435456 bitai

    private int systemAreaBeginAddress = 0; // 4kb
    private int codeBeginAddress = 4096; //4kb
    private int programDataBeginAddress = 18384;
    private int heapBeginAddress;
    private int systemFunctionsBeginAddress;
    private int stackBeginAddress = 268369920; // 65536 bitu uzima
    private int stackEndAddress = 268435456;

    //TODO parašyti procesorių. jumpaikokie, +, - , aritmetika
    //sukurti klasę CPU, registrus, instrukcijas kokias palaikys. iš instrukcijų turi būti palakomas USER MODE (kur virtuali
    // mašina), kad užkrautų virtualią mašiną (spec registras,) registras kuris nurodo puslapių bloką
    //
}
