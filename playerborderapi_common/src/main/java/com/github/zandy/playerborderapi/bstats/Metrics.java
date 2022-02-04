package com.github.zandy.playerborderapi.bstats;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;

@SuppressWarnings("all")
public class Metrics {

    //THIS IS CLASS IS SHADED!!!
    //Original author: bStats.org
    //This is a cut-down (simplified version of the original bStats Metrics class)
    static {
        if (System.getProperty("bstats.relocatecheck") == null
                || !System.getProperty("bstats.relocatecheck").equals("false")) {
            final String defaultPackage = new String(
                    new byte[]{'o', 'r', 'g', '.', 'b', 's', 't', 'a', 't', 's', '.', 'b', 'u', 'k', 'k', 'i', 't'});
            final String examplePackage = new String(
                    new byte[]{'y', 'o', 'u', 'r', '.', 'p', 'a', 'c', 'k', 'a', 'g', 'e'});
            if (Metrics.class.getPackage().getName().equals(defaultPackage)
                    || Metrics.class.getPackage().getName().equals(examplePackage)) {
                throw new IllegalStateException("bStats Metrics class has not been relocated correctly!");
            }
        }
    }
    public static final int B_STATS_VERSION = 1;
    private static final String URL = "https://bStats.org/submitData/bukkit";
    private static boolean logFailedRequests;
    private static String serverUUID;
    private final JavaPlugin plugin;
    private final List<CustomChart> charts = new ArrayList<>();

    public Metrics(JavaPlugin plugin) {
        if (plugin == null) throw new IllegalArgumentException("Plugin cannot be null!");
        this.plugin = plugin;
        File cfgFile = new File(new File(plugin.getDataFolder().getParentFile(), "bStats"), "config.yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(cfgFile);
        if (!cfg.isSet("serverUuid")) {
            cfg.addDefault("enabled", true);
            cfg.addDefault("serverUuid", UUID.randomUUID().toString());
            cfg.addDefault("logFailedRequests", false);
            cfg.options().header(
                    "bStats collects some data for plugin authors like how many servers are using their plugins.\n" +
                            "To honor their work, you should not disable it.\n" +
                            "This has nearly no effect on the server performance!\n" +
                            "Check out https://bStats.org/ to learn more :)"
            ).copyDefaults(true);
            try {
                cfg.save(cfgFile);
            } catch (Exception ignored) {}
        }
        serverUUID = cfg.getString("serverUuid");
        logFailedRequests = cfg.getBoolean("logFailedRequests", false);
        if (cfg.getBoolean("enabled", true)) {
            boolean found = false;
            for (Class<?> service : Bukkit.getServicesManager().getKnownServices()) {
                try {
                    service.getField("B_STATS_VERSION");
                    found = true;
                    break;
                } catch (NoSuchFieldException ignored) { }
            }
            Bukkit.getServicesManager().register(Metrics.class, this, plugin, ServicePriority.Normal);
            if (!found) startSubmitting();
        }
    }

    public void addCustomChart(CustomChart chart) {
        if (chart == null) throw new IllegalArgumentException("Chart cannot be null!");
        charts.add(chart);
    }

    private void startSubmitting() {
        final Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!plugin.isEnabled()) {
                    timer.cancel();
                    return;
                }
                Bukkit.getScheduler().runTask(plugin, () -> submitData());
            }
        }, 300000, 1800000);
    }

    public JSONObject getPluginData() {
        JSONObject data = new JSONObject();
        data.put("pluginName", plugin.getDescription().getName());
        data.put("pluginVersion", plugin.getDescription().getVersion());
        JSONArray customCharts = new JSONArray();
        for (CustomChart customChart : charts) {
            JSONObject chart = customChart.getRequestJsonObject();
            if (chart == null) continue;
            customCharts.add(chart);
        }
        data.put("customCharts", customCharts);
        return data;
    }

    private JSONObject getServerData() {
        String bukkitVersion = org.bukkit.Bukkit.getVersion();
        bukkitVersion = bukkitVersion.substring(bukkitVersion.indexOf("MC: ") + 4, bukkitVersion.length() - 1);
        JSONObject data = new JSONObject();
        data.put("serverUUID", serverUUID);
        data.put("playerAmount", Bukkit.getOnlinePlayers().size());
        data.put("onlineMode", Bukkit.getOnlineMode() ? 1 : 0);
        data.put("bukkitVersion", bukkitVersion);
        data.put("javaVersion", System.getProperty("java.version"));
        data.put("osName", System.getProperty("os.name"));
        data.put("osArch", System.getProperty("os.arch"));
        data.put("osVersion", System.getProperty("os.version"));
        data.put("coreCount", Runtime.getRuntime().availableProcessors());
        return data;
    }

    private void submitData() {
        final JSONObject data = getServerData();
        JSONArray pluginData = new JSONArray();
        for (Class<?> service : Bukkit.getServicesManager().getKnownServices()) {
            try {
                service.getField("B_STATS_VERSION");
                Bukkit.getServicesManager().getRegistrations(service).forEach(provider -> {
                    try {
                        pluginData.add(
                                provider.getService().getMethod("getPluginData").invoke(provider.getProvider()));
                    } catch (Exception ignored) {}
                });
            } catch (Exception ignored) {}
        }
        data.put("plugins", pluginData);
        new Thread(() -> {
            try {
                sendData(data);
            } catch (Exception e) {
                if (logFailedRequests) plugin.getLogger()
                        .log(Level.WARNING, "Could not submit plugin stats of " + plugin.getName(), e);
            }
        }).start();
    }

    private static void sendData(JSONObject data) throws Exception {
        if (data == null) throw new IllegalArgumentException("Data cannot be null!");
        if (Bukkit.isPrimaryThread())
            throw new IllegalAccessException("This method must not be called from the main thread!");
        HttpsURLConnection connection = (HttpsURLConnection) new URL(URL).openConnection();
        byte[] compressedData = compress(data.toString());
        connection.setRequestMethod("POST");
        connection.addRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Connection", "close");
        connection.addRequestProperty("Content-Encoding", "gzip");
        connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "MC-Server/" + B_STATS_VERSION);
        connection.setDoOutput(true);
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.write(compressedData);
        outputStream.flush();
        outputStream.close();
        connection.getInputStream().close();
    }

    private static byte[] compress(final String str) throws IOException {
        if (str == null) return null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(outputStream);
        gzip.write(str.getBytes("UTF-8"));
        gzip.close();
        return outputStream.toByteArray();
    }

    public static abstract class CustomChart {
        final String chartId;

        CustomChart(String chartId) {
            if (chartId == null || chartId.isEmpty())
                throw new IllegalArgumentException("ChartId cannot be null or empty!");
            this.chartId = chartId;
        }

        private JSONObject getRequestJsonObject() {
            JSONObject chart = new JSONObject();
            chart.put("chartId", chartId);
            try {
                JSONObject data = getChartData();
                if (data == null) return null;
                chart.put("data", data);
            } catch (Throwable t) {
                if (logFailedRequests) Bukkit.getLogger()
                        .log(Level.WARNING, "Failed to get data for custom chart with id " + chartId, t);
                return null;
            }
            return chart;
        }

        protected abstract JSONObject getChartData() throws Exception;
    }

    public static class SimplePie extends CustomChart {
        private final Callable<String> callable;

        public SimplePie(String chartId, Callable<String> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JSONObject getChartData() throws Exception {
            String value = callable.call();
            if (value == null || value.isEmpty()) return null;
            JSONObject data = new JSONObject();
            data.put("value", value);
            return data;
        }
    }

    public static class AdvancedPie extends CustomChart {
        private final Callable<Map<String, Integer>> callable;

        public AdvancedPie(String chartId, Callable<Map<String, Integer>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JSONObject getChartData() throws Exception {
            Map<String, Integer> map = callable.call();
            if (map == null || map.isEmpty()) return null;
            boolean allSkipped = true;
            JSONObject data = new JSONObject();
            JSONObject values = new JSONObject();
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue() == 0) continue;
                allSkipped = false;
                values.put(entry.getKey(), entry.getValue());
            }
            if (allSkipped) return null;
            data.put("values", values);
            return data;
        }
    }

    public static class DrilldownPie extends CustomChart {
        private final Callable<Map<String, Map<String, Integer>>> callable;

        public DrilldownPie(String chartId, Callable<Map<String, Map<String, Integer>>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        public JSONObject getChartData() throws Exception {
            Map<String, Map<String, Integer>> map = callable.call();
            if (map == null || map.isEmpty()) return null;
            boolean reallyAllSkipped = true;
            JSONObject data = new JSONObject();
            JSONObject values = new JSONObject();
            for (Map.Entry<String, Map<String, Integer>> entryValues : map.entrySet()) {
                JSONObject value = new JSONObject();
                boolean allSkipped = true;
                for (Map.Entry<String, Integer> valueEntry : map.get(entryValues.getKey()).entrySet()) {
                    value.put(valueEntry.getKey(), valueEntry.getValue());
                    allSkipped = false;
                }
                if (!allSkipped) {
                    reallyAllSkipped = false;
                    values.put(entryValues.getKey(), value);
                }
            }
            if (reallyAllSkipped) return null;
            data.put("values", values);
            return data;
        }
    }

    public static class SingleLineChart extends CustomChart {
        private final Callable<Integer> callable;

        public SingleLineChart(String chartId, Callable<Integer> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JSONObject getChartData() throws Exception {
            int value = callable.call();
            if (value == 0) return null;
            JSONObject data = new JSONObject();
            data.put("value", value);
            return data;
        }

    }

    public static class MultiLineChart extends CustomChart {
        private final Callable<Map<String, Integer>> callable;

        public MultiLineChart(String chartId, Callable<Map<String, Integer>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JSONObject getChartData() throws Exception {
            Map<String, Integer> map = callable.call();
            if (map == null || map.isEmpty()) return null;
            JSONObject data = new JSONObject();
            JSONObject values = new JSONObject();
            boolean allSkipped = true;
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue() == 0) continue;
                allSkipped = false;
                values.put(entry.getKey(), entry.getValue());
            }
            if (allSkipped) return null;
            data.put("values", values);
            return data;
        }
    }

    public static class SimpleBarChart extends CustomChart {
        private final Callable<Map<String, Integer>> callable;

        public SimpleBarChart(String chartId, Callable<Map<String, Integer>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JSONObject getChartData() throws Exception {
            Map<String, Integer> map = callable.call();
            if (map == null || map.isEmpty()) return null;
            JSONObject data = new JSONObject();
            JSONObject values = new JSONObject();
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                JSONArray categoryValues = new JSONArray();
                categoryValues.add(entry.getValue());
                values.put(entry.getKey(), categoryValues);
            }
            data.put("values", values);
            return data;
        }
    }

    public static class AdvancedBarChart extends CustomChart {
        private final Callable<Map<String, int[]>> callable;

        public AdvancedBarChart(String chartId, Callable<Map<String, int[]>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JSONObject getChartData() throws Exception {
            Map<String, int[]> map = callable.call();
            if (map == null || map.isEmpty()) return null;
            JSONObject data = new JSONObject();
            JSONObject values = new JSONObject();
            boolean allSkipped = true;
            for (Map.Entry<String, int[]> entry : map.entrySet()) {
                if (entry.getValue().length == 0) continue;
                allSkipped = false;
                JSONArray categoryValues = new JSONArray();
                Arrays.stream(entry.getValue()).forEach(categoryValues::add);
                values.put(entry.getKey(), categoryValues);
            }
            if (allSkipped) return null;
            data.put("values", values);
            return data;
        }
    }
}