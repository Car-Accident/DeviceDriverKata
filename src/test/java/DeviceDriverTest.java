import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeviceDriverTest {
    public static final long ADDRESS_OxFF = 0xFF;
    public static final long ADDRESS_0xF3 = 0xF3;
    public static final byte BYTE_ERASED = (byte) 0xFF;
    public static final byte BYTE_WRITTEN = (byte) 0x00;
    public static final byte BYTE_1 = 1;
    public static final byte BYTE_2 = 2;
    DeviceDriver driver;

    @Mock
    FlashMemoryDevice mockFlashMemoryDevice;

    @BeforeEach
    void setUp() {
        driver = new DeviceDriver(mockFlashMemoryDevice);
    }

    @ParameterizedTest
    @ValueSource(bytes = {BYTE_1, BYTE_2})
    public void read_data_From_Hardware(byte data) throws ReadFailException {
        // arrange(given)
        lenient().when(mockFlashMemoryDevice.read(ADDRESS_OxFF))
                .thenReturn(data)
                .thenReturn(data)
                .thenReturn(data)
                .thenReturn(data)
                .thenReturn(data)
                .thenThrow(IllegalAccessError.class);

        // act(when)
        byte ret = driver.read(ADDRESS_OxFF);

        // assert(then)
        assertEquals(data, ret);
        verify(mockFlashMemoryDevice, times(5)).read(ADDRESS_OxFF);
    }

    @Test
    public void read_From_Hardware_with_interval() throws ReadFailException {
        // arrange(given)
        ArrayList<LocalDateTime> timeArray = new ArrayList<>();
        Mockito.doAnswer((a) -> {
            timeArray.add(LocalDateTime.now());
            return BYTE_1;}
        ).when(mockFlashMemoryDevice).read(ADDRESS_OxFF);

        // act(when)

        byte data = driver.read(ADDRESS_OxFF);

        // assert(then)
        assertEquals(BYTE_1, data);
        for (int i = 1; i < timeArray.size(); i++) {
            long millis = ChronoUnit.MILLIS.between(timeArray.get(i - 1), timeArray.get(i));
            assertThat(millis).isEqualTo(200);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4})
    void read_From_Hardware_with_ReadFailException(int changed) {
        byte[] data = {BYTE_1, BYTE_1, BYTE_1, BYTE_1, BYTE_1};
        data[changed] = BYTE_2;
        // arrange(given)
        lenient().when(mockFlashMemoryDevice.read(ADDRESS_OxFF))
                .thenReturn(data[0])
                .thenReturn(data[1])
                .thenReturn(data[2])
                .thenReturn(data[3])
                .thenReturn(data[4])
                .thenThrow(IllegalAccessError.class);

        // act(when)
        assertThrows(ReadFailException.class, () -> {
            driver.read(ADDRESS_OxFF);
        });

        // assert
    }

    @ParameterizedTest
    @MethodSource("getAddressAndData")
    public void write_To_Hardware(long address, byte data) throws WriteFailException {
        // arrange(given)
        when(mockFlashMemoryDevice.read(address))
                .thenReturn(BYTE_ERASED);

        // act(when)
        driver.write(address, data);

        // assert(then)
        verify(mockFlashMemoryDevice, times(1)).write(address, data);
    }

    @Test
    public void write_To_Hardware_with_WriteFailException() {
        // arrange(given)
        when(mockFlashMemoryDevice.read(ADDRESS_OxFF))
                .thenReturn(BYTE_WRITTEN);

        // act(when)
        assertThrows(WriteFailException.class, () -> {
            driver.write(ADDRESS_OxFF, BYTE_1);
        });

        // assert(then)
    }

    private static Stream<Arguments> getAddressAndData() {
        return Stream.of(
                Arguments.of(ADDRESS_OxFF, BYTE_1),
                Arguments.of(ADDRESS_0xF3, BYTE_2)
        );
    }
}