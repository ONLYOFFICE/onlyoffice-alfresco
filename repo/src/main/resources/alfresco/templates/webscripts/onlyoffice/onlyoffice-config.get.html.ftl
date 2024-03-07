<#include "/org/alfresco/repository/admin/admin-template.ftl" />

<!--
    Copyright (c) Ascensio System SIA 2023. All rights reserved.
    http://www.onlyoffice.com
-->

<@page title=msg("onlyoffice-config.title") readonly=true>

</form>

<span data-settings-saved="${msg('onlyoffice-config.message.settings.saved')}" data-settings-saving-error="${msg('onlyoffice-config.message.settings.saving-error')}" data-mixedcontent="${msg('onlyoffice.server.common.error.mixed-content')}" data-apijs-unreachable="${msg('onlyoffice.server.common.error.api-js')}" data-onlyoffice-convert-service-prefix="${msg('onlyoffice.service.convert.check.error-prefix')}" data-onlyoffice-command-service-prefix="${msg('onlyoffice.service.command.check.error-prefix')}" id="onlyresponse" class="hidden"></span>

<div style="margin: 1em 0 0.666em;" id="messageBlock" class="hidden"></div>

<div class="column-left">
   <@section label=msg("onlyoffice-config.doc-section") />

   <form id="docservcfg" action="${url.service}" method="POST" accept-charset="utf-8">
      <div class="description section">${msg("onlyoffice-config.description")}</div>
      <div class="control text">
         <label class="label" for="onlyurl">${msg("onlyoffice-config.doc-url")}</label>
         <span class="value">
            <input id="onlyurl" name="url" size="35" placeholder="http://docserver/" title="${msg('onlyoffice-config.doc-url-tooltip')}" pattern="(http(s)?://.*)|(/.*)" value="${(settings['url'])!}" />
         </span>
      </div>
      <div class="control text">
         <label class="label" for="jwtsecret">${msg("onlyoffice-config.jwt-secret")}</label>
         <span class="value">
            <input class="value" id="jwtsecret" name="url" size="35" value="${(settings['security.key'])!}" />
         </span>
      </div>

      <@tsection label=msg("onlyoffice-config.advanced-section")>
         <div class="control text">
            <label class="label" for="onlyinnerurl">${msg("onlyoffice-config.doc-url-inner")}</label>
            <span class="value">
               <input class="value" id="onlyinnerurl" name="innerurl" size="35" placeholder="http://docserver/" title="${msg('onlyoffice-config.doc-url-inner-tooltip')}" pattern="http(s)?://.*" value="${(settings['innerUrl'])!}" />
            </span>
         </div>
         <div class="control text">
            <label class="label" for="alfurl">${msg("onlyoffice-config.alf-url")}</label>
            <span class="value">
               <input class="value" id="alfurl" name="alfurl" size="35" placeholder="http://alfresco/" title="${msg('onlyoffice-config.alf-url-tooltip')}" pattern="http(s)?://.*" value="${(settings['productInnerUrl'])!}" />
            </span>
         </div>
      </@tsection>

      <@section label=msg("onlyoffice-config.common-section") />
      <div class="control field">
         <input class="value" id="onlycert" name="cert" type="checkbox" <#if (settings['security.ignoreSSLCertificate'])! == 'true'>checked</#if> />
         <label class="label" for="onlycert">${msg("onlyoffice-config.ignore-ssl-cert")}</label>
      </div>
      <div class="control field">
         <input class="value" id="webpreview" name="cert" type="checkbox" <#if webpreview>checked</#if> />
         <label class="label" for="webpreview">${msg("onlyoffice-config.webpreview")}</label>
      </div>
      <div class="control field">
         <input class="value" id="convertOriginal" name="convertOriginal" type="checkbox" <#if convertOriginal>checked</#if> />
         <label class="label" for="convertOriginal">${msg("onlyoffice-config.convert-original")}</label>
      </div>
      <div class="control field section">
          <label class="label">${msg("onlyoffice-config.file-type")}</label>
          <div style="padding-top: 4px">
              <#list lossyEditable?keys as key>
                <div style="display: inline-block;">
                  <input class="value lossy-edit" id="${key}" name="${key}" type="checkbox" <#if lossyEditable[key]>checked="checked"</#if>/>
                  <label class="label" style="margin-right: 10px; width: 40px; display: inline-block" for="${key}">${key}</label>
                </div>
              </#list>
          </div>
      </div>

      <@section label=msg("onlyoffice-config.customization-section")/>
      <div class="control field">
          <input class="value" id="forcesave" name="forcesave" type="checkbox" <#if (settings['customization.forcesave'])! == 'true'>checked</#if> />
          <label class="label" for="forcesave">${msg("onlyoffice-config.forcesave")}</label>
      </div>
      <label class="control label">${msg("onlyoffice-config.customization-label")}</label>
      <div class="control field">
          <input class="value" id="chat" name="chat" type="checkbox" <#if (settings['customization.chat'])! == 'true'>checked</#if> />
          <label class="label" for="chat">${msg("onlyoffice-config.chat")}</label>
      </div>
      <div class="control field">
          <input class="value" id="compactHeader" name="compactHeader" type="checkbox" <#if (settings['customization.compactHeader'])! == 'true'>checked</#if> />
          <label class="label" for="compactHeader">${msg("onlyoffice-config.compact-header")}</label>
      </div>
      <div class="control field">
          <input class="value" id="feedback" name="feedback" type="checkbox" <#if (settings['customization.feedback'])! == 'true'>checked</#if> />
          <label class="label" for="feedback">${msg("onlyoffice-config.feedback")}</label>
      </div>
      <div class="control field">
          <input class="value" id="help" name="help" type="checkbox" <#if (settings['customization.help'])! == 'true'>checked</#if> />
          <label class="label" for="help">${msg("onlyoffice-config.help")}</label>
      </div>
      <div class="control field">
          <input class="value" id="toolbarNoTabs" name="toolbarNoTabs" type="checkbox" <#if (settings['customization.toolbarNoTabs'])! == 'true'>checked</#if> />
          <label class="label" for="toolbarNoTabs">${msg("onlyoffice-config.toolbar-no-tabs")}</label>
      </div>
      <div class="control field section">
          <p class="label">${msg("onlyoffice-config.review-mode-label")}</p>
          <div style="padding-top: 4px">
              <input class="value" id="reviewDisplayMarkup" name="reviewDisplay" type="radio" value="markup" <#if (settings['customization.review.reviewDisplay'])! == 'MARKUP'>checked</#if> />
              <label class="label" for="reviewDisplayMarkup" style="margin-right: 21px">${msg("onlyoffice-config.review-mode-markup")}</label>

              <input class="value" id="reviewDisplayFinal" name="reviewDisplay" type="radio" value="final" <#if (settings['customization.review.reviewDisplay'])! == 'FINAL'>checked</#if> />
              <label class="label" for="reviewDisplayFinal" style="margin-right: 21px">${msg("onlyoffice-config.review-mode-final")}</label>

              <input class="value" id="reviewDisplayOriginal" name="reviewDisplay" type="radio" value="original" <#if (settings['customization.review.reviewDisplay'])! == 'ORIGINAL'>checked</#if> />
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
                      <input class="value" id="onlyofficeDemo" name="onlyofficeDemo" type="checkbox" <#if (settings['demo'])! == 'true'>checked</#if> <#if !demoAvailable> disabled="disabled" </#if>/>
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

      var form = document.getElementById("docservcfg");
      var btn = document.getElementById("postonlycfg");
      var msg = document.getElementById("onlyresponse");

      var chat = document.getElementById("chat");
      var help = document.getElementById("help");
      var compactHeader = document.getElementById("compactHeader");
      var toolbarNoTabs = document.getElementById("toolbarNoTabs");
      var feedback = document.getElementById("feedback");
      var lossyEdit = document.querySelectorAll(".lossy-edit");
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

         if (xhr.status != 200 || !xhr.response) {
               showMessage(msg.dataset["settingsSavingError"], true);
               return;
         }

         if (xhr.response) {
             const responseJson = JSON.parse(xhr.response);
             const validationResults = responseJson.validationResults;

            if (validationResults.documentServer) {
                if (validationResults.documentServer.status == "failed") {
                    showMessage(validationResults.documentServer.message, true);
                }
            }

            if (validationResults.commandService) {
                if (validationResults.commandService.status == "failed") {
                    showMessage(
                        msg.dataset["onlyofficeCommandServicePrefix"].replace(
                            "$",
                            validationResults.commandService.message
                        ),
                        true
                    );
                }
            }

            if (validationResults.convertService) {
                if (validationResults.convertService.status == "failed") {
                    showMessage(
                        msg.dataset["onlyofficeConvertServicePrefix"].replace(
                            "$",
                            validationResults.convertService.message
                        ),
                        true
                    );
                }
            }

            showMessage(msg.dataset["settingsSaved"]);
         }
      };

      var parseForm = function() {
         var obj = {};

         obj.lossyEdit = [];

         lossyEdit.forEach((element) => {
            if (element.checked) obj.lossyEdit.push(element.id);
         });

         obj.url = url.value.trim();
         obj.innerUrl = innerurl.value.trim();
         obj.productInnerUrl = alfurl.value.trim();
         obj.security = {
            key: jwts.value.trim()
         };
         obj.ignoreSSLCertificate = cert.checked.toString();
         obj.demo = demo.checked.toString();
         obj.customization = {
            forcesave: fs.checked.toString(),
            feedback: feedback.checked.toString(),
            chat: chat.checked.toString(),
            help: help.checked.toString(),
            compactHeader: compactHeader.checked.toString(),
            toolbarNoTabs: toolbarNoTabs.checked.toString(),
            review: {
                reviewDisplay: document.querySelector("input[name='reviewDisplay']:checked").id.replace("reviewDisplay", "").toLowerCase()
            }
         };

         obj.convertOriginal = convertOriginal.checked.toString();
         obj.webpreview = webpreview.checked.toString();

         return obj;
      };

      var showMessage = function(message, error) {
        let messageBlock = document.getElementById("messageBlock");
        let messageElement = document.createElement("span");
        messageElement.classList.add('message');
        messageElement.style.width = "fit-content";
        messageElement.style.margin = "5px 0";
        messageElement.style.display = "block";
        messageElement.innerText = message;

        if (error) {
            messageElement.classList.add("error");
        }

        messageBlock.appendChild(messageElement);

        messageBlock.classList.remove("hidden");
      };

      var hideMessages = function() {
        document.getElementById("messageBlock").innerHTML = '';
      };

      var testDocServiceApi = function (obj) {
          var testApiResult = function () {
              var result = typeof DocsAPI != "undefined";

              if (!result) {
                showMessage(msg.dataset["apijsUnreachable"], true);
              }

              doPost(obj);
          };

          if (window.location.href.startsWith("https://") && obj.url.toLowerCase().startsWith("http://")) {
              btn.disabled = false;
              showMessage(msg.dataset["mixedcontent"], true);
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
         if (btn.disabled) return;

         btn.disabled = true;
         hideMessages();

         var obj = parseForm();

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
