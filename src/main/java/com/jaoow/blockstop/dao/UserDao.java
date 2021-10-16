package com.jaoow.blockstop.dao;

import com.jaoow.blockstop.dao.adapter.MineUserAdapter;
import com.jaoow.blockstop.model.MineUser;
import com.jaoow.sql.executor.SQLExecutor;

import java.util.Set;
import java.util.UUID;

public class UserDao {

    private static final String TABLE = "mine_users";
    private final SQLExecutor sqlExecutor;

    public UserDao(SQLExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }

    public void createTable() {
        sqlExecutor.updateQuery("CREATE TABLE IF NOT EXISTS " + TABLE + "(" +
                "id VARCHAR(64) NOT NULL PRIMARY KEY UNIQUE," +
                "data TEXT" +
                ");");
    }

    public MineUser selectOne(UUID uuid) {
        return sqlExecutor.resultOneQuery(
                "SELECT * FROM " + TABLE + " WHERE id = ?",
                statement -> statement.set(1, uuid.toString()),
                MineUserAdapter.class
        );
    }

    public Set<MineUser> selectAll() {
        return selectAll("");
    }

    public Set<MineUser> selectAll(String preferences) {
        return sqlExecutor.resultManyQuery(
                "SELECT * FROM " + TABLE + " " + preferences,
                statement -> {},
                MineUserAdapter.class
        );
    }

    public void saveOne(MineUser user) {
        sqlExecutor.updateQuery(
                String.format("REPLACE INTO %s VALUES(?,?)", TABLE),
                statement -> {
                    statement.set(1, user.getUniqueId().toString());
                    statement.set(2, MineUserAdapter.GSON.toJson(user, MineUser.class));
                }
        );
    }
}
