package ru.lisa.entity;

public final class Command {

    public static final String CREATE = "add";
    public static final String READ_ALL = "list";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String EXIT = "exit";
    public static final String HELP = "help";

    private Command() {
        throw new RuntimeException();
    }

}
