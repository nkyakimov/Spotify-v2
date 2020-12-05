package spotify.enums;

import java.io.PrintWriter;
import java.util.Arrays;

public enum Options {
    Login("login", "login <username> <password>"),
    NewUser("new user", "new user <user> <password>"),
    DeleteUser("delete user", "delete user <user> <password>"),
    Create("create", "create <playlist>"),
    Add("add", "add <playlist> <song or artist>"),
    RemovePlaylist("remove", "remove <playlist> (<song or artist>)"),
    Play("play", "play <song>"),
    Print("print", "print (<playlist>)"),
    Top("top", "top <n>"),
    Help("help", "help"),
    Logout("logout", "logout"),
    Quit("quit", "quit");

    private final String option;
    private final String help;

    Options(String option, String help) {
        this.option = option;
        this.help = help;
    }

    public static void print(PrintWriter printWriter) {
        Arrays.stream(Options.values()).forEach(option -> printWriter.println(option.help));
    }

    public static Options getOption(String request) {
        for(Options option : Options.values()) {
            if(request.startsWith(option.option)) {
                return option;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return option;
    }
}
