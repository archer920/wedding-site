$(document).ready(function () {
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
        })
    });

    $('#delete_carousel').submit(function (e) {
        e.preventDefault();
        ajaxFormSubmit('#delete_carousel', function (response) {
            ajaxUpdate('#delete_carousel', response);
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