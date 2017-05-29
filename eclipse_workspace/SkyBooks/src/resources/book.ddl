CREATE TABLE IF NOT EXISTS Book (
BookCode INTEGER UNIQUE NOT NULL PRIMARY KEY AUTOINCREMENT,
Title TEXT,
Author TEXT,
Publisher TEXT,
Subject TEXT,
Type INTEGER,
Date TEXT,
Language TEXT,
FileName TEXT,
Position REAL DEFAULT 0,
IsFixedLayout INTEGER DEFAULT 0,
IsGlobalPagination INTEGER DEFAULT 0,
IsDownloaded INTEGER DEFAULT 0,
FileSize INTEGER DEFAULT -1,
CustomOrder INTEGER DEFAULT 0,
URL	TEXT,
CoverURL TEXT,
DownSize INTEGER DEFAULT -1,
IsRead INTEGER DEFAULT 0,
LastRead Text,
IsRTL INTEGER DEFAULT 0,
IsVerticalWriting INTEGER DEFAULT 0,
Res0 INTEGER DEFAULT 0,
Res1 INTEGER DEFAULT 0,
Res2 INTEGER DEFAULT 0,
Etc	 TEXT,
Spread INTEGER DEFAULT 0,
Orientation INTEGER DEFAULT 0,
UserID TEXT,
IsFree INTEGER DEFAULT 0,
IsSample INTEGER DEFAULT 0,
ExpiredDate TEXT,
PurchaseDate TEXT,
CategoryID TEXT,
IsRated INTEGER DEFAULT 0
);

Create Index TitleIndex on Book(Title);
Create Index AuthorIndex on Book(Author);
Create Index LastReadIndex on Book(LastRead);
Create Index PurchaseDateIndex on Book(PurchaseDate);
