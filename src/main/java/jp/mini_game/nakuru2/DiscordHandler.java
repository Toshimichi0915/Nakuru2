package jp.mini_game.nakuru2;

import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class DiscordHandler extends Handler {

    private static final int LENGTH_LIMIT = 1000;

    private final Set<Level> levels;
    private final TextChannel output;
    private final Formatter formatter;

    public DiscordHandler(Set<Level> levels, TextChannel output, Formatter formatter) {
        this.levels = levels;
        this.output = output;
        this.formatter = formatter;
    }

    @Override
    public void publish(LogRecord logRecord) {
        // レベルを満たさなかったらreturn
        if(!levels.contains(logRecord.getLevel())) return;
        // キュー内に入れて、flushで送信する
        output.sendMessage("**" + logRecord.getLevel().getName() + "**").queue();
        separate(formatter.format(logRecord)).forEach(m->output.sendMessage(m).queue());
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

    /**
     * メッセージをDiscordで送信可能なように文字列を分割します
     * @param message 分割したいメッセージ
     * @return 分割されたメッセージ
     */
    private List<String> separate(String message) {
        List<String> result = new ArrayList<>();
        // 改行で分けて考える
        String[] lines = message.split("\n");
        StringBuilder builder = new StringBuilder("```");

        // 行ごとの処理
        for(int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
            String line = lines[lineNumber];

            // 現在の長さを調べる
            int count = builder.codePointCount(0, builder.length())
                    + line.codePointCount(0, line.length());

            // 長さの限界を超えていなくて、最後の行でなければ
            if(count < LENGTH_LIMIT&& lineNumber + 1 != lines.length) {
                builder.append(line);
                builder.append('\n');
            } else {
                builder.append(line);
                // 終わりを表す```を追加する
                builder.append("```\n");
                // 結果に追加
                result.add(builder.toString());
                // builderの初期化
                // これが最後の行だとしても、このbuilderは単に無駄になりうだけなので問題ない
                builder = new StringBuilder("```\n");
            }
        }
        return result;
    }
}
