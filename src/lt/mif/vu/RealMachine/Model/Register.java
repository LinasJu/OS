package lt.mif.vu.RealMachine.Model;

import lombok.Getter;
import lombok.Setter;
import lt.mif.vu.UI.Observer.JKVObservable;
import lt.mif.vu.UI.VMWindow;

import java.io.Serializable;

@Getter
@Setter
public class Register extends JKVObservable implements Serializable {

    private int size;
    private int number;
    private String name;
    private Object value;
    private Object defaultValue;

    public Register(int size, String name, int number, Object value) {
        addObserverForKey(UIController.rmWindow, name);

        this.number = number;
        this.size = size;
        this.name = name;
        this.defaultValue = value; // used to clear registers

        setValue(value);
    }

    public Register(VMWindow vmWindow, int size, String name, int number, Object value) {
        addObserverForKey(vmWindow, name);

        this.number = number;
        this.size = size;
        this.name = name;
        this.defaultValue = value; // used to clear registers

        setValue(value);
    }

    public void setValue(Object value) {
        this.value = setKVOValue(name, this.value, value);
    }

    public Integer getIntValue() {
        return (Integer) this.getValue();
    }

    public void clear() {
        setValue(defaultValue);
    }

    public void inc() {
        setValue((int) value + 1);
    }

    public void dec() {
        setValue((int) value - 1);
    }
}