import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.util.regex.Pattern;

public class SimpleCrawler extends WebCrawler {

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg|png|mp3|mp4|zip|gz))$");

    /**
     * This method receives two parameters. The first parameter is the page
     * in which we have discovered this new url and the second parameter is
     * the new url. You should implement this function to specify whether
     * the given url should be crawled or not (based on your crawling logic).
     * In this example, we are instructing the crawler to ignore urls that
     * have css, js, git, ... extensions and to only accept urls that start
     * with "https://www.ics.uci.edu/". In this case, we didn't need the
     * referringPage parameter to make the decision.
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches()
                && href.startsWith("http://siscomex.gov.br/sistema/demais-sistemas/importacao-2/");
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        int docid = page.getWebURL().getDocid();
        String url = page.getWebURL().getURL();
        String domain = page.getWebURL().getDomain();
        String path = page.getWebURL().getPath();
        String subDomain = page.getWebURL().getSubDomain();
        String parentUrl = page.getWebURL().getParentUrl();

        logger.debug("Docid: {}", docid);
        logger.info("URL: {}", url);
        logger.debug("Domain: '{}'", domain);
        logger.debug("Sub-domain: '{}'", subDomain);
        logger.debug("Path: '{}'", path);
        logger.debug("Parent page: {}", parentUrl);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String html = htmlParseData.getHtml();

            String filteredContentText = extractContentFromHtml(html);

            String collectDataPath = System.getProperty("user.dir")
                    + "/crawler4j/src/main/resources/data/crawl/collected/content.txt";
            File collectDataFile = new File(collectDataPath);

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(collectDataFile));
                writer.write(filteredContentText);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Header[] responseHeaders = page.getFetchResponseHeaders();
        if (responseHeaders != null) {
            logger.debug("Response headers:");
            for (Header header : responseHeaders) {
                logger.debug("\t{}: {}", header.getName(), header.getValue());
            }
        }

        logger.debug("=============");
    }

    private String extractContentFromHtml(String html) {
        Document document = Jsoup.parse(html);
        Elements elements = document.select(
                "#post-216 > div > div.content.clearfix > div > "
                        + "div.wpb_column.vc_column_container.vc_col-sm-9 > "
                        + "div > div > div > div"
        );

        StringBuilder filteredContent = new StringBuilder("SISTEMAS DE COMÉRCIO EXTERIOR | IMPORTAÇÃO");

        for (Element element : elements) {
            Element paragraph = element.child(0);
            Element titleSpan = paragraph.child(0);
            Element titleLink = titleSpan.child(0);
            String titleText = titleLink.text();
            String titleUrl = titleLink.attr("href");

            Element subtitleSpan = paragraph.child(2);
            String subtitleText = subtitleSpan.text();

            Element contentSpan = paragraph.child(4);
            String contentText = contentSpan.text();

            filteredContent.append("\n\n");
            filteredContent.append(titleText);
            filteredContent.append("\n");
            filteredContent.append(subtitleText);
            filteredContent.append("\n");
            filteredContent.append(contentText);
            filteredContent.append("\n");
            filteredContent.append("URL: ");
            filteredContent.append(titleUrl);
        }

        return filteredContent.toString();
    }
}
