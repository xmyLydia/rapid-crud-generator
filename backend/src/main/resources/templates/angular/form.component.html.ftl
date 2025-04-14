<form [formGroup]="form" (ngSubmit)="submit()">
<#list fields as field>
  <label>${field.name}</label>
  <input formControlName="${field.name}" /><br />
</#list>
  <button type="submit">Submit</button>
</form>
