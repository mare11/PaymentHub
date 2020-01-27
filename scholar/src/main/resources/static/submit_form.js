$(document).ready(
    $('#submit_btn').click(function (event) {

        event.preventDefault();

        $.ajax({
            url: '/register',
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify({"name": $('#name').val(), "issn": $('#issn').val()}),
            dataType: 'json',
            success: function (response) {
                if (response && response.redirectionUrl) {
                    window.location.href = response.redirectionUrl;
                }
            }
        });
    }),

    $('#submit_payment_btn').click(function (event) {

        event.preventDefault();

        $.ajax({
            url: '/prepare',
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify({
                "item": $('#item').val(),
                "description": $('#description').val(),
                "price": $('#price').val(),
                "merchantId": $('#merchantId').val()
            }),
            dataType: 'json',
            success: function (response) {
                if (response && response.redirectionUrl) {
                    window.location.href = response.redirectionUrl;
                }
            }
        });
    }),

    $('#submit_subscription_btn').click(function (event) {

        event.preventDefault();

        $.ajax({
            url: '/subscription',
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify({"merchantId": $('#merchantId').val()}),
            dataType: 'json',
            success: function (response) {
                if (response && response.redirectionUrl) {
                    window.location.href = response.redirectionUrl;
                }
            }
        });
    }),

    $('#submit_subscription_cancel_btn').click(function (event) {

        event.preventDefault();

        $.ajax({
            url: '/subscription/cancel',
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify({
                "merchantSubscriptionId": $('#merchantSubscriptionId').val(),
                "cancelingReason": $('#cancelingReason').val()
            }),
            dataType: 'json',
            success: function (response) {
                if (response && response.cancellationFlag) {
                    alert('You have canceled your subscription successfully!');
                } else {
                    alert(response.cancellationMessage);
                }
            }
        });
    }),
);