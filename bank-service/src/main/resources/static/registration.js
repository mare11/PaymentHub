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
            dataType: 'json',
            success: function (response) {
                if (response) {
                    if (response.success) {
                        $('#reg_container').hide(300);
                        $('#reg_confirmed').show(300);
                    } else {
                        alert(response.message);
                    }
                } else {
                    alert('An unexpected error occurred!');
                }
            }
        });
    })
);
