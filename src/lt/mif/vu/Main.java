package lt.mif.vu;

import lt.mif.vu.RealMachine.Hardware.Keyboard;
import lt.mif.vu.RealMachine.Hardware.RealMemory;
import lt.mif.vu.RealMachine.Hardware.Screen;
import lt.mif.vu.RealMachine.Resource.ChannelMechanism;

public class Main {

    public static void main(String[] args) {
        // Creation of channel mechanism
        Screen scr = new Screen();
        Keyboard key = new Keyboard();
        ChannelMechanism channelMechanism = new ChannelMechanism(scr, key);//, hd);
    }
}
