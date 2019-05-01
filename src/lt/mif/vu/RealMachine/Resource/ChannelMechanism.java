package lt.mif.vu.RealMachine.Resource;

import lt.mif.vu.Enums.RMRegistersEnum;
import lt.mif.vu.RealMachine.Hardware.*;
import lt.mif.vu.RealMachine.Model.Interrupt;
import lt.mif.vu.RealMachine.Model.Register;
import lt.mif.vu.RealMachine.Processor.Processor;
import lt.mif.vu.RealMachine.Model.Registers;

import java.io.Serializable;

public class ChannelMechanism implements Serializable {//extends Resource implements Serializable {
    private Screen screen;
    private Keyboard keyboard;
  //  private HardDisk hardDisk;

    private Processor processor;
    private RealMemory userMemory;

    public ChannelMechanism(Screen scr, Keyboard key){//, HardDisk hd) {
        this.screen = scr;
        this.keyboard = key;
       // this.hardDisk = hd;
    }

    public String startIO(int channelNumber) {
        String returnValue = null;

        Registers.get(RMRegistersEnum.MODE).setValue(1); // supervisor
        Register ch1 = Registers.get(RMRegistersEnum.CH1);
        Register ch2 = Registers.get(RMRegistersEnum.CH2);
        Register ch3 = Registers.get(RMRegistersEnum.CH3);
        switch (channelNumber) {
            case 1:
                // Keyboard
                if (ch1.getIntValue() == 0) { // available
                    ch1.inc();
                    returnValue = keyboard.getInput();
                    // TODO: keyboard holds 16 words buffer and end of buffer is indicated as $
                } else {
                    // TODO: wait
                }
                break;
            case 2:
                if (ch2.getIntValue() == 0) { // available
                    ch2.inc();
                } else {
                    // TODO: wait
                }
                break;
            case 3:
                if (ch3.getIntValue() == 0) { // available
                    ch3.inc();
                } else {
                    // TODO: wait
                }
                break;
            default:
                Registers.set(RMRegistersEnum.PI, Interrupt.PI_WRONG_OP_CODE);
                Screen.writeln("[ChannelMechanism] Wrong channel number");
        }
        Registers.get(RMRegistersEnum.MODE).setValue(0); // user mode
        return returnValue;
    }
}
