import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;
import net.sourceforge.htmlunit.corejs.javascript.json.JsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClients;
import org.apache.pdfbox.pdmodel.PDDocument;
import sun.net.www.http.HttpClient;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public class SimpleHtmlUnitCrawlerPoC {

    private static final Logger logger = Logger.getLogger(SimpleHtmlUnitCrawlerPoC.class.getName());

    public static void main(String[] args) throws IOException, JsonParser.ParseException {
        try (WebClient webClient = configureWebClient()) {
            webClient.getCookieManager().setCookiesEnabled(true);

//            startCustomsTablesCrawler(webClient);
            startMerchantCrawler(webClient);
            startSiscomexImportCrawlers(webClient);
        }
    }

    private static void startSiscomexImportCrawlers(WebClient webClient) throws IOException, JsonParser.ParseException {
        webClient.getPage(configureRequestToLogonSiscomex());

        Cookie jSessionIdCookie = webClient.getCookieManager().getCookie("JSESSIONID");
        Cookie logonCertCookie = webClient.getCookieManager().getCookie("LtpaToken2");

        logger.info(jSessionIdCookie.toString());
        logger.info(logonCertCookie.toString());

        doFindLatestNoticesOnSiscomexImport(webClient);

        WebRequest importDeclarationRequest = configureRequestToFindImportDeclarationOnSiscomex();
        final HtmlPage importDeclarationPage = webClient.getPage(importDeclarationRequest);
        DomElement importDeclarationMainElement = importDeclarationPage.getElementById("principal");
        logger.info(importDeclarationPage.getTitleText());
        logger.info(importDeclarationMainElement.asXml());

        doDownloadImportDeclarationExtractPdfOnSiscomex(webClient);
        doDownloadImportDeclarationXmlOnSiscomex(webClient);
    }

    private static void doDownloadImportDeclarationXmlOnSiscomex(WebClient webClient) throws IOException {
        HttpWebConnection httpWebConnection = new HttpWebConnection(webClient);
        WebRequest downloadImportDeclarationXmlRequest = configureRequestToDownloadImportDeclarationXmlOnSiscomex();
        WebResponse downloadImportDeclarationXmlResponse = httpWebConnection.getResponse(downloadImportDeclarationXmlRequest);
        httpWebConnection.close();

        InputStream downloadImportDeclarationXmlAsStream = downloadImportDeclarationXmlResponse.getContentAsStream();

        String extension = "xml";
        long identifier = Math.round((Math.random() * 9999999));

        String path = System.getProperty("user.dir")
                + "/src/main/resources/import-declarations/xml/di-" + identifier + "." + extension;

        saveFileFromInputStream(downloadImportDeclarationXmlAsStream, path);
    }

    private static void doDownloadImportDeclarationExtractPdfOnSiscomex(WebClient webClient) throws IOException {
        HttpWebConnection httpWebConnection = new HttpWebConnection(webClient);
        WebRequest downloadImportDeclarationExtractRequest = configureRequestToDownloadImportDeclarationExtractOnSiscomex();
        WebResponse downloadImportDeclarationExtractResponse = httpWebConnection.getResponse(downloadImportDeclarationExtractRequest);
        httpWebConnection.close();

        InputStream downloadImportDeclarationExtractAsStream = downloadImportDeclarationExtractResponse.getContentAsStream();

        String extension = "pdf";
        long identifier = Math.round((Math.random() * 9999999));

        String path = System.getProperty("user.dir")
                + "/src/main/resources/import-declarations/extract/di-" + identifier + "." + extension;

        saveFileFromInputStream(downloadImportDeclarationExtractAsStream, path);
    }

    private static void saveFileFromInputStream(InputStream inputStream, String path) {
        File file = new File(path);
        try (BufferedInputStream in = new BufferedInputStream(inputStream);
             FileOutputStream out = new FileOutputStream(file))
        {
            byte[] buffer = new byte[1024];
            int lengthRead;
            while ((lengthRead = in.read(buffer, 0, 1024)) != -1) {
                out.write(buffer, 0, lengthRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static WebRequest configureRequestToLogonSiscomex() throws MalformedURLException {
        return new WebRequest(
                new URL("https://www1c.siscomex.receita.fazenda.gov.br/siscomexImpweb-7/private_siscomeximpweb_inicio.do"),
                HttpMethod.POST
        );
    }

    private static void doFindLatestNoticesOnSiscomexImport(WebClient webClient) throws IOException {
        WebRequest importerProfilesRequest = configureRequestToFindLatestNoticesOnSiscomexImport();
        HttpWebConnection httpWebConnection = new HttpWebConnection(webClient);
        WebResponse importerProfilesResponse = httpWebConnection.getResponse(importerProfilesRequest);
        httpWebConnection.close();
        InputStream importerProfilesResponseContentAsStream = importerProfilesResponse.getContentAsStream();

        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(importerProfilesResponseContentAsStream)
        );
        String jsonContent = bufferedReader.lines()
                .reduce(String::concat)
                .orElse("");

        logger.info(jsonContent);
    }

    private static WebRequest configureRequestToFindLatestNoticesOnSiscomexImport() throws MalformedURLException {
        return new WebRequest(
                new URL("https://www1c.siscomex.receita.fazenda.gov.br/siscomexImpweb-7/private_siscomeximpweb_AjaxSubmit.do?listaPerfisOrdenada=IMPORTADOR"),
                HttpMethod.POST
        );
    }

    private static WebRequest configureRequestToFindImportDeclarationOnSiscomex() throws MalformedURLException {
        WebRequest request = new WebRequest(
                new URL("https://www1c.siscomex.receita.fazenda.gov.br/importacaoweb-7/ConsultarDI.do"),
                HttpMethod.POST
        );

        List<NameValuePair> requestParameters = new ArrayList<>();

        requestParameters.add(new NameValuePair("enviar", "Consultar"));
        requestParameters.add(new NameValuePair("rdpesq", "pesquisar"));
        requestParameters.add(new NameValuePair("perfil", "IMPORTADOR"));
        requestParameters.add(new NameValuePair("listaNrsDeclaracao", "21/0157855-6"));
        requestParameters.add(new NameValuePair("nrDeclaracao", ""));
        requestParameters.add(new NameValuePair("numeroRetificacao", ""));

        request.setRequestParameters(requestParameters);

        return request;
    }

    private static WebRequest configureRequestToDownloadImportDeclarationXmlOnSiscomex() throws MalformedURLException {
        WebRequest request = new WebRequest(
                new URL("https://www1c.siscomex.receita.fazenda.gov.br/importacaoweb-7/ConsultarDiXml.do"),
                HttpMethod.POST
        );
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new NameValuePair("xml", "21/0157855-6"));
        request.setRequestParameters(parameters);

        return request;
    }

    private static WebRequest configureRequestToDownloadImportDeclarationExtractOnSiscomex() throws MalformedURLException {
        return new WebRequest(
                new URL("https://www1c.siscomex.receita.fazenda.gov.br/importacaoweb-7/ExtratoDI.do?nrDeclaracao=21/0157855-6&consulta=true"),
                HttpMethod.POST
        );
    }

    private static void startMerchantCrawler(WebClient webClient) throws IOException {
        webClient.getPage(
                "https://www.mercante.transportes.gov.br:1443/g33159MT/servlet/certificado.LogonCertificado?ind=0"
        );

        Cookie jSessionIdCookie = webClient.getCookieManager().getCookie("JSESSIONID");
        Cookie logonCertCookie = webClient.getCookieManager().getCookie("LogonCert");

        logger.info(jSessionIdCookie.toString());
        logger.info(logonCertCookie.toString());

        WebRequest consigneeCnpjRequest = configureRequestToFindByConsigneeCnpj();
        final HtmlPage merchantConsigneePage = webClient.getPage(consigneeCnpjRequest);
        logger.info(merchantConsigneePage.getTitleText());
        logger.info(merchantConsigneePage.asXml());

        WebRequest containersTypeRequest = configureRequestToFindAllContainersType();
        final HtmlPage merchantContainersTypePage = webClient.getPage(containersTypeRequest);
        logger.info(merchantContainersTypePage.getTitleText());
        logger.info(merchantContainersTypePage.asXml());

        WebRequest consigneeRepresentationsRequest = configureRequestToFindAllConsigneeRepresentations();
        final HtmlPage merchantConsigneeRepresentationsPage = webClient.getPage(consigneeRepresentationsRequest);
        logger.info(merchantConsigneeRepresentationsPage.getTitleText());
        logger.info(merchantConsigneeRepresentationsPage.asXml());
    }

    private static WebRequest configureRequestToFindAllConsigneeRepresentations() throws MalformedURLException {
        WebRequest request = new WebRequest(
                new URL("https://www.mercante.transportes.gov.br/g36127/servlet/tabelas.repleg.RepLegSvlet"),
                HttpMethod.POST
        );

        List<NameValuePair> requestParameters = new ArrayList<>();
        requestParameters.add(new NameValuePair("pagina", "RepLegConsul2"));

        request.setRequestParameters(requestParameters);

        return request;
    }

    private static WebRequest configureRequestToFindAllContainersType() throws MalformedURLException {
        WebRequest request = new WebRequest(
                new URL("https://www.mercante.transportes.gov.br/g36127/servlet/serpro.siscomex.mercante.servlet.MercanteController?acao=%27logon%27&passo=%271%27")
        );

        List<NameValuePair> requestParameters = new ArrayList<>();
        requestParameters.add(new NameValuePair("passo", "4"));
        requestParameters.add(new NameValuePair("acao", "TTCO-CON"));
        requestParameters.add(new NameValuePair("tipoConsulta", "L"));
        requestParameters.add(new NameValuePair("titulo", "Tipo+de+Cont%EAiner+-+Consultar"));
        requestParameters.add(new NameValuePair("atencao", "+consultar+tabela+Tipo+de+Cont%EAiner+"));

        request.setRequestParameters(requestParameters);

        return request;
    }

    private static WebRequest configureRequestToFindByConsigneeCnpj() throws MalformedURLException {
        WebRequest request = new WebRequest(
                new URL("https://www.mercante.transportes.gov.br/g36127/servlet/serpro.siscomex.mercante.servlet.MercanteController?acao=%27logon%27&passo=%271%27")
        );

        List<NameValuePair> requestParameters = new ArrayList<>();
        requestParameters.add(new NameValuePair("CNPJConsignatario", "59408005000281"));
        requestParameters.add(new NameValuePair("DtFim", "06/12/2019"));
        requestParameters.add(new NameValuePair("DtInicio", "01/12/2019"));
        requestParameters.add(new NameValuePair("acao", "CONS-CECON"));
        requestParameters.add(new NameValuePair("passo", "2"));

        request.setRequestParameters(requestParameters);

        return request;
    }

    private static void startCustomsTablesCrawler(WebClient webClient) throws IOException {
        HtmlPage customsTablesSystemPublicLoginPage = webClient.getPage(
                "https://www35.receita.fazenda.gov.br/tabaduaneiras-web/public/pages/security/login_publico.jsf"
        );

        logger.info(customsTablesSystemPublicLoginPage.asXml());

        String decodedCaptchaCharacters = extractCaptchaFromCustomsTablesSystemPublicLoginPage(webClient, customsTablesSystemPublicLoginPage);

        DomElement captchaInputElement = customsTablesSystemPublicLoginPage.getElementById("txtTexto_captcha_serpro_gov_br");
        captchaInputElement.setAttribute("value", decodedCaptchaCharacters);

        HtmlPage customsTablesSystemInitialPage = customsTablesSystemPublicLoginPage
                .getElementByName("j_id11:j_id16")
                .click();

        logger.info(customsTablesSystemInitialPage.asXml());
    }

    private static String extractCaptchaFromCustomsTablesSystemPublicLoginPage(WebClient webClient, HtmlPage customsTablesSystemPublicLoginPage) throws IOException {
//        DomElement dataCaptchaSeproElement = customsTablesSystemPublicLoginPage.getFirstByXPath("//*[@id=\"j_id11\"]/div[2]/div");
//        String dataClientId = dataCaptchaSeproElement.getAttribute("data-clienteid");
//
//        String captchaBase64Image = doRecoveryBase64CaptchaByDataClientId(webClient, "b087fbd1daca4fa19d09a7912cc6a816");
//
//        logger.info("Base64 CAPTCHA Image: " + captchaBase64Image);

        HtmlImage catpchaImageElement = (HtmlImage) customsTablesSystemPublicLoginPage
                .getElementById("img_captcha_serpro_gov_br");

        saveImageFromHtmlElement(catpchaImageElement);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter with CAPTCHA characters:  ");
        String decodedCaptcha = bufferedReader.readLine().trim();

        logger.info("Decoded CAPTCHA characters: " + decodedCaptcha);

        return decodedCaptcha;
    }

    private static void saveImageFromHtmlElement(HtmlImage catpchaImageElement) throws IOException {
        logger.info(catpchaImageElement.getSrcAttribute());

        String extension = "png";
        long identifier = Math.round((Math.random() * 9999999));

        String path = System.getProperty("user.dir")
                + "/src/main/resources/captcha/captcha-" + identifier + "." + extension;
        File file = new File(path);

        catpchaImageElement.saveAs(file);
    }

    //    private static void decodeFromBase64ImageAndSave(String base64ImageString) {
//        String[] strings = base64ImageString.split(",");
//        String base64Image = strings[1];
//        String extension = strings[0].split("/")[1].split(";")[0];
//
//        long identifier = Math.round((Math.random() * 9999999));
//
//        byte[] data = Base64.getDecoder().decode(base64Image);
//        String path = System.getProperty("user.dir")
//                + "/src/main/resources/captcha/captcha-" + identifier + "." + extension;
//        File file = new File(path);
//
//        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
//            outputStream.write(data);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private static String doRecoveryBase64CaptchaByDataClientId(WebClient webClient, String dataClientId) throws IOException {
        WebRequest recoveryBase64CaptchaRequest = new WebRequest(
                new URL("https://www4.receita.fazenda.gov.br/captchaserpro/captcha/1.0.0/imagem"),
                HttpMethod.POST
        );

        recoveryBase64CaptchaRequest.setRequestBody(dataClientId);

        HttpWebConnection httpWebConnection = new HttpWebConnection(webClient);
        WebResponse recoveryBase64CaptchaResponse = httpWebConnection.getResponse(recoveryBase64CaptchaRequest);
        InputStream base64CaptchaResponseContentAsStream = recoveryBase64CaptchaResponse.getContentAsStream();
        httpWebConnection.close();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(base64CaptchaResponseContentAsStream));
        String inputLineBase64Captcha;
        StringBuilder content = new StringBuilder();
        while ((inputLineBase64Captcha = bufferedReader.readLine()) != null) {
            content.append(inputLineBase64Captcha);
        }
        bufferedReader.close();

        return "data:image/png;base64," + content.toString().split("@")[1];
    }

    private static WebClient configureWebClient()  {
        final WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
        try {
            webClient.getOptions().setJavaScriptEnabled(false);
            configureCertificateAndSetUpWebClientOptions(webClient);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return webClient;
    }

    private static void configureCertificateAndSetUpWebClientOptions(WebClient webClient) throws IOException {
        File certificate = new File(System.getProperty("user.dir") + "/../../certificate/CERTIFICADO_WCR2020.pfx");
        byte[] byteCertificate = FileUtils.readFileToByteArray(certificate);
        InputStream inputStream = new ByteArrayInputStream(byteCertificate);

        String certificatePassword = "Giovana10";
        String certificateType = "PKCS12";

        webClient.getOptions().setSSLClientCertificate(inputStream, certificatePassword, certificateType);
        webClient.getOptions().setUseInsecureSSL(true);
    }
}
