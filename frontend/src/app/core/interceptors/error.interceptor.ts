import { catchError } from 'rxjs/operators';
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { throwError } from 'rxjs';

export const ErrorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      let message = 'Unknown error';

      if (typeof err.error === 'string') {
        try {
          const parsed = JSON.parse(err.error);
          message = parsed.error || message;
        } catch {
          message = err.error;
        }
      }

      return throwError(() => message);
    })
  );
};
