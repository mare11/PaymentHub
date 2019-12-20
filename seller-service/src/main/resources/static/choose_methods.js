$(document).ready(
    $('#submit_btn').click(function (event) {

        event.preventDefault();

        methods = [];
        $('input.form-check-input:checked').each(function () {
            methods.push({"id": this.id, "name": this.value});
        });

        var data = {};
        data.paymentMethods = methods;
        data.sellerId = $('#seller_id').val();

        console.dir(data);

        $.ajax({
            url: '/methods_chosen',
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify(data),
            dataType: 'json',
            success: function () {
                console.log('success');
            }
        });
    })
);