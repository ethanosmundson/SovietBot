/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rr.industries.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.exceptions.BotException;
import rr.industries.util.sql.PermTable;
import rr.industries.util.sql.PrefixTable;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class CommContext {
    private static final Logger LOG = LoggerFactory.getLogger(CommContext.class);
    private final List<String> args = new ArrayList<>();
    private String commChar;
    private Permissions callerPerms;
    private final BotActions actions;
    private final IMessage message;

    public CommContext(MessageReceivedEvent e, BotActions actions) {
        this.commChar = actions.getTable(PrefixTable.class).getPrefix(e.getMessage());
        this.actions = actions;
        this.message = e.getMessage();
        try {
            callerPerms = actions.getTable(PermTable.class).getPerms(e.getMessage().getAuthor(), e.getMessage());
        } catch (BotException ex) {
            actions.channels().exception(ex, builder());
            callerPerms = Permissions.NORMAL;
        }
        Scanner parser = new Scanner(e.getMessage().getContent());
        while (parser.hasNext()) {
            args.add(parser.next());
        }
        if (args.get(0).startsWith(commChar)) {
            args.set(0, args.get(0).substring(commChar.length()));
        }
    }

    public String getConcatArgs(int first) {
        if (first >= args.size()) {
            return "";
        }
        List<String> concat = args;
        for (int i = 0; i < first && args.size() > 0; i++)
            concat.remove(0);
        return concat.stream().collect(Collectors.joining(" "));
    }

    public MessageBuilder builder() {
        return new MessageBuilder(actions.getClient()).withChannel(message.getChannel());
    }

    public BotActions getActions() {
        return actions;
    }

    public Permissions getCallerPerms() {
        return callerPerms;
    }

    public List<String> getArgs() {
        return args;
    }

    public IMessage getMessage() {
        return message;
    }

    public IDiscordClient getClient() {
        return actions.getClient();
    }

    public String getCommChar() {
        return commChar;
    }
}
