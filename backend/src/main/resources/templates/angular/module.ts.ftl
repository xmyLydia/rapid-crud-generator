import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { ${className}TableComponent } from './${className?lower_case}-table.component';
import { ${className}FormComponent } from './${className?lower_case}-form.component';

@NgModule({
  declarations: [
    ${className}TableComponent,
    ${className}FormComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule
  ]
})
export class ${className}Module {}
