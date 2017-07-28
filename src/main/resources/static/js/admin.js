$(document).ready(function () {
    $('#banner').empty().load('/banner/navbarLinks');

    $('#delete_roles').submit(function (e) {
        e.preventDefault();
        ajaxSubmitUpdate('#delete_roles', function(){
            $('#add_site_user').empty().load('/admin/site_user/add_site_user');
        });
    });

    $('#add_roles').submit(function (e) {
        e.preventDefault();
        ajaxSubmitUpdate('#add_roles', function(){
            $('#delete_roles').empty().load('/admin/user_roles/delete_roles');
            $('#add_site_user').empty().load('/admin/site_user/add_site_user');
        });
    });

    $('#delete_site_users').submit(function (e) {
        e.preventDefault();
        ajaxSubmitUpdate('#delete_site_users');
    });

    $('#add_site_user').submit(function (e) {
        e.preventDefault();
        ajaxSubmitUpdate('#add_site_user', function(){
            $('#delete_site_users').empty().load('/admin/site_user/delete_site_user');
        });
    });

    $('#add_index_carousel').submit(function (e) {
        e.preventDefault();
        ajaxFileSubmitUpdate('#add_index_carousel', function(){
            $('#delete_carousel').empty().load('/admin/index_carousal/delete_carousal');
            $('#banner').empty().load('/banner/navbarLinks');
        });
    });

    $('#delete_carousel').submit(function (e) {
        e.preventDefault();
        ajaxSubmitUpdate('#delete_carousel', function(){
            $('#banner').empty().load('/banner/navbarLinks');
        });
    });

    $('#add_date').submit(function(e){
        e.preventDefault();
        ajaxSubmitUpdate('#add_date', function () {
            $('#delete_event_date').empty().load('/admin/event_dates/delete_event_date');
            $('#banner').empty().load('/banner/navbarLinks');
        });
    });

    $('#delete_event_date').submit(function(e){
        e.preventDefault();
        ajaxSubmitUpdate('#delete_event_date', function(){
            $('#banner').empty().load('/banner/navbarLinks');
        });
    });

    $('#edit_venue_text').submit(function(e){
        e.preventDefault();
        ajaxSubmitUpdate('#edit_venue_text')
    });

    $('#venue_image_upload').submit(function (e) {
        e.preventDefault();
        ajaxFileSubmitUpdate('#venue_image_upload', function(){
            $('#delete_wedding_venue_images').empty().load('/admin/wedding_venue/delete_wedding_venue_images');
        });
    });

    $('#delete_wedding_venue_images').submit(function (e) {
        e.preventDefault();
        ajaxSubmitUpdate('#delete_wedding_venue_images');
    });

    $('#wedding_theme_content').submit(function (e) {
        e.preventDefault();
        ajaxSubmitUpdate('#wedding_theme_content');
    });

    $('#men_picture_upload').submit(function (e) {
        e.preventDefault();
        ajaxFileSubmitUpdate('#men_picture_upload', function(){
            $('#men_picture_delete').empty().load('/admin/wedding_theme/men/delete');
        });
    });

    $('#men_picture_delete').submit(function (e) {
        e.preventDefault();
        ajaxSubmitUpdate('#men_picture_delete');
    });

    $('#women_picture_upload').submit(function (e) {
        e.preventDefault();
        ajaxFileSubmitUpdate('#women_picture_upload', function(){
            $('#women_picture_delete').empty().load('/admin/wedding_theme/women/delete');
        });
    });

    $('#women_picture_delete').submit(function (e) {
        e.preventDefault();
        ajaxSubmitUpdate('#women_picture_delete');
    });

    $('#food_bar_add').submit(function (e) {
        e.preventDefault()
        ajaxSubmitUpdate('#food_bar_add', function(){
            $('#food_bar_delete').empty().load('/admin/food_bar/delete');
        });
    });

    $('#food_bar_delete').submit(function (e) {
        e.preventDefault();
        ajaxSubmitUpdate('#food_bar_delete');
    });

    $('#edit_after_party').submit(function (e) {
        e.preventDefault();
        ajaxSubmitUpdate('#edit_after_party');
    });

    $('#add_registry').submit(function (e) {
        e.preventDefault();
        ajaxFileSubmitUpdate('#add_registry', function(){
            $('#delete_registry').empty().load('/admin/registry/delete_registry');
        });
    });

    $('#delete_registry').submit(function(e) {
        e.preventDefault();
        ajaxSubmitUpdate('#delete_registry');
    });

    function ajaxFileUploadSubmit(selector, success, error) {
        var form = $(selector);

        prepareAjax(selector);
        $.ajax({
            url: $(form).attr('action'),
            type: 'post',
            contentType: false,
            processData: false,
            cache: false,
            data: new FormData($(form)[0]),
            success: function (response) {
                if (success) {
                    success(response);
                }
            },
            error: function () {
                if (error) {
                    error();
                }
            }
        });
    }

    function ajaxFileSubmitUpdate(selector, oncomplete){
        ajaxFileUploadSubmit(selector, function(response){
            ajaxUpdate(selector, response);
            if(oncomplete){
                oncomplete();
            }
        });
    }

    function ajaxSubmitUpdate(selector, oncomplete) {
        ajaxFormSubmit(selector, function(response){
            ajaxUpdate(selector, response);
            if(oncomplete){
                oncomplete();
            }
        });
    }

    function ajaxFormSubmit(selector, success, error) {
        var form = $(selector);
        prepareAjax(selector);

        $.ajax({
            url: $(form).attr('action'),
            type: 'post',
            data: $(form).serialize(),
            success: function (response) {
                if (success) {
                    success(response);
                }
            },
            error: function () {
                if (error) {
                    error();
                }
            }
        });
    }

    function prepareAjax(selector) {
        var form = $(selector);

        var token = $(form).find("input[name='_csrf']").val();
        $.ajaxSetup({
            beforeSend: function (xhr) {
                xhr.setRequestHeader('X-CSRF-Token', token);
            }
        });
    }

    function ajaxUpdate(selector, response) {
        $(selector).empty().html(response);
        $('#alerts').empty().load('/messages');
    }
});