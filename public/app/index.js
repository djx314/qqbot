define(function(require) {
    var system = require("durandal/system"),
        app = require("durandal/app");
    system.debug(true);
    app.title = "学生报考个人页面";
    app.configurePlugins({
        router: true,
        dialog: true
    });
    app.start().then(function() {
        app.setRoot("lifan/shell/index");
    });
});