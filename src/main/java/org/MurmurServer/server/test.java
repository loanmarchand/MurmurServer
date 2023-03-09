package org.MurmurServer.server;

import org.MurmurServer.model.Protocol;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {

    private static final Protocol protocol = new Protocol();

    public static void main(String[] args){
        Pattern pattern = Pattern.compile(protocol.getRxConfirm());
        Matcher matcher = pattern.matcher("CONFIRM adadzaadaz551541zgrg");
        if (matcher.find()) {
            String matchedText = matcher.group(1);
            System.out.println("Matched text:" + matchedText);
        }
    }



}
