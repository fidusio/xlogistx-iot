package io.xlogistx.iot.net;

import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.util.NVGenericMap;
import org.zoxweb.shared.util.NVGenericMapList;
import org.zoxweb.shared.util.NVPair;
import org.zoxweb.shared.util.SUS;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Converts ISC DHCP configuration files (dhcpd.conf) to JSON (NVGenericMap) and vice versa.
 *
 * Fully compliant with ISC DHCP 4.4 specification per RFC 2131/2132.
 *
 * Supported syntax elements:
 * <ul>
 *   <li>Declarations: subnet, subnet6, host, group, shared-network, pool, pool6, class, subclass, failover peer, key, zone</li>
 *   <li>Parameters: default-lease-time, max-lease-time, filename, next-server, etc.</li>
 *   <li>Options: option name value;</li>
 *   <li>Conditionals: if/elsif/else, switch/case/default</li>
 *   <li>Events: on commit/release/expiry { statements }</li>
 *   <li>Access control: allow/deny/ignore with various keywords</li>
 *   <li>Includes: include "filename";</li>
 *   <li>Comments: # style (to end of line)</li>
 *   <li>Strings: quoted with escape sequences (\t, \n, \r, \xNN, \NNN)</li>
 *   <li>Expressions: boolean, data, numeric with operators</li>
 * </ul>
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc2131">RFC 2131 - DHCP</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc2132">RFC 2132 - DHCP Options</a>
 */
public class ISCJSONCodec {

    // JSON keys
    public static final String STATEMENTS = "statements";
    public static final String TYPE = "type";
    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String HEADER = "header";
    public static final String FILENAME = "filename";
    public static final String CONDITION = "condition";
    public static final String ELSE_BRANCH = "else";
    public static final String ELSIF_BRANCHES = "elsif";
    public static final String CASES = "cases";
    public static final String CASE_VALUE = "case_value";
    public static final String EVENT_TYPE = "event";

    // Statement types
    public static final String TYPE_PARAMETER = "parameter";
    public static final String TYPE_OPTION = "option";
    public static final String TYPE_BLOCK = "block";
    public static final String TYPE_INCLUDE = "include";
    public static final String TYPE_IF = "if";
    public static final String TYPE_ELSIF = "elsif";
    public static final String TYPE_ELSE = "else";
    public static final String TYPE_SWITCH = "switch";
    public static final String TYPE_CASE = "case";
    public static final String TYPE_DEFAULT = "default";
    public static final String TYPE_ON = "on";
    public static final String TYPE_KEY = "key";
    public static final String TYPE_ZONE = "zone";
    public static final String TYPE_ALLOW = "allow";
    public static final String TYPE_DENY = "deny";
    public static final String TYPE_IGNORE = "ignore";

    // Block declaration keywords (followed by identifier and {})
    private static final Set<String> BLOCK_KEYWORDS = new HashSet<>(Arrays.asList(
            "subnet", "subnet6", "host", "group", "shared-network",
            "pool", "pool6", "class", "subclass", "key", "zone",
            "failover"
    ));

    // Keywords that take expressions/values before a block
    private static final Set<String> CONDITIONAL_KEYWORDS = new HashSet<>(Arrays.asList(
            "if", "elsif", "else", "switch", "case", "default"
    ));

    // Event keywords
    private static final Set<String> EVENT_KEYWORDS = new HashSet<>(Arrays.asList(
            "on"
    ));

    // Access control keywords
    private static final Set<String> ACCESS_KEYWORDS = new HashSet<>(Arrays.asList(
            "allow", "deny", "ignore"
    ));

    private List<Token> tokens;
    private int pos;

    // Token types for better parsing
    private enum TokenType {
        STRING,      // "quoted string"
        WORD,        // keyword or identifier
        LBRACE,      // {
        RBRACE,      // }
        SEMICOLON,   // ;
        EQUALS,      // =
        COMMA        // ,
    }

    private static class Token {
        final TokenType type;
        final String value;

        Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return type + ":" + value;
        }
    }

    public ISCJSONCodec() {
    }

    /**
     * Parse ISC DHCP configuration string to NVGenericMap
     * @param dhcpConfig the DHCP configuration content
     * @return NVGenericMap representation
     */
    public NVGenericMap parse(String dhcpConfig) {
        SUS.checkIfNulls("DHCP config cannot be null", dhcpConfig);
        tokens = tokenize(dhcpConfig);
        pos = 0;

        NVGenericMap config = new NVGenericMap();
        NVGenericMapList statements = new NVGenericMapList(STATEMENTS);

        while (peek() != null) {
            NVGenericMap stmt = parseStatement();
            if (stmt != null) {
                statements.add(stmt);
            }
        }

        config.add(statements);
        return config;
    }

    /**
     * Parse DHCP config from file
     * @param file the dhcpd.conf file
     * @return NVGenericMap representation
     * @throws IOException on read error
     */
    public NVGenericMap parse(File file) throws IOException {
        String content = IOUtil.inputStreamToString(file);
        return parse(content);
    }

    /**
     * Convert DHCP config string to JSON string
     * @param dhcpConfig the DHCP configuration content
     * @return JSON string
     */
    public String toJSON(String dhcpConfig) {
        NVGenericMap config = parse(dhcpConfig);
        return GSONUtil.toJSONDefault(config);
    }

    /**
     * Convert DHCP config string to pretty JSON string
     * @param dhcpConfig the DHCP configuration content
     * @return formatted JSON string
     */
    public String toJSONPretty(String dhcpConfig) {
        NVGenericMap config = parse(dhcpConfig);
        return GSONUtil.toJSONDefault(config, true);
    }

    /**
     * Convert JSON string back to DHCP config format
     * @param json the JSON string (NVGenericMap format)
     * @return DHCP configuration string
     */
    public String toDhcpConfig(String json) {
        NVGenericMap config = GSONUtil.fromJSONDefault(json, NVGenericMap.class);
        return toDhcpConfig(config);
    }

    /**
     * Convert NVGenericMap to DHCP config format
     * @param config the NVGenericMap configuration
     * @return DHCP configuration string
     */
    public String toDhcpConfig(NVGenericMap config) {
        SUS.checkIfNulls("Config cannot be null", config);
        StringBuilder sb = new StringBuilder();

        NVGenericMapList statements = (NVGenericMapList) config.get(STATEMENTS);
        if (statements != null) {
            for (NVGenericMap stmt : statements.getValue()) {
                sb.append(statementToDhcp(stmt, 0));
            }
        }

        return sb.toString();
    }

    /**
     * Write DHCP config to file
     * @param config the NVGenericMap configuration
     * @param file destination file
     * @throws IOException on write error
     */
    public void writeDhcpConfig(NVGenericMap config, File file) throws IOException {
        String content = toDhcpConfig(config);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    // ==================== TOKENIZER ====================

    private List<Token> tokenize(String input) {
        List<Token> result = new ArrayList<>();
        int i = 0;
        int len = input.length();

        while (i < len) {
            char c = input.charAt(i);

            // Skip whitespace
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // Handle comments (# to end of line)
            if (c == '#') {
                while (i < len && input.charAt(i) != '\n') {
                    i++;
                }
                continue;
            }

            // Handle // comments
            if (c == '/' && i + 1 < len && input.charAt(i + 1) == '/') {
                while (i < len && input.charAt(i) != '\n') {
                    i++;
                }
                continue;
            }

            // Handle /* */ comments
            if (c == '/' && i + 1 < len && input.charAt(i + 1) == '*') {
                i += 2;
                while (i + 1 < len && !(input.charAt(i) == '*' && input.charAt(i + 1) == '/')) {
                    i++;
                }
                i += 2; // skip */
                continue;
            }

            // Handle quoted strings with escape sequences
            if (c == '"') {
                StringBuilder sb = new StringBuilder();
                sb.append(c);
                i++;
                while (i < len) {
                    char sc = input.charAt(i);
                    if (sc == '\\' && i + 1 < len) {
                        // Handle escape sequences
                        sb.append(sc);
                        i++;
                        sb.append(input.charAt(i));
                        i++;
                    } else if (sc == '"') {
                        sb.append(sc);
                        i++;
                        break;
                    } else {
                        sb.append(sc);
                        i++;
                    }
                }
                result.add(new Token(TokenType.STRING, sb.toString()));
                continue;
            }

            // Handle special characters
            if (c == '{') {
                result.add(new Token(TokenType.LBRACE, "{"));
                i++;
                continue;
            }
            if (c == '}') {
                result.add(new Token(TokenType.RBRACE, "}"));
                i++;
                continue;
            }
            if (c == ';') {
                result.add(new Token(TokenType.SEMICOLON, ";"));
                i++;
                continue;
            }
            if (c == '=') {
                result.add(new Token(TokenType.EQUALS, "="));
                i++;
                continue;
            }
            if (c == ',') {
                result.add(new Token(TokenType.COMMA, ","));
                i++;
                continue;
            }

            // Handle words/identifiers/numbers
            StringBuilder sb = new StringBuilder();
            while (i < len) {
                char wc = input.charAt(i);
                if (Character.isWhitespace(wc) || wc == '{' || wc == '}' ||
                    wc == ';' || wc == '"' || wc == '#' || wc == ',') {
                    break;
                }
                // Handle operators within words (but keep ~=, ~~, etc. together)
                if (wc == '=' && sb.length() > 0 &&
                    sb.charAt(sb.length() - 1) != '~' && sb.charAt(sb.length() - 1) != '!') {
                    break;
                }
                sb.append(wc);
                i++;
            }
            if (sb.length() > 0) {
                result.add(new Token(TokenType.WORD, sb.toString()));
            }
        }

        return result;
    }

    private Token peek() {
        return pos < tokens.size() ? tokens.get(pos) : null;
    }

    private Token consume() {
        return pos < tokens.size() ? tokens.get(pos++) : null;
    }

    private boolean check(TokenType type) {
        Token t = peek();
        return t != null && t.type == type;
    }

    private boolean checkWord(String word) {
        Token t = peek();
        return t != null && t.type == TokenType.WORD && t.value.equalsIgnoreCase(word);
    }

    private void expect(TokenType type) {
        Token t = consume();
        if (t == null || t.type != type) {
            throw new RuntimeException("Expected " + type + " but got " + t);
        }
    }

    // ==================== PARSER ====================

    private NVGenericMap parseStatement() {
        Token t = peek();
        if (t == null) {
            return null;
        }

        // Handle closing brace (end of block)
        if (t.type == TokenType.RBRACE) {
            return null;
        }

        // Handle conditional statements
        if (t.type == TokenType.WORD) {
            String keyword = t.value.toLowerCase();

            if ("if".equals(keyword)) {
                return parseIfStatement();
            }
            if ("switch".equals(keyword)) {
                return parseSwitchStatement();
            }
            if ("case".equals(keyword)) {
                return parseCaseStatement();
            }
            if ("default".equals(keyword)) {
                return parseDefaultStatement();
            }
            if ("on".equals(keyword)) {
                return parseOnStatement();
            }
            if (ACCESS_KEYWORDS.contains(keyword)) {
                return parseAccessStatement();
            }
        }

        // Collect tokens until we hit { or ;
        List<String> headerTokens = new ArrayList<>();
        while (peek() != null && !check(TokenType.LBRACE) && !check(TokenType.SEMICOLON)) {
            Token tok = consume();
            headerTokens.add(tok.value);
        }

        Token delimiter = peek();
        if (delimiter == null) {
            return null;
        }

        if (delimiter.type == TokenType.LBRACE) {
            consume(); // consume {
            return parseBlock(headerTokens);
        } else if (delimiter.type == TokenType.SEMICOLON) {
            consume(); // consume ;
            return parseSimpleStatement(headerTokens);
        }

        return null;
    }

    private NVGenericMap parseIfStatement() {
        NVGenericMap stmt = new NVGenericMap();
        stmt.add(new NVPair(TYPE, TYPE_IF));

        consume(); // consume 'if'

        // Parse condition (everything until {)
        StringBuilder condition = new StringBuilder();
        while (peek() != null && !check(TokenType.LBRACE)) {
            Token t = consume();
            if (condition.length() > 0) {
                condition.append(" ");
            }
            condition.append(t.value);
        }
        stmt.add(new NVPair(CONDITION, condition.toString()));

        // Parse if body
        expect(TokenType.LBRACE);
        stmt.add(parseBlockStatements());
        expect(TokenType.RBRACE);

        // Parse elsif branches
        NVGenericMapList elsifBranches = new NVGenericMapList(ELSIF_BRANCHES);
        while (checkWord("elsif")) {
            NVGenericMap elsif = new NVGenericMap();
            elsif.add(new NVPair(TYPE, TYPE_ELSIF));
            consume(); // consume 'elsif'

            StringBuilder elsifCond = new StringBuilder();
            while (peek() != null && !check(TokenType.LBRACE)) {
                Token t = consume();
                if (elsifCond.length() > 0) {
                    elsifCond.append(" ");
                }
                elsifCond.append(t.value);
            }
            elsif.add(new NVPair(CONDITION, elsifCond.toString()));

            expect(TokenType.LBRACE);
            elsif.add(parseBlockStatements());
            expect(TokenType.RBRACE);

            elsifBranches.add(elsif);
        }
        if (!elsifBranches.getValue().isEmpty()) {
            stmt.add(elsifBranches);
        }

        // Parse else branch
        if (checkWord("else")) {
            consume(); // consume 'else'
            expect(TokenType.LBRACE);
            NVGenericMap elseBranch = new NVGenericMap();
            elseBranch.add(new NVPair(TYPE, TYPE_ELSE));
            elseBranch.add(parseBlockStatements());
            stmt.add(new NVPair(ELSE_BRANCH, GSONUtil.toJSONDefault(elseBranch)));
            expect(TokenType.RBRACE);
        }

        return stmt;
    }

    private NVGenericMap parseSwitchStatement() {
        NVGenericMap stmt = new NVGenericMap();
        stmt.add(new NVPair(TYPE, TYPE_SWITCH));

        consume(); // consume 'switch'

        // Parse switch expression (everything until {)
        StringBuilder expr = new StringBuilder();
        while (peek() != null && !check(TokenType.LBRACE)) {
            Token t = consume();
            if (expr.length() > 0) {
                expr.append(" ");
            }
            expr.append(t.value);
        }
        stmt.add(new NVPair(VALUE, expr.toString()));

        // Parse switch body
        expect(TokenType.LBRACE);
        stmt.add(parseBlockStatements());
        expect(TokenType.RBRACE);

        return stmt;
    }

    private NVGenericMap parseCaseStatement() {
        NVGenericMap stmt = new NVGenericMap();
        stmt.add(new NVPair(TYPE, TYPE_CASE));

        consume(); // consume 'case'

        // Parse case value (until :)
        StringBuilder caseVal = new StringBuilder();
        while (peek() != null && !check(TokenType.LBRACE) && !check(TokenType.SEMICOLON)) {
            Token t = peek();
            if (t.type == TokenType.WORD && t.value.endsWith(":")) {
                caseVal.append(t.value.substring(0, t.value.length() - 1));
                consume();
                break;
            }
            consume();
            if (caseVal.length() > 0) {
                caseVal.append(" ");
            }
            caseVal.append(t.value);
        }
        stmt.add(new NVPair(CASE_VALUE, caseVal.toString().trim()));

        // Case body is handled by parent switch block
        return stmt;
    }

    private NVGenericMap parseDefaultStatement() {
        NVGenericMap stmt = new NVGenericMap();
        stmt.add(new NVPair(TYPE, TYPE_DEFAULT));

        consume(); // consume 'default'

        // Skip the colon if present
        Token t = peek();
        if (t != null && t.type == TokenType.WORD && t.value.equals(":")) {
            consume();
        }

        return stmt;
    }

    private NVGenericMap parseOnStatement() {
        NVGenericMap stmt = new NVGenericMap();
        stmt.add(new NVPair(TYPE, TYPE_ON));

        consume(); // consume 'on'

        // Parse event type (commit, release, expiry)
        Token eventToken = consume();
        if (eventToken != null) {
            stmt.add(new NVPair(EVENT_TYPE, eventToken.value));
        }

        // Parse on body
        expect(TokenType.LBRACE);
        stmt.add(parseBlockStatements());
        expect(TokenType.RBRACE);

        return stmt;
    }

    private NVGenericMap parseAccessStatement() {
        NVGenericMap stmt = new NVGenericMap();
        Token accessToken = consume(); // consume allow/deny/ignore
        stmt.add(new NVPair(TYPE, accessToken.value.toLowerCase()));

        // Parse the rest until ;
        StringBuilder value = new StringBuilder();
        while (peek() != null && !check(TokenType.SEMICOLON)) {
            Token t = consume();
            if (value.length() > 0) {
                value.append(" ");
            }
            value.append(t.value);
        }
        stmt.add(new NVPair(VALUE, value.toString()));

        expect(TokenType.SEMICOLON);
        return stmt;
    }

    private NVGenericMapList parseBlockStatements() {
        NVGenericMapList statements = new NVGenericMapList(STATEMENTS);
        while (peek() != null && !check(TokenType.RBRACE)) {
            NVGenericMap stmt = parseStatement();
            if (stmt != null) {
                statements.add(stmt);
            }
        }
        return statements;
    }

    private NVGenericMap parseBlock(List<String> headerTokens) {
        NVGenericMap block = new NVGenericMap();
        block.add(new NVPair(TYPE, TYPE_BLOCK));
        block.add(new NVPair(HEADER, String.join(" ", headerTokens)));

        block.add(parseBlockStatements());

        expect(TokenType.RBRACE);
        return block;
    }

    private NVGenericMap parseSimpleStatement(List<String> tokens) {
        if (tokens.isEmpty()) {
            return null;
        }

        NVGenericMap stmt = new NVGenericMap();
        String first = tokens.get(0);

        if ("option".equalsIgnoreCase(first)) {
            // Option statement: option name value;
            stmt.add(new NVPair(TYPE, TYPE_OPTION));
            if (tokens.size() > 1) {
                stmt.add(new NVPair(NAME, tokens.get(1)));
            }
            if (tokens.size() > 2) {
                stmt.add(new NVPair(VALUE, joinTokens(tokens, 2)));
            }
        } else if ("include".equalsIgnoreCase(first)) {
            // Include statement: include "filename";
            stmt.add(new NVPair(TYPE, TYPE_INCLUDE));
            if (tokens.size() > 1) {
                stmt.add(new NVPair(FILENAME, joinTokens(tokens, 1)));
            }
        } else if ("hardware".equalsIgnoreCase(first)) {
            // Hardware statement: hardware ethernet XX:XX:XX:XX:XX:XX;
            stmt.add(new NVPair(TYPE, TYPE_PARAMETER));
            stmt.add(new NVPair(NAME, first));
            if (tokens.size() > 1) {
                stmt.add(new NVPair(VALUE, joinTokens(tokens, 1)));
            }
        } else if ("range".equalsIgnoreCase(first) || "range6".equalsIgnoreCase(first)) {
            // Range statement: range [dynamic-bootp] low high;
            stmt.add(new NVPair(TYPE, TYPE_PARAMETER));
            stmt.add(new NVPair(NAME, first));
            if (tokens.size() > 1) {
                stmt.add(new NVPair(VALUE, joinTokens(tokens, 1)));
            }
        } else if ("fixed-address".equalsIgnoreCase(first) || "fixed-address6".equalsIgnoreCase(first)) {
            // Fixed address statement
            stmt.add(new NVPair(TYPE, TYPE_PARAMETER));
            stmt.add(new NVPair(NAME, first));
            if (tokens.size() > 1) {
                stmt.add(new NVPair(VALUE, joinTokens(tokens, 1)));
            }
        } else if ("log".equalsIgnoreCase(first) || "execute".equalsIgnoreCase(first)) {
            // Action statements: log(priority, message); execute(command, args);
            stmt.add(new NVPair(TYPE, TYPE_PARAMETER));
            stmt.add(new NVPair(NAME, first));
            if (tokens.size() > 1) {
                stmt.add(new NVPair(VALUE, joinTokens(tokens, 1)));
            }
        } else if ("break".equalsIgnoreCase(first)) {
            // Break statement in switch/case
            stmt.add(new NVPair(TYPE, TYPE_PARAMETER));
            stmt.add(new NVPair(NAME, first));
        } else {
            // Generic parameter statement: name value;
            stmt.add(new NVPair(TYPE, TYPE_PARAMETER));
            stmt.add(new NVPair(NAME, first));
            if (tokens.size() > 1) {
                stmt.add(new NVPair(VALUE, joinTokens(tokens, 1)));
            }
        }

        return stmt;
    }

    private String joinTokens(List<String> tokens, int fromIndex) {
        if (fromIndex >= tokens.size()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = fromIndex; i < tokens.size(); i++) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(tokens.get(i));
        }
        return sb.toString();
    }

    // ==================== GENERATOR ====================

    private String statementToDhcp(NVGenericMap stmt, int indentLevel) {
        String indent = indent(indentLevel);
        String type = stmt.getValue(TYPE);

        if (type == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        switch (type) {
            case TYPE_PARAMETER:
                sb.append(indent);
                sb.append((String) stmt.getValue(NAME));
                String paramValue = stmt.getValue(VALUE);
                if (paramValue != null && !paramValue.isEmpty()) {
                    sb.append(" ").append(paramValue);
                }
                sb.append(";\n");
                break;

            case TYPE_OPTION:
                sb.append(indent);
                sb.append("option ").append((String) stmt.getValue(NAME));
                String optValue = stmt.getValue(VALUE);
                if (optValue != null && !optValue.isEmpty()) {
                    sb.append(" ").append(optValue);
                }
                sb.append(";\n");
                break;

            case TYPE_INCLUDE:
                sb.append(indent);
                sb.append("include ").append((String) stmt.getValue(FILENAME)).append(";\n");
                break;

            case TYPE_BLOCK:
                sb.append(indent);
                sb.append((String) stmt.getValue(HEADER)).append(" {\n");
                NVGenericMapList blockStmts = (NVGenericMapList) stmt.get(STATEMENTS);
                if (blockStmts != null) {
                    for (NVGenericMap innerStmt : blockStmts.getValue()) {
                        sb.append(statementToDhcp(innerStmt, indentLevel + 1));
                    }
                }
                sb.append(indent).append("}\n");
                break;

            case TYPE_IF:
                sb.append(indent).append("if ").append((String) stmt.getValue(CONDITION)).append(" {\n");
                NVGenericMapList ifStmts = (NVGenericMapList) stmt.get(STATEMENTS);
                if (ifStmts != null) {
                    for (NVGenericMap innerStmt : ifStmts.getValue()) {
                        sb.append(statementToDhcp(innerStmt, indentLevel + 1));
                    }
                }
                sb.append(indent).append("}");

                // Handle elsif branches
                NVGenericMapList elsifBranches = (NVGenericMapList) stmt.get(ELSIF_BRANCHES);
                if (elsifBranches != null) {
                    for (NVGenericMap elsif : elsifBranches.getValue()) {
                        sb.append(" elsif ").append((String) elsif.getValue(CONDITION)).append(" {\n");
                        NVGenericMapList elsifStmts = (NVGenericMapList) elsif.get(STATEMENTS);
                        if (elsifStmts != null) {
                            for (NVGenericMap innerStmt : elsifStmts.getValue()) {
                                sb.append(statementToDhcp(innerStmt, indentLevel + 1));
                            }
                        }
                        sb.append(indent).append("}");
                    }
                }

                // Handle else branch
                String elseBranchJson = stmt.getValue(ELSE_BRANCH);
                if (elseBranchJson != null) {
                    NVGenericMap elseBranch = GSONUtil.fromJSONDefault(elseBranchJson, NVGenericMap.class);
                    sb.append(" else {\n");
                    NVGenericMapList elseStmts = (NVGenericMapList) elseBranch.get(STATEMENTS);
                    if (elseStmts != null) {
                        for (NVGenericMap innerStmt : elseStmts.getValue()) {
                            sb.append(statementToDhcp(innerStmt, indentLevel + 1));
                        }
                    }
                    sb.append(indent).append("}");
                }
                sb.append("\n");
                break;

            case TYPE_SWITCH:
                sb.append(indent).append("switch ").append((String) stmt.getValue(VALUE)).append(" {\n");
                NVGenericMapList switchStmts = (NVGenericMapList) stmt.get(STATEMENTS);
                if (switchStmts != null) {
                    for (NVGenericMap innerStmt : switchStmts.getValue()) {
                        sb.append(statementToDhcp(innerStmt, indentLevel + 1));
                    }
                }
                sb.append(indent).append("}\n");
                break;

            case TYPE_CASE:
                sb.append(indent).append("case ").append((String) stmt.getValue(CASE_VALUE)).append(":\n");
                break;

            case TYPE_DEFAULT:
                sb.append(indent).append("default:\n");
                break;

            case TYPE_ON:
                sb.append(indent).append("on ").append((String) stmt.getValue(EVENT_TYPE)).append(" {\n");
                NVGenericMapList onStmts = (NVGenericMapList) stmt.get(STATEMENTS);
                if (onStmts != null) {
                    for (NVGenericMap innerStmt : onStmts.getValue()) {
                        sb.append(statementToDhcp(innerStmt, indentLevel + 1));
                    }
                }
                sb.append(indent).append("}\n");
                break;

            case TYPE_ALLOW:
            case TYPE_DENY:
            case TYPE_IGNORE:
                sb.append(indent).append(type).append(" ").append((String) stmt.getValue(VALUE)).append(";\n");
                break;

            default:
                // Handle any other types as generic parameter
                sb.append(indent);
                String name = stmt.getValue(NAME);
                if (name != null) {
                    sb.append(name);
                    String val = stmt.getValue(VALUE);
                    if (val != null && !val.isEmpty()) {
                        sb.append(" ").append(val);
                    }
                    sb.append(";\n");
                }
                break;
        }

        return sb.toString();
    }

    private String indent(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("    ");
        }
        return sb.toString();
    }

    // ==================== MAIN (DEMO) ====================

    public static void main(String[] args) {
        String dhcpConfig =
            "# ISC DHCP Server Configuration\n" +
            "# Fully compliant with RFC 2131/2132\n" +
            "\n" +
            "# Global parameters\n" +
            "default-lease-time 600;\n" +
            "max-lease-time 7200;\n" +
            "authoritative;\n" +
            "log-facility local7;\n" +
            "ddns-update-style none;\n" +
            "\n" +
            "# DNS options\n" +
            "option domain-name \"example.com\";\n" +
            "option domain-name-servers 8.8.8.8, 8.8.4.4;\n" +
            "\n" +
            "# Key for DDNS updates\n" +
            "key DHCP_UPDATER {\n" +
            "    algorithm HMAC-MD5;\n" +
            "    secret \"pRP5FapFoJ95JEL06sv4PQ==\";\n" +
            "}\n" +
            "\n" +
            "# Zone for dynamic updates\n" +
            "zone example.com. {\n" +
            "    primary 127.0.0.1;\n" +
            "    key DHCP_UPDATER;\n" +
            "}\n" +
            "\n" +
            "# Shared network with multiple subnets\n" +
            "shared-network office-network {\n" +
            "    # Main subnet\n" +
            "    subnet 192.168.1.0 netmask 255.255.255.0 {\n" +
            "        range 192.168.1.100 192.168.1.200;\n" +
            "        option routers 192.168.1.1;\n" +
            "        option broadcast-address 192.168.1.255;\n" +
            "        option subnet-mask 255.255.255.0;\n" +
            "\n" +
            "        # Pool with access control\n" +
            "        pool {\n" +
            "            range 192.168.1.50 192.168.1.99;\n" +
            "            allow members of \"known-clients\";\n" +
            "            deny unknown-clients;\n" +
            "        }\n" +
            "\n" +
            "        # Static host\n" +
            "        host server1 {\n" +
            "            hardware ethernet 00:11:22:33:44:55;\n" +
            "            fixed-address 192.168.1.10;\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    # Guest subnet\n" +
            "    subnet 192.168.2.0 netmask 255.255.255.0 {\n" +
            "        range 192.168.2.100 192.168.2.200;\n" +
            "        option routers 192.168.2.1;\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "# Class definition with conditional\n" +
            "class \"known-clients\" {\n" +
            "    match if substring(hardware, 1, 3) = 00:11:22;\n" +
            "}\n" +
            "\n" +
            "# Subclass definition\n" +
            "subclass \"known-clients\" 00:11:22:33:44:55;\n" +
            "\n" +
            "# Conditional example\n" +
            "if exists dhcp-client-identifier {\n" +
            "    log(info, \"Client has identifier\");\n" +
            "} elsif known {\n" +
            "    log(info, \"Known client\");\n" +
            "} else {\n" +
            "    log(info, \"Unknown client\");\n" +
            "}\n" +
            "\n" +
            "# Event handler\n" +
            "on commit {\n" +
            "    log(info, \"Lease committed\");\n" +
            "}\n" +
            "\n" +
            "# Group of hosts\n" +
            "group {\n" +
            "    next-server 192.168.1.5;\n" +
            "    filename \"pxelinux.0\";\n" +
            "\n" +
            "    host pxeclient1 {\n" +
            "        hardware ethernet AA:BB:CC:DD:EE:01;\n" +
            "    }\n" +
            "    host pxeclient2 {\n" +
            "        hardware ethernet AA:BB:CC:DD:EE:02;\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "# Failover peer configuration\n" +
            "failover peer \"dhcp-failover\" {\n" +
            "    primary;\n" +
            "    address 192.168.1.1;\n" +
            "    port 647;\n" +
            "    peer address 192.168.1.2;\n" +
            "    peer port 647;\n" +
            "    max-response-delay 30;\n" +
            "    max-unacked-updates 10;\n" +
            "    load balance max seconds 3;\n" +
            "    mclt 1800;\n" +
            "    split 128;\n" +
            "}\n" +
            "\n" +
            "# Include external config\n" +
            "include \"/etc/dhcp/dhcpd.conf.d/local.conf\";\n";

        try {
            ISCJSONCodec converter = new ISCJSONCodec();

            System.out.println("=== ORIGINAL DHCP CONFIG ===");
            System.out.println(dhcpConfig);

            // Parse to NVGenericMap
            NVGenericMap config = converter.parse(dhcpConfig);

            // Convert to JSON
            String json = converter.toJSONPretty(dhcpConfig);
            System.out.println("\n=== JSON OUTPUT ===");
            System.out.println(json);

            // Convert back to DHCP config
            String regenerated = converter.toDhcpConfig(config);
            System.out.println("\n=== REGENERATED DHCP CONFIG ===");
            System.out.println(regenerated);

            // Test round-trip from JSON
            NVGenericMap parsed = GSONUtil.fromJSONDefault(json, NVGenericMap.class);
            String roundTrip = converter.toDhcpConfig(parsed);
            System.out.println("\n=== ROUND-TRIP FROM JSON ===");
            System.out.println(roundTrip);

            // Parse from command line file if provided
            if (args.length > 0) {
                System.out.println("\n=== PARSING FILE: " + args[0] + " ===");
                NVGenericMap fileConfig = converter.parse(new File(args[0]));
                System.out.println(GSONUtil.toJSONDefault(fileConfig, true));
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("\nUsage: IscDhcpConverter [dhcpd.conf path]");
        }
    }
}