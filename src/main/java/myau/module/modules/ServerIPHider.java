package myau.module.modules;

import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.TextProperty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerIPHider extends Module {
    public final BooleanProperty tablist = new BooleanProperty("tablist", true);
    public final BooleanProperty scoreboard = new BooleanProperty("scoreboard", true);
    public final TextProperty replacementIP = new TextProperty("replacement-ip", "127.0.0.1");

    // Regex to match IPv4 addresses and domain names, optionally preceded by a Minecraft formatting code
    private static final Pattern SERVER_ADDRESS_PATTERN = Pattern.compile("\\b(?:§[0-9a-fk-or])?(?:\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,})\\b");

    public ServerIPHider() {
        super("ServerIPHider", false, true);
    }

    public String replaceIP(String input) {
        if (input == null || !this.isEnabled()) {
            return input;
        }

        // Apply obfuscation if scoreboard is enabled
        if (this.scoreboard.getValue()) {
            // Similar to NickHider, if it matches a common pattern, obfuscate part of it
            // This regex is for a date/time, so we'll need to adapt for IP addresses if needed
            // For now, just replace IPs directly.
        }

        Matcher matcher = SERVER_ADDRESS_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacementIP.getValue()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
