package org.Feature;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {
    private static final String API_URL = "https://query1.finance.yahoo.com/v8/finance/chart/%5EDJI";
    private static final OkHttpClient client = new OkHttpClient();
    public static final Queue<String> queue = new LinkedList<>();

    public static void main(String[] args) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        Runnable fetchTask = () -> {
            try {
                String MarketStatus = getMarketStatus();
                String stockValue = getStockValue();
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                queue.add("Timestamp: " + timestamp + ", Stock Value: $" + stockValue + ", MarketStatus : " + MarketStatus);
                System.out.println("Data added to queue: " + queue.peek());
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        executor.scheduleAtFixedRate(fetchTask, 0, 5, TimeUnit.SECONDS);
    }

    public static String getStockValue() throws IOException {
        Request request = new Request.Builder()
                .url(API_URL)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            JSONObject json = new JSONObject(response.body().string());
            JSONArray resultArray = json.getJSONObject("chart").getJSONArray("result");
            JSONObject indicators = resultArray.getJSONObject(0).getJSONObject("indicators");
            JSONArray quoteArray = indicators.getJSONArray("quote");
            JSONObject quote = quoteArray.getJSONObject(0);
            double stockValue = quote.getJSONArray("close").getDouble(0);
            return String.valueOf(stockValue);
        }
    }
    public static String getMarketStatus() {
        // Get the current time in Eastern Time
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        String currentTime = dateFormat.format(new Date());

        // Define market open and close times
        String marketOpenTime = "09:30";
        String marketCloseTime = "16:00";

        // Compare the current time with market open and close times
        if (currentTime.compareTo(marketOpenTime) >= 0 && currentTime.compareTo(marketCloseTime) <= 0) {
            return "Market is Open";
        } else {
            return "Market is Closed";
        }
    }

}
