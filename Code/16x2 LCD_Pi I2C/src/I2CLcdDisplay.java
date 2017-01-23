import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import java.util.BitSet;

public class I2CLcdDisplay {
    boolean             rsFlag                = false;
    boolean             eFlag                 = false;
    private static I2CDevice   dev                   = null;
    private final int[] LCD_LINE_ADDRESS      = { 0x80, 0xC0, 0x94, 0xD4 };

    private final boolean LCD_CHR = true;
    private final static boolean LCD_CMD = false;

    int         rsBit=0;
    int         eBit=1;
    int         d7Bit=5;
    int         d6Bit=4;
    int         d5Bit=3;
    int         d4Bit=2;

	public static void main(String[] args) throws Exception{

        I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);

        dev = bus.getDevice(0x20);
        dev.write(0x01, (byte) 0x00);
		I2CLcdDisplay lcd= new I2CLcdDisplay();
		lcd.init();
		lcd.lcd_byte(0x01, LCD_CMD); //LCD Clear
		lcd.lcd_byte(0x02, LCD_CMD); //LCD Home
		lcd.write("Hello World");
	}
	
    public void write(byte data) {
        try {
            lcd_byte(data, LCD_CHR);
        } catch (Exception ex) {
           ex.printStackTrace();
        }
    }

    public void write(String data) {
        for (int i = 0; i < data.length(); i++) {
            try {
                lcd_byte(data.charAt(i), LCD_CHR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void lcd_byte(int val, boolean type) throws Exception {

        // typ zapisu
    	rsFlag=type;

        // High Bit
        write(val >> 4);
        pulse_en(type, val >> 4);    // cmd or display data

        // lowbit
        write(val & 0x0f);
        pulse_en(type, val & 0x0f);
    }

    public static BitSet fromByte(byte b) {
        BitSet bits = new BitSet(8);

        for (int i = 0; i < 8; i++) {
            bits.set(i, (b & 1) == 1);
            b >>= 1;
        }

        return bits;
    }

    private void init() throws Exception {
        lcd_byte(0x33, LCD_CMD);    // 4 bit
        lcd_byte(0x32, LCD_CMD);    // 4 bit
        lcd_byte(0x28, LCD_CMD);    // 4bit - 2 line
        lcd_byte(0x08, LCD_CMD);    // don't shift, hide cursor
        lcd_byte(0x01, LCD_CMD);    // clear and home display
        lcd_byte(0x06, LCD_CMD);    // move cursor right
        lcd_byte(0x0c, LCD_CMD);    // turn on
    }

    private void pulse_en(boolean type, int val) throws Exception {
        eFlag = true;
        write(val);
        eFlag =false;
        write(val);

        // po CMD by se melo chvilku pockat
        if (type == LCD_CMD) {
            Thread.sleep(1);
        }
    }    // private voi

    private void write(int incomingData) throws Exception {
        int    tmpData = incomingData;
        BitSet bits    = fromByte((byte) tmpData);
        byte   out     = (byte) ((bits.get(3)
                                  ? 1 << d7Bit
                                  : 0 << d7Bit) | (bits.get(2)
                ? 1 << d6Bit
                : 0 << d6Bit) | (bits.get(1)
                                 ? 1 << d5Bit
                                 : 0 << d5Bit) | (bits.get(0)
                ? 1 << d4Bit
                : 0 << d4Bit) | (rsFlag
            					 ? 1 << rsBit
                : 0 << rsBit) | (eFlag
                                 ? 1 << eBit
                                 : 0 << eBit));

        dev.write(0x13,out);
    }

    public void setCursorPosition(int row, int column) {

        try {
            lcd_byte(LCD_LINE_ADDRESS[row] + column, LCD_CMD);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}