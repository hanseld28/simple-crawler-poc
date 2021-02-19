import com.google.gson.Gson;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;

import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleSeleniumCrawlerPoC {

    private static final Logger logger = Logger.getLogger(SimpleSeleniumCrawlerPoC.class.getName());

    private static final String RESOURCES_FOLDER_PATH = System.getProperty("user.dir")
            + File.separator + "src"
            + File.separator + "main"
            + File.separator + "resources";
    private static final String RESOURCES_DATA_FOLDER_PATH = RESOURCES_FOLDER_PATH + File.separator + "data";
    private static final String RESOURCES_DRIVERS_FOLDER_PATH = RESOURCES_FOLDER_PATH + File.separator + "drivers";
    private static final String RESOURCES_DOWNLOADS_FOLDER_PATH = RESOURCES_FOLDER_PATH + File.separator + "downloads";
    private static final String RESOURCES_SCREENSHOTS_FOLDER_PATH = RESOURCES_FOLDER_PATH + File.separator + "screenshots";

    private static final String CHROME_DRIVER_EXE_NAME = "chromedriver.exe";
    private static final String GECKO_DRIVER_EXE_NAME = "geckodriver.exe";
    private static final String IE_DRIVER_EXE_NAME = "IEDriverServer.exe";

    private static final String CHROME_DRIVER_FULL_PATH = RESOURCES_DRIVERS_FOLDER_PATH + File.separator + CHROME_DRIVER_EXE_NAME;
    private static final String FIREFOX_DRIVER_FULL_PATH = RESOURCES_DRIVERS_FOLDER_PATH + File.separator + GECKO_DRIVER_EXE_NAME;
    private static final String IE_DRIVER_FULL_PATH = RESOURCES_DRIVERS_FOLDER_PATH + File.separator + IE_DRIVER_EXE_NAME;

    private static final String CHROME_PROPERTY_KEY = "webdriver.chrome.driver";
    private static final String GECKO_PROPERTY_KEY = "webdriver.gecko.driver";
    private static final String IE_PROPERTY_KEY = "webdriver.ie.driver";

    public static void main(String[] args) {
        removeFileFromFolderIfExistsByPrefix(RESOURCES_DATA_FOLDER_PATH, "coins");
        removeFileFromFolderIfExistsByPrefix(RESOURCES_DOWNLOADS_FOLDER_PATH, "Moeda");
        removeFileFromFolderIfExistsByPrefix(RESOURCES_SCREENSHOTS_FOLDER_PATH, "download-options-view-01");

        WebDriver driver = getFirefoxDriver();

        final String SISTEMA_TABELA_ADUANEIRAS_URL = "https://www35.receita.fazenda.gov.br/tabaduaneiras-web/private/pages/telaInicial.jsf";

        driver.get(SISTEMA_TABELA_ADUANEIRAS_URL);
        driver.manage().window().maximize();

        String pageTitle = driver.getTitle();

        logger.log(Level.INFO, "Título da Página -> " + pageTitle);

        awaitFor(5, "Aguardando a inserção manual do CAPTCHA.");

        WebElement captchaInput = driver.findElement(By.id("txtTexto_captcha_serpro_gov_br"));

        logger.log(Level.INFO, "CAPTCHA INPUT NAME: " + captchaInput.getAttribute("name"));
        logger.log(Level.INFO, "CAPTCHA INPUT VALUE: " + captchaInput.getAttribute("value"));

        driver.findElement(By.name("j_id11:j_id16"))
                .click();

        driver.findElement(By.xpath("//*[@id=\"j_id110:agrupamento:7:grupo:4:j_id121\"]/center/a"))
                .click();

        driver.findElement(By.id("j_id113:downloadMoeda"))
                .click();

        driver.findElement(By.id("j_id113:downloadMoedaarquivocsv"))
                .click();

        String screenshotFileNameWithPath = RESOURCES_SCREENSHOTS_FOLDER_PATH + File.separator + "download-options-view-01.png";
        takeScreenshot(driver, screenshotFileNameWithPath);

        awaitFor(1, "Aguardando a finalização do download do arquivo CSV.");

        logger.log(Level.INFO, "Closed driver");
        driver.close();

        printList(readCoinCsvFile());
    }

    public static void takeScreenshot(WebDriver driver, String fileWithPath) {
        TakesScreenshot takesScreenshot = ((TakesScreenshot) driver);
        File sourceFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
        File copiedFile = new File(fileWithPath);

        try {
            InputStream in = new BufferedInputStream(new FileInputStream(sourceFile));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(copiedFile));
            byte[] buffer = new byte[1024];
            int lengthRead;
            while ((lengthRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, lengthRead);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static WebDriver getChromeDriver() {
        System.setProperty(CHROME_PROPERTY_KEY, CHROME_DRIVER_FULL_PATH);

        Map preferencesMap = new HashMap();
        preferencesMap.put("download.default_directory", RESOURCES_DOWNLOADS_FOLDER_PATH);

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", preferencesMap);

        return new ChromeDriver(options);
    }

    public static WebDriver getFirefoxDriver() {
        System.setProperty(GECKO_PROPERTY_KEY, FIREFOX_DRIVER_FULL_PATH);

//        FirefoxBinary binary = new FirefoxBinary();
//        binary.addCommandLineOptions("--headless");

        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("browser.download.folderList", 2);
        profile.setPreference("browser.download.dir", RESOURCES_DOWNLOADS_FOLDER_PATH);
        profile.setPreference("browser.download.manager.alertOnEXEOpen", false);
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/msword,application/csv,text/csv,image/png,image/jpeg,application/pdf,text/html,text/plain,application/octet-stream");

        FirefoxOptions options = new FirefoxOptions();
        options.setProfile(profile);
//        options.setBinary(binary);

        return new FirefoxDriver(options);
    }

    public static WebDriver getIEDriver() {
        System.setProperty(IE_PROPERTY_KEY, IE_DRIVER_FULL_PATH);

        InternetExplorerOptions options = new InternetExplorerOptions();
        options.useCreateProcessApiToLaunchIe();

        return new InternetExplorerDriver(options);
    }

    public static void awaitFor(int seconds, String reason) {
        try {
            logger.log(Level.INFO, "[PAUSED] Execução pausada por " + seconds + " segundos. \n[REASON] " + reason);
            Thread.sleep(Duration.ofSeconds(seconds).toMillis());
            logger.log(Level.INFO, "[EXECUTING] Execução retomada.");
        } catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage());
        }
    }

    public static List<Coin> readCoinCsvFile() {
        String coinCsvFilePath = RESOURCES_DOWNLOADS_FOLDER_PATH + File.separator + "Moeda.csv";

        List<Coin> coins = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(coinCsvFilePath));
            String line;
            int counter = 0;
            while ((line = br.readLine()) != null) {
                ++counter;
                if (counter < 3) {
                    continue;
                }
                String[] values = line.split(",");
                Coin coin = new Coin(
                        values[0],
                        values[1],
                        values[2],
                        values[3],
                        values[4]
                );
                coins.add(coin);
            }

            String jsonCoinsFilePath = RESOURCES_DATA_FOLDER_PATH + File.separator + "coins.json";
            convertObjectListToJsonAndSaveFile(coins, jsonCoinsFilePath);

        } catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage());
        }

        return coins;
    }

    public static <T extends Object> void convertObjectListToJsonAndSaveFile(List<T> objects, String fileNameWithPath) throws IOException {
        Gson gson = new Gson();

        String jsonObjectsString = gson.toJson(objects);

        BufferedWriter writer = new BufferedWriter(new FileWriter(fileNameWithPath));
        writer.write(jsonObjectsString);

        writer.close();
    }

    public static <T extends Object> void printList(List<T> list) {
        int total = list.size();
        logger.log(Level.INFO, total + " registros encontrados.");
        for (int i = 0; i < total; i++) {
            if (i == 5) {
                logger.log(Level.INFO, (total - i) + " resultados restantes...");
                break;
            }
            logger.log(Level.INFO, list.get(i).toString());
        }
    }

    public static void removeFileFromFolderIfExistsByPrefix(String folderPath, String prefix) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        try {
            for (File file : Objects.requireNonNull(files)) {
                if (file.getName().startsWith(prefix)) {
                    file.delete();
                }
            }
        } catch (Exception ignored) {
        }
    }
}
