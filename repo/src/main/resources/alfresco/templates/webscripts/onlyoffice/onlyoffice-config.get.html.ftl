<#include "/org/alfresco/repository/admin/admin-template.ftl" />

<!--
    Copyright (c) Ascensio System SIA 2023. All rights reserved.
    http://www.onlyoffice.com
-->

<@page title=msg("onlyoffice-config.title") readonly=true>

</form>

<div style="margin: 1em 0 0.666em;" class="hidden">
    <span data-saved="${msg('onlyoffice-config.saved')}" data-error="${msg('onlyoffice-config.error')}" data-mixedcontent="${msg('onlyoffice-config.mixedcontent')}" data-jsonparse="${msg('onlyoffice-config.jsonparse')}" data-docservunreachable="${msg('onlyoffice-config.docservunreachable')}" data-docservcommand="${msg('onlyoffice-config.docservcommand')}" data-docservconvert="${msg('onlyoffice-config.docservconvert')}" data-jwterror="${msg('onlyoffice-config.jwterror')}" data-statuscode="${msg('onlyoffice-config.statuscode')}" id="onlyresponse" class="message hidden"></span>
</div>

<div class="column-left">
   <@section label=msg("onlyoffice-config.doc-section") />

   <form id="docservcfg" action="${url.service}" method="POST" accept-charset="utf-8">
      <div class="description section">${msg("onlyoffice-config.description")}</div>
      <div class="control text">
         <label class="label" for="onlyurl">${msg("onlyoffice-config.doc-url")}</label>
         <span class="value">
            <input id="onlyurl" name="url" size="35" placeholder="http://docserver/" title="${msg('onlyoffice-config.doc-url-tooltip')}" pattern="(http(s)?://.*)|(/.*)" value="${docurl}" />
         </span>
      </div>
      <div class="control text">
         <label class="label" for="jwtsecret">${msg("onlyoffice-config.jwt-secret")}</label>
         <span class="value">
            <input class="value" id="jwtsecret" name="url" size="35" value="${jwtsecret}" />
         </span>
      </div>

      <@tsection label=msg("onlyoffice-config.advanced-section")>
         <div class="control text">
            <label class="label" for="onlyinnerurl">${msg("onlyoffice-config.doc-url-inner")}</label>
            <span class="value">
               <input class="value" id="onlyinnerurl" name="innerurl" size="35" placeholder="http://docserver/" title="${msg('onlyoffice-config.doc-url-inner-tooltip')}" pattern="http(s)?://.*" value="${docinnerurl}" />
            </span>
         </div>
         <div class="control text">
            <label class="label" for="alfurl">${msg("onlyoffice-config.alf-url")}</label>
            <span class="value">
               <input class="value" id="alfurl" name="alfurl" size="35" placeholder="http://alfresco/" title="${msg('onlyoffice-config.alf-url-tooltip')}" pattern="http(s)?://.*" value="${alfurl}" />
            </span>
         </div>
      </@tsection>

      <@section label=msg("onlyoffice-config.common-section") />
      <div class="control field">
         <input class="value" id="onlycert" name="cert" type="checkbox" ${cert} />
         <label class="label" for="onlycert">${msg("onlyoffice-config.ignore-ssl-cert")}</label>
      </div>
      <div class="control field">
         <input class="value" id="webpreview" name="cert" type="checkbox" ${webpreview} />
         <label class="label" for="webpreview">${msg("onlyoffice-config.webpreview")}</label>
      </div>
      <div class="control field">
         <input class="value" id="convertOriginal" name="convertOriginal" type="checkbox" ${convertOriginal} />
         <label class="label" for="convertOriginal">${msg("onlyoffice-config.convert-original")}</label>
      </div>
      <div class="control field section">
          <label class="label">${msg("onlyoffice-config.file-type")}</label>
          <div style="padding-top: 4px">
              <input class="value" id="csv" name="csv" type="checkbox" ${formatCSV} />
              <label class="label" style="margin-right: 21px" for="csv">csv</label>
              <input class="value" id="odp" name="odp" type="checkbox" ${formatODP} />
              <label class="label" style="margin-right: 21px" for="odp">odp</label>
              <input class="value" id="ods" name="ods" type="checkbox" ${formatODS} />
              <label class="label" style="margin-right: 21px" for="ods">ods</label>
              <input class="value" id="odt" name="odt" type="checkbox" ${formatODT} />
              <label class="label" style="margin-right: 21px" for="odt">odt</label>
              <input class="value" id="rtf" name="rtf" type="checkbox" ${formatRTF} />
              <label class="label" style="margin-right: 21px" for="rtf">rtf</label>
              <input class="value" id="txt" name="txt" type="checkbox" ${formatTXT} />
              <label class="label" style="margin-right: 21px" for="txt">txt</label>
          </div>
      </div>

      <@section label=msg("onlyoffice-config.customization-section")/>
      <div class="control field">
          <input class="value" id="forcesave" name="forcesave" type="checkbox" ${forcesave} />
          <label class="label" for="forcesave">${msg("onlyoffice-config.forcesave")}</label>
      </div>
      <label class="control label">${msg("onlyoffice-config.customization-label")}</label>
      <div class="control field">
          <input class="value" id="chat" name="chat" type="checkbox" ${chat} />
          <label class="label" for="chat">${msg("onlyoffice-config.chat")}</label>
      </div>
      <div class="control field">
          <input class="value" id="compactHeader" name="compactHeader" type="checkbox" ${compactHeader} />
          <label class="label" for="compactHeader">${msg("onlyoffice-config.compact-header")}</label>
      </div>
      <div class="control field">
          <input class="value" id="feedback" name="feedback" type="checkbox" ${feedback} />
          <label class="label" for="feedback">${msg("onlyoffice-config.feedback")}</label>
      </div>
      <div class="control field">
          <input class="value" id="help" name="help" type="checkbox" ${help} />
          <label class="label" for="help">${msg("onlyoffice-config.help")}</label>
      </div>
      <div class="control field">
          <input class="value" id="toolbarNoTabs" name="toolbarNoTabs" type="checkbox" ${toolbarNoTabs} />
          <label class="label" for="toolbarNoTabs">${msg("onlyoffice-config.toolbar-no-tabs")}</label>
      </div>
      <div class="control field section">
          <p class="label">${msg("onlyoffice-config.review-mode-label")}</p>
          <div style="padding-top: 4px">
              <input class="value" id="reviewDisplayMarkup" name="reviewDisplay" type="radio" value="markup" <#if reviewDisplay == 'markup'>checked</#if> />
              <label class="label" for="reviewDisplayMarkup" style="margin-right: 21px">${msg("onlyoffice-config.review-mode-markup")}</label>

              <input class="value" id="reviewDisplayFinal" name="reviewDisplay" type="radio" value="final" <#if reviewDisplay == 'final'>checked</#if> />
              <label class="label" for="reviewDisplayFinal" style="margin-right: 21px">${msg("onlyoffice-config.review-mode-final")}</label>

              <input class="value" id="reviewDisplayOriginal" name="reviewDisplay" type="radio" value="original" <#if reviewDisplay == 'original'>checked</#if> />
              <label class="label" for="reviewDisplayOriginal" style="margin-right: 21px">${msg("onlyoffice-config.review-mode-original")}</label>
          </div>
      </div>

      <br>
      <table>
          <tr style="vertical-align: top;">
              <td>
                  <input id="postonlycfg" type="button" value="${msg('onlyoffice-config.save-btn')}"/>
              </td>
              <td>
                  <div class="control field" style="margin-left: 20px;">
                      <input class="value" id="onlyofficeDemo" name="onlyofficeDemo" type="checkbox" ${demo} <#if !demoAvailable> disabled="disabled" </#if>/>
                      <label class="label" for="onlyofficeDemo">${msg("onlyoffice-config.demo-connect")}</label>
                      </br>
                      <#if demoAvailable>
                          <div class="description">${msg("onlyoffice-config.trial")}</div>
                      <#else>
                          <div class="description">${msg("onlyoffice-config.trial-is-over")}</div>
                      </#if>
                  </div>
              </td>
          </tr>
      </table>
   </form>
</div>

<script type="text/javascript">//<![CDATA[
   (function() {
      var url = document.getElementById("onlyurl");
      var innerurl = document.getElementById("onlyinnerurl");
      var alfurl = document.getElementById("alfurl");
      var cert = document.getElementById("onlycert");
      var fs = document.getElementById("forcesave");
      var webpreview = document.getElementById("webpreview");
      var convertOriginal = document.getElementById("convertOriginal");
      var jwts = document.getElementById("jwtsecret");
      var demo = document.getElementById("onlyofficeDemo");
      var odt = document.getElementById("odt");
      var ods = document.getElementById("ods");
      var odp = document.getElementById("odp");
      var csv = document.getElementById("csv");
      var txt = document.getElementById("txt");
      var rtf = document.getElementById("rtf");

      var form = document.getElementById("docservcfg");
      var btn = document.getElementById("postonlycfg");
      var msg = document.getElementById("onlyresponse");

      var chat = document.getElementById("chat");
      var help = document.getElementById("help");
      var compactHeader = document.getElementById("compactHeader");
      var toolbarNoTabs = document.getElementById("toolbarNoTabs");
      var feedback = document.getElementById("feedback");
      var reviewDisplay = document.getElementsByName("reviewDisplay");

      var doPost = function(obj) {
         var xhr = new XMLHttpRequest();
         xhr.open("POST", form.action, true);
         xhr.setRequestHeader("Content-type", "application/json");
         xhr.setRequestHeader("Accept", "application/json");
         xhr.overrideMimeType("application/json");

         xhr.onload = function () { callback(xhr); };

         xhr.send(JSON.stringify(obj));
      };

      var callback = function(xhr) {
         btn.disabled = false;

         if (xhr.status != 200) {
               showMessage(msg.dataset.error + " " + msg.dataset.statuscode + " " + xhr.status, true);
               return;
         }

         var response = JSON.parse(xhr.response);

         if (!response.success) {
               showMessage(msg.dataset.error + " " + msg.dataset[response.message], true);
               return;
         }

         showMessage(msg.dataset.saved);
      };

      var parseForm = function() {
         var obj = {};

         var reg = RegExp(url.pattern);
         if (!reg.test(url.value)) { return null; }

         obj.url = url.value.trim();
         obj.innerurl = innerurl.value.trim();
         obj.alfurl = alfurl.value.trim();
         obj.cert = cert.checked.toString();
         obj.forcesave = fs.checked.toString();
         obj.webpreview = webpreview.checked.toString();
         obj.convertOriginal = convertOriginal.checked.toString();
         obj.jwtsecret = jwts.value.trim();
         obj.demo = demo.checked.toString();
         obj.formats = {
            odt: odt.checked.toString(),
            ods: ods.checked.toString(),
            odp: odp.checked.toString(),
            csv: csv.checked.toString(),
            txt: txt.checked.toString(),
            rtf: rtf.checked.toString()
         };
         obj.chat = chat.checked.toString();
         obj.help = help.checked.toString();
         obj.compactHeader = compactHeader.checked.toString();
         obj.toolbarNoTabs = toolbarNoTabs.checked.toString();
         obj.feedback = feedback.checked.toString();

         reviewDisplay.forEach((element) => {
             if (element.checked) obj.reviewDisplay = element.value.trim();
         });

         return obj;
      };

      var hideMessage = function() {
         msg.classList.add("hidden");
         msg.parentNode.classList.add("hidden");
         msg.classList.remove("error");
         msg.innerText = "";
      };

      var msgTimeout = null;
      var showMessage = function(message, error) {
         if (error) {
               msg.classList.add("error");
         }

         msg.innerText = message;
         msg.classList.remove("hidden");
         msg.parentNode.classList.remove("hidden");

         if (msgTimeout != null) {
            clearTimeout(msgTimeout);
         }
         msgTimeout = setTimeout(hideMessage, 3000);
      };

      var testDocServiceApi = function (obj) {
          var testApiResult = function () {
              var result = typeof DocsAPI != "undefined";

              if (result) {
                  doPost(obj);
              } else {
                  btn.disabled = false;
                  showMessage(msg.dataset.error + " " + msg.dataset.docservunreachable, true);
              }
          };

          if (window.location.href.startsWith("https://") && obj.url.toLowerCase().startsWith("http://")) {
              btn.disabled = false;
              showMessage(msg.dataset.error + " " + msg.dataset["mixedcontent"], true);
              return;
          }

          delete DocsAPI;

          var scriptAddress = document.getElementById("scripDocServiceAddress");
          if (scriptAddress) scriptAddress.parentNode.removeChild(scriptAddress);

          var js = document.createElement("script");
          js.setAttribute("type", "text/javascript");
          js.setAttribute("id", "scripDocServiceAddress");
          document.getElementsByTagName("head")[0].appendChild(js);

          scriptAddress = document.getElementById("scripDocServiceAddress");

          scriptAddress.onload = testApiResult;
          scriptAddress.onerror = testApiResult;

          var docServiceUrlApi = obj.url;

          if (!docServiceUrlApi.endsWith("/")) {
              docServiceUrlApi += "/";
          }
          docServiceUrlApi += "web-apps/apps/api/documents/api.js";

          scriptAddress.src = docServiceUrlApi;
      };

      btn.onclick = function() {
         hideMessage();
         if (btn.disabled) return;

         var obj = parseForm();
         if (!obj) return;

         btn.disabled = true;
         if (demo.checked && !demo.disabled) {
            doPost(obj);
         } else {
            testDocServiceApi(obj);
         }
      };

      var demoToggle = function () {
          if (!demo.disabled) {
               url.disabled = demo.checked;
               jwts.disabled = demo.checked;
               innerurl.disabled = demo.checked;
          }
      };

      demo.onclick = demoToggle;
      demoToggle();
   })();
//]]></script>
</@page>
