import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

<#list modules as module>
import { ${module?cap_first}TableComponent } from './${module?lower_case}/${module?lower_case}-table.component';
</#list>

const routes: Routes = [
<#list modules as module>
  { path: '${module?lower_case}', component: ${module?cap_first}TableComponent },
</#list>
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
