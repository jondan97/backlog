function showChart(categories, ideal_burn, actual_burn) {
    let titleWithImage = 'Burndown Chart';
    if (checkCookie('hints')) {
        titleWithImage = 'Burndown Chart' +
            ' ' +
            '<img class="hintSymbol" src="../../../../../images/hintSymbol.png" style="width: 30px;height: 30px;margin: 0 0 7px 1px;" onclick="openHintModal(\'burndownChart\')">';
    }
    $(function () {
        $('#burndown').highcharts({
            title: {
                useHTML: true,
                text: titleWithImage,
                x: -20 //center
            },
            colors: ['blue', 'red'],
            plotOptions: {
                line: {
                    lineWidth: 3
                },
                tooltip: {
                    hideDelay: 200
                }
            },
            subtitle: {
                text: 'This Sprint',
                x: -20
            },
            xAxis: {
                categories: categories
            },
            yAxis: {
                title: {
                    text: 'Effort'
                },
                plotLines: [{
                    value: 0,
                    width: 1
                }]
            },
            tooltip: {
                valueSuffix: ' effort',
                crosshairs: true,
                shared: true
            },
            legend: {
                layout: 'vertical',
                align: 'right',
                verticalAlign: 'middle',
                borderWidth: 0
            },
            series: [{
                name: 'Ideal Remaining',
                color: 'rgba(255,0,0,0.25)',
                lineWidth: 1,
                data: ideal_burn
            }, {
                name: 'Actual Remaining',
                color: 'rgba(0,120,200,0.75)',
                marker: {
                    radius: 6
                },
                data: actual_burn
            }]
        });
    });
}

function openViewItemModal(projectId, itemId, sprintId) {
    $('.modal-body').load('/user/project/' + projectId + '/item/' + itemId + '/?source=sprintHistoryPage&sprintIdModal=' + sprintId, function () {
        $('#viewItemModal').modal({show: true});
    });
}

function redirectToModal(projectId, itemId, sprintId) {
    $('.modal-body').load('/user/project/' + projectId + '/item/' + itemId + '/?source=sprintHistoryPage&sprintIdModal=' + sprintId, function () {
        $('#viewItemModal').modal({show: true});
    });
}