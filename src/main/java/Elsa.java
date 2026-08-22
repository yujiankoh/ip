/**
 * Entry point of the Elsa chatbot.
 */
public class Elsa {
    public static void main(String[] args) {
        String border = "____________________________________________________________";
        // Each "\\" in the source produces a single backslash in the ASCII-art banner.
        String banner = " _____ _           \n"
                + "|  ___| |___  __ _ \n"
                + "| |__ | / __|/ _` |\n"
                + "|  __|| \\__ \\ (_| |\n"
                + "|_____|_|___/\\__,_|";
        String greeting = "Hello! I'm Elsa.\n"
                + "What can I do for you?";
        String farewell = "Bye. Hope to see you again soon!";

        System.out.println(border);
        System.out.println(banner);
        System.out.println(greeting);
        System.out.println(border);
        System.out.println(farewell);
        System.out.println(border);
    }
}
