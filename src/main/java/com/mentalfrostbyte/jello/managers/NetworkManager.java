package com.mentalfrostbyte.jello.managers;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.managers.util.account.CaptchaChecker;
import com.mentalfrostbyte.jello.util.client.network.auth.Encryptor;
import com.mentalfrostbyte.jello.util.client.network.auth.PremiumChecker;
import com.mentalfrostbyte.jello.util.game.player.tracker.CombatTracker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import team.sdhq.eventBus.EventBus;
import totalcross.json.JSONException;
import totalcross.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NetworkManager {

    public HttpClient httpClient;
    public CaptchaChecker captcha;
    public Encryptor encryptor;
    public String username;
    public String field38425;
    public CombatTracker combatTracker;

    public String mainURL;
    public String loginUrl;
    public String registerUrl;
    public String claimPremiumUrl;
    public String challengeUrl;
    public String field25693;
    public String token;

    public static boolean premium = false;
    public static boolean field25696 = false;

    public NetworkManager() {
        this.mainURL = "https://jelloprg.sigmaclient.info/";
        this.loginUrl = this.mainURL + "/login";
        this.registerUrl = this.mainURL + "/register";
        this.claimPremiumUrl = this.mainURL + "/claim_premium";
        this.challengeUrl = this.mainURL + "/challenge";
        this.token = UUID.randomUUID().toString().replaceAll("-", "");
        this.httpClient = HttpClients.createDefault();
    }

    public void init() {
        EventBus.register(this);
        this.combatTracker = new CombatTracker();
    }

    public String newAccount(String var1, String var2, CaptchaChecker var3) {
        String var6 = "Unexpected error";

        try {
            HttpPost var7 = new HttpPost(this.loginUrl);
            ArrayList var8 = new ArrayList();
            var8.add(new BasicNameValuePair("username", var1));
            var8.add(new BasicNameValuePair("password", var2));
            var8.add(new BasicNameValuePair("challengeUid", var3.getChallengeUid()));
            var8.add(new BasicNameValuePair("challengeAnswer", var3.getChallengeAnswer()));
            var8.add(new BasicNameValuePair("token", this.token));
            var3.method30473(false);
            var7.setEntity(new UrlEncodedFormEntity(var8, "UTF-8"));
            HttpResponse var9 = this.httpClient.execute(var7);
            HttpEntity var10 = var9.getEntity();
            if (var10 != null) {
                String var15;
                try (InputStream var11 = var10.getContent()) {
                    String var13 = IOUtils.toString(var11, "UTF-8");
                    JSONObject jsonInstance = new JSONObject(var13);
                    if (jsonInstance.getBoolean("success")) {
                        if (jsonInstance.has("premium")) {
                            new Thread(new PremiumChecker(jsonInstance.has("premium"))).start();
                        }

                        this.handleLoginResponse(jsonInstance);
                        return null;
                    }

                    if (jsonInstance.has("error")) {
                        var6 = jsonInstance.getString("error");
                    }

                    var15 = var6;
                }

                return var15;
            }
        } catch (IOException var28) {
            var6 = var28.getMessage();
            var28.printStackTrace();
        }

        return var6;
    }

    public void method30448(final String s, final String s2, String s3, final CaptchaChecker captchaChecker) {
        if (s3 == null) {
            s3 = "";
        }
        String s4 = "Unexpected error";
        try {
            final HttpPost httpPost = new HttpPost(this.registerUrl);
            final List<BasicNameValuePair> list = new ArrayList<>();
            list.add(new BasicNameValuePair("username", s));
            list.add(new BasicNameValuePair("password", s2));
            list.add(new BasicNameValuePair("email", s3));
            list.add(new BasicNameValuePair("challengeUid", captchaChecker.getChallengeUid()));
            list.add(new BasicNameValuePair("challengeAnswer", captchaChecker.getChallengeAnswer()));
            list.add(new BasicNameValuePair("token", this.token));
            captchaChecker.method30473(false);
            httpPost.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
            final HttpEntity entity = this.httpClient.execute(httpPost).getEntity();
            if (entity != null) {
                try (final InputStream content = entity.getContent()) {
                    final JSONObject class8774 = new JSONObject(IOUtils.toString(content, StandardCharsets.UTF_8));
                    if (class8774.getBoolean("success")) {
                        this.handleLoginResponse(class8774);
                        return;
                    }
                    if (class8774.has("error")) {
                        s4 = class8774.getString("error");
                    }
                }
            }
        }
        catch (final IOException ex) {
            s4 = ex.getMessage();
            ex.printStackTrace();
        }
    }

    public String validateToken() {
        new Thread(new PremiumChecker(true)).start();
        return "Cracked";
    }

    public void loadLicense() {
        if (this.encryptor != null) {
            return;
        }
        final File file = new File("jello/jello.lic");
        if (file.exists()) {
            try {
                this.encryptor = new Encryptor(Files.readAllBytes(file.toPath()));
                if (this.encryptor.username == null || this.encryptor.username.isEmpty()) {
                    this.encryptor = null;
                }
                if (this.validateToken() != null) {
                    this.encryptor = null;
                }
                else {
                    Client.getInstance().getLogger().setThreadName("Logged in!");
                }
            }
            catch (final IOException ex) {}
        }
    }

    public String redeemPremium(final String s, final CaptchaChecker captchaChecker) {
        String s2 = "Unexpected error";
        try {
            final HttpPost httpPost = new HttpPost(this.claimPremiumUrl);
            final ArrayList list = new ArrayList();
            list.add(new BasicNameValuePair("key", s));
            list.add(new BasicNameValuePair("challengeUid", captchaChecker.getChallengeUid()));
            list.add(new BasicNameValuePair("challengeAnswer", captchaChecker.getChallengeAnswer()));
            list.add(new BasicNameValuePair("token", this.token));
            captchaChecker.method30473(false);
            httpPost.setEntity((HttpEntity)new UrlEncodedFormEntity((List)list, "UTF-8"));
            final HttpEntity entity = this.httpClient.execute((HttpUriRequest)httpPost).getEntity();
            if (entity != null) {
                try (final InputStream content = entity.getContent()) {
                    final JSONObject class8774 = new JSONObject(IOUtils.toString(content, "UTF-8"));
                    if (class8774.getBoolean("success")) {
                        this.handleLoginResponse(class8774);
                        return null;
                    }
                    if (class8774.has("error")) {
                        s2 = class8774.getString("error");
                    }
                    return s2;
                }
            }
        }
        catch (final IOException ex) {
            s2 = ex.getMessage();
            ex.printStackTrace();
        }
        return s2;
    }

    public void method19346(final String field25693) {
        if (!field25693.equals("error")) {
            this.field25693 = field25693;
        }
        else {
            this.field25693 = null;
        }
    }

    public Encryptor method19347() {
        return this.encryptor;
    }

    public void resetLicense() {
        this.field25693 = null;
        this.encryptor = null;
        premium = false;
        final File file = new File("jello/jello.lic");
        if (file.exists()) {
            file.delete();
        }
    }

    public CaptchaChecker getChallengeResponse() {
        if (this.captcha != null && this.captcha.isCompleted()) {
            return this.captcha;
        }
        try {
            final HttpPost httpPost = new HttpPost(this.challengeUrl);
            final List<BasicNameValuePair> list = new ArrayList<>();
            list.add(new BasicNameValuePair("token", this.token));
            httpPost.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
            final HttpEntity entity = this.httpClient.execute(httpPost).getEntity();
            if (entity != null) {
                try (final InputStream content = entity.getContent()) {
                    final JSONObject jsonInstance = new JSONObject(IOUtils.toString(content, StandardCharsets.UTF_8));
                    if (jsonInstance.getBoolean("success")) {
                        final String uid = jsonInstance.getString("uid");
                        boolean completed = false;

                        if (jsonInstance.has("captcha")) {
                            completed = jsonInstance.getBoolean("captcha");
                        }
                        this.captcha = new CaptchaChecker(uid, completed);
                        return this.captcha;
                    }
                    return null;
                }
            }
        }
        catch (final IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void handleLoginResponse(final JSONObject JSONObject) throws JSONException {
        String method13268 = null;
        String method13269 = null;
        String method13270 = null;
        if (JSONObject.has("username")) {
            method13269 = JSONObject.getString("username");
        }
        if (JSONObject.has("auth_token")) {
            method13268 = JSONObject.getString("auth_token");
        }
        if (JSONObject.has("agora_token")) {
            method13270 = JSONObject.getString("agora_token");
        }
        if (method13268 != null && method13269 != null && method13270 != null) {
            try {
                this.encryptor = new Encryptor(method13269, method13268, method13270);
                FileUtils.writeByteArrayToFile(new File("jello/jello.lic"), this.encryptor.encrypt());
            }
            catch (final IOException ex) {}
        }
        if (JSONObject.has("session")) {
            this.method19346(JSONObject.getString("session"));
        }
    }

    public boolean isPremium() {
        return premium;
    }
}
