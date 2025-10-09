import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { registerLocaleData } from '@angular/common';
import localeUk from '@angular/common/locales/uk';
import { LOCALE_ID, ErrorHandler } from '@angular/core';

import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { AuthInterceptor } from './app/core/interceptors/auth.interceptor';
// import { GlobalErrorHandler } from './app/core/error/global-error-handler';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),                               
    provideHttpClient(withInterceptors([AuthInterceptor])),                              
    { provide: LOCALE_ID, useValue: 'uk' },              
    // { provide: ErrorHandler, useClass: GlobalErrorHandler }
  ]
}).catch(err => console.error(err));