package lt.vytzab.engine.market.workers;

import lt.vytzab.engine.api.MarketAPIService;
import lt.vytzab.engine.dao.MarketDAO;
import lt.vytzab.engine.market.Market;
import lt.vytzab.engine.market.MarketTableModel;

import javax.swing.*;
import java.util.List;

public class MarketFillWorker extends SwingWorker<Void, List<Market>> {
    private MarketTableModel tableModel;

    public MarketFillWorker(MarketTableModel tableModel) {
        this.tableModel = tableModel;
    }

    @Override
    protected Void doInBackground() throws Exception {
        List<Market> updatedMarkets = fetchData();

        publish(updatedMarkets);

        return null;
    }

    @Override
    protected void process(List<List<Market>> chunks) {
        if (!isCancelled()) {
            tableModel.setMarkets(chunks.get(chunks.size() - 1));
        }
    }

    @Override
    protected void done() {
    }

    private List<Market> fetchData() {
        MarketAPIService marketAPIService = new MarketAPIService();
        List<Market> markets = MarketDAO.readAllMarkets();
        if (markets.isEmpty()) {
            markets = marketAPIService.fetchMarketData();
            for (Market market : markets) {
                MarketDAO.createMarket(market);
            }
        }
        return markets;
    }
}
