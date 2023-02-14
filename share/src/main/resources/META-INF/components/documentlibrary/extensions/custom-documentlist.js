/*
    Copyright (c) Ascensio System SIA 2023. All rights reserved.
    http://www.onlyoffice.com
*/

var addSubMenu = function () {
    var elem = document.getElementsByClassName("document-onlyoffice-create-docxf-file")[0];
    var li = elem.parentElement.parentElement;
    li.classList += " yuimenuitem-hassubmenu";

    var submenu =
        '<div id = "onlyoffice-new-form-submenu" class = "yui-module yui-overlay yuimenu yui-overlay-hidden" style = "position: absolute; visibility: hidden; z-index: 1; left: 0; top: 30px">' +
        '<div class= "bd">' +
        '<ul class= "first-of-type">' +
        '<li class= "yuimenuitem first-of-type" id="onlyoffice-newform-blank">' +
        '<a href = "#" class = "yuimenuitemlabel"><span title = "">' + Alfresco.util.message("actions.document.onlyoffice-create-docxf.blank") + '</span><a>' +
        '</li>' +
        '<li class= "yuimenuitem" id="onlyoffice-newform-docx">' +
        '<a href = "#" class = "yuimenuitemlabel"><span title = "">' + Alfresco.util.message("actions.document.onlyoffice-create-docxf.form-exs") + '</span><a>' +
        '</li></ul></div></div>';

    li.innerHTML += submenu;

  setTimeout(function() {
      var formDiv = document.getElementById("onlyoffice-new-form-submenu");

      $("#onlyoffice-new-form-submenu li").bind("mouseover", function () {
          $(this).addClass("yuimenuitem-selected");
          $(this).children("a").addClass("yuimenuitemlabel-selected");
      });
      $("#onlyoffice-new-form-submenu li").bind("mouseout", function () {
          $(this).removeClass("yuimenuitem-selected");
          $(this).children("a").addClass("yuimenuitemlabel-selected");
      });

      $(li).bind("mouseover", function() {
          let left = 4 + li.offsetWidth;
          if (li.parentElement.parentElement.parentElement.getBoundingClientRect().right + formDiv.offsetWidth > document.documentElement.clientWidth) {
              left = -formDiv.offsetWidth + 4;
          }

          $(this).children("#onlyoffice-new-form-submenu")[0].style.left = left + "px";
          $(this).addClass("yuimenuitem-selected yuimenuitem-hassubmenu-selected");
          formDiv.style.visibility = "visible";
          formDiv.classList = "yui-module yui-overlay yuimenu visible";
      });
      $(li).bind("mouseout", function() {
          $(this).removeClass("yuimenuitem-selected yuimenuitem-hassubmenu-selected");
          formDiv.style.visibility = "hidden";
          formDiv.classList = "yui-module yui-overlay yuimenu yui-overlay-hidden";
      });

      var documentPicker = new Alfresco.module.DocumentPicker("onlyoffice-docx-docPicker", Alfresco.ObjectRenderer);
      documentPicker.setOptions({
          selectableMimeType: ['application/vnd.openxmlformats-officedocument.wordprocessingml.document'],
          displayMode: "items",
          itemFamily: "node",
          itemType: "cm:content",
          multipleSelectMode: false,
          parentNodeRef: YAHOO.Bubbling.bubble.ready.scope.docListToolbar.doclistMetadata.container,
          restrictParentNavigationToDocLib: true
      });
      documentPicker.onComponentsLoaded();

      YAHOO.Bubbling.on("onDocumentsSelected", function (eventName, payload) {
          var waitDialog = Alfresco.util.PopupManager.displayMessage({
              text: "",
              spanClass: "wait",
              displayTime: 0
          });

          if (payload && payload[1].items) {
              var items = [];
              for (var i = 0; i < payload[1].items.length; i++) {
                  items.push(payload[1].items[i].nodeRef);
              }

              if (items.length > 0) {
                  Alfresco.util.Ajax.jsonPost({
                      url: Alfresco.constants.PROXY_URI + "parashift/onlyoffice/editor-api/from-docx",
                      dataObj: {
                          parentNode: YAHOO.Bubbling.bubble.ready.scope.docListToolbar.doclistMetadata.parent.nodeRef,
                          nodes: items
                      },
                      successCallback: {
                          fn: function (response) {
                              waitDialog.destroy();
                              window.open("onlyoffice-edit?nodeRef=" + response.json.nodeRef);
                              setTimeout(function () {
                                  location.reload();
                              }, 1000);
                          },
                          scope: this
                      },
                      failureCallback: {
                          fn: function () {
                              documentPicker.options.currentValue='';
                              delete documentPicker.singleSelectedItem;
                              waitDialog.destroy();
                              Alfresco.util.PopupManager.displayMessage({
                                  text: Alfresco.util.message("actions.document.onlyoffice-create-docxf.form-exs-failure")
                              });
                          },
                          scope: this
                      }
                  });
              }
          }
      });

      $("#onlyoffice-newform-blank").on("mousedown", function () {
          window.open("onlyoffice-edit?parentNodeRef=" + YAHOO.Bubbling.bubble.ready.scope.docListToolbar.doclistMetadata.parent.nodeRef
              + "&new=application/vnd.openxmlformats-officedocument.wordprocessingml.document.docxf");
          setTimeout(function () {
              location.reload();
          }, 1000);
      });
      $("#onlyoffice-newform-docx").on("mousedown", function () {
          documentPicker.onShowPicker();
      });
  }, 150);

};

var waitElemLoading = function(){
    if ($(".document-onlyoffice-create-docxf-file").length) {
        addSubMenu();
    } else {
        setTimeout(function () {
            waitElemLoading();
        }, 100);
    }
};

window.addEventListener("load", function () {
    waitElemLoading();
});

