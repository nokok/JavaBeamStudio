package net.orekyuu.javatter.api.twitter;

import net.orekyuu.javatter.api.twitter.model.Tweet;

/**
 * @since 1.0.0
 */
public interface TweetControl {

    /**
     * ツイートを削除します
     * @param tweet 削除するツイート
     * @since 1.0.0
     */
    void removeTweet(Tweet tweet);

    /**
     * 非同期でツイートを削除します
     * @param tweet 削除するツイート
     * @since 1.0.0
     */
    void removeTweetAsync(Tweet tweet);

    /**
     * ツイートを作成します。
     *
     * @return ツイートを作成するためのビルダー
     * @since 1.0.0
     */
    TweetBuilder createTweet();
}
