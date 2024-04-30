/**
 * This class represents the interface to a Flash Memory Device. The hardware has only two methods - 'read' and 'write'
 * However, the interface for using the device is a lot more complex than that.
 */
public interface FlashMemoryDevice {

    byte read(long address);

    void write(long address, byte data);

}