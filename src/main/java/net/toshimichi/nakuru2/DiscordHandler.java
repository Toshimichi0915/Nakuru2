package net.toshimichi.nakuru2;

import net.dv8tion.jda.api.entities.TextChannel;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class DiscordHandler extends Handler {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final int LENGTH_LIMIT = 1000;

    private final Set<Level> levels;
    private final TextChannel output;
    private final Formatter formatter;
    private final HashSet<String> messages;

    public DiscordHandler(Set<Level> levels, TextChannel output, Formatter formatter) {
        this.levels = levels;
        this.output = output;
        this.formatter = formatter;
        this.messages = new HashSet<>();
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    public void publish(LogRecord logRecord) {
        if (!levels.contains(logRecord.getLevel())) return;

        MessageDigest digest;

        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        }

        String md5 = bytesToHex(digest.digest(logRecord.getMessage().getBytes(StandardCharsets.UTF_8)));
        if (messages.contains(md5)) return;
        messages.add(md5);

        output.sendMessage("**" + logRecord.getLevel().getName() + "**").queue();
        separate(formatter.format(logRecord)).forEach(m -> output.sendMessage(m).queue());
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

    private List<String> separate(String message) {
        List<String> result = new ArrayList<>();
        String[] lines = message.split("\n");
        StringBuilder builder = new StringBuilder("```");

        // process each line by line
        for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
            String line = lines[lineNumber];

            // check current text length
            int count = builder.codePointCount(0, builder.length())
                    + line.codePointCount(0, line.length());

            if (count < LENGTH_LIMIT && lineNumber + 1 != lines.length) {
                builder.append(line);
                builder.append('\n');
            } else {
                builder.append(line);
                builder.append("```\n");
                result.add(builder.toString());
                builder = new StringBuilder("```\n");
            }
        }
        return result;
    }
}
