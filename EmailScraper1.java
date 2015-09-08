import java.io.IOException;
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
public class EmailScraper1 {

    Set<String> visited = new HashSet();
    HashMap<String, String> emails = new HashMap();
    LinkedList<String> pageQueue = new LinkedList();

    static String domainName;
    int pagesSearched = 0;
    Pattern emailPattern;
    Pattern urlPattern;

    public static void main(String[] args) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        EmailScraper1 scraper = new EmailScraper1(args[0]);
        scraper.getEmails();
        scraper.printEmails();
    }

    public EmailScraper1(String dn) {
        //initialize by adding domain_name.com to page queue and list of visited pages
        //and setting regex search patterns for email addresses and page url's.
        
        domainName = dn;
        String firstPage = "http://www." + domainName;
        pageQueue.add(firstPage);
        visited.add(firstPage);
        //
        emailPattern = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
        urlPattern = Pattern.compile("https*://[^/]*" + domainName + "\\b.*");
    }

    private void getEmails() throws IOException, KeyManagementException, NoSuchAlgorithmException {
        enableSSLSocket();
        while (pageQueue.size() > 0) {
            processPage(pageQueue.poll());
        }
    }

    private void processPage(String url) throws IOException {
        try {
                Document doc = Jsoup.connect(url).get();
                getNewEmails(doc, url);
                getNewLinks(doc);
        } catch (Exception e) {
            //System.out.println("url: " + url);
            //System.out.println(e);
        }

    }

    private void getNewEmails(Document doc, String url) {
        //find new emails and add to emails hashmap
        //Also print out email and url email came from to see progress
        
        Matcher m = emailPattern.matcher(doc.toString());
        while (m.find()) {
            String emailString = m.group();
            if (!emails.containsKey(emailString)) {
                System.out.println("***** email: " + emailString + "; " + url);
                emails.put(emailString, "");
            }
        }
    }

    private void getNewLinks(Document doc) {
        //find new links and add to page queue
        
        Elements pageLinks = doc.select("[href]");
        for (Element link : pageLinks) {
            String linkString = link.attr("abs:href");
            Matcher linkMatcher = urlPattern.matcher(linkString);
            if (linkMatcher.matches() && !visited.contains(linkString)) {
                pageQueue.add(linkString);
                visited.add(linkString);
            }
        }
    }

    private void printEmails() {
        System.out.println("Finished scraping; emails found: ");
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
