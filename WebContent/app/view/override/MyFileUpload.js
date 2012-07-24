Ext.define('MyApp.view.override.MyFileUpload', {
    requires: 'MyApp.view.MyFileUpload'
}, function() {
    Ext.override(MyApp.view.MyFileUpload, {
        reset: function() {
            var me =this;
            var v = me.value;
            me.emptyText = v;
            me.callParent();
        }
    });
});