package com.github.athingx.athing.aliyun.config.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpUtils {

    private static String getContentTypeCharset(HttpURLConnection connection) {
        final Pattern pattern = Pattern.compile("charset=\\S*");
        final Matcher matcher = pattern.matcher(connection.getContentType());
        final String charset;
        if (matcher.find()) {
            charset = matcher.group().replace("charset=", "");
        } else {
            charset = "UTF-8";
        }
        return charset;
    }

    /**
     * 从指定URL下载文本内容
     *
     * @param url              URL地址
     * @param connectTimeoutMs 连接超时时间
     * @param timeoutMs        超时时间
     * @return URL的文本信息
     * @throws IOException 下载文本出错
     */
    public static String getAsString(URL url, long connectTimeoutMs, long timeoutMs) throws IOException {

        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.setUseCaches(false);
        connection.setRequestMethod("GET");
        connection.setConnectTimeout((int) (connectTimeoutMs / 1000));
        connection.setReadTimeout((int) (timeoutMs / 1000));
        connection.connect();

        final int code = connection.getResponseCode();
        if (code != 200) {
            throw new IOException("http response code: " + code);
        }

        try (final InputStream input = connection.getInputStream()) {

            final String charset = getContentTypeCharset(connection);
            final byte[] data = new byte[connection.getContentLength()];
            final byte[] buffer = new byte[2048];

            int size, sum = 0;
            while ((size = input.read(buffer)) != -1) {
                System.arraycopy(buffer, 0, data, sum, size);
                sum += size;
            }
            return new String(data, charset);
        } finally {
            connection.disconnect();
        }

    }

}
