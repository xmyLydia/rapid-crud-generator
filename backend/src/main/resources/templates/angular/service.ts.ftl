import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ${className}Service {
  constructor(private http: HttpClient) {}

  getAll(): Observable<any[]> {
    return this.http.get<any[]>('/api/${className?lower_case}');
  }

  create(data: any) {
    return this.http.post('/api/${className?lower_case}', data);
  }
}
