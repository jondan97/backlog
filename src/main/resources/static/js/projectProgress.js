function showChart(categories, ideal_burn, actual_burn) {
//in case the PM has not input any estimation about the sprints needed to finish the project
    let visible = true;
    if (!ideal_burn.length) {
        visible = false;
    }
    let titleWithImage = 'Product Burndown';
    if (checkCookie('hints')) {
        titleWithImage = 'Product Burndown' +
            ' ' +
            '<img class="hintSymbol" src="../../../../../images/hintSymbol.png" style="width: 30px;height: 30px;margin: 0 0 7px 2px;" onclick="openHintModal(\'productBurndown\')">';
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
                text: 'All Sprints',
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
                visible: visible,
                color: 'rgba(255,0,22,0.25)',
                lineWidth: 1,
                data: ideal_burn
            }, {
                name: 'Actual Remaining',
                color: 'rgba(133,200,98,0.75)',
                marker: {
                    radius: 6
                },
                data: actual_burn
            }]
        });
    });
}

function openViewItemModal(projectId, itemId) {
    $('.modal-body').load('/user/project/' + projectId + '/item/' + itemId + '/?source=projectProgressPage', function () {
        $('#viewItemModal').modal({show: true});
    });
}

function redirectToModal(projectId, itemId) {
    $('.modal-body').load('/user/project/' + projectId + '/item/' + itemId + '/?source=projectProgressPage', function () {
        $('#viewItemModal').modal({show: true});
    });
}