package com.xuzp.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author za-xuzhiping
 * @Date 2018/8/1
 * @Time 16:27
 */
public class FileUtils {

    private static final String EMOJI_PATTERN = "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]";

    private static final Pattern pattern = Pattern.compile(EMOJI_PATTERN);

    public static boolean hasEmoji(String content) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    /**
     * 过滤表情符号。MySQL如果没有额外设置，无法保存表情符号
     * @param str
     * @return
     */
    public static String replaceEmoji(String str) {
        if (!hasEmoji(str)) {
            return str;
        } else {
            str = str.replaceAll(EMOJI_PATTERN, "");
            return str;
        }
    }

    /**
     * 去除文件名中的非法字符
     * @param filename
     * @return
     */
    public static String normalize(String filename){
        if (StringUtils.isNotEmpty(filename)) {
            return filename.replaceAll("[\\s\\\\/:\\*\\?\\\"<>\\|]", "");
        }
        return null;
    }

    public static File getOutputAccountPath(String outputPath, String account){
        File folder = new File(outputPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }


    public static String loadInput(InputStream input, String charsetName) {
        if(input != null) {
            try {
                return new String(loadByteArray(input), charsetName);
            } catch (Exception var3) {
                var3.printStackTrace();
            }
        }

        return null;
    }

    public static byte[] loadByteArray(InputStream input) {
        if(input != null) {
            BufferedInputStream br = null;

            try {
                br = new BufferedInputStream(input);
                byte[] bf = new byte[1024];
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                boolean var4 = false;

                int len;
                while((len = br.read(bf)) > 0) {
                    out.write(bf, 0, len);
                }

                byte[] var5 = out.toByteArray();
                return var5;
            } catch (Exception var15) {
                var15.printStackTrace();
                return null;
            } finally {
                if(br != null) {
                    try {
                        br.close();
                    } catch (IOException var14) {
                        var14.printStackTrace();
                    }
                }

            }
        } else {
            return null;
        }
    }

    public static String loadInput(InputStream input) {
        return loadInput(input, "UTF-8");
    }
}
