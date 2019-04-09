package lt.mif.vu.RealMachine.Model;

public class Interrupt {

    // Programiniai, kurių registras yra PI.
    public static final int PI_MEMORY = 1; // – atminties apsaugos pažeidimas.
    public static final int PI_WRONG_OP_CODE = 2; // – blogas operacijos kodas.
    public static final int PI_WRONG_ASSIGNMENT = 3; // – neteisingas priskyrimas.
    // Supervizoriniai, kurių registras SI. Galimi atvejai:
    public static final int SI_OUTS = 1; // – komanda OUTS
    public static final int SI_OUTN = 2; // – komanda OUTN
    public static final int SI_OUTB = 3; // – komanda OUTB
    public static final int SI_READ = 4; // – komanda READ
    public static final int SI_HALT = 5; // – komanda HALT
    public static final int SI_LOCK = 6; // – komanda LOCK.
    public static final int SI_ULCK = 7; // – komanda ULCK.
    // Taimerio, kurio registras TI. Galimi atvejai:
    public static final int TI = 0; // – taimerio skaitliukas lygus 0.
}
