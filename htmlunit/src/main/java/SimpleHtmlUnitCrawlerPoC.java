import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

public class SimpleHtmlUnitCrawlerPoC {

    private static final Logger logger = Logger.getLogger(SimpleHtmlUnitCrawlerPoC.class.getName());

    public static void main(String[] args) throws IOException {
        try (WebClient webClient = configureWebClient()) {
            webClient.getCookieManager().setCookiesEnabled(true);

            startMerchantCrawler(webClient);
//            startSiscomexImportCrawler(webClient);
        }
    }

//    private static void startSiscomexImportCrawler(WebClient webClient) throws IOException {
//        webClient.getPage(configureRequestToLogonSiscomex());
//
//        Cookie jSessionIdCookie = webClient.getCookieManager().getCookie("JSESSIONID");
//        Cookie logonCertCookie = webClient.getCookieManager().getCookie("LtpaToken2");
//
//        logger.info(jSessionIdCookie.toString());
//        logger.info(logonCertCookie.toString());
//
//        WebRequest importerProfilesRequest = configureRequestToFindSiscomexImporterProfiles();
//        final HtmlPage siscomexImporterProfilesPage = webClient.getPage(importerProfilesRequest);
//
//        logger.info(siscomexImporterProfilesPage.getTitleText());
//        logger.info(siscomexImporterProfilesPage.asXml());
//    }
//
//    private static WebRequest configureRequestToLogonSiscomex() throws MalformedURLException {
//        return new WebRequest(
//                new URL("https://www1c.siscomex.receita.fazenda.gov.br/siscomexImpweb-7/private_siscomeximpweb_inicio.do"),
//                HttpMethod.POST
//        );
//    }
//
//    private static WebRequest configureRequestToFindSiscomexImporterProfiles() throws MalformedURLException {
//        return new WebRequest(
//                new URL("https://www1c.siscomex.receita.fazenda.gov.br/siscomexImpweb-7/private_siscomeximpweb_AjaxSubmit.do?listaPerfisOrdenada=IMPORTADOR"),
//                HttpMethod.POST
//        );
//    }

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

//    private static void extractCaptchaFromCustomsTablesSystemPage() {
//                    String captchaBase64StringUrl = page
//                    .executeJavaScript("document.getElementById(\"img_captcha_serpro_gov_br\").")
//                    .getJavaScriptResult()
//                    .toString();
//                    .getElementById("img_captcha_serpro_gov_br")
//                    .getAttribute("src");

//            logger.info("CAPTCHA: " + captchaBase64StringUrl);
//
//            decodeFromBase64ImageAndSave(captchaBase64StringUrl);
//
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
//            System.out.print("Digite o CAPTCHA para prosseguir:  ");
//            String decodedCaptcha = bufferedReader.readLine();
//
//            logger.info("CAPTCHA: " + decodedCaptcha);
//    }

    private static WebClient configureWebClient()  {
        final WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
        try {
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

    private static void decodeFromBase64ImageAndSave(String imageBase64String) {
        String[] strings = imageBase64String.split(",");
        String extension;
        switch (strings[0]) {
            case "data:image/jpeg;base64":
                extension = "jpeg";
                break;
            case "data:image/png;base64":
                extension = "png";
                break;
            default:
                extension = "jpg";
                break;
        }

        long identifier = Math.round((Math.random() * 999));

        byte[] data = Base64.getDecoder().decode(imageBase64String);
        String path = System.getProperty("user.dir")
                + "/src/main/resources/captcha/captcha-" + identifier + "." + extension;
        File file = new File(path);

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            outputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
