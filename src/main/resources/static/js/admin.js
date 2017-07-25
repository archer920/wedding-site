$(document).ready(function () {
    $('#banner').empty().load('/navbarLinks');

    $('#delete_roles').submit(function (e) {
        e.preventDefault();
        ajaxFormSubmit('#delete_roles', function (response) {
            ajaxUpdate('#delete_roles', response);
            $('#add_site_user').empty().load('/admin/add_site_user');
        });
    });

    $('#add_roles').submit(function (e) {
        e.preventDefault();
        ajaxFormSubmit('#add_roles', function (response) {
            ajaxUpdate('#add_roles', response);
            $('#delete_roles').empty().load('/admin/delete_roles');
            $('#add_site_user').empty().load('/admin/add_site_user');
        });
    });

    $('#delete_site_users').submit(function (e) {
        e.preventDefault();
        ajaxFormSubmit('#delete_site_users', function (response) {
            ajaxUpdate('#delete_site_users', response);
        });
    });

    $('#add_site_user').submit(function (e) {
        e.preventDefault();
        ajaxFormSubmit('#add_site_user', function (response) {
            ajaxUpdate('#add_site_user', response);
            $('#delete_site_users').empty().load('/admin/delete_site_user');
        })
    });

    $('#add_index_carousel').submit(function (e) {
        e.preventDefault();
        ajaxFileUploadSubmit('#add_index_carousel', function (response) {
            ajaxUpdate('#add_index_carousel', response);
            $('#delete_carousel').empty().load('/admin/delete_carousel');
            $('#banner').empty().load('/navbarLinks');
        })
    });

    $('#delete_carousel').submit(function (e) {
        e.preventDefault();
        ajaxFormSubmit('#delete_carousel', function (response) {
            ajaxUpdate('#delete_carousel', response);
            $('#banner').empty().load('/navbarLinks');
        });
    });

    $('#add_date').submit(function(e){
        e.preventDefault();
        ajaxFormSubmit('#add_date', function (response) {
            ajaxUpdate('#add_date', response);
            $('#delete_event_date').empty().load('/admin/delete_event_date');
            $('#banner').empty().load('/navbarLinks');
        });
    });

    $('#delete_event_date').submit(function(e){
        e.preventDefault();
        ajaxFormSubmit('#delete_event_date', function(response){
            ajaxUpdate('#delete_event_date', response);
        });
    });

    $('#edit_venue_text').submit(function(e){
        e.preventDefault();
        ajaxFormSubmit('#edit_venue_text', function (response) {
            ajaxUpdate('#edit_venue_text', response);
        })
    });

    $('#venue_image_upload').submit(function (e) {
        e.preventDefault();
        ajaxFileUploadSubmit('#venue_image_upload', function (response) {
            ajaxUpdate('#venue_image_upload', response);
            $('#delete_wedding_venue_images').empty().load('/admin/delete_wedding_venue_images');
        });
    });

    $('#delete_wedding_venue_images').submit(function (e) {
        e.preventDefault();
        ajaxFormSubmit('#delete_wedding_venue_images', function(response){
            ajaxUpdate('#delete_wedding_venue_images', response);
        });
    });

    $('#wedding_theme_content').submit(function (e) {
        e.preventDefault();
        ajaxFormSubmit('#wedding_theme_content', function(response){
            ajaxUpdate('#wedding_theme_content', response);
        });
    });

    $('#men_picture_upload').submit(function (e) {
        e.preventDefault();
        ajaxFileUploadSubmit('#men_picture_upload', function (response) {
            ajaxUpdate('#men_picture_upload', response);
            $('#men_picture_delete').empty().load('/admin/wedding_theme/men/delete');
        });
    });

    $('#men_picture_delete').submit(function (e) {
        e.preventDefault();
        ajaxFormSubmit('#men_picture_delete', function(response){
            ajaxUpdate('#men_picture_delete', response);
        });
    });

    $('#women_picture_upload').submit(function (e) {
        e.preventDefault();
        ajaxFileUploadSubmit('#women_picture_upload', function (response) {
            ajaxUpdate('#women_picture_upload', response);
            $('#women_picture_delete').empty().load('/admin/wedding_theme/women/delete');
        });
    });

    $('#women_picture_delete').submit(function (e) {
        e.preventDefault();
        ajaxFormSubmit('#women_picture_delete', function(response){
            ajaxUpdate('#women_picture_delete', response);
        });
    });

    $('#food_bar_add').submit(function (e) {
        e.preventDefault()
        ajaxFormSubmit('#food_bar_add', function(response){
            ajaxUpdate('#food_bar_add', response)
            $('#food_bar_delete').empty().load('/admin/food_bar/delete');
        });
    });

    $('#food_bar_delete').submit(function (e) {
        e.preventDefault();
        ajaxFormSubmit('#food_bar_delete', function (response) {
            ajaxUpdate('#food_bar_delete', response);
        });
    });

    $('#edit_after_party').submit(function (e) {
        e.preventDefault();
        ajaxFormSubmit('#edit_after_party', function (response) {
            ajaxUpdate('#edit_after_party', response);
        });
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