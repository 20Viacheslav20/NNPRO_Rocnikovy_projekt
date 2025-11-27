import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'dateTime', standalone: true })
export class DateTimePipe implements PipeTransform {
    transform(value?: string | null): string {
        if (!value) return '-';
        try {
            const d = new Date(value);
            return `${String(d.getDate()).padStart(2, '0')}.${String(d.getMonth() + 1).padStart(2, '0')}.${d.getFullYear()} ` +
                `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
        } catch {
            return String(value);
        }
    }
}
