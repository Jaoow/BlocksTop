package com.jaoow.blockstop;

import com.jaoow.blockstop.commands.BlocksTopCommand;
import com.jaoow.blockstop.dao.UserDao;
import com.jaoow.blockstop.inventory.AllInv;
import com.jaoow.blockstop.inventory.CategoriesInv;
import com.jaoow.blockstop.inventory.OreInv;
import com.jaoow.blockstop.manager.MineUserManager;
import com.jaoow.blockstop.model.MineUser;
import com.jaoow.blockstop.utils.inventory.InventoryBuilder;
import com.jaoow.sql.connector.SQLConnector;
import com.jaoow.sql.connector.type.impl.MySQLDatabaseType;
import com.jaoow.sql.connector.type.impl.SQLiteDatabaseType;
import com.jaoow.sql.executor.SQLExecutor;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.SQLException;

@Getter
public final class BlocksTop extends JavaPlugin {

    private static @Getter
    BlocksTop instance;

    private SQLConnector sqlConnector;
    private UserDao userDao;
    private MineUserManager userManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();

        // Init MySQL.
        getLogger().info("Initializing database...");
        try {
            sqlConnector = configureSqlProvider(this.getConfig().getConfigurationSection("connection"));
            userDao = new UserDao(new SQLExecutor(sqlConnector));
            userManager = new MineUserManager(userDao);

            // Create table.
            userDao.createTable();

            // Load all signs.
            for (MineUser mineUser : userDao.selectAll()) {
                userManager.loadUser(mineUser);
            }
        } catch (Exception e) {
            this.getLogger().warning("Failed to connect to database. Shutting down.");
            return;
        }

        getServer().getPluginManager().registerEvents(new InventoryBuilder.Listener(), this);
        getServer().getPluginManager().registerEvents(new Listener() {

            @EventHandler(priority = EventPriority.MONITOR)
            public void onBlock(BlockBreakEvent event) {
                if (event.isCancelled()) return;
                userManager.getOrCreate(event.getPlayer().getUniqueId()).addMaterial(event.getBlock().getType());
            }

        }, this);

        getCommand("blockstop").setExecutor(new BlocksTopCommand());
    }


    private @NotNull SQLConnector configureSqlProvider(@NotNull ConfigurationSection section) throws SQLException {

        SQLConnector connector;
        ConfigurationSection mysqlTable = section.getConfigurationSection("mysql");

        if (mysqlTable.getBoolean("enable")) {
            connector = MySQLDatabaseType.builder()
                    .address(mysqlTable.getString("address"))
                    .username(mysqlTable.getString("username"))
                    .password(mysqlTable.getString("password"))
                    .database(mysqlTable.getString("database"))
                    .build()
                    .connect();
        } else {
            ConfigurationSection sqliteTable = section.getConfigurationSection("sqlite");
            connector = SQLiteDatabaseType.builder()
                    .file(new File(sqliteTable.getString("file")))
                    .build()
                    .connect();
        }
        return connector;
    }
}
