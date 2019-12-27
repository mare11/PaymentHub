$(document).ready(
    $('#submit_btn').click(function (event) {

        event.preventDefault();
        let data = JSON.stringify({
            "id": $('#cardId').val(), "pan": $('#pan').val(), "ccv": $('#ccv').val(),
            "expirationDate": $('#date').val(), "cardholderName": $('#name').val()
        });
        console.log(data);

        $.ajax({
            url: '/transaction/execute/' + $('#id').val(),
            type: 'post',
            contentType: 'application/json',
            data: data,
            dataType: 'json',
            success: function (response) {
                if (response && response.paymentUrl) {
                    window.location.href = response.paymentUrl;
                }
            }
        });
    })
);