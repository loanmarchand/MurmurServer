package org.model;

/**
 *
 */
public class Protocol {
    public final String RX_DIGIT = "[0-9]";
    public final String RX_LETTER = "[a-zA-Z]";
    public final String RX_LETTER_DIGIT = RX_LETTER + "|" + RX_DIGIT;
    public final String RX_SYMBOL = "[\\x21-\\x2f]|[\\x3a-\\x40]|[\\x5B-\\x60]";
    public final String RX_CRLF = "(\\\\x0d\\\\x0a){0,1}";
    public final String RX_ROUND = "(" + RX_DIGIT + "{2})";
    public final String RX_RANDOM = "((" + RX_LETTER_DIGIT + "|" + RX_SYMBOL + "){22})";
    public final String RX_ESP = " ";
    public final String RX_DOMAIN = "((" + RX_LETTER_DIGIT + "|\\.){5,200})";
    public final String RX_USERNAME = "((" + RX_LETTER_DIGIT + "){5,20})";
    public final String RX_USER_DOMAIN = "(" + RX_USERNAME + "@" + RX_DOMAIN + ")";
    public final String RX_HELLO = "HELLO" + RX_ESP + RX_DOMAIN + RX_ESP + RX_RANDOM + RX_CRLF;
    public final String RX_SALT = "((\\$2b\\$(14)\\$)|\\$2y\\$14\\$)" + RX_RANDOM;
    public final String RX_HASH = "((" + RX_LETTER_DIGIT + "|" + RX_SYMBOL + "){0,200})";
    private final String RX_REGISTER = "REGISTER" + RX_ESP + RX_USERNAME + RX_ESP + RX_ROUND + RX_ESP + RX_SALT + RX_HASH + RX_CRLF;
    public final String PARAM_MSG = "PARAM <round> <bcryptsel>\r\n";
    public final String RX_CONNECT = "CONNECT" + RX_ESP + RX_USERNAME + RX_CRLF;
    public final String RX_CONFIRM = "CONFIRM" + RX_ESP + "([0-9a-fA-F]{64}$)" + RX_CRLF;
    private final String RX_TAG_DOMAIN = "(^#\\w+@[\\w\\.]+$)";
    private final String RX_FOLLOW = "^FOLLOW\\s+(#?\\w+@[\\w\\.]+)$" + RX_CRLF;
    private final String RX_MSG = "^MSG\\s+(.*)$" + RX_CRLF;
    private final String MSGS = "MSGS <user> <message>\r\n";

    /**
     * Créer un message en remplacent les champs <user> et <message> par les valeurs passées en paramètres.
     *
     * @param utilisateur
     * @param message
     * @return
     */
    public String createMessage(String utilisateur, String message) {
        return MSGS.replace("<user>", utilisateur).replace("<message>", message);
    }

    private final String HELLO_MSG = "HELLO <domain> <random>\r\n";

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

    public String buildParamMessage(int round, String sel) {
        return PARAM_MSG.replace("<round>", Integer.toString(round)).replace("<bcryptsel>", sel);
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

    public boolean matchesWithServDomain(String group, String currentDomain) {
        return group.split("@")[1].equals(currentDomain);
    }

    public String getRxMessage() {
        return RX_MSG;
    }
}
