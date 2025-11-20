-- CreateTable
CREATE TABLE "PageViews" (
    "pageId" TEXT NOT NULL,
    "count" INTEGER NOT NULL DEFAULT 1,

    CONSTRAINT "PageViews_pkey" PRIMARY KEY ("pageId")
);
