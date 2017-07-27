$(document).ready(function(){
    $('#banner').empty().load('/banner/navbarLinks');

    $('#carousel').on('slid.bs.carousel', function () {
        var imageWidth = $('.active > img').width();
        var newWidth = imageWidth * .80;

        var label = $('.active').find('.carousel-label');
        var labelWidth = label.width();

        if(labelWidth > imageWidth){
            label.width(newWidth);
        }
    });
});