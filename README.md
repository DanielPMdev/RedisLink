# 🔗 RedisLink

<div align="center">

<img src="Docs/images/redislink.png" alt="RedisLink Logo" width="80"/>

![RedisLink Logo](https://img.shields.io/badge/RedisLink-URL%20Shortener-pink?style=for-the-badge&logo=link&logoColor=white)

**Acortador de URLs profesional Full-Stack**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-20.3.5-red?style=flat&logo=angular)](https://angular.io/)
[![Redis](https://img.shields.io/badge/Redis-8.2+-dc382d?style=flat&logo=redis&logoColor=white)](https://redis.io/)
[![Java](https://img.shields.io/badge/Java-21-orange?style=flat&logo=openjdk)](https://openjdk.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.0+-blue?style=flat&logo=typescript)](https://www.typescriptlang.org/)
[![TailwindCSS](https://img.shields.io/badge/TailwindCSS-3.0-38bdf8?style=flat&logo=tailwindcss)](https://tailwindcss.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

[Características](#-características) •
[Tecnologías](#️-tecnologías) •
[Instalación](#-instalación) •
[Uso](#-uso) •
[API](#-api-rest) •
[Contribuir](#-contribuir)

</div>

---

## 📖 Descripción

**RedisLink** es un acortador de URLs moderno y eficiente. Combina la velocidad de **Redis** con la robustez de **Spring Boot** en el backend y la modernidad de **Angular 20** con **TailwindCSS** en el frontend.

Perfecto para acortar enlaces largos, rastrear clics y obtener estadísticas detalladas en tiempo real.

---

## ✨ Características

### Backend (RedisLinkAPI)
- ⚡ **Ultra rápido**: Tiempos de respuesta < 5ms gracias a Redis
- 🔤 **Códigos legibles**: Generación automática de códigos basados en palabras (ej: `rich-violet-osprey`)
- 📊 **Contador de clics**: Seguimiento en tiempo real de cada URL acortada
- ⏰ **TTL automático**: URLs temporales con expiración configurable
- 🔄 **Gestión de colisiones**: Sistema inteligente de fallback
- 🧪 **Testing robusto**: 154 tests con 85%+ de cobertura
- 🏗️ **Arquitectura limpia**: Patrón en capas (Controller → Service → Repository)

### Frontend (RedisLinkWeb)
- 🎨 **Diseño moderno**: Interfaz con glassmorphism y degradados premium
- 📱 **100% Responsive**: Adaptado a móvil, tablet y desktop
- 📈 **Dashboard de estadísticas**: Análisis detallado con:
  - Total de clics
  - Fecha de creación y antigüedad
  - Promedio de clics diarios
  - Nivel de popularidad
  - Barra de progreso visual
- 📋 **Copiar con un click**: Funcionalidad de portapapeles integrada
- 🔍 **Búsqueda por código**: Consulta estadísticas de cualquier URL
- ⚠️ **Manejo de errores**: Mensajes amigables y descriptivos

---

## 🛠️ Tecnologías

### Backend
| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.5.6 | Framework backend |
| Redis | 7.0+ | Base de datos en memoria |
| Maven | 3.9+ | Gestión de dependencias |
| JUnit 5 | 5.10+ | Testing unitario |
| Mockito | 5.0+ | Mocking para tests |
| Lombok | 1.18+ | Reducción de boilerplate |
| Bean Validation | 3.0 | Validación de datos |

### Frontend
| Tecnología | Versión | Uso |
|------------|---------|-----|
| Angular | 20.3.5 | Framework frontend |
| TypeScript | 5.0+ | Lenguaje tipado |
| TailwindCSS | 3.0 | Framework CSS |
| RxJS | 7.0+ | Programación reactiva |
| HttpClient | - | Comunicación con API |

---

## 📋 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

- ✅ **Java 21** o superior ([Descargar](https://adoptium.net/))
- ✅ **Node.js 18+** y npm ([Descargar](https://nodejs.org/))
- ✅ **Redis Server 7.0+** ([Descargar](https://redis.io/download))
- ✅ **Angular CLI 20+** (`npm install -g @angular/cli`)
- ✅ **Git** ([Descargar](https://git-scm.com/))

---

## 🚀 Instalación

### 1️⃣ Clonar el Repositorio

```bash
git clone https://github.com/DanielPMdev/RedisLink.git
cd RedisLink
```

### 2️⃣ Configurar Redis

**Redis Docker:**
```bash
docker run -d -p 6379:6379 redis:8.2-alpine
```

### 3️⃣ Configurar el Backend

```bash
cd RedisLinkAPI

# Opción 1: Usar Maven Wrapper (recomendado)
./mvnw clean install

# Opción 2: Usar Maven instalado globalmente
mvn clean install

# Ejecutar el backend
./mvnw spring-boot:run
```

El backend estará disponible en: **http://localhost:8080**

**Endpoints principales:**
- API Base: `http://localhost:8080/api/v1/urls`

### 4️⃣ Configurar el Frontend

Abre una **nueva terminal** y ejecuta:

```bash
cd RedisLinkWeb

# Instalar dependencias
npm install

# Iniciar el servidor de desarrollo
ng serve --open
```

El frontend estará disponible en: **http://localhost:4200**

---

## 🎯 Uso

### Acortar una URL

1. Abre el navegador en `http://localhost:4200`
2. Ingresa una URL larga en el campo de texto
3. Click en el botón **"Cortar"**
4. ¡Listo! Copia tu URL corta

### Ver Estadísticas

**Opción 1: Desde la página principal**
1. Después de acortar una URL, click en **"Ver Estadísticas Completas"**

**Opción 2: Desde el menú de estadísticas**
1. Click en **"Estadísticas"** en el menú superior
2. Ingresa el código corto (ej: `rich-violet-osprey`)
3. Click en **"Buscar"**

### Usar la URL Corta

Cuando alguien visite tu URL corta:
- Será redirigido automáticamente a la URL original
- El contador de clics se incrementará

---

## 📡 API REST

### Base URL
```
http://localhost:8080/api/v1/urls
```

### Endpoints

#### 1. Acortar URL
```http
POST /api/v1/urls/short
Content-Type: application/json

{
    "url": "https://www.vocesdecuenca.com/provincia/tarancon/la-guardia-civil-detiene-a-tres-personas-como-presuntos-autores-de-los-robos-en-el-cementerio-de-tarancon/"
}
```

**Respuesta exitosa (200 OK):**
```json
{
    "successful": true,
    "message": "Link successfully created",
    "link": {
        "long_url": "https://www.lasnoticiasdecuenca.es/provincia/cuatro-pueblos-se-ofrecen-para-acoger-nuevos-parque-bomberos-cuenca--43525",
        "short": "rich-violet-osprey",
        "hits": 0,
        "created_at": "2025-10-21T13:30:12.5459048"
    },
    "shortUrl": "http://localhost:8080/api/v1/urls/rich-violet-osprey"
}
```

#### 2. Redirección
```http
GET /api/v1/urls/{shortCode}
```

**Respuesta:**
- HTTP 301 (Redirect permanente) a la URL original
- Incrementa el contador de clics

#### 3. Obtener Estadísticas
```http
GET /api/v1/urls/{shortCode}/stats
```

**Respuesta exitosa (200 OK):**
```json
{
    "long_url": "https://www.vocesdecuenca.com/provincia/un-guardia-civil-fuera-de-servicio-salva-la-vida-a-un-hombre-en-villarejo-de-fuentes/",
    "short": "stark-bronze-deer",
    "hits": 4,
    "created_at": "2025-10-13T11:19:08.9804202"
}
```


## 🧪 Ejecutar Tests

### Backend
```bash
cd RedisLinkAPI

# Ejecutar todos los tests
./mvnw test
```


---

## 🔧 Configuración Avanzada

### Backend (application.properties)

```properties
# Redis Configuration
spring.redis.host=localhost
spring.redis.port=6379
```

### Frontend (environment.ts)

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1/urls'
};
```

---

## 📁 Estructura del Proyecto

```
RedisLink/
├── RedisLinkAPI/                    # Backend Spring Boot
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── danielpm/dev/redislink/
│   │   │   │       ├── controller/  # API Controllers
│   │   │   │       ├── service/     # Business Logic
│   │   │   │       ├── repository/  # Data Access
│   │   │   │       ├── model/       # DTOs y Entities
|   |   |   |       ├── exceptions/  # Exceptions
|   |   |   |       ├── util/        # Utilities
│   │   │   │       └── config/      # Configuración
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/                    # Tests unitarios
│   └── pom.xml
│
└── RedisLinkWeb/                    # Frontend Angular
    ├── src/
    │   ├── app/
    │   │   ├── core/
    │   │   │   ├── models/          # Interfaces TypeScript
    │   │   │   └── services/        # Servicios HTTP
    │   │   ├── features/
    │   │   │   ├── home/            # Página principal
    │   │   │   └── stats/           # Dashboard estadísticas
    │   │   └── shared/
    │   │       └── components/      # Componentes reutilizables
    │   │
    |   ├── environmets/             #Entornos
    │   ├── index.html
    │   ├── main.ts
    │   └── styles.css
    ├── angular.json
    ├── package.json
    ├── tailwind.config.js
    └── README.md
```

---

## 🎨 Capturas de Pantalla

### Landing Page
![Landing Page](https://raw.githubusercontent.com/DanielPMdev/RedisLink/main/Docs/images/landing.png)

### Dashboard de Estadísticas
![Dashboard](https://raw.githubusercontent.com/DanielPMdev/RedisLink/main/Docs/images/stats.png)

### DEMO
![Demo](https://raw.githubusercontent.com/DanielPMdev/RedisLink/main/Docs/demo/demo.mkv)
---

## 🛣️ Roadmap

- [ ] Autenticación con JWT
- [ ] Panel de administración
- [ ] URLs personalizadas (custom slugs)
- [ ] QR Code generator
- [ ] Análisis avanzado (geolocalización, dispositivos)
- [ ] Rate limiting con Redis
- [ ] Tests E2E con Playwright
- [ ] Modo oscuro/claro
- [ ] Exportar estadísticas (CSV, PDF)

---

## 🤝 Contribuir

¡Las contribuciones son bienvenidas! Si quieres mejorar RedisLink:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/nueva-caracteristica`)
3. Commit tus cambios (`git commit -m 'Añade nueva característica'`)
4. Push a la rama (`git push origin feature/nueva-caracteristica`)
5. Abre un Pull Request

### Guías de Contribución

- Sigue las convenciones de código existentes
- Añade tests para nuevas funcionalidades
- Actualiza la documentación si es necesario
- Asegúrate de que todos los tests pasen

---

## 📝 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo [LICENSE](LICENSE) para más detalles.

---

## 👨‍💻 Autor

**Daniel PM**
- GitHub: [@DanielPMdev](https://github.com/DanielPMdev)
- LinkedIn: [Daniel PM](https://linkedin.com/in/danielpmdev/)

---

<div align="center">

**⭐ Si te gusta este proyecto, dale una estrella en GitHub ⭐**

Hecho con ❤️ por [Daniel PM](https://github.com/DanielPMdev)

</div>