/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/

(function () {
    var documentPicker;
    const subMenuItemsData = [
        {
            id: "onlyoffice-new-pdf-from-blank",
            title: Alfresco.util.message("actions.document.onlyoffice-create-pdf.blank"),
            onClick: _onCreateFormFromBlank
        },
        {
            id: "onlyoffice-new-pdf-from-docx",
            title: Alfresco.util.message("actions.document.onlyoffice-create-pdf.from-exs"),
            onClick: _onCreateFormFromExistingFile
        }
    ];

    onLoadCreateMenuItems(function() {
        var subMenu;
        var targetMenuItem = document.getElementsByClassName("document-onlyoffice-create-pdf-file")[0]
                .parentElement
                .parentElement;
        targetMenuItem.classList.add("yuimenuitem-hassubmenu");

        targetMenuItem.onmouseover = function() {
            if (!subMenu) {
                documentPicker = _createDocumentPicker();
                subMenu = _createSubMenu(subMenuItemsData);
                this.appendChild(subMenu);
            }

            // Determine position of SubMenu.
            let left = 4 + this.offsetWidth;
            if (this.parentElement.parentElement.parentElement.getBoundingClientRect().right + subMenu.offsetWidth > document.documentElement.clientWidth) {
                left = -subMenu.offsetWidth + 4;
            }
            subMenu.style.left = left + "px";

            subMenu.style.visibility = "visible";
            subMenu.classList.add("visible");
            subMenu.classList.remove("yui-overlay-hidden");
        };
        targetMenuItem.onmouseout = function() {
            subMenu.style.visibility = "hidden";
            subMenu.classList.remove("visible");
            subMenu.classList.add("yui-overlay-hidden");
        };
    });

    var _createSubMenu = function(items) {
        var div = document.createElement("div");
        div.id = "onlyoffice-new-form-submenu";
        div.className = "yui-module yui-overlay yuimenu yui-overlay-hidden"
        div.style = "position: absolute; visibility: visible; z-index: 1; left: 0; top: 30px";

        var divBD = document.createElement("div");
        divBD.className = "bd";

        var ul = document.createElement("ul");
        ul.className = "first-of-type";

        for (var i = 0; i < items.length; i++) {
            if (i == 0) {
                items[i].first = true;
            }
            ul.appendChild(_createSubMenuItem(items[i]));
        }

        divBD.appendChild(ul);
        div.appendChild(divBD);

        return div;
    }

    var _createSubMenuItem = function(data) {
        var li = document.createElement("li");
        li.id = data.id;
        li.className = "yuimenuitem";
        li.onmousedown = data.onClick;
        if (data.first) {
            li.classList.add("first-of-type");
        }

        var a = document.createElement("a");
        a.href = "#";
        a.className = "yuimenuitemlabel";

        var span = document.createElement("span");
        span.innerText = data.title;

        li.onmouseover = function () {
            this.classList.add("yuimenuitem-selected");
            a.classList.add("yuimenuitemlabel-selected");
        }
        li.onmouseout = function () {
            this.classList.remove("yuimenuitem-selected");
            a.classList.remove("yuimenuitemlabel-selected");
        }

        a.appendChild(span);
        li.appendChild(a);

        return li;
    }

    function onLoadCreateMenuItems(callback) {
        if (document.getElementsByClassName("document-onlyoffice-create-pdf-file").length
            && Alfresco.ObjectRenderer
        ) {
            callback();
        } else {
            setTimeout(function () {
                onLoadCreateMenuItems(callback);
            }, 100);
        }
    };

    function _onCreateFormFromBlank() {
        window.open("onlyoffice-edit?parentNodeRef=" + YAHOO.Bubbling.bubble.ready.scope.docListToolbar.doclistMetadata.parent.nodeRef
                        + "&new=application/pdf");
        setTimeout(function () {
            location.reload();
        }, 1000);
    };

    function _onCreateFormFromExistingFile() {
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
                                    text: Alfresco.util.message("actions.document.onlyoffice-create-pdf.from-exs-failure")
                                });
                            },
                            scope: this
                        }
                    });
                }
            }
        });
        documentPicker.onShowPicker();
    };

    function _createDocumentPicker() {
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

        return documentPicker;
    }
})();
