package rr.industries.commands;

import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.SubCommand;
import rr.industries.util.Syntax;
import sx.blah.discord.util.MessageBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

@CommandInfo(
        commandName = "cat",
        helpText = "Posts a random cat picture."
)
public class Cat implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Sends a random picture of a cat in a text channel", args = {})})
    public void execute(CommContext cont) {

        URL url;
        try {
            url = new URL("http://random.cat/meow");
        } catch (MalformedURLException ex) {
            cont.getActions().channels().customException("Cat", ex.getMessage(), ex, LOG, true);
            return;
        }
        InputStream is;
        try {
            URLConnection con = url.openConnection();
            is = con.getInputStream();
        } catch (IOException ex) {
            cont.getActions().channels().customException("Cat", ex.getMessage(), ex, LOG, true);
            return;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String message;
        try {
            message = br.readLine();
        } catch (IOException ex) {
            cont.getActions().channels().customException("Cat", ex.getMessage(), ex, LOG, true);
            return;
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                LOG.warn("unable to close Cat url reader", ex);
            }
        }
        message = message.substring(9, message.length() - 2);
        message = message.replace("\\/", "/");
        message = cont.getMessage().getAuthor().mention() + " " + message;
        cont.getActions().channels().sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getChannel()));
    }
}
