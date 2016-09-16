package rr.industries.commands;

import rr.industries.CommandList;
import rr.industries.util.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

@CommandInfo(
        commandName = "stop",
        helpText = "Shuts down SovietBot",
        permLevel = Permissions.BOTOPERATOR,
        deleteMessage = false
)
public class Stop implements Command {
    static {
        CommandList.defaultCommandList.add(Stop.class);
    }
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Stops the proccess running the bot", args = {})})
    public void execute(CommContext cont) {
        if (cont != null) {
            if (!cont.getMessage().getMessage().getAuthor().getID().equals("141981833951838208")) {
                cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withContent("Communism marches on!").withChannel(cont.getMessage().getMessage().getChannel()));
                return;
            }
            if (!cont.getMessage().getMessage().getChannel().isPrivate()) {
                //todo: stop delete message testing
                try {
                    cont.getMessage().getMessage().delete();
                } catch (MissingPermissionsException ex) {
                    //fail silently
                } catch (RateLimitException ex) {
                    //todo: implement ratelimit
                } catch (DiscordException ex) {
                    cont.getActions().customException("Stop", ex.getErrorMessage(), ex, LOG, true);
                }
            }
        }
        cont.getActions().terminate(false);
    }
}
