package com.ztgeo.suqian.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

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


}
