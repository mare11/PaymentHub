$(document).ready(
    $('#submit_btn').click(function (event) {

        event.preventDefault();

        var data = JSON.stringify({
            "clientId": $('#client_id').val(),
            "clientSecret": $('#client_secret').val(),
            "merchantId": $('#merchantId').val()
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