import { Controller, Get, Post, Query } from '@nestjs/common';
import { AppService } from './app.service';

@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {}

  @Get('/views')
  async geViews(@Query() query: Record<string, string>): Promise<object> {
    const pageId = query['pageId'];
    console.log(pageId);
    const viewsCount = await this.appService.getViewsCount(pageId);
    return { count: viewsCount };
  }

  @Post('/views/report')
  async reportView(@Query() query: Record<string, string>): Promise<object> {
    const pageId = query['pageId'];
    await this.appService.incrementViewsCount(pageId);
    return {};
  }
}
