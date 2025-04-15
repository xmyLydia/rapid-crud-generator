<h2 style="text-align: center; margin-top: 16px;">Admin Dashboard</h2>
<nav style="display: flex; gap: 16px; justify-content: center; margin: 16px 0;">
<#list modules as module>
  <a routerLink="/${module?lower_case}" routerLinkActive="active">${module?cap_first}</a>
</#list>
</nav>
<router-outlet></router-outlet>
