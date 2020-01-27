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
                if(response) {
                    alert("You have registered on Bitcoin service successfully!");
                } else {
                    alert("Error while registering on Bitcoin service");
                }
            }
        });
    })
);