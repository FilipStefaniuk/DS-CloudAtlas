

$(document).ready(function() {

    setInterval(function() {
        var select = document.getElementById("zones");
        var zone = select.options[select.selectedIndex].value;
        $.get('ajax/attributes', {name: zone})
            .done(function (data) {
                var table = $('#attributes').empty().append('<table><tbody></tbody></table>').find('tbody');
                var attributes = JSON.parse(data);
                for(var key in attributes.map) {
                    table.append('<tr><td>' + key + '</td><td>'+ JSON.stringify(attributes.map[key].value) +'</td></tr>');
                }
            });
    }, 1000);
});