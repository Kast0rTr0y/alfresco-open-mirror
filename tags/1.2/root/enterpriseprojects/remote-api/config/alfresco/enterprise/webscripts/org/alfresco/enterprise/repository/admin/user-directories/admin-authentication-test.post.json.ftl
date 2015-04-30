<#escape x as jsonUtils.encodeJSONString(x)>
{
   "userName": "${userName}",
   "authenticatorName": "${authenticatorName}",
<#if authenticationMessage??>
   "authenticationMessage": "${authenticationMessage}",
</#if>
<#if diagnostic??>
   "diagnostic":
   [
   <#list diagnostic.steps as s>
      {
         "success": ${s.success?string},
         "message": "${s.message}"
      }<#if s_has_next>,</#if>
   </#list>
   ],
</#if>
   "testPassed": ${testPassed?string}
}
</#escape>