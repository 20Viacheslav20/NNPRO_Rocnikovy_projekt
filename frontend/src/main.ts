import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import {provideHttpClient, withInterceptors, withXsrfConfiguration} from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { registerLocaleData } from '@angular/common';
import localeUk from '@angular/common/locales/uk';
import { LOCALE_ID, ErrorHandler } from '@angular/core';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { AuthInterceptor } from './app/core/interceptors/auth.interceptor';
import { ErrorInterceptor } from './app/core/interceptors/error.interceptor';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([AuthInterceptor, ErrorInterceptor]),
      withXsrfConfiguration({ cookieName: 'XSRF-TOKEN', headerName: 'X-XSRF-TOKEN' })
    ),
    { provide: LOCALE_ID, useValue: 'uk' },
  ]
}).catch(err => console.error(err));
