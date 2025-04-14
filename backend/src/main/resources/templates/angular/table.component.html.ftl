<table>
  <thead>
    <tr>
<#list fields as field>
      <th>${field.name}</th>
</#list>
    </tr>
  </thead>
  <tbody>
    <tr *ngFor="let item of ${className?lower_case}s">
<#list fields as field>
      <td>{{ item.${field.name} }}</td>
</#list>
    </tr>
  </tbody>
</table>
