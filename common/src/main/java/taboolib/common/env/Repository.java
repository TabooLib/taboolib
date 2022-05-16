package taboolib.common.env;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import taboolib.common.TabooLibCommon;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
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
     * The url of the central repository
     *
     * @since 1.0.0
     */
    private static final String MAVEN_CENTRAL = "https://maven.aliyun.com/repository/central";

    /**
     * The base url of this repository
     *
     * @since 1.0.0
     */
    private final String url;

    /**
     * Creates a new repository with the specified url
     *
     * @param url The url of the repository
     * @since 1.0.0
     */
    public Repository(String url) {
        this.url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * Creates a new repository from the specified element in the pom
     *
     * @param node The element to create the repository from
     * @throws ParseException If the xml could not be parsed
     * @since 1.0.0
     */
    public Repository(Element node) throws ParseException {
        this(find("url", node, null));
    }

    /**
     * Creates a new repository from Maven Central
     *
     * @since 1.0.0
     */
    public Repository() {
        this(MAVEN_CENTRAL);
    }

    /**
     * Downloads a file from this repository
     *
     * @param dep The dependency to download
     * @param out The file to save the downloaded content to
     * @throws IOException If an I/O error has occurred
     * @since 1.0.0
     */
    public void downloadToFile(Dependency dep, File out) throws IOException {
        URL url = new URL(String.format("%s/%s/%s/%s/%s", getUrl(), dep.getGroupId().replace('.', '/'), dep.getArtifactId(), dep.getVersion(), out.getName()));
        TabooLibCommon.print(String.format("Downloading ... %s", url));
        downloadToFile(url, out);
    }

    /**
     * Sets the latest version of a dependency if it was not specified in the
     * pom
     *
     * @param dep The dependency to set
     * @throws IOException If an I/O error has occurred
     * @since 1.0.0
     */
    public void setVersion(Dependency dep) throws IOException {
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

    /**
     * Gets the url the repository is at
     *
     * @return The url
     * @since 1.0.0
     */
    public String getUrl() {
        return url;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public static void downloadToFile(URL url, File out) throws IOException {
        InputStream ins = url.openStream();
        OutputStream outs = Files.newOutputStream(out.toPath());
        byte[] buffer = new byte[4096];
        for (int len; (len = ins.read(buffer)) > 0; outs.write(buffer, 0, len))
            ;
        outs.close();
        ins.close();
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
