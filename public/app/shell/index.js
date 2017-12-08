define(function (require) {
    var router = require("plugins/router");

    return {
        router: router,
        activate: function() {
            router.map([
                { route: "assets(/*details)", moduleId: "lifan/files/index", title: "里番首页", nav: 3, hash: "#assets" }
            ]).buildNavigationModel();
            return router.activate();
        }
    };
});