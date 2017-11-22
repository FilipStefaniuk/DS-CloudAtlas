function installQuery() {
    var data = $("#install_query").serialize();
    $.get('ajax/installquery?' + data)
        .done(function() {
            $("#install_query").trigger('reset');
        })
}

function uninstallQuery(name) {
    $.get('ajax/uninstallquery', {name:name})
        .done(function() {
            $("#uninstall_query").trigger('reset');
        });
}


$(document).ready(function() {
    var onchart = null;
    var series = new TimeSeries();
    var smoothie = new SmoothieChart({maxValueScale:1.2,minValueScale:1.2,responsive:true,grid:{fillStyle:'#eee'}, labels:{fillStyle:'#000000'}});
    smoothie.addTimeSeries(series, {lineWidth:2, strokeStyle:'#333'});
    smoothie.streamTo(document.getElementById("chart-canvas"), 1000);

    $("#attributes-table").on('click', '.clickable-row', function(event) {
        // $(this).addClass('active').siblings().removeClass('active');
        onchart = $(this).children().first().html();
        $("#chart-title").text(onchart);
        series.clear();
    });

    setInterval(function() {
        var select = document.getElementById("zones");
        var zone = select.options[select.selectedIndex].value;

        $.get('ajax/attributes', {name: zone})
            .done(function (data) {
                var attributes = JSON.parse(data);
                var table = $('#attributes-table').empty().append('<tbody></tbody>').find('tbody');
                var table2 = $('#queries-table').empty().append('<tbody></tbody>').find('tbody');

                for(var key in attributes) {
                    if (key.substring(0, 1) === '&') {
                        var qname = key.substring(1);
                        table2.append('<tr><td>' + qname + '</td><td>' + attributes[key] + '</td><td><button class="btn btn-default" onclick="uninstallQuery(\''+ qname +'\')">Delete</button></td></tr>');
                    } else {
                        table.append('<tr class="clickable-row"><td>' + key + '</td><td>' + attributes[key] + '</td></tr>');
                    }
                }
                series.append(new Date().getTime(), attributes[onchart]);
            });
    }, 1000);

});