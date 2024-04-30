import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * This class is used by the operating system to interact with the hardware 'FlashMemoryDevice'.
 */
public class DeviceDriver {
    public static final int HARDWARE_ACCESS_TRY = 5;
    public static final byte BYTE_ERASED = (byte) 0xFF;
    private FlashMemoryDevice hardware;

    public DeviceDriver(FlashMemoryDevice hardware) {
        this.hardware = hardware;
    }

    public byte read(long address) throws ReadFailException {
        byte ret = this.hardware.read(address);
        checkReliability(address, ret);
        return ret;
    }

    private void checkReliability(long address, byte ret) throws ReadFailException {
        LocalDateTime previous = LocalDateTime.now();
        for (int i = 1; i < HARDWARE_ACCESS_TRY; i++) {
            while (ChronoUnit.MILLIS.between(previous, LocalDateTime.now()) < 200){}
            if (this.hardware.read(address) != ret) {
                throw new ReadFailException();
            }
            previous = LocalDateTime.now();
        }
    }

    public void write(long address, byte data) throws WriteFailException {
        if (this.hardware.read(address) != BYTE_ERASED) {
            throw new WriteFailException();
        }
        this.hardware.write(address, data);
    }
}