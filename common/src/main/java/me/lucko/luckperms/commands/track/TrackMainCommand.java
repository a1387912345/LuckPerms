package me.lucko.luckperms.commands.track;

import me.lucko.luckperms.LuckPermsPlugin;
import me.lucko.luckperms.commands.MainCommand;
import me.lucko.luckperms.commands.Sender;
import me.lucko.luckperms.commands.SubCommand;
import me.lucko.luckperms.constants.Message;
import me.lucko.luckperms.tracks.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrackMainCommand extends MainCommand {

    private final List<TrackSubCommand> subCommands = new ArrayList<>();

    public TrackMainCommand() {
        super("Track", "/perms track <track>", 2);
    }

    @Override
    protected void execute(LuckPermsPlugin plugin, Sender sender, List<String> args) {
        if (args.size() < 2) {
            sendUsage(sender);
            return;
        }

        Optional<TrackSubCommand> o = subCommands.stream().filter(s -> s.getName().equalsIgnoreCase(args.get(1))).limit(1).findAny();

        if (!o.isPresent()) {
            Message.COMMAND_NOT_RECOGNISED.send(sender);
            return;
        }

        final TrackSubCommand sub = o.get();
        if (!sub.isAuthorized(sender)) {
            Message.COMMAND_NO_PERMISSION.send(sender);
            return;
        }

        List<String> strippedArgs = new ArrayList<>();
        if (args.size() > 2) {
            strippedArgs.addAll(args.subList(2, args.size()));
        }

        if (sub.isArgLengthInvalid(strippedArgs.size())) {
            sub.sendUsage(sender);
            return;
        }

        final String trackName = args.get(0).toLowerCase();
        plugin.getDatastore().loadTrack(trackName, success -> {
            if (!success) {
                Message.TRACK_NOT_FOUND.send(sender);
                return;
            }

            Track track = plugin.getTrackManager().getTrack(trackName);
            if (track == null) {
                Message.TRACK_NOT_FOUND.send(sender);
                return;
            }

            sub.execute(plugin, sender, track, strippedArgs);
        });
    }

    @Override
    public List<? extends SubCommand> getSubCommands() {
        return subCommands;
    }

    public void registerSubCommand(TrackSubCommand subCommand) {
        subCommands.add(subCommand);
    }

}