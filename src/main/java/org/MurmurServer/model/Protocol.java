package org.MurmurServer.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *Classe de manipulation de REGEX
 */
public class Protocol {

    //REGEX de base

    public static final String RX_DIGIT = "[0-9]";
    public static final String RX_LETTER = "[a-zA-Z]";
    public static final String RX_LETTER_DIGIT = RX_LETTER + "|" + RX_DIGIT;
    public static final String RX_SYMBOL = "[\\x21-\\x2f]|[\\x3a-\\x40]|[\\x5B-\\x60]";
    public static final String RX_CRLF = "(\\\\x0d\\\\x0a){0,1}";
    public static final String RX_ROUND = "(" + RX_DIGIT + "{2})";
    public static final String RX_RANDOM = "((" + RX_LETTER_DIGIT + "|" + RX_SYMBOL + "){22})";
    public static final String RX_ESP = " ";
    public static final String RX_DOMAIN = "((" + RX_LETTER_DIGIT + "|\\.){5,200})";
    public static final String RX_USERNAME = "((" + RX_LETTER_DIGIT + "){5,20})";
    public static final String RX_USER_DOMAIN = "(" + RX_USERNAME + "@" + RX_DOMAIN + ")";
    public static final String RX_SALT = "((\\$2b\\$(14)\\$)|\\$2y\\$14\\$)" + RX_RANDOM;
    public static final String RX_HASH = "((" + RX_LETTER_DIGIT + "|" + RX_SYMBOL + "){0,200})";
    private static final String RX_ID_DOMAIN = "(\\d{1,5})";
    private static final String RX_TAG_DOMAIN = "(#\\w+@([\\w\\.]+))";
    private static final String RX_MESSAGE = "((FOLLOW|MSG)\\s+(.*))";
    private static final String RX_PORT = "(\\d{1,5})";


    //REGEX de messages

    public static final String RX_HELLO = "HELLO" + RX_ESP + RX_DOMAIN + RX_ESP + RX_RANDOM + RX_CRLF;
    public static final String RX_CONNECT = "CONNECT" + RX_ESP + RX_USERNAME + RX_CRLF;
    public static final String RX_CONFIRM = "CONFIRM" + RX_ESP + "([0-9a-fA-F]{64}$)" + RX_CRLF;
    private static final String RX_REGISTER = "REGISTER" + RX_ESP + RX_USERNAME + RX_ESP + RX_ROUND + RX_ESP + RX_SALT + RX_HASH + RX_CRLF;
    public static final String RX_DISCONNECT = "DISCONNECT\r\n";
    private static final String RX_ECHO = "ECHO ([\\w\\.]{5,200}) (\\d{1,5})[\\r\\n]";
    private static final String RX_MESSAGE_INTERNE = "(FOLLOW|MSGS)\\s+([a-zA-Z]+)\\s+#?([a-zA-Z0-9]+)(?:@([a-zA-Z0-9.-]+(?:\\.[a-zA-Z]{2,}){1,2}))?\\s*(.*)";
    private static final String RX_SEND = "SEND" + RX_ESP + RX_ID_DOMAIN + RX_ESP + RX_DOMAIN + RX_ESP + "("+RX_DOMAIN+"|"+RX_TAG_DOMAIN+")" + RX_ESP + RX_MESSAGE_INTERNE + RX_CRLF;


    private static final String RX_FOLLOW = "FOLLOW\\s+((#?)\\w+@([\\w\\.]+))" + RX_CRLF;
    private static final String RX_MSG = "^MSG\\s+(.*)$" + RX_CRLF;
    private static final String RX_MSGS = "MSGS"+RX_ESP+"(.*)"+RX_CRLF;


    //REGEX de construction

    private static final String HELLO_MSG = "HELLO <domain> <random>\r\n";
    private static final String MSGS_MSG = "MSGS <user> <message>\r\n";
    public static final String ECHO_MSG = "ECHO <domain> <port>\r\n";
    public static final String PARAM_MSG = "PARAM <round> <bcryptsel>\r\n";
    public static final String FOLLOW_MSG = "FOLLOW <user>@<domain>";

    //"SEND 1234 server1.godswila.guru server2.godswila.guru FOLLOW michel@server2.godswila.guru\r\n";


    /**
     * Créer un message en remplacent les champs <user> et <message> par les valeurs passées en paramètres.
     *
     * @param utilisateur nom de l'utilisateur
     * @param message    message à envoyer
     * @return message formaté
     */
    public String createMessage(String utilisateur, String message) {
        return MSGS_MSG.replace("<user>", utilisateur).replace("<message>", message);
    }

    /**
     * Créer un message HELLO en remplacent les champs <domain> par le nom de domaine fourni et <random> par une chaine de charactères aléatoires
     * @param domain domaine
     * @return string
     */
    public String buildHelloMessage(String domain) {
        // generate random string with RX_RANDOM 22 characters
        StringBuilder random = new StringBuilder();
        for (int i = 0; i < 22; i++) {
            int random_int = (int) (Math.random() * 3);
            switch (random_int) {
                case 0:
                    random.append((char) ((int) (Math.random() * 10) + 48));
                    break;
                case 1:
                    random.append((char) ((int) (Math.random() * 26) + 65));
                    break;
                case 2:
                    random.append((char) ((int) (Math.random() * 26) + 97));
                    break;
                default:
                    random.append((char) ((int) (Math.random() * 15) + 33));
                    break;
            }
        }
        return HELLO_MSG.replace("<domain>", domain).replace("<random>", random.toString());
    }

    /**
     * Construis le message PARAM sur base du round fourni, et du sel fourni (remplace <round> et <bcryptsel>
     * @param round round
     * @param sel sel
     * @return string
     */
    public String buildParamMessage(int round, String sel) {
        return PARAM_MSG.replace("<round>", Integer.toString(round)).replace("<bcryptsel>", sel);
    }

    /**
     * Construis le message ECHO sur base du domaine fourni, et du port fourni (remplace <domain> et <port>
     * @param currentDomain domaine actuel
     * @param defaultPort port par defaut
     * @return string
     */
    public String build_echo(String currentDomain, int defaultPort) {
        return ECHO_MSG.replace("<domain>", currentDomain).replace("<port>", Integer.toString(defaultPort));
    }

    /**
     * Vérifie si le domaine fourni dans le premier string est le même que celui fourni dans le second
     * @param group groupedText
     * @param currentDomain domaine actuel
     * @return TRUE|FALSE
     */
    public boolean matchesWithServDomain(String group, String currentDomain) {
        String testingDomain;
        Pattern pattern = Pattern.compile(RX_USER_DOMAIN);
        Matcher matcher = pattern.matcher(group);
        if (matcher.find()) {
            testingDomain = matcher.group(4);
            return testingDomain.equals(currentDomain);
        }else{
            return false;
        }
    }

    /**
     * Récupère le domain sur base du message envoyé
     * @param message msg
     * @return null si le message envoyé n'est pas celui attendu
     */
    public String getDestinationDomainFromMessage(String message) {
        String text;
        Pattern pattern = Pattern.compile(RX_SEND);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            text = matcher.group(5);
            if (text == null) {
                text = matcher.group(8);
            }
            if (text == null) {
                text = matcher.group(4);
            }
            return text;
        }
        return null;
    }

    public String buildFollow(String user, String domain) {
        return FOLLOW_MSG.replace("<user>", user).replace("<domain>", domain);
    }



    public String getRxRegister() {
        return RX_REGISTER;
    }

    public String getRxConnect() {
        return RX_CONNECT;
    }

    public String getRxConfirm() {
        return RX_CONFIRM;
    }

    public String getRxHello() {
        return RX_HELLO;
    }

    public String getRxFollow() {
        return RX_FOLLOW;
    }

    public String getRxUserDomain() {
        return RX_USER_DOMAIN;
    }

    public String getRxTagDomain() {
        return RX_TAG_DOMAIN;
    }

    public String getRxMessage() {
        return RX_MSG;
    }

    public String getRxDisconnect() {
        return RX_DISCONNECT;
    }

    public String getRxEcho() {
        return RX_ECHO;
    }

    public String getRxSend() { return RX_SEND; }

    public String geRxMsgs() {
        return RX_MSGS;
    }

    public String getUSernameFromUserDomain(String userDomain) {
        String text;
        Pattern pattern = Pattern.compile(RX_USER_DOMAIN);
        Matcher matcher = pattern.matcher(userDomain);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    public String getMessageIntern() {
        return RX_MESSAGE_INTERNE;
    }

    public boolean matchesWithServDomainTAG(String ligne, String currentDomain) {
        return false;
    }

    public String getDomainFromUserDomain(String user) {
        Pattern pattern = Pattern.compile(RX_USER_DOMAIN);
        Matcher matcher = pattern.matcher(user);
        if (matcher.find()) {
            return matcher.group(4);
        }
        return null;
    }
}
