$(function() {

    $.ajax({
        type: "POST",
        url: "/dirInfoRequest",
        dataType: "json",
        data: { path: currentUrl }
    }).done(function(response) {
        console.log(response);
    });

});