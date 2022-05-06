package org.mpack;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RobotHandler {
    HashMap<String,RobotRules> PreVisitedUrls = new HashMap<>();

    boolean ReadRobotFile(URL url)
    {
        //define an object of RobotRules
        RobotRules robotrules = new RobotRules();

        //form the link of robot file
        //protocol://host/robot.txt
        String RobotUrl = url.getProtocol()+"://"+url.getHost()+"/robots.txt";

        //access robot.txt file as html file and parse it using jsoup library
        Document Robothtml;
        try {
            Robothtml = Jsoup.connect(RobotUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        String Robottxt = Robothtml.body().text();

        //read robot rules
        String[] rules = Robottxt.split(" ");
        //i = 0 --> User-Agent
        //i = 1 --> *
        //i = 2 --> start of the rules
        for (int i=2;i<rules.length;i++)
        {
            switch (rules[i]) {
                case "Disallow:" -> {
                    i++;
                    if (i < rules.length)
                        if (!robotrules.addDisallowed(rules[i])) {
                            i--;
                        }
                }
                case "Allow:" -> {
                    i++;
                    if (i < rules.length)
                        if (!robotrules.addAllowed(rules[i])) {
                            i--;
                        }
                }
            }
        }
        PreVisitedUrls.put(url.getHost(), robotrules);
        return true;
    }

    public boolean isDisallowed (URL url)
    {
        if (!PreVisitedUrls.containsKey(url.getHost()))
        {
            ReadRobotFile(url);
        }
        RobotRules robotrules = PreVisitedUrls.get(url.getHost());
        String UrlFile;
        if (url.getQuery() == null)
        {
            UrlFile = url.getPath();
        }
        else
        {
            UrlFile = url.getFile();
        }
        return (robotrules.isDisallowed(UrlFile));
    }

    public static void main(String[] args) {

        URL url = null;
        try {
            url = new URL("https://www.geeksforgeeks.org");
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RobotHandler robot = new RobotHandler();
        if (url != null) {
            robot.ReadRobotFile(url);
        }
    }
}

class RobotRules {
    HashSet <Pattern> disallowed = new HashSet<>();
    HashSet <Pattern> Allowed = new HashSet<>();


    public boolean addDisallowed(String path) {
        // Ignore Directive if path is empty
        if (path.isEmpty() || !path.startsWith("/"))
            return false;
        disallowed.add(createPattern(path));
        return true;
    }

    public boolean addAllowed(String path) {
        // Ignore Directive if path is empty
        if (path.isEmpty() || !path.startsWith("/"))
            return false;
        Allowed.add(createPattern(path));
        return true;
    }

    public boolean isAllowed(String path) {
        if (!this.isDisallowed(path)) {
            return true;
        }
        for (Pattern p : Allowed) {
            Matcher matcher = p.matcher(path);
            if (matcher.find())
                return true;
        }
        return false;
    }

    public Pattern createPattern(String path) {
        // * in robots.txt --> zero or more character
        // .*in regex --> zero or more character
        // note to escape especial characters
        return Pattern.compile(path.replace("*", ".*").replace("?", "\\?").replace("+", "\\+"));
    }

    public boolean isDisallowed(String path) {
        for (Pattern p : disallowed) {
            Matcher matcher = p.matcher(path);
            if (matcher.find())
                return true;
        }
        return false;
    }

}