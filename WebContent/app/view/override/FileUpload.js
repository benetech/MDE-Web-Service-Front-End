Ext.define('MyApp.view.override.FileUpload', {
    requires: 'MyApp.view.FileUpload'
}, function() {
    Ext.override(MyApp.view.FileUpload, {
        reset: function() {
            var me =this;
            var v = me.value;
            me.emptyText = v;
            me.callParent();
        }
    });
});