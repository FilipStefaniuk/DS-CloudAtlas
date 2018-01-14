function installQuery() {
    var agent = $("#agents").val();
    var data = $("#install_query").serialize();
    $.get('agents/' + agent + "/install?" + data)
        .done(function() {
            $("#install_query").trigger('reset');
        })
}

function uninstallQuery(name) {
    var agent = $("#agents").val();
    $.get('agents/' + agent + "/install", {name:name})
        .done(function() {
            $("#uninstall_query").trigger('reset');
        });
}

$(document).ready(function () {

    setInterval(function () {
        $.get("/agents", function (data) {
            var agents_list = $("#agents-list");
            agents_list.empty();
            $.each(data, function (i, item) {
                agents_list.append("<li>" + item + "</li>");
            });
        })
    }, 5000);

    setInterval(function () {
        var agent = $("#agents").val();
        if (agent !== "") {
            agent = agent.split("_").join("-");
            var zones = $("#zones-list");
            zones.empty();
            $.get("/agents/" + agent, function (data) {
                $.each(data, function(i, item) {
                    zones.append("<li>" + item + "</li>");
                });
            });
        }
    }, 3000);

    setInterval(function () {
        var agent = $("#agents").val();
        var zone = $("#zones").val();
        if (zone !== "") {
            zone = zone.split("/").join("-");
            $.get("/agents/" + agent + "/zone" + zone, function (data) {
                var table = $('#attributes-table').empty().append('<tbody></tbody>').find('tbody');
                var table2 = $('#queries-table').empty().append('<tbody></tbody>').find('tbody');

                for(var key in data) {
                    if (data[key] !== "") {
                        if (key.substring(0, 1) === '&') {
                            var tr = '<tr><td>' + key + '</td><td>' + data[key] + '</td><td>';
                            if (key !== "&nmembers" && key !== "&contacts") {
                                tr += '<button class="btn btn-default" onclick="uninstallQuery(\'' + key + '\')">Delete</button>';
                            }
                            tr += '</td></tr>'
                            table2.append(tr);
                        } else {
                            table.append('<tr class="clickable-row"><td>' + key + '</td><td>' + data[key] + '</td></tr>');
                        }
                    }
                }
            });
        }
    }, 3000);
});