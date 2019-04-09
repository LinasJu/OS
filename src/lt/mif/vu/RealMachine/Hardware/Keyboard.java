package lt.mif.vu.RealMachine.Hardware;

import lt.mif.vu.Constants;

public class Keyboard {

    private Word[] buffer;

    public Keyboard() {
        this.buffer = new Word[Constants.KEYBOARD_BUFFER_SIZE];
    }
}
