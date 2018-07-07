package rr.industries;

public class Configuration {
    public String commChar;
    public String url;
    public String[] operators;
    public String googleKey;
    public String dictKey;
    public String discordSecret;
    public String outputServer;
    public String outputChannel;
    public int websitePort;
    public int apiPort;
    public String keystorePath;
    public String keystorePassword;
    public String filterConfigPath;

    public Configuration() {
        this.commChar = ">";
        this.url = "";
        this.operators = new String[0];
        this.googleKey = "";
        this.dictKey = "";
        this.discordSecret = "";
        this.outputServer = "";
        this.outputChannel = "";
        this.apiPort = 8080;
        this.websitePort = 80;
        this.filterConfigPath = "";
    }

    @Override
    public String toString() {
        return "Bot Configuration";
    }
}
