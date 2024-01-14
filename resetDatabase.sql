-- DROP DATABSE DB_MTCG
DROP TABLE IF EXISTS Users CASCADE;
DROP TABLE IF EXISTS Cards CASCADE;
DROP TABLE IF EXISTS Package CASCADE;
DROP TABLE IF EXISTS Deck CASCADE;
DROP TABLE IF EXISTS Trading CASCADE;
DROP TABLE IF EXISTS Battle CASCADE;
DROP TABLE IF EXISTS Battle_Log CASCADE;
DROP TABLE IF EXISTS Tokens CASCADE;

-- CREATE DATABASE DB_MTCG;

-- create tables
CREATE TABLE Users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR UNIQUE NOT NULL,
    name VARCHAR DEFAULT NULL,
    bio VARCHAR DEFAULT NULL,
    image VARCHAR DEFAULT NULL,
    elo INT NOT NULL DEFAULT 100,
    wins INT NOT NULL Default 0,
    losses INT NOT NULL Default 0,
    coins INT NOT NULL Default 20,
    password VARCHAR NOT NULL
);

CREATE TABLE Tokens (
    token_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    token VARCHAR NOT NULL
);

CREATE TABLE Cards (
    card_id VARCHAR PRIMARY KEY,
    card_name VARCHAR NOT NULL,
    card_type VARCHAR NOT NULL,
    damage INT NOT NULL,
    user_id INT NULL,
    package_id INT NULL,
    deck_id INT NULL,
    trading_id VARCHAR NULL
);

CREATE TABLE Package (
    package_id SERIAL PRIMARY KEY
);

CREATE TABLE Deck (
    deck_id SERIAL PRIMARY KEY
);

CREATE TABLE Trading (
    trading_id VARCHAR PRIMARY KEY,
    card_to_trade VARCHAR NOT NULL,
    card_type VARCHAR NOT NULL,
    minimum_damage INT NOT NULL
);

CREATE TABLE Battle (
    battle_id SERIAL PRIMARY KEY,
    user1_id INT NULL,
    user2_id INT NULL,
    battle_status BOOLEAN DEFAULT FALSE,
    active_battle_timestamp TIMESTAMP  NOT NULL DEFAULT (NOW() + interval '30 seconds')
);

CREATE TABLE Battle_Log (
    battle_log_id SERIAL PRIMARY KEY,
    battle_id INT NOT NULL,
    log VARCHAR NULL
);

-- alter tables
ALTER TABLE Cards
ADD CONSTRAINT package_id_fk
FOREIGN KEY (package_id)
REFERENCES Package (package_id)
ON DELETE CASCADE;

ALTER TABLE Cards
ADD CONSTRAINT deck_id_fk
FOREIGN KEY (deck_id)
REFERENCES Deck (deck_id)
ON DELETE CASCADE;

ALTER TABLE Cards
ADD CONSTRAINT user_id_fk
FOREIGN KEY (user_id)
REFERENCES Users (user_id)
ON DELETE CASCADE;

ALTER TABLE Cards
ADD CONSTRAINT trading_id_fk
FOREIGN KEY (trading_id)
REFERENCES Trading (trading_id)
ON DELETE CASCADE;

ALTER TABLE Tokens
ADD CONSTRAINT user_id_fk
FOREIGN KEY (user_id)
REFERENCES Users (user_id)
ON DELETE CASCADE;


ALTER TABLE Battle_Log
ADD CONSTRAINT battle_id_fk
FOREIGN KEY (battle_id)
REFERENCES Battle (battle_id)
ON DELETE CASCADE;

SET TIMEZONE = 'Europe/Berlin';