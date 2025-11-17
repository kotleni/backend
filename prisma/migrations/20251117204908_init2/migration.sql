/*
  Warnings:

  - You are about to drop the `PostViews` table. If the table is not empty, all the data it contains will be lost.

*/
-- DropTable
PRAGMA foreign_keys=off;
DROP TABLE "PostViews";
PRAGMA foreign_keys=on;

-- CreateTable
CREATE TABLE "PageViews" (
    "pageId" TEXT NOT NULL PRIMARY KEY,
    "count" INTEGER NOT NULL DEFAULT 1
);
