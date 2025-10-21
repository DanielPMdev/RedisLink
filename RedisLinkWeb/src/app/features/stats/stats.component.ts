import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UrlShortenerService } from '../../core/services/url-shortener.service';
import { LinkStats } from '../../core/models/link-stats.model';
import { HeaderComponent } from '../../shared/components/header/header.component';
import { FooterComponent } from '../../shared/components/footer/footer.component';

@Component({
    selector: 'app-stats',
    standalone: true,
    imports: [CommonModule, FormsModule, HeaderComponent, FooterComponent],
    templateUrl: './stats.component.html',
    styleUrls: ['./stats.component.css']
})
export class StatsComponent implements OnInit {
    shortCode: string = '';
    stats: LinkStats | null = null;
    loading: boolean = false;
    error: string = '';
    searched: boolean = false;

    constructor(
        private urlShortenerService: UrlShortenerService,
        private route: ActivatedRoute,
        private router: Router
    ) { }

    ngOnInit(): void {
        // Si viene un shortCode por parámetro en la URL
        this.route.params.subscribe(params => {
            if (params['shortCode']) {
                this.shortCode = params['shortCode'];
                this.loadStats();
            }
        });

        // También podemos obtenerlo de query params: /stats?code=abc123
        this.route.queryParams.subscribe(params => {
            if (params['code'] && !this.shortCode) {
                this.shortCode = params['code'];
                this.loadStats();
            }
        });
    }

    loadStats(): void {
        if (!this.shortCode.trim()) {
            this.error = 'Por favor, ingresa un código corto válido';
            return;
        }

        this.loading = true;
        this.error = '';
        this.stats = null;
        this.searched = true;

        if (this.shortCode.startsWith("http://localhost:8080/api/v1/urls/")) {
            this.shortCode = this.extractShortUrlId(this.shortCode)
        }

        this.urlShortenerService.getUrlStats(this.shortCode).subscribe({
            next: (data) => {
                this.stats = data;
                this.loading = false;
            },
            error: (err) => {
                if (err.status === 404) {
                    this.error = 'No se encontró ninguna URL con ese código corto';
                } else {
                    this.error = 'Error al obtener las estadísticas. Por favor, intenta nuevamente.';
                }
                this.loading = false;
                console.error('Error:', err);
            }
        });
    }

    handleSearch(): void {
        this.loadStats();
    }

    handleKeyPress(event: KeyboardEvent): void {
        if (event.key === 'Enter') {
            this.handleSearch();
        }
    }

    formatDate(dateString: string): string {
        const date = new Date(dateString);
        return date.toLocaleDateString('es-ES', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    getTimeSinceCreation(dateString: string): string {
        const now = new Date();
        const created = new Date(dateString);
        const diffInMs = now.getTime() - created.getTime();

        const diffInMinutes = Math.floor(diffInMs / (1000 * 60));
        const diffInHours = Math.floor(diffInMs / (1000 * 60 * 60));
        const diffInDays = Math.floor(diffInMs / (1000 * 60 * 60 * 24));
        const diffInMonths = Math.floor(diffInDays / 30);
        const diffInYears = Math.floor(diffInDays / 365);

        if (diffInMinutes < 60) {
            return `Hace ${diffInMinutes} minuto${diffInMinutes !== 1 ? 's' : ''}`;
        } else if (diffInHours < 24) {
            return `Hace ${diffInHours} hora${diffInHours !== 1 ? 's' : ''}`;
        } else if (diffInDays < 30) {
            return `Hace ${diffInDays} día${diffInDays !== 1 ? 's' : ''}`;
        } else if (diffInMonths < 12) {
            return `Hace ${diffInMonths} mes${diffInMonths !== 1 ? 'es' : ''}`;
        } else {
            return `Hace ${diffInYears} año${diffInYears !== 1 ? 's' : ''}`;
        }
    }

    copyToClipboard(text: string): void {
        navigator.clipboard.writeText(text).then(() => {
            // Podrías agregar un toast notification aquí
            console.log('Copiado al portapapeles');
        });
    }

    goToHome(): void {
        this.router.navigate(['/']);
    }

    getClicksPercentage(): number {
        if (!this.stats) return 0;
        // Esto es solo visual, puedes ajustar la lógica según tus necesidades
        return Math.min((this.stats.hits / 100) * 100, 100);
    }

    get dailyAverage(): string {
        if (!this.stats || !this.stats.created_at) return '0.0';

        const days = Math.max(
            1,
            Math.floor((Date.now() - new Date(this.stats.created_at).getTime()) / (1000 * 60 * 60 * 24))
        );

        const avg = this.stats.hits / days;
        return avg.toFixed(1);
    }

    extractShortUrlId(fullUrl: string): string {
        // Asegurarse de que no haya slash al final
        fullUrl = fullUrl.replace(/\/$/, "");

        // Capturar todo después de la última barra
        const regex = /\/([^/]+)$/;
        const match = fullUrl.match(regex);

        return match ? match[1] : "";
    }
}