package taboolib.common.env.legacy;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import taboolib.common.PrimitiveIO;
import taboolib.common.PrimitiveSettings;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.Objects;

/**
 * Represents a maven repository that artifacts can be downloaded from
 *
 * @author Zach Deibert, sky
 * @since 1.0.0
 */
public class Repository extends AbstractXmlParser {

    /**
     * 仓库地址
     */
    private final String url;

    public Repository(String url) {
        this.url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public Repository(Element node) throws ParseException {
        this(find("url", node, null));
    }

    public Repository() {
        this(PrimitiveSettings.REPO_CENTRAL);
    }

    /**
     * 从仓库下载依赖文件，以及它的 sha1 文件
     */
    public void downloadFile(Dependency dep, File out) throws IOException {
        // 获取文件扩展名
        String ext = out.getName().substring(out.getName().lastIndexOf('.') + 1);
        // 构建 URL
        URL url = dep.getURL(this, ext);
        // 提示信息
        PrimitiveIO.println("Downloading ... %s", url);
        // 下载文件
        PrimitiveIO.downloadFile(url, out);
        PrimitiveIO.downloadFile(dep.getURL(this, ext + ".sha1"), new File(out.getPath() + ".sha1"));
    }

    /**
     * 如果在 pom 中没有指定依赖项的最新版本，则设置依赖项的最新版本
     */
    public void getLatestVersion(Dependency dep) throws IOException {
        URL url = new URL(String.format("%s/%s/%s/maven-metadata.xml", getUrl(), dep.getGroupId().replace('.', '/'), dep.getArtifactId()));
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream ins = url.openStream();
            Document doc = builder.parse(ins);
            dep.setVersion(find("release", doc.getDocumentElement(), find("version", doc.getDocumentElement(), null)));
        } catch (IOException | RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Repository)) return false;
        Repository that = (Repository) o;
        return Objects.equals(getUrl(), that.getUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUrl());
    }

    @Override
    public String toString() {
        return "Repository{" +
                "url='" + url + '\'' +
                '}';
    }
}
