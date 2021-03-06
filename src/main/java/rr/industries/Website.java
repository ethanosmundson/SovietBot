package rr.industries;

import com.github.adamint.Exceptions.OAuthException;
import com.github.adamint.Main.OAuthManager;
import com.github.adamint.Models.Scope;
import com.github.adamint.Models.Token;
import com.github.adamint.Responses.Guild;
import com.github.adamint.Settings.BotSettings;
import com.github.adamint.Settings.OAuthSettings;
import com.mashape.unirest.http.exceptions.UnirestException;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Image;
import discord4j.core.object.util.Snowflake;
import org.apache.commons.io.FilenameUtils;
import org.jooq.Record2;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.commands.Command;
import rr.industries.pageengine.MarkdownEngine;
import rr.industries.pageengine.Page;
import rr.industries.pageengine.PageEngine;
import rr.industries.util.*;
import rr.industries.util.sql.GreetingTable;
import rr.industries.util.sql.PermTable;
import rr.industries.util.sql.TagTable;
import spark.Request;
import spark.Response;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

/**
 *
 */
//todo: add class description
public class Website {
    public String botId;
    public String index;
    public String help;
    public HashMap<String, String> commands = new HashMap<>();
    public HashMap<String, String> styleSheets = new HashMap<>();
    public String javascript;
    public PageEngine engine;
    public OAuthManager manager;
    public HashMap<String, byte[]> images = new HashMap<>();
    private final ClassLoader loader = SovietBot.class.getClassLoader();
    private static ClassLoader resourceLoader = SovietBot.class.getClassLoader();
    public BotActions actions;
    private static Logger LOG = LoggerFactory.getLogger(Website.class);

    private String[] styleSheetsFiles = {"github-light.css", "print.css", "stylesheet.css"};
    private String[] imagesFiles = {"body-bg.jpg", "download-button.png", "download-button-discord.png", "download-button-home.png", "download-button-help.png", "github-button.png", "header-bg.jpg", "highlight-bg.jpg", "sidebar-bg.jpg", "download-button-dashboard.png"};

    public String index(Request request, Response response){
        return index;
    }

    public String help(Request request, Response response){
        return help;
    }

    public String command(Request request, Response response){
        String content = commands.get(request.pathInfo().substring("/commands/".length()));
        if (content == null) {
            response.status(404);
            return "404 Error";
        }
        return content;
    }

    public String stylesheet(Request request, Response response){
        response.type("text/css");
        return styleSheets.get(request.pathInfo().substring(1));
    }

    public Object images(Request request, Response response){
        try {
            byte[] bytes = images.get(request.pathInfo().substring(1));
            if (bytes == null) {
                LOG.warn("Unable to load site image " + request.pathInfo());
                response.status(404);
                return "Image Not Found";
            }
            HttpServletResponse raw = response.raw();
            response.type("image/" + FilenameUtils.getExtension(request.pathInfo().substring(1)));
            raw.getOutputStream().write(bytes);
            raw.getOutputStream().flush();
            raw.getOutputStream().close();

            return response.raw();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String javascript(Request request, Response response){
        response.type("application/javascript");
        return javascript;
    }

    public Website(BotActions actions) throws IOException {
        this.botId = actions.getClient().getSelfId().orElseThrow(() -> new RuntimeException("Bot Not Logged in Yet")).asString();
        this.actions = actions;
        engine = new PageEngine().setDefaultURL("template.txt");
        engine.addEnvTag("invite-link", getInviteLink());
        engine.addEnvTag("oauth-link", getOAuthLink());
        engine.addEnvTag("main-icon", "/avatar.png");
        OAuthSettings oAuthSettings = new OAuthSettings("SovietBot", actions.getConfig().url, actions.getConfig().url + "dashboard", "1.0 BETA");
        BotSettings botSettings = new BotSettings(botId,
                actions.getConfig().discordSecret, Launcher.token);
        manager = new OAuthManager(oAuthSettings, botSettings);
        LOG.info("OAuth Link: {}", getOAuthLink());
        Page indexPage = engine.newPage();
        indexPage.setTag("title", escapeHtml4("SovietBot by robot-rover"));
        indexPage.setTag("header", "SovietBot");
        indexPage.setTag("description", escapeHtml4("Fun and Simple discord bot with support for youtube music streaming."));
        indexPage.setTag("content", loadResourceString("index.txt").replace("{invite-link}", getInviteLink()));
        index = indexPage.generate();

        Page helpPage = engine.newPage();
        StringBuilder commandListText = new StringBuilder("<ul class=\"container\">\n");
        List<CommandInfo> commInfos = new ArrayList<>();
        for (Command command : CommandList.getCommandList()) {
            CommandInfo commInfo = command.getClass().getAnnotation(CommandInfo.class);
            if (commInfo.permLevel() == Permissions.BOTOPERATOR)
                continue;
            commInfos.add(commInfo);
        }
        commInfos.sort(comparing(CommandInfo::commandName));
        for (CommandInfo commInfo : commInfos) {
            commandListText.append("<a href=\"commands").append(File.separator).append(commInfo.commandName()).append(".html").append("\"><li>").append(escapeHtml4(Information.defaultCommChar + commInfo.commandName())).append("</li></a>\n");
        }
        commandListText.append("<div style=\"clear:both\"></ul>");
        helpPage.setTag("title", "Command List");
        helpPage.setTag("header", "Commands");
        helpPage.setTag("description", escapeHtml4("All of the Commands that SovietBot supports"));
        helpPage.setTag("content", commandListText.toString());
        help = helpPage.generate();
        for (String url : styleSheetsFiles) {
            styleSheets.put("stylesheets/" + url, loadResourceString("stylesheets/" + url));
        }
        for (String url : imagesFiles) {
            images.put("images/" + url, loadResource("images/" + url));
        }
        javascript = loadResourceString("javascripts/main.js");

        for (Command command : CommandList.getCommandList()) {
            CommandInfo commInfo = command.getClass().getAnnotation(CommandInfo.class);
            if (commInfo.permLevel() == Permissions.BOTOPERATOR)
                continue;
            StringBuilder body = new StringBuilder();
            List<SubCommand> subCommands = new ArrayList<>();
            for (Method method : command.getClass().getMethods()) {
                if (method.getAnnotation(SubCommand.class) != null) {
                    subCommands.add(method.getAnnotation(SubCommand.class));
                }
            }
            for (SubCommand subComm : subCommands) {
                for (Syntax syntax : subComm.Syntax()) {
                    body.append("<h2>").append(escapeHtml4("\t" + Information.defaultCommChar + commInfo.commandName() + " " + subComm.name() + "\n"));
                    for (Argument arg : syntax.args())
                        body.append(escapeHtml4(" <" + (arg.description().equals("") ? arg.value().defaultLabel : arg.description()) + ">"));
                    body.append("</h2>\n<p>").append(escapeHtml4(syntax.helpText())).append("</p>\n");
                    body.append("<p>").append(Arrays.stream(syntax.args()).filter(v -> v.options().length > 0).map(v -> Arrays.stream(v.options()).collect(Collectors.joining(" | ", (v.description().equals("") ? v.value().defaultLabel : v.description()) + " ( ", " )"))).collect(Collectors.joining("<p>", "</p><p>", "</p>")));
                }

            }
            Page commandPage = engine.newPage();
            String htmlBody = "<h1>Syntax</h1>\n" + "<p>Possible Syntaxes for this command. Type what you see exactly, but replace what is between <strong>&lt;&gt;</strong> with your own values.\n" + body.toString();
            commandPage.setTag("title", escapeHtml4(Information.defaultCommChar + commInfo.commandName()));
            commandPage.setTag("header", escapeHtml4(Information.defaultCommChar + commInfo.commandName()));
            commandPage.setTag("description", escapeHtml4(commInfo.helpText()));
            commandPage.setTag("content", htmlBody);
            commands.put(commInfo.commandName() + ".html", commandPage.generate());
        }
    }

    private String getOAuthLink(){
        return "https://discordapp.com/oauth2/authorize?client_id=" + botId + "&scope=identify+guilds&redirect_uri=" + actions.getConfig().url + "dashboard&response_type=code";
    }

    public String redirectToOAuth(Request request, Response response){
        return engine.newPage("redirect.txt").setTag("title", "Redirecting...").setTag("redirect-link", getOAuthLink()).generate();
    }

    private String getInviteLink(){
        return Information.invite;
    }


    private String loadResourceString(String url) throws IOException {
        return spark.utils.IOUtils.toString(loader.getResourceAsStream(url));
    }

    private byte[] loadResource(String url) throws IOException {
        return spark.utils.IOUtils.toByteArray(loader.getResourceAsStream(url));

    }

    public String dashboard(Request request, Response response){
        LOG.info(request.queryString());
        try {
            response.type("text/html");
            Token token = authenticate(request, response);
            List<Guild> guilds = manager.getUserGuilds(token);
            StringBuilder content = new StringBuilder();
            content.append("<ul class=\"guilds\">");
            for(Guild guild : guilds){
                if((guild.getPermissions() & 0x00000020) == 0/* && actions.getClient().get.getGuildByID(Long.parseLong(guild.getId())) == null*/)
                    continue;
                content.append("<a href=\"/guilds/").append(guild.getId()).append("\"><li><img src=\"").append(getGuildIcon(guild.getId(), guild.getIcon())).append("\">");
                content.append("<p>").append(guild.getName()).append("</p></li></a>");
            }
            content.append("</ul>");
            Page page = engine.newPage();
            page.setTag("oauth-link", "/dashboard");
            page.setTag("title", "SovietBot - Dashboard");
            page.setTag("header", "Dashboard");
            page.setTag("description", "Select a server to continue...");
            page.setTag("content", content.toString());
            return page.generate();

        } catch(OAuthException e){
            if (e.getErrorCode() == 401){
                return redirectToOAuth(request, response);
            }
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        response.status(500);
        return "SovietBot encountered an error...";
    }

    public String guild(Request request, Response response){
        response.type("text/html");
        int length;
        try {
            Snowflake guildID = Snowflake.of(request.pathInfo().substring("/guilds/".length()));
            LOG.info("Guild ID: {}", guildID);
            discord4j.core.object.entity.Guild guild = actions.getClient().getGuildById(guildID).block();
            if(guild == null){
                response.redirect("/invite/" + guildID);
                return "Redirecting...";
            }
            //Page Setup
            Token token = authenticate(request, response);
            Page page = engine.newPage();
            page.setTag("oauth-link", "/dashboard");
            page.setTag("title", "SovietBot - " + guild.getName());
            page.setTag("header", "Dashboard");
            page.setTag("description", guild.getName());
            page.setTag("main-icon", guild.getIconUrl(Image.Format.PNG).orElse(""));
            StringBuilder content = new StringBuilder();
            //Tags Section
            content.append("<button class=\"accordion\" onclick=\"toggleAccordion(this)\">").append("Tags").append("</button><div class=\"panel\"><ul>");
            length = content.length();
            List<TagData> tags = actions.getTable(TagTable.class).getAllTags(guild.getId());
            for(TagData tag : tags){
                content.append("<li><b>").append(escapeHtml4(tag.getName())).append("</b> - ").append(MarkdownEngine.generate(tag.getContent())).append("</li>\n");
            }
            if(length == content.length())
                content.append("<p><i>None</i><p>");
            content.append("</ul></div>");
            //Global Tags Section
            content.append("<button class=\"accordion\" onclick=\"toggleAccordion(this)\">").append("Global Tags").append("</button><div class=\"panel\"><ul>");
            length = content.length();
            List<TagData> globalTags = actions.getTable(TagTable.class).getGlobalTags();
            for(TagData tag : globalTags){
                content.append("<li><b>").append(escapeHtml4(tag.getName())).append("</b> - ").append(MarkdownEngine.generate(tag.getContent())).append("</li>\n");
            }
            if(length == content.length())
                content.append("<p><i>None</i><p>");
            content.append("</ul></div>");
            //User Perms Section
            List<Record2<Long, Integer>> perms = actions.getTable(PermTable.class).getAllPerms(guild.getId());
            content.append("<button class=\"accordion\" onclick=\"toggleAccordion(this)\">").append("User Permissions").append("</button><div class=\"panel\"><ul>");
            content.append("<h2>").append(Permissions.SERVEROWNER.title).append("</h2>\n");
            Member owner = guild.getOwner().block();
            content.append("<li>").append(escapeHtml4(owner.getDisplayName())).append("</li>\n");
            for(int[] i = {Permissions.ADMIN.level}; i[0] >= Permissions.NORMAL.level; i[0]--){
                StringBuilder level = new StringBuilder();
                perms.stream().filter(v -> v.component2() == i[0]).forEach(v -> {
                    Member user = guild.getMemberById(Snowflake.of(v.component1())).block();
                    if(user != null && user != owner)
                        level.append("<li>").append(escapeHtml4(user.getDisplayName())).append("</li>\n");
                });
                if(level.length() != 0){
                    content.append("<h2>").append(BotUtils.toPerms(i[0]).title).append("</h2>");
                    content.append(level);
                }
            }
            content.append("</ul></div>");
            //Server Greeting Section
            //todo: include greeting channel

            content.append("<button class=\"accordion\" onclick=\"toggleAccordion(this)\">").append("Server Greetings").append("</button><div class=\"panel\"><ul>");
            length = content.length();
            Entry<String, Long> join = actions.getTable(GreetingTable.class).getJoinMessage(guild.getId());
            if(join != null && join.first() != null)
                    content.append("<h2>Leave:</h2><li>").append(escapeHtml4(join.first()).replace("%user", "<code>%user</code>")).append("</li>\n");
            Entry<String, Long> leave = actions.getTable(GreetingTable.class).getLeaveMessage(guild.getId());
            if(leave != null && leave.first() != null)
                    content.append("<h2>Join:</h2><li>").append(escapeHtml4(leave.first()).replace("%user", "<code>%user</code>")).append("</li>\n");
            if(length == content.length())
                content.append("<p><i>None</i><p>");
            content.append("</ul></div>");

            page.setTag("content", content.toString());
            return page.generate();
        } catch(OAuthException e){
            if (e.getErrorCode() == 401){
                return redirectToOAuth(request, response);
            }
            e.printStackTrace();
        } catch(NumberFormatException e){
            response.status(400);
            return request.pathInfo().substring("/guilds/".length()) + " is not a valid guild";
        } catch (Exception e){
            e.printStackTrace();
        }
        response.status(500);
        return "SovietBot encountered an error...";
    }

    public String invite(Request request, Response response){
        try {
            Page page = engine.newPage();
            page.setTag("title", "SovietBot - Invite");
            page.setTag("header", "Invite");
            page.setTag("description", "Add SovietBot to your server");
            if(request.cookie("sovietBot") != null){
                page.setTag("oauth-link", "/dashboard");
            }
            Token token = authenticate(request, response);
            try {
                String guildIDString = request.pathInfo().substring("/invite/".length());
                Guild guild = manager.getUserGuilds(token).stream().filter(v -> v.getId().equals(guildIDString)).findAny().orElse(null);
                if (guild != null) {
                    page.setTag("main-icon", getGuildIcon(guild.getId(), guild.getIcon()));
                    page.setTag("description", "Add SovietBot to " + guild.getName());
                }
            } catch (StringIndexOutOfBoundsException e){}

            page.setTag("content", "<h2>SovietBot isn't currently in your server</h2><h3>Invite it here:</h3><p><a href=\"" + getInviteLink() + "\">" + getInviteLink() + "</a></p>");
            return page.generate();
        } catch(OAuthException e){
            if (e.getErrorCode() == 401){
                return redirectToOAuth(request, response);
            }
            e.printStackTrace();
        } catch(NumberFormatException e){
            response.status(400);
            return request.pathInfo().substring("/guilds/".length()) + " is not a valid guild";
        } catch (Exception e){
            e.printStackTrace();
        }
        response.status(500);
        return "SovietBot encountered an error...";
    }

    private Token authenticate(Request request, Response response) throws OAuthException, UnirestException, JSONException {
        Token token;
        String code = request.queryParams("code");
        if(code == null){
            String tokenString = request.cookie("sovietBot");
            if(tokenString == null){
                throw new OAuthException("Not Authenticated", 401);
            }
            token = new Token(request.cookie("sovietBot"), null, null, null, "identify guilds");
            LOG.info("Creating return user from cookie");
        } else {
            LOG.info("Creating new User from Code - code: {}", code);
            token = manager.getToken(code);
            response.cookie("sovietBot", token.getAccessToken());
        }
        if(!token.getScopes().contains(Scope.Routes.IDENTIFY) || !token.getScopes().contains(Scope.Routes.GUILDS)){
            throw new OAuthException("Missing Required Scope", 401);
        }
        return token;
    }

    private String getGuildIcon(String guildID, String guildIcon){
        return "https://cdn.discordapp.com/icons/" + guildID + "/" + guildIcon + ".jpg";
    }
}
