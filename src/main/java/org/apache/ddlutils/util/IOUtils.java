package org.apache.ddlutils.util;

import java.io.*;

public final class IOUtils {

    public static byte[] readAllBytes(InputStream inputStream) {
        try (InputStream input = new BufferedInputStream(inputStream)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024);
            byte[] data = new byte[1024];
            int numRead;
            while ((numRead = input.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, numRead);
            }
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * close the closeable with all exceptions were caught and ignored.
     *
     * @param closeable closeable
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static BufferedReader wrapBufferReader(Reader reader) {
        BufferedReader bufferedReader;
        if (reader instanceof BufferedReader) {
            bufferedReader = (BufferedReader) reader;
        } else {
            bufferedReader = new BufferedReader(reader);
        }
        return bufferedReader;
    }
}
