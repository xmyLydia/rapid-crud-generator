import { Component } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ${className}Service } from './${className?lower_case}.service';

@Component({
  selector: 'app-${className?lower_case}-form',
  templateUrl: './${className?lower_case}-form.component.html'
})
export class ${className}FormComponent {
  form: FormGroup;

  constructor(private fb: FormBuilder, private service: ${className}Service) {
    this.form = this.fb.group({
<#list fields as field>
      ${field.name}: [''],
</#list>
    });
  }

  submit(): void {
    this.service.create(this.form.value).subscribe();
  }
}
