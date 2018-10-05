define(function(require) {
    var $ = require("jquery");
    var ko = require("knockout");
    var router = require("plugins/router");

    var ViewModel = function() {
        var self = this;
        self.parentUrl = ko.observable(null);
        self.deleteTempUrl = ko.observable(null);
        self.filesPath = ko.observableArray([]);
        /*self.deleteTempDir = function() {
            $.ajax({
                type: "POST",
                url: "/deleteTempDir",
                data: { path: currentUrl }
            }).done(function(response) {
                location.reload(true);
            });
        };

        self.encodeFile = function(filePath) {
            if (confirm("是否转码文件：" + filePath.fileName + "？") === true) {
                //if (confirm("是否附带字幕文件？") === false) {
                $.ajax({
                    type: "POST",
                    url: "/encode",
                    data: { path: filePath.encodeUrl }
                }).done(function(response) {
                    alert(response);
                });
                /!*} else {
                    window.location.href = "/withAss?path=" + encodeURIComponent(filePath.encodeUrl);
                }*!/
            }
        };*/
    };

    var viewModel = new ViewModel();
    var currentPath = null;

    return {
        vm: viewModel,
        router: router,
        deleteTempDir: function() {
            $.ajax({
                type: "POST",
                url: "/deleteTempDir",
                data: { path: currentPath }
            }).done(function(response) {
                location.reload(true);
            });
        },
        encodeFile: function(filePath) {
            if (confirm("是否转码文件：" + filePath.fileName + "？") === true) {
                //if (confirm("是否附带字幕文件？") === false) {
                $.ajax({
                    type: "POST",
                    url: "/encode",
                    data: { path: filePath.requestUrl }
                }).done(function(response) {
                    alert(response);
                });
                /*} else {
                    window.location.href = "/withAss?path=" + encodeURIComponent(filePath.encodeUrl);
                }*/
            }
        },
        /*enterUrl: function(filePath) {

        },*/
        activate: function(path) {
            currentPath = path;
            var def = $.Deferred();
            $.ajax({
                type: "POST",
                url: "/dirInfoRequest",
                data: { path: path }
            }).done(function(response) {
                viewModel.parentUrl(response.parentPath);
                viewModel.filesPath(response.urls);
                viewModel.deleteTempUrl(response.deleteTempUrl);
                def.resolve(response.data);
            }).fail(function(response) {
                def.reject(response);
            });
            return def.promise();
        }
    }
});