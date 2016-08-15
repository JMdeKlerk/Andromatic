package nz.johannes.andromatic;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONObject;

import java.util.Arrays;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

public class Settings extends AppCompatActivity {

    private static RequestToken twitterRequestToken;
    private static CallbackManager callbackManager;

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (!prefs.getString("twitterID", "").equals("")) {
                Preference twitterPreference = findPreference("twitterAccount");
                twitterPreference.setSummary("Logged in as " + prefs.getString("twitterID", ""));
            }
            if (!prefs.getString("facebookID", "").equals("")) {
                Preference facebookPreference = findPreference("facebookAccount");
                facebookPreference.setSummary("Logged in as " + prefs.getString("facebookID", ""));
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, final Preference preference) {
            if (preference.getSummary() != null && preference.getSummary().toString().startsWith("Logged in as")) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(this.getActivity());
                alert.setTitle("Log out?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (preference.getKey()) {
                            case "twitterAccount":
                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                                editor.remove("twitterID");
                                editor.remove("twitterToken");
                                editor.remove("twitterSecret");
                                editor.commit();
                                getActivity().recreate();
                            case "facebookAccount":
                                editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                                editor.remove("facebookID");
                                editor.remove("facebookToken");
                                editor.commit();
                                getActivity().recreate();
                        }
                    }
                });
                alert.setNegativeButton("No", null);
                alert.show();
                return true;
            }
            if (preference.getKey().equals("twitterAccount")) {
                final Handler handler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message inputMessage) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        if (!prefs.getString("twitterID", "").equals("")) {
                            Main.showToast(getActivity(), "Twitter account linked successfully.");
                            Preference twitterPreference = findPreference("twitterAccount");
                            twitterPreference.setSummary("Logged in as " + prefs.getString("twitterID", ""));
                        } else {
                            Main.showToast(getActivity(), "Failed to link Twitter account!");
                        }
                    }
                };
                final AlertDialog.Builder alert = new AlertDialog.Builder(this.getActivity());
                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(SocialMediaManager.TWITTER_CONSUMER_KEY);
                builder.setOAuthConsumerSecret(SocialMediaManager.TWITTER_CONSUMER_SECRET);
                TwitterFactory factory = new TwitterFactory(builder.build());
                final Twitter twitter = factory.getInstance();
                View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_singleline, null);
                final EditText textView = (EditText) view.findViewById(R.id.text);
                textView.setHint("PIN");
                alert.setView(view);
                alert.setNeutralButton("Get your PIN...", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    twitterRequestToken = twitter.getOAuthRequestToken();
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(twitterRequestToken.getAuthorizationURL()));
                                    startActivity(browserIntent);
                                } catch (TwitterException e) {
                                }
                            }
                        }).start();
                    }
                });
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    AccessToken accessToken = twitter.getOAuthAccessToken(twitterRequestToken, textView.getText().toString());
                                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                                    editor.putString("twitterID", accessToken.getScreenName());
                                    editor.putString("twitterToken", accessToken.getToken());
                                    editor.putString("twitterSecret", accessToken.getTokenSecret());
                                    editor.commit();
                                } catch (TwitterException e) {
                                }
                                handler.sendMessage(new Message());
                            }
                        }).start();
                    }
                });
                alert.setNegativeButton("Cancel", null);
                alert.show();
            }
            if (preference.getKey().equals("facebookAccount")) {
                FacebookSdk.sdkInitialize(getActivity());
                LoginManager loginManager = LoginManager.getInstance();
                loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject me, GraphResponse response) {
                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                                editor.putString("facebookID", me.optString("name"));
                                editor.putString("facebookToken", loginResult.getAccessToken().getToken());
                                editor.commit();
                                Main.showToast(getActivity(), "Facebook account linked successfully.");
                                Preference facebookPreference = findPreference("facebookAccount");
                                facebookPreference.setSummary("Logged in as " + me.optString("name"));
                            }
                        });
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Main.showToast(getActivity(), "Error connecting to Facebook");
                    }
                });
                loginManager.logInWithPublishPermissions(getActivity(), Arrays.asList("publish_actions"));
            }
            return true;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
        callbackManager = CallbackManager.Factory.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
