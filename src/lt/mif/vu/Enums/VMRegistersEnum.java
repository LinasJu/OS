package lt.mif.vu.Enums;

import java.io.Serializable;

public enum VMRegistersEnum implements Serializable {

    // VM
    VPC(0, 1), // komandų skaitliukas
    VSP(1, 1), // registras saugantis steko viršūnės žodžio indeksą
    VSF(2, 1); // Požymių registras ZERO bitas CARRY bitas OVERFLOW bitas SIGN bitasr

    private final int number;
    private final int size;
    private Object defaultValue;

    VMRegistersEnum(int number, int size) {
        this.number = number;
        this.size = size;
        this.defaultValue = 0;
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
