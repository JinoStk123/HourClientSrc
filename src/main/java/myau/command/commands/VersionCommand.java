package myau.command.commands;

import myau.HourClient;
import myau.command.Command;
import myau.util.ChatUtil;

import java.util.ArrayList;

public class VersionCommand extends Command {
    public VersionCommand() {
        super(new ArrayList<String>() {{ add("version"); add("ver"); }});
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        if (args.size() == 1) {
            ChatUtil.sendFormatted(String.format("%sClient Version: &a%s&r", HourClient.clientName, HourClient.version));
        } else {
            ChatUtil.sendFormatted(String.format("%sUsage: .Hour %s", HourClient.clientName, names.get(0)));
        }
    }
}
