$(document).ready(
    $('#submit_btn').click(function (event) {

        event.preventDefault();

        var data = JSON.stringify({
            "token": $('#token').val(),
            "merchantId": $('#merchantId').val()
        });

        $.ajax({
            url: '/register_seller',
            type: 'post',
            contentType: 'application/json',
            data: data,
            dataType: 'text',
            success: function (response) {
                if(response === "You have registered on Bitcoin service successfully!") {
                    alert(response);
                    $('#submit_btn').prop('disabled', true);
                } else {
                    alert(response);
                }
            }
        });
    })
);