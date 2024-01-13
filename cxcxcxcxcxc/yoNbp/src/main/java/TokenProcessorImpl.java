import nc.bs.framework.exception.FrameworkRuntimeException;
import org.granite.lang.util.HexEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class TokenProcessorImpl {
    public static void main(String[] args) {
        String s = "80441e756c4df8bccf71298ee65369bd";
        HexEncoder e = new HexEncoder();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            e.decode(s, out);
        } catch (IOException var5) {
            throw new FrameworkRuntimeException("error priviledged token", var5);
        }

        byte[] priviledgedToken = out.toByteArray();
        System.out.println(Arrays.toString(priviledgedToken));
    }
}
