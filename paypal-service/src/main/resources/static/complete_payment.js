$(document).ready(
    $('#submit_success_btn').click(function () {
        $("#submit_success_btn").prop("disabled", true);
        completePayment('SUCCESS');
    }),

    $('#submit_cancel_btn').click(function () {
        $("#submit_cancel_btn").prop("disabled", true);
        completePayment('CANCEL');
    })
);

var completePayment = function (status) {
    var searchParams = new URLSearchParams(window.location.search);
    var orderId = searchParams.get('token');

    $.ajax({
        url: '/complete',
        type: 'post',
        contentType: 'application/json',
        data: JSON.stringify({"orderId": orderId, "status": status}),
        dataType: 'text',
        success: function (redirectionUrl) {
            window.location.href = redirectionUrl;
        }
    });
};