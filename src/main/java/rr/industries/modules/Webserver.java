package rr.industries.modules;

import com.google.gson.Gson;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.Information;
import rr.industries.SovietBot;
import rr.industries.Website;
import rr.industries.pojos.travisciwebhooks.TravisWebhook;
import rr.industries.util.BotActions;
import spark.Request;
import spark.Response;
import spark.Service;
import spark.Spark;
import spark.utils.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * Credit to Chrislo for the basis and the POJOs
 */
public class Webserver implements Module {

    private final Gson gson = new Gson();
    private BotActions actions;
    private final Logger LOG = LoggerFactory.getLogger(Webserver.class);
    private static final File imageDirectory = new File("image");
    private boolean isEnabled;
    private Website site;

    /**
     * Should be initalized in ReadyEvent
     */
    public Webserver() {
        isEnabled = false;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public Module enableModule(BotActions actions) {
        boolean useSSL = actions.getConfig().keystorePath != null && actions.getConfig().keystorePassword != null;
        System.out.println("Using SSL? " + useSSL);
        this.actions = actions;
        try {
            site = new Website(actions);
        } catch (IOException e) {
            LOG.error("Error initializing website sources", e);
        }
        Service apis = Service.ignite().port(actions.getConfig().apiPort);
        if(useSSL)
            apis.secure(actions.getConfig().keystorePath, actions.getConfig().keystorePassword, null, null);
        apis.get("/ping", ((request, response) -> {
            response.type("text/plain");
            response.status(418);
            return "I'm a Teapot!";
        }));

        apis.post("/travis", (Request request, Response response) -> {
            response.type("text/plain");
            try {
                LOG.info("Received Travis Post");
                TravisWebhook payload = gson.fromJson(URLDecoder.decode(request.body(), "UTF-8").replace("payload=", ""), TravisWebhook.class);
                StringBuilder message = new StringBuilder("Travis-Ci Build");
                if (payload.repository != null)
                    if (payload.repository.name != null)
                        message.append(": ").append(payload.repository.name);
                if (payload.statusMessage != null)
                    message.append(" **").append(payload.statusMessage).append("**\n");
                if (payload.authorName != null)
                    message.append("[*").append(payload.authorName).append("*]");
                if (payload.repository != null)
                    if (payload.branch != null)
                        message.append(" - ").append(payload.branch);
                message.append("\n");
                if (payload.startedAt != null) {
                    message.append("Started ").append(DateTimeFormatter.ofLocalizedTime(FormatStyle.FULL).format(ZonedDateTime.parse(payload.startedAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("UTC")))));

                }
                if (payload.duration != null)
                    message.append(" -> ").append(payload.duration).append("s");
                sendMessageToChannels("Travis Build", message.toString());
            } catch (Exception e) {
                LOG.error("Error Responding to Travis Webhook", e);
                response.status(500);
                return e.getMessage();
            }
            return "\uD83D\uDC4C OK";
        });

        Service website = Service.ignite().port(actions.getConfig().websitePort);
        if(useSSL)
            website.secure(actions.getConfig().keystorePath, actions.getConfig().keystorePassword, null, null);

        //website.before(((request, response) -> LOG.info("Http Request -> {}", request.pathInfo())));

        website.redirect.get("/", "/index.html");

        website.get("/index.html", (site::index));

        website.get("/commandList.html", (site::help));

        website.get("/dashboard", site::dashboard);

        website.get("/invite/*", site::invite);

        website.get("/invite", site::invite);

        website.get("/redirect", site::redirectToOAuth);

        website.get("/avatar", ((request, response) -> {
            byte[] bytes = IOUtils.toByteArray(SovietBot.class.getClassLoader().getResourceAsStream(Information.botAvatar));
            if (bytes.length == 0) {
                LOG.error("Unable to load Bot Avatar!");
            }
            LOG.warn("Deprecated Avatar Endpoint accessed!");
            new Exception().printStackTrace();
            HttpServletResponse raw = response.raw();
            response.type("image/" + FilenameUtils.getExtension(Information.botAvatar));
            raw.getOutputStream().write(bytes);
            raw.getOutputStream().flush();
            raw.getOutputStream().close();

            return response.raw();
        }));

        website.get("/avatar.png", (((request, response) -> {
            byte[] bytes = IOUtils.toByteArray(SovietBot.class.getClassLoader().getResourceAsStream(Information.botAvatar));
            if (bytes.length == 0) {
                LOG.error("Unable to load Bot Avatar!");
            }
            HttpServletResponse raw = response.raw();
            response.type("image/" + FilenameUtils.getExtension(Information.botAvatar));
            raw.getOutputStream().write(bytes);
            raw.getOutputStream().flush();
            raw.getOutputStream().close();

            return response.raw();
        })));

        website.get("/image/*", (Request request, Response response) -> {
            File image = new File(request.pathInfo().substring(1));
            if (image.isDirectory() || !image.exists()) {
                response.status(404);
                return "Requested image not found";
            }

            byte[] bytes = Files.readAllBytes(image.toPath());
            HttpServletResponse raw = response.raw();
            response.type("image/" + FilenameUtils.getExtension(image.getAbsolutePath()));
            raw.getOutputStream().write(bytes);
            raw.getOutputStream().flush();
            raw.getOutputStream().close();

            return response.raw();
        });

        website.get("/favicon.ico", ((Request request, Response response) -> {
            byte[] bytes = IOUtils.toByteArray(SovietBot.class.getClassLoader().getResourceAsStream(Information.botIcon));
            if (bytes.length == 0) {
                LOG.error("Unable to load favicon!");
            }
            HttpServletResponse raw = response.raw();
            response.type("image/" + FilenameUtils.getExtension(Information.botIcon));
            raw.getOutputStream().write(bytes);
            raw.getOutputStream().flush();
            raw.getOutputStream().close();

            return response.raw();
        }));

        website.get("/guilds/*", site::guild);

        website.get("/stylesheets/*", (site::stylesheet));

        website.get("/commands/*", (site::command));

        website.get("/images/*", site::images);

        website.get("/javascripts/main.js", (site::javascript));

        apis.init();
        website.init();
        LOG.info("Initialized webhooks on port " + apis.port());
        LOG.info("Initialized http server on port " + website.port());
        isEnabled = true;
        return this;
    }

    @Override
    public Module disableModule() {
        Spark.stop();
        isEnabled = false;
        return this;
    }

    private void sendMessageToChannels(String event, String content) {
        LOG.info("Sent a webhook message to channels for event " + event);
        actions.messageOwner(content).subscribe();
    }

}
