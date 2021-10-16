package com.jaoow.blockstop.dao.adapter;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jaoow.blockstop.model.MineUser;
import com.jaoow.sql.executor.adapter.SQLResultAdapter;
import com.jaoow.sql.executor.result.SimpleResultSet;
import org.jetbrains.annotations.NotNull;

public final class MineUserAdapter implements SQLResultAdapter<MineUser> {

    public static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting().create();

    @Override
    public MineUser adaptResult(@NotNull SimpleResultSet resultSet) {
        return GSON.fromJson((String) resultSet.get("data"), MineUser.class);
    }
}
