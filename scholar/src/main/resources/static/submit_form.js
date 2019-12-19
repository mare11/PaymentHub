$(document).ready(
    $('#submit_btn').click(function (event) {

        event.preventDefault();

        $.ajax({
            url: '/register',
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify({"name": $('#name').val(), "issn": $('#issn').val()}),
            dataType: 'text',
            success: function (data) {
                console.log(data);
                // window.location.replace(data.toString().substr(9));
            }
        });
    })
);