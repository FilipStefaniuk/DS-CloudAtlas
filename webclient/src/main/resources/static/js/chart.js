$(document).ready(function () {

    var selected_attribute = undefined;

    var dataPoints = [];

    var chart = new CanvasJS.Chart("attribute-chart", {
        zoomEnabled: true,
        title: {
            text: "Attribute: "
        },
        axisY:{
            includeZero: false
        },
        data: [{
            type: "line",
            xValueType: "dateTime",
            yValueFormatString: "####.00",
            xValueFormatString: "hh:mm:ss TT",
            showInLegend: true,
            name: "Company A",
            dataPoints: dataPoints
        }]
    });

    $("#attributes-table").on("click", ".clickable-row", function () {
        selected_attribute = $(this).children().first().html()
        dataPoints.length = 0;
        chart.options.title.text = selected_attribute;
        chart.options.data[0].name = selected_attribute;
        chart.render();
    });


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

                var tbody1 = $('#attributes-table').find('tbody').empty();
                var tbody2 = $('#queries-table').find('tbody').empty();

                for(var key in data) {
                    if (data[key] !== "") {
                        if (key.substring(0, 1) === '&') {

                            var tr = '<tr style="clear:left"><td class="col-xs-2">' + key + '</td><td class="col-xs-8">' + data[key] + '</td><td>';
                            if (key !== "&nmembers" && key !== "&contacts") {
                                tr += '<button class="btn btn-default" onclick="uninstallQuery(\'' + key + '\')">Delete</button>';
                            }
                            tr += '</td></tr>'
                            tbody2.append(tr);

                        } else {
                            tbody1.append('<tr class="clickable-row"><td class="col-xs-8">' + key + '</td><td class="col-xs-2">' + data[key] + '</td></tr>');

                            if(key === selected_attribute) {
                                dataPoints.push({
                                    x: new Date,
                                    y: parseFloat(data[key])
                                });
                                chart.render();
                            }
                        }
                    }
                }
            });
        }
    }, 3000);

});

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