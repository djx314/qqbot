$(function() {

    var ViewModel = function() {
        var self = this;
        self.parentUrl = ko.observable(null);
        self.filesPath = ko.observableArray([]);

        self.gotoParent = function() {
            $.ajax({
                type: "POST",
                url: "/filesWithAss",
                dataType: "json",
                data: { path: self.parentUrl() }
            }).done(function(response) {
                viewModel.parentUrl(response.parentPath);
                viewModel.filesPath(response.urls);
            });
        };

        self.enterFile = function(filePath) {
            if (filePath.isDir === true) {
                $.ajax({
                    type: "POST",
                    url: "/filesWithAss",
                    dataType: "json",
                    data: { path: filePath.encodeUrl }
                }).done(function(response) {
                    viewModel.parentUrl(response.parentPath);
                    viewModel.filesPath(response.urls);
                });
            } else {
                self.encodeFile(filePath);
            }
        };

        self.encodeFile = function(filePath) {
            if (confirm("是否发送带字幕转码命令？") === true) {
                $.ajax({
                    type: "POST",
                    url: "/encodeWithAss",
                    data: { videoPath: filePath.encodeUrl, assPath: filePath.encodeUrl }
                }).done(function(response) {
                    alert(response);
                    if (initParentUrl === "") {
                        window.location.href = "/assets";
                    } else {
                        window.location.href = "/assets/" + initParentUrl;
                    }
                });
            }
        };
    };

    var viewModel = new ViewModel();

    ko.applyBindings(viewModel);

    $.ajax({
        type: "POST",
        url: "/filesWithAss",
        dataType: "json",
        data: { path: initParentUrl }
    }).done(function(response) {
        viewModel.parentUrl(response.parentPath);
        viewModel.filesPath(response.urls);
    });

});