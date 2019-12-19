$(document).ready(
    $('#submit_btn').click(function (event) {

        event.preventDefault();

        var form = $('#methods_form')[0];
        var data = new FormData(form);
        console.dir(data);
        return;

        $.ajax({
            url: '/register',
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify({"name": $('#name').val(), "issn": $('#issn').val()}),
            dataType: 'text',
            success: function (data) {
                window.location.replace(data.toString().substr(9));
            }
        });
    })
);