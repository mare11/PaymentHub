$(document).ready(
    $('#submit_btn').click(function (event) {

        event.preventDefault();

        var data = JSON.stringify({
            "merchantId": $('#merchantId').val(),
            "bankMerchantId": $('#id').val(),
            "bankMerchantPassword": $('#pass').val()
        });

        $.ajax({
            url: '/register_merchant',
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
