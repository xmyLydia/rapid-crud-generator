import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class ${className}Service {
  constructor(private http: HttpClient) {}

  getAll() {
    return this.http.get('/api/${className?lower_case}');
  }

  create(data: any) {
    return this.http.post('/api/${className?lower_case}', data);
  }
}
