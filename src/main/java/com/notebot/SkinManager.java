package com.notebot;

import org.bukkit.configuration.file.YamlConfiguration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 假人皮肤：把 plugins/Notebot/skins/ 下的 .png 上传到 Mineskin，
 * 得到 textures.minecraft.net 的皮肤链接（原版客户端可以直接显示），
 * 并按文件内容哈希缓存，避免重复上传（Mineskin 免费接口有频率限制）。
 */
public final class SkinManager {

    /** 皮肤数据：textures 属性的 value 与 signature */
    public record SkinData(String value, String signature) {
    }

    private static final Pattern P_VALUE = Pattern.compile("\"value\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
    private static final Pattern P_SIGNATURE = Pattern.compile("\"signature\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
    private static final Pattern P_URL = Pattern.compile("\"url\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");

    private final NotebotPlugin plugin;
    private final File skinsDir;
    private final File cacheFile;
    private final Map<String, SkinData> cache = new LinkedHashMap<>();
    private boolean uploading;

    public SkinManager(NotebotPlugin plugin) {
        this.plugin = plugin;
        this.skinsDir = new File(plugin.getDataFolder(), "skins");
        this.cacheFile = new File(this.skinsDir, "cache.yml");
        if (!this.skinsDir.exists()) {
            this.skinsDir.mkdirs();
        }
        this.loadCache();
    }

    private void loadCache() {
        this.cache.clear();
        if (!this.cacheFile.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(this.cacheFile);
        org.bukkit.configuration.ConfigurationSection section = yaml.getConfigurationSection("skins");
        if (section == null) {
            return;
        }
        for (String hash : section.getKeys(false)) {
            String value = section.getString(hash + ".value");
            String signature = section.getString(hash + ".signature");
            if (value != null) {
                this.cache.put(hash, new SkinData(value, signature == null ? "" : signature));
            }
        }
    }

    private void saveCache() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<String, SkinData> entry : this.cache.entrySet()) {
            yaml.set("skins." + entry.getKey() + ".value", entry.getValue().value());
            yaml.set("skins." + entry.getKey() + ".signature", entry.getValue().signature());
        }
        try {
            yaml.save(this.cacheFile);
        }
        catch (IOException ex) {
            this.plugin.getLogger().warning("\u65e0\u6cd5\u4fdd\u5b58\u76ae\u80a4\u7f13\u5b58: " + ex.getMessage());
        }
    }

    /** 该假人应使用的皮肤 .png 文件（skins/<假人名>.png 优先，其次 bot.skin） */
    private File skinFileFor(String botName) {
        File own = new File(this.skinsDir, botName + ".png");
        if (own.isFile()) {
            return own;
        }
        String global = this.plugin.config().botSkin();
        if (global != null && !global.isEmpty()) {
            File file = new File(this.skinsDir, global);
            if (file.isFile()) {
                return file;
            }
        }
        return null;
    }

    /**
     * 皮肤模型：配置 skins.model 强制 classic/slim；auto 时按原版算法检测：
     * 64x64 皮肤右臂底部外列（x=54..55, y=20..31）全透明视为 slim。
     */
    private String modelOf(File file) {
        String forced = this.plugin.config().skinModel();
        if (!forced.equals("auto")) {
            return forced;
        }
        byte[] png = readBytes(file);
        if (png == null) {
            return "classic";
        }
        BufferedImage image = decodePng(png);
        if (image == null || image.getWidth() != 64 || image.getHeight() != 64) {
            return "classic";
        }
        for (int x = 54; x <= 55; x++) {
            for (int y = 20; y <= 31; y++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    return "classic";
                }
            }
        }
        return "slim";
    }

    private static BufferedImage decodePng(byte[] png) {
        try {
            return ImageIO.read(new java.io.ByteArrayInputStream(png));
        }
        catch (Exception ex) {
            return null;
        }
    }

    private static byte[] readBytes(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        }
        catch (IOException ex) {
            return null;
        }
    }

    /** 同步获取皮肤数据（仅缓存命中；未命中返回 null，由 warmUp 异步上传） */
    public SkinData dataFor(String botName) {
        File file = this.skinFileFor(botName);
        if (file == null) {
            return null;
        }
        String key = cacheKey(file);
        return key == null ? null : this.cache.get(key);
    }

    /** 缓存键：文件内容哈希 + 皮肤模型（同一张图 classic/slim 上传结果不同） */
    private String cacheKey(File file) {
        String hash = hashOf(file);
        if (hash == null) {
            return null;
        }
        return hash + "-" + modelOf(file);
    }

    /** 异步预热：为所有皮肤文件准备缓存；完成后给在线假人补发玩家信息以显示皮肤 */
    public void warmUp() {
        if (this.uploading) {
            return;
        }
        this.uploading = true;
        CompletableFuture.runAsync(() -> {
            try {
                for (File file : this.listSkins()) {
                    String key = cacheKey(file);
                    if (key == null || this.cache.containsKey(key)) {
                        continue;
                    }
                    SkinData data = upload(file, modelOf(file));
                    if (data != null) {
                        this.cache.put(key, data);
                        this.saveCache();
                        this.plugin.getLogger().info("\u76ae\u80a4\u5df2\u51c6\u5907: " + file.getName()
                                + " (" + modelOf(file) + ")");
                    }
                }
            }
            finally {
                this.uploading = false;
            }
            // 主线程：给已在线且皮肤文件已缓存的假人补发玩家信息（客户端立即显示皮肤）
            this.plugin.getServer().getScheduler().runTask((org.bukkit.plugin.Plugin)this.plugin, () -> {
                for (com.notebot.bot.FakeBot bot : this.plugin.fakePlayers().onlineBots()) {
                    if (this.dataFor(bot.name()) != null && bot.player() != null && bot.player().isOnline()) {
                        Nms.INSTANCE.resendPlayerInfo(bot.player());
                    }
                }
            });
        });
    }

    private java.util.List<File> listSkins() {
        java.util.List<File> files = new java.util.ArrayList<>();
        File[] children = this.skinsDir.listFiles();
        if (children == null) {
            return files;
        }
        for (File file : children) {
            if (file.isFile() && file.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".png")) {
                files.add(file);
            }
        }
        return files;
    }

    private SkinData upload(File file, String model) {
        try {
            byte[] png = Files.readAllBytes(file.toPath());
            if (png.length < 8 || (png[0] & 0xFF) != 0x89 || png[1] != 'P' || png[2] != 'N' || png[3] != 'G') {
                this.plugin.getLogger().warning("\u76ae\u80a4\u6587\u4ef6\u4e0d\u662f\u6709\u6548\u7684 PNG: " + file.getName());
                return null;
            }
            if (png.length > 1024 * 1024) {
                this.plugin.getLogger().warning("\u76ae\u80a4\u6587\u4ef6\u8d85\u8fc7 1MB: " + file.getName());
                return null;
            }
            String boundary = "----NotebotSkin" + UUID.randomUUID().toString().replace("-", "");
            java.io.ByteArrayOutputStream body = new java.io.ByteArrayOutputStream();
            writePart(body, boundary, "variant", model);
            writeFilePart(body, boundary, "file", file.getName(), png);
            writePart(body, boundary, "visibility", "1");
            body.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

            String key = this.plugin.config().mineskinKey();
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mineskin.org/generate/upload"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .header("User-Agent", "Notebot/1.0.2 (Minecraft skin uploader)")
                    .header("Accept", "application/json");
            if (key != null && !key.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + key);
            }
            HttpRequest request = requestBuilder
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                    .build();
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(20))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 429) {
                this.plugin.getLogger().warning("\u76ae\u80a4\u4e0a\u4f20\u88ab\u9650\u6d41(429)\uff1a" + file.getName()
                        + "\uff08\u514d\u8d39\u63a5\u53e3\u6709\u9891\u7387\u9650\u5236\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5\u6216\u914d\u7f6e skins.mineskin-key\uff09");
                return null;
            }
            if (response.statusCode() != 200) {
                this.plugin.getLogger().warning("\u76ae\u80a4\u4e0a\u4f20\u5931\u8d25(HTTP " + response.statusCode() + "): "
                        + file.getName() + " - " + truncate(response.body(), 200));
                return null;
            }
            String value = match(P_VALUE, response.body());
            String signature = match(P_SIGNATURE, response.body());
            if (value == null) {
                String url = match(P_URL, response.body());
                this.plugin.getLogger().warning("\u76ae\u80a4\u4e0a\u4f20\u54cd\u5e94\u5f02\u5e38: " + truncate(response.body(), 300));
                return null;
            }
            return new SkinData(value, signature == null ? "" : signature);
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            this.plugin.getLogger().warning("\u76ae\u80a4\u4e0a\u4f20\u4e2d\u65ad: " + file.getName());
            return null;
        }
        catch (Exception ex) {
            this.plugin.getLogger().warning("\u76ae\u80a4\u4e0a\u4f20\u5931\u8d25\uff08\u68c0\u67e5\u670d\u52a1\u5668\u662f\u5426\u80fd\u8bbf\u95ee api.mineskin.org\uff09: "
                    + file.getName() + " - " + ex.getMessage());
            return null;
        }
    }

    private static void writePart(java.io.ByteArrayOutputStream out, String boundary, String name, String value)
            throws IOException {
        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(value.getBytes(StandardCharsets.UTF_8));
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private static void writeFilePart(java.io.ByteArrayOutputStream out, String boundary, String name, String fileName,
                                      byte[] data) throws IOException {
        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"\r\n")
                .getBytes(StandardCharsets.UTF_8));
        out.write("Content-Type: image/png\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        out.write(data);
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private static String match(Pattern pattern, String body) {
        if (body == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(body);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max);
    }

    private static String hashOf(File file) {
        try (InputStream in = Files.newInputStream(file.toPath())) {            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            StringBuilder sb = new StringBuilder();
            for (byte b : digest.digest()) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (Exception ex) {
            return null;
        }
    }
}
