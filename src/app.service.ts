import { Injectable } from '@nestjs/common';
import { PrismaClient } from '@prisma/client';

@Injectable()
export class AppService {
  prisma = new PrismaClient({} as any);

  async getViewsCount(pageId: string): Promise<number> {
    const pageViews = await this.prisma.pageViews.findFirst({
      where: {
        pageId: pageId,
      },
    });
    if (pageViews) {
      return pageViews.count;
    }
    return 0;
  }

  async incrementViewsCount(pageId: string): Promise<void> {
    const viewCount = await this.getViewsCount(pageId);

    if (viewCount > 0) {
      await this.prisma.pageViews.update({
        where: {
          pageId: pageId,
        },
        data: {
          count: viewCount + 1,
        },
      });
    } else {
      await this.prisma.pageViews.create({
        data: {
          pageId: pageId,
          count: 1,
        },
      });
    }
  }
}
