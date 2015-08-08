package net.orekyuu.javatter.core.column;

import com.gs.collections.api.list.ImmutableList;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import net.orekyuu.javatter.api.column.Column;
import net.orekyuu.javatter.api.column.ColumnController;
import net.orekyuu.javatter.api.column.ColumnState;
import net.orekyuu.javatter.api.service.ColumnService;
import net.orekyuu.javatter.api.service.TwitterUserService;
import net.orekyuu.javatter.api.twitter.TwitterUser;
import net.orekyuu.javatter.api.twitter.model.Tweet;
import net.orekyuu.javatter.api.twitter.userstream.events.OnStatus;
import net.orekyuu.javatter.core.control.TweetCell;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class HomeTimeLineColumn implements ColumnController, Initializable {
    public static final String ID = "timeline";

    private static final String KEY = "userId";
    @FXML
    private MenuButton accountSelect;
    @FXML
    private ListView<Tweet> timeline;
    @FXML
    private Label title;
    @FXML
    private VBox root;

    private Optional<TwitterUser> user;
    private ObjectProperty<TwitterUser> owner = new SimpleObjectProperty<>();
    private static final Logger logger = Logger.getLogger(HomeTimeLineColumn.class.getName());

    @Inject
    private TwitterUserService userService;
    @Inject
    private ColumnService columnService;

    //弱参照でリスナが保持されるためフィールドに束縛しておく
    private OnStatus onStatus = this::onStatus;

    @Override
    public void restoration(ColumnState columnState) {
        if (columnState == null) {
            columnState = newColumnState();
        }
        logger.info(columnState.toString());
        String userName = (String) columnState.getData(KEY).getFirst();
        if (userName == null) {
            user = userService.selectedAccount();
        } else {
            user = userService.findTwitterUser(userName);
        }

        owner.set(user.orElse(null));
        Runnable runnable = () -> user
                .map(u -> u.getUser().getScreenName())
                .map(name -> "@" + name)
                .ifPresent(title::setText);
        Platform.runLater(runnable);
        user.ifPresent(user -> user.userStream().onStatus(onStatus));
    }

    private void onStatus(Tweet tweet) {
        Platform.runLater(() -> {
            logger.info("onStatus: " + tweet.getText());

            ScrollBar bar = getScrollBar().get();
            if (bar == null) {
                timeline.getItems().add(0, tweet);
            } else {
                if (bar.getOnScroll() == null) {
                    EventHandler<ScrollEvent> scrollEventEventHandler = e -> clearQueue();
                    bar.setOnScroll(scrollEventEventHandler);
                    bar.setOnDragDone(event -> clearQueue());
                    timeline.setOnScroll(scrollEventEventHandler);
                }
                if (bar.getValue() == bar.getMin()) {
                    timeline.getItems().add(0, tweet);
                } else {
                    queue.add(tweet);
                }
            }
        });
    }

    private void clearQueue() {
        Optional<ScrollBar> scrollBar = getScrollBar();
        if (!scrollBar.isPresent()) {
            return;
        }
        ScrollBar bar = scrollBar.get();
        if (bar.getValue() != bar.getMin()) {
            return;
        }
        while (!queue.isEmpty()) {
            try {
                Tweet take = queue.take();
                timeline.getItems().add(0, take);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    private Optional<ScrollBar> getScrollBar() {
        Set<Node> nodes = timeline.lookupAll(".scroll-bar");
        return nodes.stream()
                .filter(e -> e instanceof ScrollBar)
                .map(e -> (ScrollBar) e)
                .filter(bar -> bar.getOrientation() == Orientation.VERTICAL)
                .findFirst();
    }

    @Override
    public String getPluginId() {
        return ColumnInfos.PLUGIN_ID_BUILDIN;
    }

    @Override
    public String getColumnId() {
        return HomeTimeLineColumn.ID;
    }

    @Override
    public void onClose(ColumnState columnState) {
        logger.info("save");
        user.map(TwitterUser::getUser).ifPresent(u -> columnState.setData(KEY, u.getScreenName()));
    }

    private BlockingQueue<Tweet> queue = new LinkedBlockingQueue<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("initialize");
        timeline.setCellFactory(param -> {
            try {
                TweetCell tweetCell = new TweetCell();
                tweetCell.ownerProperty().bind(owner);
                return tweetCell;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        ImmutableList<TwitterUser> twitterUsers = userService.allUser();
        for (TwitterUser twitterUser : twitterUsers) {
            MenuItem item = new MenuItem(twitterUser.getUser().getScreenName());
            item.setOnAction(e -> {
                user = Optional.of(twitterUser);
                title.setText(twitterUser.getUser().getScreenName());
                timeline.getItems().clear();
                twitterUser.userStream().onStatus(this::onStatus);
                owner.set(user.orElse(null));
                System.gc();
            });
            accountSelect.getItems().add(item);
        }
    }

    public void closeRequest() {
        columnService.removeColumn(new Column(this, root));
    }
}
