package io.izzel.taboolib.util.chat;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public final class TranslationRegistry {

    public static final TranslationRegistry INSTANCE = new TranslationRegistry();
    //
    private final List<TranslationProvider> providers = new LinkedList<>();

    static {
        try {
            INSTANCE.addProvider(new JsonProvider("/assets/minecraft/lang/en_us.json"));
        } catch (Exception ignored) {
        }

        try {
            INSTANCE.addProvider(new JsonProvider("/mojang-translations/en_us.json"));
        } catch (Exception ignored) {
        }

        try {
            INSTANCE.addProvider(new ResourceBundleProvider("mojang-translations/en_US"));
        } catch (Exception ignored) {
        }
    }

    public TranslationRegistry() {
    }

    private void addProvider(TranslationProvider provider) {
        providers.add(provider);
    }

    public String translate(String s) {
        for (TranslationProvider provider : providers) {
            String translation = provider.translate(s);

            if (translation != null) {
                return translation;
            }
        }

        return s;
    }

    public List<TranslationProvider> getProviders() {
        return this.providers;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof TranslationRegistry)) return false;
        final TranslationRegistry other = (TranslationRegistry) o;
        final Object this$providers = this.providers;
        final Object other$providers = other.providers;
        return this$providers == null ? other$providers == null : this$providers.equals(other$providers);
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $providers = this.providers;
        result = result * PRIME + ($providers == null ? 43 : $providers.hashCode());
        return result;
    }

    public String toString() {
        return "TranslationRegistry(providers=" + this.providers + ")";
    }

    private interface TranslationProvider {

        String translate(String s);
    }

    private static class ResourceBundleProvider implements TranslationProvider {

        private final ResourceBundle bundle;

        public ResourceBundleProvider(String bundlePath) {
            this.bundle = ResourceBundle.getBundle(bundlePath);
        }

        public ResourceBundleProvider(ResourceBundle bundle) {
            this.bundle = bundle;
        }

        @Override
        public String translate(String s) {
            return (bundle.containsKey(s)) ? bundle.getString(s) : null;
        }

        public ResourceBundle getBundle() {
            return this.bundle;
        }

        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof ResourceBundleProvider)) return false;
            final ResourceBundleProvider other = (ResourceBundleProvider) o;
            if (!other.canEqual(this)) return false;
            final Object this$bundle = this.bundle;
            final Object other$bundle = other.bundle;
            return this$bundle == null ? other$bundle == null : this$bundle.equals(other$bundle);
        }

        protected boolean canEqual(final Object other) {
            return other instanceof ResourceBundleProvider;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $bundle = this.bundle;
            result = result * PRIME + ($bundle == null ? 43 : $bundle.hashCode());
            return result;
        }

        public String toString() {
            return "TranslationRegistry.ResourceBundleProvider(bundle=" + this.bundle + ")";
        }
    }

    private static class JsonProvider implements TranslationProvider {

        private final Map<String, String> translations = new HashMap<>();

        public JsonProvider(String resourcePath) throws IOException {
            try (InputStreamReader rd = new InputStreamReader(JsonProvider.class.getResourceAsStream(resourcePath), Charsets.UTF_8)) {
                JsonObject obj = new Gson().fromJson(rd, JsonObject.class);
                for (Map.Entry<String, JsonElement> entries : obj.entrySet()) {
                    translations.put(entries.getKey(), entries.getValue().getAsString());
                }
            }
        }

        public JsonProvider() {
        }

        @Override
        public String translate(String s) {
            return translations.get(s);
        }

        public Map<String, String> getTranslations() {
            return this.translations;
        }

        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof JsonProvider)) return false;
            final JsonProvider other = (JsonProvider) o;
            if (!other.canEqual(this)) return false;
            final Object this$translations = this.translations;
            final Object other$translations = other.translations;
            return this$translations == null ? other$translations == null : this$translations.equals(other$translations);
        }

        protected boolean canEqual(final Object other) {
            return other instanceof JsonProvider;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $translations = this.translations;
            result = result * PRIME + ($translations == null ? 43 : $translations.hashCode());
            return result;
        }

        public String toString() {
            return "TranslationRegistry.JsonProvider()";
        }
    }
}
