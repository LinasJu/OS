package lt.mif.vu.RealMachine.Processor;
import lombok.Getter;
import lombok.Setter;
import lt.mif.vu.Enums.ProcessTypeEnum;
import lt.mif.vu.Enums.RMRegistersEnum;
import lt.mif.vu.Enums.ResourceTypeEnum;
import lt.mif.vu.Enums.VMRegistersEnum;
import lt.mif.vu.Exceptions.HaltException;
import lt.mif.vu.Exceptions.InterruptException;
import lt.mif.vu.RealMachine.Hardware.RealMemory;
import lt.mif.vu.RealMachine.Hardware.Screen;
import lt.mif.vu.RealMachine.Model.Interrupt;
import lt.mif.vu.RealMachine.Model.Register;
import lt.mif.vu.RealMachine.Model.Registers;
import lt.mif.vu.RealMachine.Model.Word;
import lt.mif.vu.RealMachine.Process.AbstractProcess;
import lt.mif.vu.RealMachine.Process.VirtualMachine;
import lt.mif.vu.RealMachine.Resource.ChannelMechanism;
import lt.mif.vu.RealMachine.Resource.Resource;
import lt.mif.vu.RealMachine.Resource.ResourceTable;
import lt.mif.vu.Util.Constants;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static lt.mif.vu.Util.Constants.SUPERVISOR;

@Getter
@Setter
public class Processor implements Serializable {
    private Method[] instructions;
    private AbstractProcess process;
    private ChannelMechanism channelMechanism;
    private VirtualMachine virtualMachine;

    private static Thread cpuThread;

    public Processor(ChannelMechanism channelMechanism, VirtualMachine virtualMachine) {
        this.instructions = Processor.class.getDeclaredMethods();
        this.channelMechanism = channelMechanism;
        this.virtualMachine = virtualMachine;
    }

    private int getProgramCounter() {
        return Registers.get(RMRegistersEnum.PC).getIntValue();
    }

    public void setProgramCounter(int value) {
        Registers.get(RMRegistersEnum.PC).setValue(value);
        virtualMachine.getRegisters().set(VMRegistersEnum.VPC, value);
    }

    private void incrementProgramCounter() {
        Registers.get(RMRegistersEnum.PC).inc();
        virtualMachine.getRegisters().get(VMRegistersEnum.VPC).inc();
    }

    /**
     * Sets the stack pointer to the specified
     *
     * @param sp Stack pointer value
     */
    public void setStackPointer(int sp) {
        Registers.set(RMRegistersEnum.SP, sp);
        virtualMachine.getRegisters().set(VMRegistersEnum.VSP, sp);
    }

    /**
     * Returns an int value of the stack pointer
     *
     * @return Stack pointer value
     */
    private int getStackPointer() {
        return Registers.get(RMRegistersEnum.SP).getIntValue();
    }

    private void incrementStackPointer() {
        setStackPointer(getStackPointer() + 1);
    }

    private void decrementStackPointer() {
        setStackPointer(getStackPointer() - 1);
    }

    private Word getStackTop() throws InterruptException {
        int pointer = getStackPointer();
        int wordAddress = realAddress(pointer - 1);
        return getWord(wordAddress);
    }

    private void setStackTop(int value) throws InterruptException {
        int pointer = getStackPointer();
        int wordAddress = realAddress(pointer - 1);
        getWord(wordAddress).setValue(value);
    }

    private void setStackTop(String value) throws InterruptException {
        int pointer = getStackPointer();
        int wordAddress = realAddress(pointer - 1);
        getWord(wordAddress).setValue(value);
    }

    private void decreaseTimer() {
        Registers.get(RMRegistersEnum.TI).dec();
    }

    /**
     * Resumes executing instructions
     */
    public void resume() {
        if (cpuThread != null) {
            System.out.println("[CPU] Already running, skipping.");
            return;
        }
        cpuThread = new Thread(() -> {
            runThread();
            if (cpuThread != null) {
                /* DO NOT JOIN HERE, will hang the thread */
                cpuThread = null;
            }
        });
        cpuThread.start();
    }

    private void runThread() {
    }

    /**
     * Calculates the real address based on the virtual one
     * and the page table pointed to by the PTR register
     *
     * @param virtualAddress Virtual address
     * @return Real address
     */
    // TODO: Paupdatint masinos aprasyma kaip skaiciuojami adresai
    private int realAddress(int virtualAddress) throws InterruptException {
        // TODO: Vietoj exceptionu kelt interruptus
        if ((virtualAddress & 0xFFFF0000) > 0) {
            throw new InterruptException("[Processor] Unused bytes set in virtual address");
        }

        int PTR = Registers.get(RMRegistersEnum.PTR).getIntValue();
        int virtualBlockIndex = (virtualAddress & 0xF0) >> 4;
        Word blockLocationWord = getWord((PTR << 4) + virtualBlockIndex);
        if (blockLocationWord == null) {
            throw new InterruptException("[Processor] Page table not allocated (wtf?)");
        }
        int realBlockIndex = Integer.parseInt(blockLocationWord.getStringValue());
        int wordIndex = (virtualAddress & 0xF);

        return (realBlockIndex << 4) + wordIndex;
    }

    private Word getWord(int address) {
        return RealMemory.getWord(address);
    }

    /**
     * Executes one instruction, not necessarily one clock tick
     */
    public void step() throws InterruptException {
        int opCodeIndex = realAddress(getProgramCounter());
        String opCode = getWord(opCodeIndex).getStringValue().trim();
        incrementProgramCounter();

        Method instruction = matchOperationCode(opCode);

        /* Collect instruction parameters */
        List<Object> parameters = collectInstructionParameters(instruction);

        /* Execute step instructions */
        executeStep(opCode, instruction, parameters);

        if (test() == 1) {
            throw new InterruptException();
        }
    }

    private void executeStep(String opCode, Method instruction, List<Object> parameters) throws InterruptException {
        try {
            if (parameters.isEmpty()) {
                instruction.invoke(this);
            } else {
                instruction.invoke(this, parameters.toArray());
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            Registers.set(RMRegistersEnum.PI, Interrupt.PI_WRONG_OP_CODE);
            throw new InterruptException("Unrecognized command: " + opCode);
        }
    }

    private List<Object> collectInstructionParameters(Method instruction) throws InterruptException {
        int parameterCount = instruction.getParameterCount();
        List<Object> parameters = new ArrayList<>();
        while (parameterCount > 0) {
            int argumentAddress = realAddress(getProgramCounter());
            parameters.add(getWord(argumentAddress).getStringValue().trim());
            incrementProgramCounter();
            parameterCount--;
        }
        return parameters;
    }

    /**
     * Returns the appropriate method to execute given the operation
     *
     * @param opCode Operation code
     * @return Method to execute
     */
    private Method matchOperationCode(String opCode) throws InterruptException {
        // Possible trim needed
        for (Method method : instructions) {
            String instructionCode = method
                    .getName()
                    .replace("Command", "");
            if (instructionCode.equalsIgnoreCase(opCode))
                return method;
        }
        // Method not found
        Registers.set(RMRegistersEnum.PI, Interrupt.PI_WRONG_OP_CODE);
        throw new InterruptException("Unrecognized command: '" + opCode + "'");
    }

    private int test() {
        Integer pi = Registers.get(RMRegistersEnum.PI).getIntValue();
        Integer si = Registers.get(RMRegistersEnum.SI).getIntValue();
        Integer ti = Registers.get(RMRegistersEnum.TI).getIntValue();
        return pi + si > 0 || ti == 0 ? 1 : 0;
    }

    /**
     * Nustačius įvykusį pertraukimą (t.y. test() <> 0) virtualios mašinos procesoriaus registrų
     * reikšmės yra išsaugomos, procesorius perjungiamas į supervizoriaus režimą, nustatomas
     * pertraukimo pobūdis (pavyzdžiui tiesiogiai apklausiant registrus) ir kviečiama pertraukimą
     * apdorosianti paprogramė. Vėliau, kaip buvo minėta, valdymas grįžta į virtualią mašiną,
     * registrai atstatomi, procesorius perjungiamas į vartotojo režimą, ir operacinė sistema
     * sprendžia, ką daryti toliau.
     *
     * @param message Interrupt message
     */
    public void interrupt(String message) throws HaltException {
        if (message != null && !message.isEmpty()) {
            Screen.writeln("[Interrupt]: " + message);
        }
        Map<RMRegistersEnum, Register> enumMap = new EnumMap<>(Registers.getAll());

        Registers.set(RMRegistersEnum.MODE, SUPERVISOR); // supervisor

        handleInterrupt();

        Registers.setAll(enumMap); // reset the registers

        // Interrupts should be handled so reset values
        Registers.set(RMRegistersEnum.PI, 0);
        Registers.set(RMRegistersEnum.SI, 0);

        Registers.set(RMRegistersEnum.MODE, Constants.USER); // user
    }

    private void handleInterrupt() {
        Integer pi = Registers.get(RMRegistersEnum.PI).getIntValue();
        Integer si = Registers.get(RMRegistersEnum.SI).getIntValue();
        Integer ti = Registers.get(RMRegistersEnum.TI).getIntValue();
        if (pi > 0) {
            handlePI();
        } else if (si > 0) {
            try {
                handleSI();
            } catch (InterruptException e) {
                e.printStackTrace();
            } catch (HaltException ex) {
                Screen.writeln(ex.getMessage());
                System.out.println("Program finished!");

                Resource resource = new Resource();
                resource.setSender(virtualMachine);
                ResourceTable.
                        getResourceList(ResourceTypeEnum.VM_INTERUPT).
                        releaseResource(resource, ProcessTypeEnum.JOB_HELPER, virtualMachine, ProcessTypeEnum.VIRTUAL_MACHINE);
            }
        } else if (ti <= 0) {
            handleTI();
        }
    }

    private void handlePI() {
        switch (Registers.get(RMRegistersEnum.PI).getIntValue()) {
            case Interrupt.PI_MEMORY:
                break;
            case Interrupt.PI_WRONG_OP_CODE:
                break;
            case Interrupt.PI_WRONG_ASSIGNMENT:
                break;
        }
    }

    private void handleSI() throws InterruptException, HaltException {
        switch (Registers.get(RMRegistersEnum.SI).getIntValue()) {
            case Interrupt.SI_HALT:
                Screen.writeln("Halting");
                ResourceTable.getResourceList(ResourceTypeEnum.ENDED_PROGRAM).releaseResource(new Resource(), ProcessTypeEnum.READ_FROM_INTERFACE, AbstractProcess.getProcess(ProcessTypeEnum.MAIN_PROC), ProcessTypeEnum.MAIN_PROC);
                throw new HaltException();
            case Interrupt.SI_LOCK:
                break;
            case Interrupt.SI_ULCK:
                break;
            case Interrupt.SI_OUTB:
                break;
            case Interrupt.SI_OUTN:
                Screen.writeln(Integer.toString(getStackTop().getIntegerValue()));
                Registers.set(RMRegistersEnum.SI, 0);
                break;
            case Interrupt.SI_OUTS:
                String stringValue = getStackTop().getStringValue();
                if (stringValue.equals("nnnn")) {
                    Screen.write("\n");
                } else {
                    Screen.write(stringValue);
                }
                Registers.set(RMRegistersEnum.SI, 0);
                break;
            case Interrupt.SI_READ:
                String input = channelMechanism.startIO(1);
                input = input.substring(0, input.indexOf('$') == -1 ? input.length() : input.indexOf('$')); // because $ is the end of input

                // TODO: Use RealMemory
                Integer valueOf;
                try {
                    valueOf = Integer.valueOf(input);
                    incrementStackPointer();
                    setStackTop(valueOf);
                    decreaseTimer();
                } catch (NumberFormatException numberFormatException) {
                    Registers.set(RMRegistersEnum.PI, Interrupt.PI_WRONG_ASSIGNMENT);
                    Screen.writeln("Wrong assignment");
                }
                break;
        }
    }

    private void handleTI() {
        Registers.set(RMRegistersEnum.TI, Constants.TIMER); // resetting the timer
    }

    /* Arithmetic */

    /**
     * Add two number from the stack
     */
    @SuppressWarnings("unused")
    public void addCommand() throws InterruptException {
        Word w1 = getStackTop();
        decrementStackPointer();
        Word w2 = getStackTop();

        int x1 = w1.getIntegerValue();
        int x2 = w2.getIntegerValue();
        long result = (long) x1 + x2;

        if (result < Integer.MIN_VALUE || result > Integer.MAX_VALUE) {
            setOverflow(true);
        } else {
            setOverflow(false);
        }

        setStackTop((int) result);
        decreaseTimer();
    }

    /**
     * Sets the value of the overflow bit
     *
     * @param set Value to set
     */
    private void setOverflow(boolean set) {
        setFlag(set, 3);
    }

    /**
     * Sets bits in the flag register
     *
     * @param set      Value to set
     * @param position Position of the bit
     */
    private void setFlag(boolean set, int position) {
        int register = virtualMachine.getRegisters().get(VMRegistersEnum.VSF).getIntValue();

        if (set) {
            register |= (1 << position);
        } else {
            register &= ~(1 << position);
        }

        virtualMachine.getRegisters().set(VMRegistersEnum.VSF, register);
    }

    /**
     * Subtract two numbers from the stack
     */
    @SuppressWarnings("unused")
    public void subCommand() throws InterruptException {
        Word w1 = getStackTop();
        decrementStackPointer();
        Word w2 = getStackTop();

        int x1 = w1.getIntegerValue();
        int x2 = w2.getIntegerValue();
        int result = x1 - x2;

        setStackTop(result);
        decreaseTimer();
    }

    /**
     * Multiply two numbers from the stack
     */
    @SuppressWarnings("unused")
    public void mulCommand() throws InterruptException {
        Word w1 = getStackTop();
        decrementStackPointer();
        Word w2 = getStackTop();

        int x1 = w1.getIntegerValue();
        int x2 = w2.getIntegerValue();
        int result = x1 * x2;

        setStackTop(result);
        decreaseTimer();
    }

    /**
     * Divide and round down two numbers from the stack
     */
    @SuppressWarnings("unused")
    public void divCommand() throws InterruptException {
        Word w1 = getStackTop();
        decrementStackPointer();
        Word w2 = getStackTop();

        int x1 = w1.getIntegerValue();
        int x2 = w2.getIntegerValue();
        int result = x1 / x2;

        setStackTop(result);
        decreaseTimer();
    }

    /* Memory operations */

    @SuppressWarnings("unused")
    public void pushCommand(Object x) throws Exception {
        incrementStackPointer();
        setStackTop(String.valueOf(x));
        decreaseTimer();
    }

    @SuppressWarnings("unused")
    public void popCommand(Object x) {
        // TODO: Paziuret ar pop niekur neturi issaugot dalyku
        decrementStackPointer();
        decreaseTimer();
    }

    @SuppressWarnings("unused")
    public void lockCommand(Object x) {
        Registers.set(RMRegistersEnum.SI, Interrupt.SI_LOCK);
        decreaseTimer();
    }

    @SuppressWarnings("unused")
    public void ulckCommand(Object x) {
        Registers.set(RMRegistersEnum.SI, Interrupt.SI_ULCK);
        decreaseTimer();
    }

    /* Compare operations */

    /**
     * [SP+1] = 0 jei [SP-1] > [SP]; SP--
     * [SP+1] = 1 jei [SP-1] == [SP]; SP--
     * [SP+1] = 2 jei [SP-1] < [SP]; SP--
     */
    @SuppressWarnings("unused")
    public void cmpCommand() throws InterruptException {
        Word w1 = getStackTop();
        decrementStackPointer();
        Word w2 = getStackTop();
        incrementStackPointer();

        int x1 = w1.getIntegerValue();
        int x2 = w2.getIntegerValue();

        int result;
        if (x1 < x2) {
            result = 0;
        } else if (x1 > x2) {
            result = 2;
        } else {
            result = 1;
        }

        Registers.set(RMRegistersEnum.SF, result);

        incrementStackPointer();
        setStackTop(result);
        decreaseTimer();
    }

    /* Data operations */

    @SuppressWarnings("unused")
    public void loadCommand(Object x, Object y) throws InterruptException {
        int xInt = Integer.parseInt((String) x);
        int yInt = Integer.parseInt((String) y) & 0xF;
        if (xInt > 7) {
            Registers.set(RMRegistersEnum.PI, Interrupt.PI_MEMORY);
            throw new InterruptException("Trying to access wrong virtualMemory cell");
        }
        int sourceAddress = realAddress((xInt << 4) + yInt);
        Word source = getWord(sourceAddress);

        incrementStackPointer();
        setStackTop(source.getIntegerValue());
        decreaseTimer();
    }

    @SuppressWarnings("unused")
    public void putCommand(Object x, Object y) throws InterruptException {
        int xInt = Integer.parseInt((String) x);
        int yInt = Integer.parseInt((String) y) & 0xF;
        if (xInt > 7) {
            Registers.set(RMRegistersEnum.PI, Interrupt.PI_MEMORY);
            throw new InterruptException("Trying to access wrong virtualMemory cell");
        }
        int address = realAddress((xInt << 4) + yInt);

        Word stackTop = getStackTop();
        getWord(address).setValue(stackTop.getIntegerValue());
        decrementStackPointer();
        decreaseTimer();
    }

    @SuppressWarnings("unused")
    public void putnCommand(Object x) {
        // TODO: This command
        decreaseTimer();
    }

    @SuppressWarnings("unused")
    public void putsCommand(Object x) {
        // TODO: This command
        decreaseTimer();
    }

    /* Flow control */

    @SuppressWarnings("unused")
    public void jmpCommand(Object x, Object y) {
        int blockIdx = Integer.parseInt(((String) x).trim());
        int wordIdx = Integer.parseInt(((String) y).trim());
        int jump = 16 * blockIdx + wordIdx;
        setProgramCounter(jump);
        decreaseTimer();
    }

    @SuppressWarnings("unused")
    public void jzCommand(Object x, Object y) throws InterruptException {
        // TODO: Use RealMemory
        int condition = getStackTop().getIntegerValue();
        if (condition == 0) {
            int i1 = 16 * (int) x + (int) y;
            setProgramCounter(i1);
            decreaseTimer();
        }
    }

    @SuppressWarnings("unused")
    public void jgCommand(Object x, Object y) throws InterruptException {
        // TODO: Use RealMemory
        int condition = getStackTop().getIntegerValue();
        if (condition > 0) {
            int i1 = 16 * (int) x + (int) y;
            setProgramCounter(i1);
            decreaseTimer();
        }
    }

    @SuppressWarnings("unused")
    public void jlCommand(Object x, Object y) throws InterruptException {
        Word stackTop = getStackTop();
        int condition = stackTop.getIntegerValue();
        if (condition < 0) {
            int i1 = 16 * (int) x + (int) y;
            setProgramCounter(i1);
            decreaseTimer();
        }
    }

    @SuppressWarnings("unused")
    public void haltCommand() {
        Registers.get(RMRegistersEnum.SI).setValue(Interrupt.SI_HALT); // HALT
        decreaseTimer();
    }

    /* IO operations */

    @SuppressWarnings("unused")
    public void outsCommand() {
        Registers.set(RMRegistersEnum.SI, Interrupt.SI_OUTS);
        decreaseTimer();
    }

    @SuppressWarnings("unused")
    public void outnCommand() {
        Registers.set(RMRegistersEnum.SI, Interrupt.SI_OUTN);
        decreaseTimer();
    }

    @SuppressWarnings("unused")
    public void outbCommand(Object x, Object y, Object z) {
        Registers.set(RMRegistersEnum.SI, Interrupt.SI_OUTB);
        decreaseTimer();
    }

    @SuppressWarnings("unused")
    public void readCommand() {
        Registers.set(RMRegistersEnum.SI, Interrupt.SI_READ);
        decreaseTimer();
    }

    /**
     * Duomenų failo pagalbinės komandos
     */

    @SuppressWarnings("unused")
    public void deCommand() {
        decreaseTimer();
    }

    @SuppressWarnings("unused")
    public void dwCommand(Object x) {
        decreaseTimer();
    }

    @SuppressWarnings("unused")
    public void dbCommand(Object x) {
        decreaseTimer();
    }
}