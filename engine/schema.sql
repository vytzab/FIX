-- MariaDB schema for the engine, rebuilt in 2026 from the SQL in the dao package
-- (the original database no longer exists). Load it into the database named in
-- Variables.JDBC_URL:  mariadb fix < schema.sql

-- One row per market, with the current day's prices and volumes.
CREATE TABLE IF NOT EXISTS market_data (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    symbol      VARCHAR(16) NOT NULL UNIQUE,
    last_price  DOUBLE NOT NULL,
    day_high    DOUBLE NOT NULL,
    day_low     DOUBLE NOT NULL,
    buy_volume  DOUBLE NOT NULL DEFAULT 0,
    sell_volume DOUBLE NOT NULL DEFAULT 0
);

-- End-of-day snapshots of each market.
CREATE TABLE IF NOT EXISTS historic_market_data (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    market_id   INT NOT NULL,
    date        DATE NOT NULL,
    last_price  DOUBLE NOT NULL,
    day_high    DOUBLE NOT NULL,
    day_low     DOUBLE NOT NULL,
    buy_volume  DOUBLE NOT NULL,
    sell_volume DOUBLE NOT NULL,
    FOREIGN KEY (market_id) REFERENCES market_data (id) ON DELETE CASCADE
);

-- Every order the engine has accepted, open or done. side, ordType and tif hold
-- the one-character FIX values (Side, OrdType, TimeInForce); entryTime is epoch ms.
CREATE TABLE IF NOT EXISTS market_orders (
    clOrdID              VARCHAR(64) PRIMARY KEY,
    symbol               VARCHAR(16) NOT NULL,
    senderCompID         VARCHAR(32) NOT NULL,
    targetCompID         VARCHAR(32) NOT NULL,
    side                 CHAR(1) NOT NULL,
    ordType              CHAR(1) NOT NULL,
    price                DOUBLE NOT NULL,
    quantity             BIGINT NOT NULL,
    openQuantity         BIGINT NOT NULL,
    executedQuantity     BIGINT NOT NULL,
    avgExecutedPrice     DOUBLE NOT NULL,
    lastExecutedPrice    DOUBLE NOT NULL,
    lastExecutedQuantity BIGINT NOT NULL,
    entryTime            BIGINT NOT NULL,
    rejected             BOOLEAN NOT NULL,
    canceled             BOOLEAN NOT NULL,
    entryDate            DATE NOT NULL,
    goodTillDate         DATE NOT NULL,
    tif                  CHAR(1) NOT NULL,
    INDEX (symbol),
    INDEX (senderCompID)
);
