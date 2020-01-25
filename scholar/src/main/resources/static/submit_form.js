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
                "price": $('#price').val()
            }),
            dataType: 'json',
            success: function (response) {
                if (response && response.paymentUrl) {
                    window.location.href = response.paymentUrl;
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
            data: JSON.stringify({"issn": $('#issn').val()}),
            dataType: 'json',
            success: function (response) {
                if (response && response.redirectionUrl) {
                    window.location.href = response.redirectionUrl;
                }
            }
        });
    }),
);