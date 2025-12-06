import { Injectable } from '@nestjs/common';

interface IpRecord {
  ip: string;
  timestamp: number;
}

@Injectable()
export class ViewsCacheService {
  private lastIps: IpRecord[] = [];
  private readonly TTL_MS = 1000 * 60 * 60; // 1 hour

  addIp(ip: string) {
    const now = Date.now();
    this.lastIps.push({ ip, timestamp: now });
  }

  exist(ip: string): boolean {
    const now = Date.now();
    this.lastIps = this.lastIps.filter(
      (record) => now - record.timestamp < this.TTL_MS,
    );

    return this.lastIps.some((record) => record.ip === ip);
  }
}
