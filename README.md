# FIX Engine & Order Entry System

Java-based FIX protocol implementation using QuickFIX/J.
Includes both a FIX Acceptor (Engine) and FIX Initiator (OrderEntry).

## Technologies
- Java
- Maven
- QuickFIX/J
- Swing (GUI)
- SLF4J

## Features
- FIX session management
- Order entry GUI
- Acceptor and Initiator implementations
- Configurable FIX settings

## How to Run

Engine is the acceptor, meaning it listens for incoming connections.

OrderEntry is the initiator client that connects to the engine (can use multiple).

Config via .cfg settings

QuickFIX/J factories for store/log/message

Session lifecycle: logon, logout

Separation of UI vs FIX engine
