# FIX Trading System

Two Java applications that speak FIX 4.2, the messaging standard of electronic trading, built for my bachelor's thesis in Software Systems (Vilnius University of Applied Sciences, 2024):

- **[`engine/`](engine/)**: the FIX acceptor. It runs the markets, keeps their order books, matches orders and answers every request with an execution report or a reject. Markets and orders are stored in MariaDB.
- **[`order-entry/`](order-entry/)**: the FIX initiator, a desktop client for sending orders and following them. Several clients can be connected to the engine at once.

Both are built on [QuickFIX/J](https://www.quickfixj.org/), which handles the FIX session layer, and started from its example applications (Banzai and OrderMatch).

## What it does

- Market and limit orders, for the day, good till cancelled (GTC) or good till a date (GTD); orders past their date are cancelled.
- New orders, cancels, cancel/replace, order status requests, market data snapshots and security status.
- Execution reports, order cancel rejects and business message rejects back to the client.
- Empty markets are filled with the previous day's prices from the [Polygon.io](https://polygon.io/) API.
- Swing screens with sortable, filterable tables: the engine's markets and orders (add, update and delete markets), and the client's orders, executions and markets, with CSV reports.
- Every session and message logged with Logback.

## Stack

Java 21, Maven, QuickFIX/J 2.3.1, Swing, MariaDB (JDBC), Jackson, Logback.

## Running it

You need Java 21, Maven and a MariaDB server.

1. **Database.** Create a database and a user for the engine, then load the tables from [`engine/schema.sql`](engine/schema.sql): `mariadb -u <user> -p <database> < engine/schema.sql`. The schema was rebuilt from the engine's SQL after the original database was gone.
2. **Settings.** Copy `engine/src/main/java/lt/vytzab/engine/Variables.java.example` to `Variables.java` in the same folder (it is git-ignored) and set the database URL and your Polygon.io API key. The database username and password are asked for in the terminal when the engine starts.
3. **Engine.** In `engine/`: `mvn compile exec:java`, or `mvn package` and then `java -jar target/engine.jar`. Start the session from the Session menu. It accepts the first client on port 9881 and the second on 9882 ([`engine.cfg`](engine/src/main/resources/engine.cfg)).
4. **Order entry.** In `order-entry/`: `mvn compile exec:java`, or `mvn package` and then `java -jar target/order-entry.jar`. Log on from the Session menu; it connects to `localhost:9881` as `ORDERENTRY1`. For a second client, pass the second config: `java -jar target/order-entry.jar src/main/resources/orderEntry2.cfg` (`ORDERENTRY2` on port 9882).

Both apps write their logs to a `logs/` folder in the directory they are started from.

## Known issues

The code is kept as it was submitted in January 2024. The one change is in `orderEntry.cfg`, where the last commit had commented out `FileStorePath`, so the client couldn't log on. The setting is restored. Remaining issues:

- Without a valid Polygon.io API key, seeding empty markets fails: the engine prints an error for each ticker and starts with no markets, which can then be added by hand.
- `MarketDAO.getAllHistoricMarketsForDate` and `getLatestMarketEntry` read a `symbol` column that `historic_market_data` doesn't have. Neither method is called by the app.

## History

The two apps were developed on separate branches (`Engine` and `OrderEntry`) from a shared starting point, and later merged into these folders with their full history.
