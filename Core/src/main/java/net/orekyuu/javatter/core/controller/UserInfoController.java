package net.orekyuu.javatter.core.controller;

import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import net.orekyuu.javatter.api.twitter.model.User;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class UserInfoController implements Initializable {
    public Text tweetCount;
    public Text followCount;
    public Text followerCount;
    public Text favoriteCount;
    public ImageView userIcon;
    public Text name;
    public Hyperlink screenName;
    public Label desc;
    public Hyperlink webSite;
    public Text created;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setUser(User user) {
        tweetCount.setText(String.valueOf(user.getTweetCount()));
        followCount.setText(String.valueOf(user.getFriendsCount()));
        followerCount.setText(String.valueOf(user.getFollowersCount()));
        favoriteCount.setText(String.valueOf(user.getFavCount()));
        userIcon.setImage(new Image(user.getOriginalProfileImageURL(), true));
        name.setText(user.getName());
        screenName.setText(user.getScreenName());
        screenName.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(new URL("http://twitter.com/" + user.getScreenName() + "/").toURI());
            } catch (IOException | URISyntaxException e1) {
                e1.printStackTrace();
            }
        });
        desc.setText(user.getDescription());

        if (user.getWebSite() != null) {
            webSite.setVisible(true);
            webSite.setDisable(false);
            webSite.setText(user.getWebSite());
            webSite.setOnAction(e -> {
                try {
                    Desktop.getDesktop().browse(new URL(user.getWebSite()).toURI());
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            });
        } else {
            webSite.setVisible(false);
            webSite.setDisable(true);
        }

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/ddに作成されました。");
        created.setText(user.getCreatedAt().format(format));
    }
}