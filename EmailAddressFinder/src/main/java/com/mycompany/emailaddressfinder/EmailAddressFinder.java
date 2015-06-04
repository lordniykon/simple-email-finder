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

    private final Set<String> emailAddresses = new HashSet();
    private final String domainName;
    private final String protocol;

    public static void main(String[] args) throws Exception {
        EmailAddressFinder finder = new EmailAddressFinder(args[0]);
        boolean success = finder.findEmailsOnDomain();
        if (!success) {
            System.exit(1);
        }
    }

    EmailAddressFinder(String domain) {
        String[] parts = domain.split("//");
        //separate the protocol from the domain
        if (parts.length > 1) {
            protocol = parts[0];
            domainName = parts[1].replace("www.", "");
        } else {
            domainName = domain.replace("www.", "");
            //defaults to http
            protocol = "http:";
        }
    }

    //ties it all together
    //does not work if the protocol or the domain are invalid
    //does not look through links in pages more than one level deep because that might cause an endless loop or might cause going through the entire internet - kept it simple
    public boolean findEmailsOnDomain() {
        //assuming the protocol and domain name are valid
        boolean flag = true;
        Document doc = null;
        try {
            doc = Jsoup.connect(getFullAddress()).data("query", "Java").userAgent("Mozilla").cookie("auth", "token").timeout(3000).get();
        } catch (Exception e) {
            System.out.println("An error occurred while getting e-mails from page '" + getFullAddress() + "':" + e.getMessage());
            //e.printStackTrace();
            flag = false;
        }
        if(doc != null)
        {
            getEmailAddresses(doc.toString());
            Elements links = getPageLinks(doc);
            for (Element link : links) {
                String linkAddress = link.attr("abs:href");
                try {
                    getEmailAddresses(Jsoup.connect(linkAddress).data("query", "Java").userAgent("Mozilla").cookie("auth", "token").timeout(3000).get().toString());
                } catch (IOException e) {
                    System.out.println("An error occurred while getting e-mails from page '" + linkAddress + "':" + e.getMessage());
                    //e.printStackTrace();
                    flag = false;
                }
            }
            showOutput();
        }
        return flag;
    }

    //returns all links on a page
    public Elements getPageLinks(Document doc) {
        Elements resultLinks = doc.select("a");
        return resultLinks;
    }

    //finds email addreses from an html string and adds them to a set
    public void getEmailAddresses(String html) {
        Matcher m = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+").matcher(html);
        while (m.find()) {
            String newAddress = m.group();
            try {
                //make sure I am getting distinct e-mail addresses
                if (!emailAddresses.contains(newAddress)) {
                    //not sure if we need all addresses that need that use that domain name - if we do, I need to uncomment the next 4 commented lines
                    //if (newAddress.length() > domainName.length()) {
                    //    if (newAddress.substring(newAddress.length() - domainName.length()).equals(domainName)) {
                    emailAddresses.add(newAddress);
                    //    }
                    //}
                }
            } catch (Exception e) {
                System.out.println("An error occured while parsing e-mail address '" + newAddress + "': " + e.getMessage());
                //e.printStackTrace();
            }
        }
    }
    
    //simply gets the full link to the original page provided by the user
    private String getFullAddress() {
        return protocol + "//www." + domainName;
    }
    
    //shows the user the result of the search
    private void showOutput()
    {
        if(emailAddresses.size() > 0)
        {
            System.out.println("LIST OF EMAIL ADDRESSES ON '" + getFullAddress() + "':");
            for (String str : emailAddresses) {
                System.out.println(str);
            }
        }
        else
            System.out.println("WE COULDN'T FIND ANY EMAIL ADDRESSES ON '" + getFullAddress() + "':");
    }
}
