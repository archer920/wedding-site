$(document).ready(function () {
    $('#delete_roles').submit(function (e) {
        e.preventDefault();
        ajaxFormSubmit('#delete_roles', function (response) {
            ajaxUpdate('#delete_roles', response);
        });
    });

    $('#add_roles').submit(function (e) {
        e.preventDefault();
        ajaxFormSubmit('#add_roles', function (response) {
            ajaxUpdate('#add_roles', response);
            $('#delete_roles').empty().load('/admin/delete_roles');
        });
    });

    function ajaxFormSubmit(selector, success, error) {
        var form = $(selector);

        var token = $(form).find("input[name='_csrf']").val();
        $.ajaxSetup({
            beforeSend: function (xhr) {
                xhr.setRequestHeader('X-CSRF-Token', token);
            }
        });

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

    function ajaxUpdate(selector, response) {
        $(selector).empty().html(response);
        $('#alerts').empty().load('/messages');
    }
});