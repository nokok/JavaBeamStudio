package net.orekyuu.javatter.core.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import net.orekyuu.javatter.api.service.ApplicationService;
import net.orekyuu.javatter.api.service.CurrentTweetAreaService;
import net.orekyuu.javatter.api.service.UserIconStorage;
import net.orekyuu.javatter.api.service.UserWindowService;
import net.orekyuu.javatter.api.twitter.TwitterUser;
import net.orekyuu.javatter.api.twitter.model.Tweet;
import net.orekyuu.javatter.api.twitter.model.User;
import net.orekyuu.javatter.core.settings.storage.GeneralSetting;
import net.orekyuu.javatter.core.settings.storage.SettingsStorage;

import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.ResourceBundle;

public class TweetCellController implements Initializable {

    public TweetText tweetContent;
    public ImageView currentIcon;
    public Hyperlink via;
    public Label userName;
    public Button reply;
    public Button retweet;
    public ToggleButton favorite;
    public GridPane root;
    public HBox images;
    public Label time;
    public MenuButton actions;
    private ObjectProperty<Tweet> tweet = new SimpleObjectProperty<>();
    private ObjectProperty<TwitterUser> owner = new SimpleObjectProperty<>();
    @Inject
    private UserIconStorage iconStorage;
    @Inject
    private CurrentTweetAreaService tweetAreaService;
    @Inject
    private UserWindowService service;
    @Inject
    private SettingsStorage settingsStorage;
    @Inject
    private ApplicationService applicationService;
    private MenuItem openBrowserMenu;
    private MenuItem favRTMenu;
    private MenuItem copyToClipboardMenu;

    //アクションボタンクリック時のツイートを保存するための変数
    private Tweet actionTargetTweet;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tweet.addListener(this::onChange);
        tweetContent.setOnClickLink(url -> {
            try {
                applicationService.getApplication()
                        .getHostServices()
                        .showDocument(new URL(url).toURI().toString());
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });
        tweetContent.setOnClickUserLink(userName -> {
            TwitterUser user = owner.get();
            if (user != null) {
                service.open(user.findUser(userName));
            }
        });
        openBrowserMenu = new MenuItem("ブラウザで開く");
        favRTMenu = new MenuItem("ふぁぼ&RT");
        copyToClipboardMenu = new MenuItem("ツイートをコピー");
        //アクションボタンをクリックした時に現在のツイートを保存
        actions.setOnMouseClicked(e -> actionTargetTweet = tweet.get());
        actions.getItems().addAll(openBrowserMenu, favRTMenu, copyToClipboardMenu);

        openBrowserMenu.setOnAction(e -> {
            try {
                String url = String.format("https://twitter.com/%s/status/%d",
                        actionTargetTweet.getOwner().getScreenName(), actionTargetTweet.getStatusId());
                applicationService.getApplication()
                        .getHostServices()
                        .showDocument(new URL(url).toURI().toString());
            } catch (URISyntaxException | MalformedURLException e1) {
                e1.printStackTrace();
            }
        });
        favRTMenu.setOnAction(e -> {
            owner.get().favoriteAsync(actionTargetTweet);
            owner.get().retweetAsync(actionTargetTweet);
        });
        copyToClipboardMenu.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(actionTargetTweet.getText());
            clipboard.setContent(content);
        });
    }

    private void onChange(ObservableValue<? extends Tweet> observable, Tweet oldValue, Tweet newValue) {
        tweetContent.setTweet(newValue);
        currentIcon.setImage(iconStorage.find(newValue.getOwner()));
        via.setText(newValue.getViaName());
        via.setOnAction(e -> {
            try {
                applicationService.getApplication()
                        .getHostServices()
                        .showDocument(new URL(newValue.getViaLink()).toURI().toString());
            } catch (URISyntaxException | MalformedURLException e1) {
                e1.printStackTrace();
            }
        });

        userName.setText(String.format("@%s / %s", newValue.getOwner().getScreenName(), newValue.getOwner().getName()));
        TwitterUser twitterUser = owner.get();
        if (twitterUser != null) {
            updateButtonState(newValue, twitterUser);
        }

        images.getChildren().clear();
        for (String url : newValue.medias()) {
            Image image = new Image(url, 128, 128, true, true, true);
            ImageView view = new ImageView(image);
            view.setOnMouseClicked(e -> openPreview(url));
            images.getChildren().add(view);
        }

        currentIcon.setOnMouseClicked(e -> {
            service.open(newValue.getOwner());
        });

        updateTime(newValue);

    }

    private void updateTime(Tweet tweet) {
        Tweet retweetFrom = tweet.getRetweetFrom();
        LocalDateTime from = retweetFrom == null ? tweet.getCreatedAt() : retweetFrom.getCreatedAt();
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime temp = LocalDateTime.from(from);
        long year = temp.until(to, ChronoUnit.YEARS);
        temp = temp.plusYears(year);
        long month = temp.until(to, ChronoUnit.MONTHS);
        temp = temp.plusMonths(month);
        long day = temp.until(to, ChronoUnit.DAYS);
        temp = temp.plusDays(day);
        long hour = temp.until(to, ChronoUnit.HOURS);
        temp = temp.plusHours(hour);
        long minute = temp.until(to, ChronoUnit.MINUTES);
        temp = temp.plusMinutes(minute);
        long second = temp.until(to, ChronoUnit.SECONDS);

        if (0 < year) {
            time.setText(year + "年前");
        } else if (0 < month) {
            time.setText(month + "ヶ月前");
        } else if (0 < day) {
            time.setText(day + "日前");
        } else if (0 < hour) {
            time.setText(hour + "時間前");
        } else if (0 < minute) {
            time.setText(minute + "分前");
        } else if (0 < second){
            time.setText(second + "秒前");
        } else {
            time.setText("Now");
        }

        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss");
        time.getTooltip().setText(from.format(pattern));
    }

    private void updateButtonState(Tweet tweet, TwitterUser twitterUser) {
        User owner = tweet.getOwner();
        retweet.setDisable(owner.getId() == twitterUser.getUser().getId() || tweet.isRetweeted(twitterUser));
        favorite.setSelected(tweet.isFavorited(twitterUser));

    }

    private void openPreview(String url) {
        Stage stage = new Stage();
        stage.centerOnScreen();
        Image image = new Image(url, true);
        BorderPane pane = new BorderPane();
        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setMaxSize(150, 150);
        pane.setCenter(indicator);
        Scene progressScene = new Scene(pane, 400, 400);
        image.progressProperty().addListener((observable, oldValue, newValue) -> {
            if (1.0 <= newValue.doubleValue()) {
                ImageView imageView = new ImageView(image);
                BorderPane borderPane = new BorderPane();
                borderPane.setCenter(imageView);
                Scene scene = new Scene(borderPane);
                borderPane.setPrefSize(image.getWidth(), image.getHeight());
                stage.setScene(scene);
                imageView.fitWidthProperty().bind(borderPane.widthProperty());
                imageView.fitHeightProperty().bind(borderPane.heightProperty());
            } else {
                indicator.setProgress(newValue.doubleValue());
            }
        });
        stage.setScene(progressScene);
        stage.setTitle(url);
        stage.show();
    }


    public void setTweet(Tweet tweet) {
        this.tweet.set(tweet);
    }

    public Tweet getTweet() {
        return tweet.get();
    }

    public ObjectProperty<Tweet> tweetProperty() {
        return tweet;
    }

    public TwitterUser getOwner() {
        return owner.get();
    }

    public ObjectProperty<TwitterUser> ownerProperty() {
        return owner;
    }

    public void setOwner(TwitterUser owner) {
        this.owner.set(owner);
    }

    public void clickReply() {
        Tweet tweet = getTweet();
        if (tweet != null) {
            tweetAreaService.insertTop("@" + tweet.getOwner().getScreenName() + " ");
            tweetAreaService.setReply(tweet);
        }
    }

    public void clickRetweet() {
        GeneralSetting setting = settingsStorage.getGeneralSetting();
        if (setting.isCheckRT()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "リツイートしますか？", ButtonType.YES, ButtonType.NO);
            alert.setTitle("確認");
            Optional<ButtonType> result = alert.showAndWait();
            result.filter(t -> t == ButtonType.YES).ifPresent(t -> retweet());
        } else {
            retweet();
        }
    }

    private void retweet() {
        TwitterUser twitterUser = owner.get();
        Tweet tweet = this.tweet.get();
        if (twitterUser != null && tweet != null) {
            twitterUser.retweetAsync(tweet);
        }
    }

    public void clickFavorite() {
        GeneralSetting setting = settingsStorage.getGeneralSetting();
        if (setting.isCheckFavorite()) {
            String str = favorite.isSelected() ? "ふぁぼ" : "あんふぁぼ";
            Alert alert = new Alert(Alert.AlertType.INFORMATION, str + "しますか？", ButtonType.YES, ButtonType.NO);
            alert.setTitle("確認");
            Optional<ButtonType> result = alert.showAndWait();
            result.ifPresent(t -> {
                if (t == ButtonType.YES) {
                    favorite();
                } else {
                    favorite.setSelected(!favorite.isSelected());
                }
            });
        } else {
            favorite();
        }
    }

    private void favorite() {
        TwitterUser twitterUser = owner.get();
        Tweet tweet = this.tweet.get();
        if (twitterUser != null && tweet != null) {
            if (favorite.isSelected()) {
                twitterUser.favoriteAsync(tweet);
            } else {
                twitterUser.unFavoriteAsync(tweet);
            }
        }
    }
}
