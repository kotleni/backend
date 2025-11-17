import { Injectable } from '@nestjs/common';
import { PrismaClient } from './generated/prisma/client';

@Injectable()
export class AppService {
  prisma = new PrismaClient();

  async getViewsCount(pageId: string): Promise<number> {
    const pageViews = await this.prisma.pageViews.findFirst({
      where: {
        pageId: pageId,
      },
    });
    if (pageViews) {
      return pageViews.count;
    }
    return 1;
  }

  async incrementViewsCount(pageId: string): Promise<void> {
    const viewCount = await this.getViewsCount(pageId);

    await this.prisma.pageViews.update({
      where: {
        pageId: pageId,
      },
      data: {
        count: viewCount + 1,
      },
    });
  }
}
