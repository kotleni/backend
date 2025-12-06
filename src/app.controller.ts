import {
  Controller,
  Get,
  HttpException,
  HttpStatus,
  Ip,
  Post,
  Query,
} from '@nestjs/common';
import { AppService } from './app.service';
import { ViewsCacheService } from './viewscache.service';

@Controller()
export class AppController {
  constructor(
    private readonly appService: AppService,
    private readonly viewsCacheService: ViewsCacheService,
  ) {}

  @Get('/views')
  async geViews(@Query() query: Record<string, string>): Promise<object> {
    const pageId: string | undefined = query['pageId'];
    if (!pageId) {
      throw new HttpException(
        'You should provide pageId parameter.',
        HttpStatus.BAD_REQUEST,
      );
    }
    const viewsCount = await this.appService.getViewsCount(pageId);
    return { count: viewsCount };
  }

  @Post('/views/report')
  async reportView(
    @Query() query: Record<string, string>,
    @Ip() ip: string,
  ): Promise<object> {
    const pageId = query['pageId'];
    if (!pageId) {
      throw new HttpException(
        'You should provide pageId parameter.',
        HttpStatus.BAD_REQUEST,
      );
    }
    if (this.viewsCacheService.exist(ip)) {
      const viewsCount = await this.appService.getViewsCount(pageId);
      return { count: viewsCount };
    }

    this.viewsCacheService.addIp(ip);
    const viewsCount = await this.appService.incrementViewsCount(pageId);
    return { count: viewsCount };
  }
}
