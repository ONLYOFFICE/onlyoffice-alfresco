Alfresco.WebPreview.prototype.Plugins.onlyoffice = function(wp, attributes) {
    this.wp = wp;
    this.attributes = YAHOO.lang.merge(Alfresco.util.deepCopy(this.attributes), attributes);
    return this;
};

Alfresco.WebPreview.prototype.Plugins.onlyoffice.prototype = {
    attributes: {},

    report: function() {
        return null;
    },

    display: function() {
        var el = document.getElementById(this.wp.id);
        new DocsAPI.DocEditor(this.wp.id + "-body", this.attributes.config);
        el.style.width = "100%";
        el.style.height = "75vh";
    }
};