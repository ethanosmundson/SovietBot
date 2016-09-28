package rr.industries.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.CommandList;
import rr.industries.Configuration;
import rr.industries.modules.Module;
import rr.industries.util.sql.Table;
import sx.blah.discord.api.IDiscordClient;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Sam
 * @project sovietBot
 * @created 8/29/2016
 */
public final class BotActions {
    private static final Logger LOG = LoggerFactory.getLogger(BotActions.class);
    private final IDiscordClient client;
    private final Configuration config;
    private final CommandList commands;
    private final Statement sql;
    private final Table[] tables;
    private final Module[] modules;
    private final ChannelActions message;
    private static List<BotActions> cache = new ArrayList<>();

    public static BotActions getActions(IDiscordClient client) {
        return cache.stream().filter((v) -> v.getClient().equals(client)).findAny().orElse(null);
    }

    public BotActions(IDiscordClient client, CommandList commands, Statement sql, Table[] tables, Module[] modules, ChannelActions message) {
        this.client = client;
        this.tables = tables;
        this.config = message.config;
        this.commands = commands;
        this.sql = sql;
        this.modules = modules;
        this.message = message;
        cache.add(this);
    }

    public void enableModules() {
        for (Module m : modules) {
            if (!m.isEnabled())
                m.enable();
        }
    }

    public void disableModules() {
        for (Module m : modules) {
            if (m.isEnabled())
                m.disable();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> moduleType) {
        for (Module module : modules) {
            if (module.getClass().equals(moduleType))
                return (T) module;
        }
        throw new NoSuchElementException("Table of type: " + moduleType.getName() + " not found!");
    }

    @SuppressWarnings("unchecked")
    public <T extends Table> T getTable(Class<T> tableType) {
        for (Table table : tables) {
            if (table.getClass().equals(tableType))
                return (T) table;
        }
        throw new NoSuchElementException("Table of type: " + tableType.getName() + " not found!");
    }

    public IDiscordClient getClient() {
        return client;
    }

    public ChannelActions channels() {
        return message;
    }

    public CommandList getCommands() {
        return commands;
    }

    public Configuration getConfig() {
        return config;
    }

}
