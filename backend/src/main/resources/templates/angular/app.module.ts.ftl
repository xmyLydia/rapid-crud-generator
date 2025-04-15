import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

<#list modules as module>
import { ${module?cap_first}Module } from './${module?lower_case}/${module?lower_case}.module';
</#list>

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
<#list modules as module>
    ${module?cap_first}Module,
</#list>
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
