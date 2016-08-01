package nz.johannes.andromatic;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;

import java.util.Date;

import twitter4j.DirectMessage;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class SocialMediaManager {

    public static String TWITTER_CONSUMER_KEY = "ZqcNUNBdCV51qT95fgHTDDj2p";
    public static String TWITTER_CONSUMER_SECRET = "o1x7oMl2nWHzm7ey6I0oprAOzg5qSzaIfoVWGT7p3phwKfoleZ";

    private static long lastCheckTime;

    public static void sendTweet(final Context context, final String tweet) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
        configBuilder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
        configBuilder.setOAuthAccessToken(prefs.getString("twitterToken", ""));
        configBuilder.setOAuthAccessTokenSecret(prefs.getString("twitterSecret", ""));
        TwitterFactory twitterFactory = new TwitterFactory(configBuilder.build());
        final Twitter twitter = twitterFactory.getInstance();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    twitter.updateStatus(tweet);
                } catch (TwitterException e) {
                    Main.showToast(context, "Failed to connect to Twitter - check your device is online and you are logged in.");
                }
            }
        }).start();
    }

    public static void sendTwitterMessage(final Context context, final String user, final String message) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
        configBuilder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
        configBuilder.setOAuthAccessToken(prefs.getString("twitterToken", ""));
        configBuilder.setOAuthAccessTokenSecret(prefs.getString("twitterSecret", ""));
        TwitterFactory twitterFactory = new TwitterFactory(configBuilder.build());
        final Twitter twitter = twitterFactory.getInstance();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    twitter.sendDirectMessage(user, message);
                } catch (TwitterException e) {
                    Main.showToast(context, "Failed to connect to Twitter - check your device is online and you are logged in.");
                }
            }
        }).start();
    }

    public static void check(final Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        lastCheckTime = prefs.getLong("socialMediaLastCheck", System.currentTimeMillis() - 1000 * 60 * 15);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("socialMediaLastCheck", System.currentTimeMillis()).apply();
        for (final Task task : Main.getAllStoredTasks(context)) {
            for (final Trigger trigger : task.getTriggers()) {
                switch (trigger.getType()) {
                    case "Trigger.NewTweet":
                    case "Trigger.NewTweetByContent":
                    case "Trigger.NewTweetByUser":
                        checkTweets(context, task, trigger);
                        break;
                    case "Trigger.NewDM":
                    case "Trigger.NewDMByContent":
                    case "Trigger.NewDMByUser":
                        checkTwitterMessages(context, task, trigger);
                        break;
                    case "Trigger.NewRedditPostBySubreddit":
                        checkRedditPosts(context, task, trigger);
                        break;
                }
            }
        }
    }

    private static void checkTweets(final Context context, final Task task, final Trigger trigger) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
        configBuilder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
        configBuilder.setOAuthAccessToken(prefs.getString("twitterToken", ""));
        configBuilder.setOAuthAccessTokenSecret(prefs.getString("twitterSecret", ""));
        TwitterFactory twitterFactory = new TwitterFactory(configBuilder.build());
        final Twitter twitter = twitterFactory.getInstance();
        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                task.runTask(context);
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ResponseList<Status> tweets = twitter.getHomeTimeline();
                    Date date = new Date(lastCheckTime);
                    switch (trigger.getType()) {
                        case "Trigger.NewTweet":
                            for (Status tweet : tweets) if (tweet.getCreatedAt().after(date)) handler.sendMessage(new Message());
                            break;
                        case "Trigger.NewTweetByContent":
                            for (Status tweet : tweets) {
                                if (tweet.getCreatedAt().after(date)) {
                                    if (trigger.getExtraData().get(0).equals("Exact") && tweet.getText().equalsIgnoreCase(trigger.getMatch()))
                                        handler.sendMessage(new Message());
                                    if (trigger.getExtraData().get(0).equals("Partial") && tweet.getText().contains(trigger.getMatch().toLowerCase()))
                                        handler.sendMessage(new Message());
                                }
                            }
                            break;
                        case "Trigger.NewTweetByUser":
                            for (Status tweet : tweets) {
                                if (tweet.getUser().getName().equals(trigger.getMatch()) && tweet.getCreatedAt().after(date))
                                    handler.sendMessage(new Message());
                            }
                            break;
                    }
                } catch (TwitterException e) {
                    Main.showToast(context, "Failed to connect to Twitter - check your device is online and you are logged in.");
                }
            }
        }).start();
    }

    private static void checkTwitterMessages(final Context context, final Task task, final Trigger trigger) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
        configBuilder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
        configBuilder.setOAuthAccessToken(prefs.getString("twitterToken", ""));
        configBuilder.setOAuthAccessTokenSecret(prefs.getString("twitterSecret", ""));
        TwitterFactory twitterFactory = new TwitterFactory(configBuilder.build());
        final Twitter twitter = twitterFactory.getInstance();
        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                task.runTask(context);
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ResponseList<DirectMessage> messages = twitter.getDirectMessages();
                    Date date = new Date(lastCheckTime);
                    switch (trigger.getType()) {
                        case "Trigger.NewDM":
                            for (DirectMessage message : messages) if (message.getCreatedAt().after(date)) handler.sendMessage(new Message());
                            break;
                        case "Trigger.NewDMByContent":
                            for (DirectMessage message : messages) {
                                if (message.getCreatedAt().after(date)) {
                                    if (trigger.getExtraData().get(0).equals("Exact") &&
                                            message.getText().equalsIgnoreCase(trigger.getMatch()))
                                        handler.sendMessage(new Message());
                                    if (trigger.getExtraData().get(0).equals("Partial") &&
                                            message.getText().contains(trigger.getMatch().toLowerCase()))
                                        handler.sendMessage(new Message());
                                }
                            }
                            break;
                        case "Trigger.NewDMByUser":
                            for (DirectMessage message : messages) {
                                if (message.getSender().getName().equals(trigger.getMatch()) && message.getCreatedAt().after(date))
                                    handler.sendMessage(new Message());
                            }
                            break;
                    }
                } catch (TwitterException e) {
                    Main.showToast(context, "Failed to connect to Twitter - check your device is online and you are logged in.");
                }
            }
        }).start();
    }

    private static void checkRedditPosts(final Context context, final Task task, final Trigger trigger) {

    }

}
