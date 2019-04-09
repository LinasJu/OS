package lt.mif.vu.Enums;

import lt.mif.vu.Constants;

import java.io.Serializable;

public class RMRegistersEnum implements Serializable {
    // RM
    PTR(1, 4), // 4 baitų puslapių lentelės registras
    PC(1, 1), // komandų skaitliukas
    SP(2, 1), // registras saugantis steko viršūnės žodžio indeksą
    CH1(3, 1), // kanalų registras. 0 - kanalas laisvas, 1 - kanalas užimtas.
    CH2(4, 1), // kanalų registras. 0 - kanalas laisvas, 1 - kanalas užimtas.
    CH3(5, 1), // kanalų registras. 0 - kanalas laisvas, 1 - kanalas užimtas.
    TI(6, 1,Constants.TIMER), // timerio registras
    PI(7, 1), // program interupt
    SI(8, 1), // supervisor interupt
    SM(9, 1), // registras rodantis į bendrą atmintį
    MODE(10, 1), // registras nusakantis darbo režimą
    SF(11, 1); // Požymių registras ZERO bitas CARRY bitas OVERFLOW bitas SIGN bitas

    private final int number;
    private final int size;
    private Object defaultValue;

    RMRegistersEnum(int number, int size) {
        this.number = number;
        this.size = size;
        this.defaultValue = 0;
    }

    RMRegistersEnum(int number, int size, Object defaultValue) {
        this.number = number;
        this.size = size;
        this.defaultValue = defaultValue;
    }


    public int getNumber() {
        return number;
    }

    public int getSize() {
        return size;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
