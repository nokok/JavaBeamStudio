package net.orekyuu.javatter.api.twitter.model;

import com.gs.collections.api.list.ImmutableList;
import net.orekyuu.javatter.api.twitter.TwitterUser;

import java.time.LocalDateTime;

public interface Tweet {
    long getStatusId();

    String getText();

    LocalDateTime getCreatedAt();

    long getReplyStatusId();

    String getViaName();

    String getViaLink();

    Tweet getRetweetFrom();

    User getOwner();

    boolean isRetweeted(TwitterUser user);

    boolean isFavorited(TwitterUser user);

    ImmutableList<String> medias();
}
