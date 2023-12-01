package lt.vytzab.engine;

import java.util.HashMap;

public class MarketCreator {

    public static HashMap<String, Market> createMarkets() {
        HashMap<String, Market> markets = new HashMap<>();

        String[] symbols = new String[]{"AAPL","MSFT","GOOG","GOOGL","AMZN","NVDA","BRK.B","META",
                "TSLA","LLY","V","UNH","JNJ","XOM","JPM","WMT","MA","PG","AVGO","HD","ORCL","CVX","MRK",
                "ABBV","KO","PEP","COST","ADBE","BAC","CSCO","PFE","TMO","MCD","ACN","CRM","CMCSA","DHR",
                "LIN","ABT","NFLX","AMD","NKE","TMUS","DIS","WFC","TXN","PM","UPS","MS","COP","AMGN","CAT",
                "VZ","UNP","NEE","INTC","BA","INTU","BMY","IBM","LOW","RTX","HON","QCOM","GE","SPGI","AMAT",
                "AXP","DE","PLD","LMT","SBUX","NOW","BKNG","ELV","MDT","SCHW","GS","SYK","ADP","TJX","ISRG",
                "T","BLK","MDLZ","GILD","MMC","VRTX","ADI","REGN","LRCX","CVS","ETN","ZTS","SLB","AMT",
                "CB","CI","C","BDX"};

        for (String symbol : symbols) {
            markets.put(symbol, new Market());
        }

        return markets;
    }
}
