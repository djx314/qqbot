$(function() {

    var ViewModel = function() {
        var self = this;
        self.parentUrl = ko.observable(null);
        self.filesPath = ko.observableArray([]);
    };

    var viewModel = new ViewModel();

    ko.applyBindings(viewModel);

    $.ajax({
        type: "POST",
        url: "/dirInfoRequest",
        dataType: "json",
        data: { path: currentUrl }
    }).done(function(response) {
        viewModel.parentUrl(response.parentPath);
        viewModel.filesPath(response.urls);
    });

});