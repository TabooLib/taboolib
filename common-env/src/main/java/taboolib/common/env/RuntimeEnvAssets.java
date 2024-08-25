package taboolib.common.env;

import org.jetbrains.annotations.NotNull;
import org.tabooproject.reflex.ClassAnnotation;
import org.tabooproject.reflex.ReflexClass;
import taboolib.common.PrimitiveIO;
import taboolib.common.PrimitiveSettings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

public class RuntimeEnvAssets {

    private final String defaultAssets = PrimitiveSettings.FILE_ASSETS;

    @NotNull
    public List<ParsedResource> getAssets(@NotNull ReflexClass clazz) {
        List<ParsedResource> resourceList = new ArrayList<>();
        ClassAnnotation runtimeResource = clazz.getAnnotationIfPresent(RuntimeResource.class);
        if (runtimeResource != null) {
            resourceList.add(new ParsedResource(runtimeResource.properties()));
        }
        ClassAnnotation runtimeResources = clazz.getAnnotationIfPresent(RuntimeResources.class);
        if (runtimeResources != null) {
            runtimeResources.mapList("value").forEach(map -> resourceList.add(new ParsedResource(map)));
        }
        return resourceList;
    }

    public int loadAssets(@NotNull ReflexClass clazz) throws IOException {
        int total = 0;
        List<ParsedResource> resources = getAssets(clazz);
        for (ParsedResource resource : resources) {
            loadAssets(resource.name(), resource.hash(), resource.value(), resource.zip());
            total++;
        }
        return total;
    }

    /**
     * 下载资源文件到 assets 目录下
     *
     * @param name 文件名
     * @param hash 文件的 SHA-1（如果是压缩包，则为原始文件的 SHA-1）
     * @param url  文件下载地址
     * @param zip  是否为压缩包格式
     */
    public void loadAssets(String name, String hash, String url, boolean zip) throws IOException {
        File file;
        if (name.isEmpty()) {
            file = new File(defaultAssets, hash.substring(0, 2) + "/" + hash);
        } else {
            file = new File(defaultAssets, name);
        }
        if (file.exists() && PrimitiveIO.getHash(file).equals(hash)) {
            return;
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        PrimitiveIO.println("Downloading assets " + url.substring(url.lastIndexOf('/') + 1));
        if (zip) {
            File cacheFile = new File(file.getParentFile(), file.getName() + ".zip");
            PrimitiveIO.downloadFile(new URL(url + ".zip"), cacheFile);
            try (ZipFile zipFile = new ZipFile(cacheFile)) {
                InputStream inputStream = zipFile.getInputStream(zipFile.getEntry(url.substring(url.lastIndexOf('/') + 1)));
                try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    fileOutputStream.write(PrimitiveIO.readFully(inputStream));
                }
            } finally {
                cacheFile.delete();
            }
        } else {
            PrimitiveIO.downloadFile(new URL(url), file);
        }
    }
}
