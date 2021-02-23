import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import java.io.File;

public class SimpleCrawler4jPoC {

    private static final String RESOURCES_FOLDER_PATH = System.getProperty("user.dir")
            + File.separator + "crawler4j"
            + File.separator + "src"
            + File.separator + "main"
            + File.separator + "resources";

    private static final String CRAWL_STORAGE_FOLDER = RESOURCES_FOLDER_PATH + "/data/crawl/root";

    public static void main(String[] args) throws Exception {
        setUpSimpleCrawler();
//        setUpMerchantCrawler();
    }

    private static void setUpSimpleCrawler() throws Exception {
        int numberOfCrawlers = 7;

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(CRAWL_STORAGE_FOLDER);
        config.setMaxDepthOfCrawling(2);
        config.setIncludeHttpsPages(true);

        // Instantiate the controller for this crawl.
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        // For each crawl, you need to add some seed urls. These are the first
        // URLs that are fetched and then the crawler starts following links
        // which are found in these pages
        controller.addSeed("http://siscomex.gov.br/");

        // The factory which creates instances of crawlers.
        CrawlController.WebCrawlerFactory<SimpleCrawler> factory = SimpleCrawler::new;

        // Start the crawl. This is a blocking operation, meaning that your code
        // will reach the line after this only when crawling is finished.
        controller.start(factory, numberOfCrawlers);
    }

    private static void setUpMerchantCrawler() throws Exception {
        int numberOfCrawlers = 7;

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(CRAWL_STORAGE_FOLDER);
        config.setMaxDepthOfCrawling(2);
        config.setIncludeHttpsPages(true);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        controller.addSeed("https://www.mercante.transportes.gov.br:1443/g33159MT/jsp/LogonCertificado.jsp?ind=0");

        CrawlController.WebCrawlerFactory<MerchantCrawler> factory = MerchantCrawler::new;

        controller.start(factory, numberOfCrawlers);
    }
}
