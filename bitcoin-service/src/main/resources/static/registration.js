$(document).ready(
    $('#submit_btn').click(function (event) {

        event.preventDefault();

        var data = JSON.stringify({
            "token": $('#token').val(),
            "issn": $('#issn').val()
        });

        $.ajax({
            url: '/register_seller',
            type: 'post',
            contentType: 'application/json',
            data: data,
            dataType: 'text',
            success: function (response) {
                window.location.href = response;
            }
        });
    })
);