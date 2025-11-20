import {
  Controller,
  Get,
  HttpException,
  HttpStatus,
  Post,
  Query,
} from '@nestjs/common';
import { AppService } from './app.service';

@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {}

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
  async reportView(@Query() query: Record<string, string>): Promise<object> {
    const pageId = query['pageId'];
    if (!pageId) {
      throw new HttpException(
        'You should provide pageId parameter.',
        HttpStatus.BAD_REQUEST,
      );
    }
    const viewsCount = await this.appService.incrementViewsCount(pageId);
    return { count: viewsCount };
  }
}
