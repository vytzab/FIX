package lt.vytzab.engine.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lt.vytzab.engine.Variables;
import lt.vytzab.engine.market.Market;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MarketAPIService {
    private final String[] tickers = {"AAPL", "MSFT", "AMZN", "NVDA", "GOOG"};

    public List<Market> fetchMarketData() {
        List<Market> markets = new ArrayList<>();

        for (String ticker : tickers) {
            try {
                String aggUrl = "/prev?adjusted=true&apiKey=";
                String apiUrl = "https://api.polygon.io/v2/aggs/ticker/";
                String tickerApiUrl = apiUrl + ticker + aggUrl + Variables.getApiKey();
                URL url = new URI(tickerApiUrl).toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                try (Scanner scanner = new Scanner(connection.getInputStream())) {
                    scanner.useDelimiter("\\A");
                    String apiResponse = scanner.hasNext() ? scanner.next() : "";
                    Market market = parseApiResponseToMarket(apiResponse);
                    markets.add(market);
                }
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return markets;
    }

    public static Market parseApiResponseToMarket(String apiResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(apiResponse);

            String symbol = rootNode.path("ticker").asText();
            Double lastPrice = Math.floor(rootNode.path("results").get(0).path("c").asDouble() * 100) / 100;
            Double dayHigh = Math.floor(rootNode.path("results").get(0).path("h").asDouble() * 100) / 100;
            Double dayLow = Math.floor(rootNode.path("results").get(0).path("l").asDouble() * 100) / 100;

            double totalVolume = rootNode.path("results").get(0).path("v").asDouble();
            Double buyVolume = Math.floor(totalVolume * 0.6);
            Double sellVolume = Math.floor(totalVolume * 0.4);

            return new Market(symbol, lastPrice, dayHigh, dayLow, buyVolume, sellVolume);
        } catch (IOException e) {
            e.printStackTrace();  // Handle the exception based on your application's needs
            return null;
        }
    }
}
