package com.ztgeo.suqian.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

/**
 * 流(inputStream、outputStream)相关工具集
 *
 * @author zoupeidong
 * @version 2018-12-7
 */
public class StreamOperateUtils {

    /**
     * 拷贝inputStream
     *
     * @param initInputStream 需要拷贝的输入流
     * @return 拷贝的新输入流
     */
    public static InputStream cloneInputStream(InputStream initInputStream) throws IOException {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = initInputStream.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (!Objects.equals(null, baos)) {
                baos.close();
            }
        }
    }

    /**
     * 拷贝inputStream
     *
     * @param initInputStream 需要拷贝的输入流
     * @return 拷贝的新输入流
     */
    public static ByteArrayOutputStream cloneInputStreamToByteArray(InputStream initInputStream) throws IOException {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = initInputStream.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return baos;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (!Objects.equals(null, baos)) {
                baos.close();
            }
        }
    }

    /**
     *  生成不重复短UUID
     * @return
     */
    public static String getShortUUID(){
        char[] chars = new char[]{'a', 'b', 'c', 'd', 'e', 'f',
                'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
                't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
                '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
                'W', 'X', 'Y', 'Z'};
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 8; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(chars[x % 0x3E]);
        }
        return shortBuffer.toString();
    }

}
