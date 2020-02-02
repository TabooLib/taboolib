package io.izzel.taboolib.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author Arasple
 * @date 2020/1/20 16:40
 */
public class Hastebin {

    private static final String HASTEBIN_URL = "https://hasteb.in/";

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

        private String source;
        private JsonObject sourceJson;

        public Result(String source) {
            this.source = source;
            this.sourceJson = new JsonParser().parse(source).getAsJsonObject();
        }

        public String getURL() {
            return HASTEBIN_URL + sourceJson.get("key").getAsString();
        }

        public String getSource() {
            return source;
        }

        public JsonObject getSourceJson() {
            return sourceJson;
        }
    }

}