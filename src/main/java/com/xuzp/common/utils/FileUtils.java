package com.xuzp.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.*;

/**
 * @author za-xuzhiping
 * @Date 2018/8/1
 * @Time 16:27
 */
public class FileUtils {

    public static String normalizeFileName(String filename){
        if (StringUtils.isNotEmpty(filename)) {
            return filename.replaceAll("[\\s\\\\/:\\*\\?\\\"<>\\|]", "");
        }
        return null;
    }

    public static String readFile(String path) {
        File f = new File(path);
        if(!f.exists()) {
            return null;
        } else {
            FileReader fileReader = null;
            BufferedReader br = null;
            StringBuilder buffer = new StringBuilder("");

            try {
                fileReader = new FileReader(f);
                br = new BufferedReader(fileReader);

                String str;
                while((str = br.readLine()) != null) {
                    buffer.append(str);
                }
            } catch (Exception var18) {
                var18.printStackTrace();
            } finally {
                if(br != null) {
                    try {
                        br.close();
                    } catch (IOException var17) {
                        var17.printStackTrace();
                    }
                }

                if(fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException var16) {
                        var16.printStackTrace();
                    }
                }

            }

            return buffer.toString();
        }
    }

    public static void writerFile(String path, String content) {
        try {
            FileWriter writer = new FileWriter(path, true);
            writer.write(content);
            writer.close();
        } catch (IOException var3) {
            var3.printStackTrace();
        }

    }

    public static boolean delFile(String path) {
        File file = new File(path);
        return file.exists() && file.isFile()?file.delete():true;
    }

    public static String loadInput(InputStream input) {
        return loadInput(input, "UTF-8");
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

}
