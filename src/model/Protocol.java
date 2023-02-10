package model;

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
    public static final String CONFIRM_MSG = "CONFIRM <sha3hexstring>\r\n";
    public static final String REGISTER_MSG = "REGISTER <username> <salt_length> <bcrypt_hash>\r\n";
    public static final String MSG_MSG = "MSG <message>\r\n";
    public static final String FOLLOW_MSG = "FOLLOW <follow>\r\n";

    public static final String CONNECT_MSG = "CONNECT <username>\r\n";

    public static final String DISCONNECT_MSG = "DISCONNECT\r\n";
    public static final String[] ALL_MESSAGES = {RX_HELLO,RX_PARAM,RX_OK,RX_ERR,RX_MSGS};
    public static final int PARSE_UNKNOW = -1;
    public static final int PARSE_HELLO = 0;
    public static final int PARSE_PARAM = 1;
    public static final int PARSE_OK = 2;
    public static final int PARSE_ERR = 3;
    public static final int PARSE_MSGS = 4;
    public String build_connect(String username){
        return CONNECT_MSG.replace("<username>",username);
    }
    public String build_register(String username,int length, String hash){
        return REGISTER_MSG.replace("<username>",username).replace("<salt_length>",String.valueOf(length)).replace("<bcrypt_hash>",hash);
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
    public String[] parse_Hello(String message){
        if(parse(message,false)== PARSE_HELLO) return get_elements_from_regex(message,RX_HELLO, new int[]{1, 3});
        return null;
    }
    public String[] parse_Param(String message){
        if(parse(message,false)== PARSE_PARAM) return get_elements_from_regex(message,RX_PARAM, new int[]{1, 2});
        return null;
    }
    public String[] parse_Msgs(String message){
        if(parse(message,false)== PARSE_MSGS) return get_elements_from_regex(message,RX_MSGS, new int[]{1, 6});
        return null;
    }

    public String[] get_elements_from_regex(String message,String rx,int[] elements){
        String[] result = new String[elements.length];
        String[] split = message.split(rx);
        for(int i = 0; i < elements.length; i++){
            result[i] = split[elements[i]];
        }
        return result;
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


    public String build_hello_message(String domain) {
        // generate sring with 22 random characters en utilisant regex
        StringBuilder random = new StringBuilder();
        for (int i = 0; i < 22; i++) {
            random.append((char) (Math.random() * 255));
        }
        return RX_HELLO.replace(RX_RANDOM, random.toString()).replace(RX_DOMAIN, domain).replace(RX_CRLF, "\r\n");

    }
}
