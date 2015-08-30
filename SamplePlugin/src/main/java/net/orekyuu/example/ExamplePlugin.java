package net.orekyuu.example;

import net.orekyuu.javatter.api.command.CommandManager;
import net.orekyuu.javatter.api.plugin.OnPostInit;

import javax.inject.Inject;

public class ExamplePlugin {

    @Inject
    private CommandManager commandManager;

    public static final String PLUGIN_ID = "net.orekyuu.example";

    @OnPostInit
    public void initialize() {
        commandManager.registerCommand(new MyCommand());
    }
}
