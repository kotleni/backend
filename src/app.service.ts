import { Injectable, Logger } from '@nestjs/common';
import { PrismaClient } from '@prisma/client';
import { PrismaPg } from '@prisma/adapter-pg';
import { Cron, CronExpression } from '@nestjs/schedule';

@Injectable()
export class AppService {
  private readonly logger = new Logger(AppService.name);
  private viewsBuffer = new Map<string, number>();

  private prisma = new PrismaClient({
    adapter: new PrismaPg({
      connectionString: process.env.DATABASE_URL,
    }),
  });

  async getViewsCount(pageId: string): Promise<number> {
    const pageViews = await this.prisma.pageViews.findFirst({
      where: { pageId: pageId },
    });

    const dbCount = pageViews ? pageViews.count : 0;
    const bufferCount = this.viewsBuffer.get(pageId) || 0;

    return dbCount + bufferCount;
  }

  async incrementViewsCount(pageId: string): Promise<number> {
    const currentBuffer = this.viewsBuffer.get(pageId) || 0;
    this.viewsBuffer.set(pageId, currentBuffer + 1);

    return this.getViewsCount(pageId);
  }

  @Cron(CronExpression.EVERY_30_SECONDS)
  async flushViewsToDatabase() {
    if (this.viewsBuffer.size === 0) return;

    this.logger.log(`Flushing ${this.viewsBuffer.size} pages to DB...`);

    const batchToProcess = new Map(this.viewsBuffer);
    this.viewsBuffer.clear();

    for (const [pageId, countToAdd] of batchToProcess) {
      try {
        await this.prisma.pageViews.upsert({
          where: { pageId: pageId },
          update: {
            count: { increment: countToAdd },
          },
          create: {
            pageId: pageId,
            count: countToAdd,
          },
        });
      } catch (error) {
        this.logger.error(`Failed to flush page ${pageId}`, error);

        const current = this.viewsBuffer.get(pageId) || 0;
        this.viewsBuffer.set(pageId, current + countToAdd);
      }
    }
  }
}
