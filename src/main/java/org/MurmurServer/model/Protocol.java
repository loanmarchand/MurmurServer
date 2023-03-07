package org.MurmurServer.model;

public class Protocol {

    public static final String RX_DIGIT = "[0-9]";
    public static final String RX_LETTER = "[a-zA-Z]";
    public static final String RX_LETTER_DIGIT = RX_LETTER + "|" + RX_DIGIT;
    public static final String RX_SYMBOL = "[\\x21-\\x2f]|[\\x3a-\\x40]|[\\x5B-\\x60]";
    public static final String RX_CRLF = "(\\\\x0d\\\\x0a){0,1}";
    public static final String RX_ROUND = "(" + RX_DIGIT + "{2})";
    public static final String RX_PASSCHAR = "[\\x22-\\xff]";
    public static final String RX_VISIBLE_CHARACTER = "[\\x20-\\xff]";
    public static final String RX_INFORMATION_MESSAGE = "((" + RX_VISIBLE_CHARACTER + "){0,200})";
    public static final String RX_RANDOM = "((" + RX_LETTER_DIGIT + "|" + RX_SYMBOL + "){22})";
    public static final String RX_BCRYPT_SALT = "((" + RX_LETTER_DIGIT + "|" + RX_SYMBOL + "){22})";
    public static final String RX_ESP = " ";
    public static final String RX_DOMAIN = "((" + RX_LETTER_DIGIT + "|\\.){5,200})";
    public static final String RX_USERNAME = "((" + RX_LETTER_DIGIT +"){5,20})";
    public static final String RX_USER_DOMAIN = "(" + RX_USERNAME + "@" + RX_DOMAIN + ")";
    public static final String RX_MESSAGE = "((" + RX_VISIBLE_CHARACTER + "){1,250})";

    public static final String RX_HELLO = "HELLO" + RX_ESP + RX_DOMAIN + RX_ESP + RX_RANDOM + RX_CRLF;
    public static final String RX_PARAM = "PARAM" + RX_ESP + RX_ROUND + RX_ESP + RX_BCRYPT_SALT + RX_CRLF;
    public static final String RX_MSGS = "MSGS" + RX_ESP + RX_USER_DOMAIN + RX_ESP + RX_MESSAGE + RX_CRLF ;
    public static final String RX_OK = "\\+OK" + RX_INFORMATION_MESSAGE + RX_CRLF;
    public static final String RX_ERR = "-ERR" + RX_INFORMATION_MESSAGE + RX_CRLF;
    public static final String RX_SALT = "((\\$2b\\$(14)\\$)|\\$2y\\$14\\$)" + RX_RANDOM;
    public static final String RX_HASH = "((" + RX_LETTER_DIGIT + "|" + RX_SYMBOL + "){0,200})";
    private static final String RX_REGISTER  = "REGISTER" + RX_ESP + RX_USERNAME + RX_ESP + RX_ROUND + RX_ESP + RX_SALT + RX_HASH + RX_CRLF;
    public static final String CONFIRM_MSG = "CONFIRM <sha3hexstring>\r\n";
    public static final String REGISTER_MSG = "REGISTER <username> <salt_length> <bcrypt_hash>\r\n";
    public static final String MSG_MSG = "MSG <message>\r\n";
    public static final String FOLLOW_MSG = "FOLLOW <follow>\r\n";

    public static final String CONNECT_MSG = "CONNECT <username>\r\n";
    public static final String PARAM_MSG = "PARAM <round> <bcryptsel>\r\n";
    public static final String RX_CONNECT = "CONNECT" + RX_ESP + RX_USERNAME + RX_CRLF;
    public static final String RX_CONFIRM = "CONFIRM" + RX_ESP + "([0-9a-fA-F]{64}$)" + RX_CRLF;
    public static final String RX_DISCONNECT = "DISCONNECT\r\n";
    public static final String DISCONNECT_MSG = "DISCONNECT\r\n";
    public static final String[] ALL_MESSAGES = {RX_HELLO,RX_PARAM,RX_OK,RX_ERR,RX_MSGS};
    public static final int PARSE_UNKNOW = -1;
    public static final int PARSE_HELLO = 0;
    public static final int PARSE_PARAM = 1;
    public static final int PARSE_OK = 2;
    public static final int PARSE_ERR = 3;
    public static final int PARSE_MSGS = 4;
    private static final String RX_TAG = "(#(" + RX_LETTER_DIGIT + "){1,20})";
    private static final String RX_TAG_DOMAIN = "(^#\\w+@[\\w\\.]+$)";
    private static final String RX_FOLLOW = "^FOLLOW\\s+(#?\\w+@[\\w\\.]+)$" + RX_CRLF;
    private static final String RX_MSG = "^MSG\\s+(.*)$" + RX_CRLF;
    private static final String MSGS = "MSGS <user> <message>\r\n";

    public static String createMessage(String s, String group1) {
        return MSGS.replace("<user>", s).replace("<message>", group1);
    }

    public String build_confirm(String sha3hex){
        return CONFIRM_MSG.replace("<sha3hexstring>",sha3hex);
    }
    public String build_msg(String message){
        return MSG_MSG.replace("<message>",message);
    }
    public String build_follow(String follow){
        return FOLLOW_MSG.replace("<follow>",follow);
    }
    public String build_disconnect(){
        return DISCONNECT_MSG;
    }

    public String[] parse_Register(String message){
        if(message.matches(RX_REGISTER)) return  get_elements_from_regex(message);
        return null;
    }

    public String[] get_elements_from_regex(String message){
        return message.split(RX_ESP);
    }

    public int parse(String message,boolean debug){
        for(int i = 0; i < ALL_MESSAGES.length; i++){
            if(message.matches(ALL_MESSAGES[i])){
                if(debug) System.out.println("Message: " + message + " matches " + ALL_MESSAGES[i]);
                return i;
            }
        }
        return PARSE_UNKNOW;
    }

    private static final String HELLO_MSG = "HELLO <domain> <random>\r\n";
    public String build_hello_message(String domain) {
        //generate random string with RX_RANDOM 22 characters
        String random ="";
        for(int i = 0; i < 22; i++){
            int random_int = (int)(Math.random() * 3);
            switch (random_int){
                case 0:
                    random += (char)((int)(Math.random() * 10) + 48);
                    break;
                case 1:
                    random += (char)((int)(Math.random() * 26) + 65);
                    break;
                case 2:
                    random += (char)((int)(Math.random() * 26) + 97);
                    break;
                default:
                    random += (char)((int)(Math.random() * 15) + 33);
                    break;
            }
        }
        return HELLO_MSG.replace("<domain>", domain).replace("<random>", random);

    }

    public String build_param_message(int round, String sel){
        return PARAM_MSG.replace("<round>",Integer.toString(round)).replace("<bcryptsel>",sel);
    }

    public String getRxRegister() {return RX_REGISTER;}

    public String getRxConnect() {return RX_CONNECT;}

    public String getRxConfirm(){return RX_CONFIRM;}

    public String getRxHello(){return RX_HELLO;}

    public String getRxFollow() {
        return RX_FOLLOW;
    }

    public String getRxUserDomain() {
        return RX_USER_DOMAIN;
    }

    public String getRxTagDomain() {
        return RX_TAG_DOMAIN;
    }

    public boolean matchesWithServDomain(String group, String currentDomain) {
        return group.split("@")[1].equals(currentDomain);
    }

    public String getRxMessage() {
        return RX_MSG;
    }
    public String getRxDisconnect(){return RX_DISCONNECT;}
}
