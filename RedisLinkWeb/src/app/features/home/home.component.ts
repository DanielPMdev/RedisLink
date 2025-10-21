import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UrlShortenerService } from '../../core/services/url-shortener.service';
import { ShortenRequest } from '../../core/models/shorten-request.model';
import { LinkStats } from '../../core/models/link-stats.model';
import { HeaderComponent } from '../../shared/components/header/header.component';
import { FooterComponent } from '../../shared/components/footer/footer.component';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent, FooterComponent],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent {
  longUrl: string = '';
  shortUrl: string = '';
  short: string = '';
  loading: boolean = false;
  error: string = '';
  copied: boolean = false;
  stats: LinkStats | null = null;
  showStats: boolean = false;

  constructor(
    private urlShortenerService: UrlShortenerService,
    private router: Router
  ) { }

  handleShorten(): void {
    if (!this.isValidUrl(this.longUrl.trim())) {
      this.error = 'Por favor, ingresa una URL válida';
      return;
    }

    this.loading = true;
    this.error = '';
    this.shortUrl = '';
    this.stats = null;
    this.showStats = false;

    const request: ShortenRequest = { url: this.longUrl };

    this.urlShortenerService.shortenUrl(request).subscribe({
      next: (response) => {
        this.shortUrl = response.shortUrl;
        this.short = response.link.short;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al procesar la solicitud. Verifica la URL e intenta nuevamente.';
        this.loading = false;
        console.error('Error:', err);
      }
    });
  }

  handleCopy(): void {
    navigator.clipboard.writeText(this.shortUrl).then(() => {
      this.copied = true;
      setTimeout(() => (this.copied = false), 2000);
    });
  }

  isValidUrl(url: string): boolean {
    try {
      new URL(url); // Intenta crear un objeto URL
      return true;  // Si no lanza error, es válida
    } catch (_) {
      return false; // Si lanza error, no es válida
    }
  }

  /*
  handleGetStats(): void {
    if (!this.short) return;

    this.loading = true;
    this.error = '';

    this.urlShortenerService.getUrlStats(this.short).subscribe({
      next: (data) => {      
        this.stats = {
          long_url: data.long_url,
          short: data.short,
          created_at: data.created_at,
          hits: data.hits
        };
        this.showStats = true;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al obtener las estadísticas';
        this.loading = false;
        console.error('Error:', err);
      }
    });
  }
    */

  handleKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      this.handleShorten();
    }
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('es-ES');
  }

  goToStats(): void {
    console.log('shortCode:', this.short);
    if (this.short) {
      this.router.navigate(['/stats', this.short]);
    }
  }
}
