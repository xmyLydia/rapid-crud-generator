import { Component, OnInit } from '@angular/core';
import { ${className}Service } from './${className?lower_case}.service';

@Component({
  selector: 'app-${className?lower_case}-table',
  templateUrl: './${className?lower_case}-table.component.html'
})
export class ${className}TableComponent implements OnInit {
  ${className?lower_case}s: any[] = [];

  constructor(private service: ${className}Service) {}

  ngOnInit(): void {
    this.service.getAll().subscribe(data => {
      this.${className?lower_case}s = data;
    });
  }
}
