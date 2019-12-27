$(document).ready(
    $('#submit_btn').click(function (event) {

        event.preventDefault();

        var data = JSON.stringify({
            "issn": $('#issn').val(),
            "merchantId": $('#id').val(),
            "merchantPassword": $('#pass').val()
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