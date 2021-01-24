package io.izzel.taboolib.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Hastebin 在线粘贴板工具
 *
 * @author Arasple
 * @since 2020/1/20 16:40
 */
public class Hastebin {

    private static final String HASTEBIN_URL = "https://hasteb.in/";

    /**
     * 创建在线粘贴板
     * 报错会被屏蔽
     *
     * @param content 文本内容
     * @return {@link Result}
     */
    @Nullable
    public static Result paste(String content) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(HASTEBIN_URL + "documents").openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Charset", "UTF-8");
            con.setDoInput(true);
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(content.getBytes(StandardCharsets.UTF_8));
            return new Result(IO.readFully(con.getInputStream(), StandardCharsets.UTF_8));
        } catch (Throwable e) {
            return null;
        }
    }

    public static class Result {

        private final String source;
        private final JsonObject sourceJson;

        public Result(String source) {
            this.source = source;
            this.sourceJson = new JsonParser().parse(source).getAsJsonObject();
        }

        /**
         * @return 地址
         */
        @NotNull
        public String getURL() {
            return HASTEBIN_URL + sourceJson.get("key").getAsString();
        }

        /**
         * @return 原始内容
         */
        @NotNull
        public String getSource() {
            return source;
        }

        /**
         * @return {@link JsonObject} 实例
         */
        @NotNull
        public JsonObject getSourceJson() {
            return sourceJson;
        }
    }
}