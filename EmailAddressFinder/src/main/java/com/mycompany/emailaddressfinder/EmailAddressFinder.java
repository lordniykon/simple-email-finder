package com.mycompany.emailaddressfinder;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class EmailAddressFinder {

    private Set<String> emailAddresses = new HashSet();

    public static void main(String[] args) throws Exception {
        EmailAddressFinder finder = new EmailAddressFinder();
        finder.findEmailsOnDomain("http://www.iana.org");
    }

    //ties it all together
    public void findEmailsOnDomain(String domainName) throws IOException {
        //assuming the protocol and domain name are valid
        Document doc = Jsoup.connect(domainName).data("query", "Java").userAgent("Mozilla").cookie("auth", "token").timeout(3000).get();
        getEmailAddresses(doc.toString());
        Elements links = getPageLinks(doc);
        for(Element link : links)
        {
            String linkAddress = link.attr("abs:href");
            if(!linkAddress.contains("protocols"))
                getEmailAddresses(Jsoup.connect(linkAddress).data("query", "Java").userAgent("Mozilla").cookie("auth", "token").timeout(3000).get().toString());
        }
        for(String str : emailAddresses)
            System.out.println(str);
    }

    //returns all links on a page
    public Elements getPageLinks(Document doc) throws IOException {
        Elements resultLinks = doc.select("a"); 
       return resultLinks;
    }

    public void getEmailAddresses(String html) {
        Matcher m = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+").matcher(html);
        while (m.find()) {
            String newAddress = m.group();
            if (!emailAddresses.contains(newAddress)) {
                emailAddresses.add(newAddress);
            }
        }
    }
}
