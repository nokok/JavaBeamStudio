package net.orekyuu.javatter.core.models;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import twitter4j.User;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * ユーザーの情報を格納したクラス
 */
public class UserModel {
    private final LocalDateTime createdAt;
    private final String description;
    private final int favCount;
    private final int followersCount;
    private final int friendsCount;
    private final long id;
    private final int listedCount;
    private final String location;
    private final String name;
    private final String screenName;
    private final String profileImageURL;

    private UserModel(User user) {
        createdAt = LocalDateTime.ofInstant(user.getCreatedAt().toInstant(), ZoneId.systemDefault());
        description = user.getDescription();
        favCount = user.getFavouritesCount();
        followersCount = user.getFollowersCount();
        friendsCount = user.getFriendsCount();
        id = user.getId();
        listedCount = user.getListedCount();
        location = user.getLocation();
        name = user.getName();
        screenName = user.getScreenName();
        profileImageURL = user.getProfileImageURL();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getDescription() {
        return description;
    }

    public int getFavCount() {
        return favCount;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public long getId() {
        return id;
    }

    public int getListedCount() {
        return listedCount;
    }

    public String getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getProfileImageURL() {
        return profileImageURL;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserModel{");
        sb.append("createdAt=").append(createdAt);
        sb.append(", description='").append(description).append('\'');
        sb.append(", favCount=").append(favCount);
        sb.append(", followersCount=").append(followersCount);
        sb.append(", friendsCount=").append(friendsCount);
        sb.append(", id=").append(id);
        sb.append(", listedCount=").append(listedCount);
        sb.append(", location='").append(location).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", screenName='").append(screenName).append('\'');
        sb.append(", profileImageURL='").append(profileImageURL).append('\'');
        sb.append('}');
        return sb.toString();
    }

    /**
     * UserModelを作成するビルダーです
     */
    public static class Builder {
        private static LoadingCache<User, UserModel> cache = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .softValues()
                .maximumSize(100)
                .build(new CacheLoader<User, UserModel>() {
                    @Override
                    public UserModel load(User key) throws Exception {
                        return new UserModel(key);
                    }
                });

        /**
         * UserModelを作成します
         * @param user User
         * @return UserModel
         */
        public static UserModel build(User user) {
            try {
                return cache.get(user);
            } catch (ExecutionException e) {
                throw new UncheckedExecutionException(e);
            }
        }
    }
}
