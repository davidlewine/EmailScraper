import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Example program to list links from a URL.
 */
public class EmailScraper2 {

    Set<String> visited = new HashSet();
    HashMap<String, String> emails = new HashMap();
    LinkedList<String[]> pageQueue = new LinkedList();

    static String domainName;
    int pagesSearched = 0;
    Pattern emailPattern;
    Pattern urlStartPattern;
    Pattern urlEndPattern;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        EmailScraper2 scraper = new EmailScraper2(args[0]);
        scraper.getEmails();
        scraper.printEmails();

    }

    public EmailScraper2(String dn) {
        domainName = dn;
        String[] firstPage = new String[2];
        firstPage[0] = "http://www." + domainName;
        firstPage[1] = "html";
        pageQueue.add(firstPage);
        visited.add(firstPage[0]);

        emailPattern = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
        urlStartPattern = Pattern.compile("https*://[^/]*" + domainName + "\\b.*");
        urlEndPattern = Pattern.compile(".*\\.[^c].{2}");
        
        
    }

    private void getEmails() throws IOException, NoSuchAlgorithmException, KeyManagementException {
        enableSSLSocket();
        while (pageQueue.size() > 0) {
            String[] url = pageQueue.poll();
            //System.out.println("url " + url[1] +": " + url[0] );
            if (url[1].equals("html")) {
                processUrlAsHtml(url[0]);
            } else if (url[1].equals("script")) {
                processUrlAsScript(url[0]);
            }
        }
    }

    private void processUrlAsScript(String url) throws IOException {
        //Gets script page as text and find new emails.  
        //This doesn't try to get links to other pages from a script.

        try {
            URL urlOb = new URL(url);
            BufferedReader in = new BufferedReader(new InputStreamReader(urlOb.openStream()));
            String line;
            StringBuilder docBuilder = new StringBuilder();
            while ((line = in.readLine()) != null) {
                docBuilder.append(line);
            }
            in.close();
            String docString = docBuilder.toString();

            getNewEmails(docString, url);

        } catch (Exception e) {
            //System.out.println("Script Exception from" + url + "; " + e.getMessage());
        }
    }

    private void processUrlAsHtml(String url) throws IOException {
        //Gets html page as Jsoup Document.
        //Finds new emails and gets links to new pages.

        try {
            Response resp = Jsoup.connect(url).timeout(50 * 1000).execute();
            String contentType = resp.contentType();
            if (contentType.startsWith("text/") || contentType.startsWith("application/xml") || contentType.startsWith("application/xhtml+xml")) {
                Document doc = Jsoup.connect(url).get();
                getNewEmails(doc.toString(), url);
                getNewPages(doc);
            }
        } catch (Exception e) {
            //System.out.println("html Exception from" + url + "; " + e.getMessage());
        }
    }

    private void getNewEmails(String doc, String url) {
        //find new emails and add to emails hashmap
        Matcher m = emailPattern.matcher(doc);
        while (m.find()) {
            String emailString = m.group();
            //System.out.println("email: " + emailString + ", " + url);
            if (!emails.containsKey(emailString)) {

                System.out.println("***** email: " + emailString + ", " + url);
                emails.put(emailString, "");
            }
        }
    }

    private void getNewPages(Document doc) {
        //get new html links and add to page queue
        Elements pageLinks = doc.select("[href]");
        for (Element link : pageLinks) {
            String linkString = link.attr("abs:href");
            Matcher linkStart = urlStartPattern.matcher(linkString);
            Matcher linkEnd = urlEndPattern.matcher(linkString);
            if (linkStart.matches() && !linkEnd.matches() && !linkString.contains("calendar")
                    && !linkString.contains("?") && !visited.contains(linkString)) {
                visited.add(linkString);
                pageQueue.add(new String[]{linkString, "html"});
            }
        }
        //get new script links and add to page queue
        Elements scriptElements = doc.getElementsByTag("script");
        Elements scriptLinks = doc.select("script");
        for (Element link : scriptLinks) {
            String linkString = link.attr("abs:src");
            if (!linkString.contains("?") && !visited.contains(linkString)) {
                visited.add(linkString);
                pageQueue.add(new String[]{linkString, "script"});
            }
        }
    }

    private void printEmails() {
        for (String email : emails.keySet()) {
            System.out.println(email);
        }
    }

    public static void enableSSLSocket() throws KeyManagementException, NoSuchAlgorithmException {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
 
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new X509TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }
 
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }
 
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }}, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
    }
 

}
