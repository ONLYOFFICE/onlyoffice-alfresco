/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/

(function () {
    YAHOO.Bubbling.fire("registerAction",
    {
        actionName: "onOnlyofficeCreateDocx",
        fn: function (obj) { openAndRefresh(obj, "application/vnd.openxmlformats-officedocument.wordprocessingml.document"); }
    });
    YAHOO.Bubbling.fire("registerAction",
    {
        actionName: "onOnlyofficeCreateXlsx",
        fn: function (obj) { openAndRefresh(obj, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); }
    });
    YAHOO.Bubbling.fire("registerAction",
    {
        actionName: "onOnlyofficeCreatePptx",
        fn: function (obj) { openAndRefresh(obj, "application/vnd.openxmlformats-officedocument.presentationml.presentation"); }
    });
    function openAndRefresh(obj, mime) {
        window.open("onlyoffice-edit?parentNodeRef=" + obj.nodeRef + "&new=" + mime);
        setTimeout(function() { YAHOO.Bubbling.fire("metadataRefresh", obj); }, 1000);
    }

    YAHOO.Bubbling.fire("registerAction",
    {
        actionName : "onOnlyofficeConvert",
        fn: function onOnlyofficeConvert(record, owner) {
            //ACE-2470 : Clone: Clicking multiple times the simple Workflow approval menu item gives unexpected results.
            if (owner.title.indexOf("_deactivated") == -1)
            {
                // Get action params
                var params = this.getAction(record, owner).params,
                    displayName = record.displayName,
                    namedParams = [
                        "function",
                        "action",
                        "success",
                        "successMessage",
                        "failure",
                        "failureMessage",
                        "waitingMessage",
                        "async"
                    ],
                    repoActionParams = {};

                this.widgets.waitDialog = Alfresco.util.PopupManager.displayMessage({
                    text : this.msg(params.waitingMessage, displayName),
                    spanClass : "wait",
                    displayTime : 0
                });

                for (var name in params)
                {
                    if (params.hasOwnProperty(name) && !Alfresco.util.arrayContains(namedParams, name))
                    {
                        repoActionParams[name] = params[name];
                    }
                }

                //Deactivate action
                var ownerTitle = owner.title;
                owner.title = owner.title + "_deactivated";

                var async = params.async ? "async=" + params.async : null;

                // Prepare genericAction config
                var config =
                {
                    success:
                    {
                        event:
                        {
                            name: "metadataRefresh",
                            obj: record
                        }
                    },
                    failure:
                    {
                        message: this.msg(params.failureMessage, displayName),
                        fn: function showAction()
                        {
                            owner.title = ownerTitle;
                        },
                        scope: this
                    },
                    webscript:
                    {
                       method: Alfresco.util.Ajax.POST,
                       stem: Alfresco.constants.PROXY_URI + "api/",
                       name: "actionQueue",
                       queryString: async
                    },
                    config:
                    {
                        requestContentType: Alfresco.util.Ajax.JSON,
                        dataObj:
                        {
                            actionedUponNode: record.nodeRef,
                            actionDefinitionName: params.action,
                            parameterValues: repoActionParams
                        }
                    }
                };

                // Add configured success callbacks and messages if provided
                if (YAHOO.lang.isFunction(this[params.success]))
                {
                    config.success.callback =
                    {
                        fn: this[params.success],
                        obj: record,
                        scope: this
                    };
                }
                if (params.successMessage)
                {
                    config.success.message = this.msg(params.successMessage, displayName);
                }

                // Acd configured failure callback and message if provided
                if (YAHOO.lang.isFunction(this[params.failure]))
                {
                    config.failure.callback =
                    {
                        fn: this[params.failure],
                        obj: record,
                        scope: this
                    };
                }
                if (params.failureMessage)
                {
                    config.failure.message = this.msg(params.failureMessage, displayName);
                }

                // Execute the repo action
                this.modules.actions.genericAction(config);
            }
        }
    });
})();