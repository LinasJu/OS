package lt.mif.vu.RealMachine.Model;

import lombok.Getter;
import lt.mif.vu.Enums.RMRegistersEnum;
import lt.mif.vu.Enums.VMRegistersEnum;
import lt.mif.vu.UI.VMWindow;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

@Getter
public class Registers implements Serializable {

    // Real machine registers - static, because only one instance
    private static Map<RMRegistersEnum, Register> rmRegisterMap = new EnumMap<>(RMRegistersEnum.class);

    static {
        RMRegistersEnum[] rmValues = RMRegistersEnum.values();
        for (RMRegistersEnum value : rmValues) {
            Register register = new Register(value.getSize(), value.name(), value.getNumber(), value.getDefaultValue());
            rmRegisterMap.put(value, register);
        }
    }

    public static Register get(RMRegistersEnum registerEnum) {
        return rmRegisterMap.get(registerEnum);
    }

    public static void set(RMRegistersEnum RMRegistersEnum, String value) {
        rmRegisterMap.get(RMRegistersEnum).setValue(value);
    }

    public static void set(RMRegistersEnum RMRegistersEnum, int value) {
        rmRegisterMap.get(RMRegistersEnum).setValue(value);
    }

    public static Map<RMRegistersEnum, Register> getAll() {
        return rmRegisterMap;
    }

    public static Register get(String registerName) {
        return rmRegisterMap
                .values()
                .stream()
                .filter(register -> register.getName().contains(registerName))
                .findFirst()
                .orElse(null);
    }

    public static void setAll(Map<RMRegistersEnum, Register> all) {
        Registers.rmRegisterMap = all;
    }

    public static void clearRMRegisters() {
        rmRegisterMap.values().forEach(Register::clear);
    }

    /* VM registers part */

    private Map<VMRegistersEnum, Register> vmRegisterMap = new EnumMap<>(VMRegistersEnum.class);

    public Registers(VMWindow vmWindow) {
        VMRegistersEnum[] rmValues = VMRegistersEnum.values();
        for (VMRegistersEnum value : rmValues) {
            Register register = new Register(vmWindow, value.getSize(), value.name(), value.getNumber(), value.getDefaultValue());
            vmRegisterMap.put(value, register);
        }
    }

    public void set(VMRegistersEnum VMRegistersEnum, int value) {
        vmRegisterMap.get(VMRegistersEnum).setValue(value);
    }

    public Register get(VMRegistersEnum registerEnum) {
        return vmRegisterMap.get(registerEnum);
    }

    public void clearVMRegisters() {
        vmRegisterMap.values().forEach(Register::clear);
    }
}
