package com.jaoow.blockstop.manager;

import com.google.common.collect.Lists;
import com.jaoow.blockstop.dao.UserDao;
import com.jaoow.blockstop.model.MineUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MineUserManager {

    private final UserDao userDao;
    private final Map<UUID, MineUser> players = new ConcurrentHashMap<>();

    public MineUserManager(UserDao userDao) {
        this.userDao = userDao;
    }

    public List<MineUser> getUsers() {
        return Lists.newArrayList(players.values());
    }

    public void loadUser(MineUser timedPlayer) {
        this.players.put(timedPlayer.getUniqueId(), timedPlayer);
    }

    @NotNull
    public MineUser getOrCreate(UUID uuid) {
        return Optional.ofNullable(players.get(uuid)).orElseGet(() -> {
            MineUser mineUser = userDao.selectOne(uuid);

            if (mineUser == null) {
                mineUser = new MineUser(uuid);
                userDao.saveOne(mineUser);
            }

            this.players.put(uuid, mineUser);
            return mineUser;
        });
    }

    @Nullable
    public MineUser getByName(UUID uuid) {
        return players.get(uuid);
    }

}
