package lt.vytzab.engine.market.workers;

import lt.vytzab.engine.dao.MarketDataDAO;
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
        // Perform background task (e.g., fetch updated market data)
        List<Market> updatedMarkets = fetchData(); // Implement this method based on your requirements

        // Publish the intermediate result to the process method
        publish(updatedMarkets);

        return null;
    }

    @Override
    protected void process(List<List<Market>> chunks) {
        // Process intermediate results on the Event Dispatch Thread
        if (!isCancelled()) {
            // Assuming you are updating the entire table
            tableModel.setMarkets(chunks.get(chunks.size() - 1));
        }
    }

    @Override
    protected void done() {
        // Executed on the Event Dispatch Thread after doInBackground is finished
        // You can perform any final UI updates or cleanup here
    }

    // Implement this method to fetch updated market data
    private List<Market> fetchData() {
        // Your implementation to fetch data (e.g., from a database or external source)
        return MarketDataDAO.readAllMarkets();
    }
}
