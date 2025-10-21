import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ShortenRequest } from '../models/shorten-request.model';
import { ShortenResponse } from '../models/shorten-response.model';
import { LinkStats } from '../models/link-stats.model';

@Injectable({
  providedIn: 'root'
})
export class UrlShortenerService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  /**
   * Acorta una URL larga
   * POST /api/v1/urls/short
   */
  shortenUrl(request: ShortenRequest): Observable<ShortenResponse> {
    return this.http.post<ShortenResponse>(`${this.apiUrl}/short`, request);
  }

  /**
   * Obtiene las estadísticas de una URL corta
   * GET /api/v1/urls/{shortCode}/stats
   */
  getUrlStats(shortCode: string): Observable<LinkStats> {
    return this.http.get<LinkStats>(`${this.apiUrl}/${shortCode}/stats`);
  }
}