window.HighchartsConnector = {

    init: function() {
        // Initialisation si nécessaire
    },

    createColumnChart: function(containerId, title, categories, seriesData) {
        Highcharts.chart(containerId, {
            chart: {
                type: 'column',
                backgroundColor: 'transparent'
            },
            title: {
                text: title,
                style: {
                    color: '#1e293b',
                    fontWeight: 'bold'
                }
            },
            xAxis: {
                categories: categories,
                crosshair: true,
                labels: {
                    style: {
                        color: '#666'
                    }
                }
            },
            yAxis: {
                min: 0,
                title: {
                    text: 'Nombre',
                    style: {
                        color: '#666'
                    }
                },
                labels: {
                    style: {
                        color: '#666'
                    }
                }
            },
            tooltip: {
                headerFormat: '<span style="font-size:10px">{point.key}</span><table>',
                pointFormat: '<tr><td style="color:{series.color};padding:0">{series.name}: </td>' +
                    '<td style="padding:0"><b>{point.y}</b></td></tr>',
                footerFormat: '</table>',
                shared: true,
                useHTML: true
            },
            plotOptions: {
                column: {
                    pointPadding: 0.2,
                    borderWidth: 0
                }
            },
            series: seriesData,
            credits: {
                enabled: false
            }
        });
    },

    createPieChart: function(containerId, title, data) {
        Highcharts.chart(containerId, {
            chart: {
                type: 'pie',
                backgroundColor: 'transparent'
            },
            title: {
                text: title,
                style: {
                    color: '#1e293b',
                    fontWeight: 'bold'
                }
            },
            tooltip: {
                pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
            },
            plotOptions: {
                pie: {
                    allowPointSelect: true,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: true,
                        format: '<b>{point.name}</b>: {point.percentage:.1f} %',
                        style: {
                            color: 'black'
                        }
                    }
                }
            },
            series: [{
                name: 'Répartition',
                colorByPoint: true,
                data: data
            }],
            credits: {
                enabled: false
            }
        });
    },

    createLineChart: function(containerId, title, categories, seriesData) {
        Highcharts.chart(containerId, {
            chart: {
                type: 'line',
                backgroundColor: 'transparent'
            },
            title: {
                text: title,
                style: {
                    color: '#1e293b',
                    fontWeight: 'bold'
                }
            },
            xAxis: {
                categories: categories,
                labels: {
                    style: {
                        color: '#666'
                    }
                }
            },
            yAxis: {
                title: {
                    text: 'Valeur',
                    style: {
                        color: '#666'
                    }
                },
                labels: {
                    style: {
                        color: '#666'
                    }
                }
            },
            plotOptions: {
                line: {
                    dataLabels: {
                        enabled: true
                    }
                }
            },
            series: seriesData,
            credits: {
                enabled: false
            }
        });
    },

    createAreaChart: function(containerId, title, categories, seriesData) {
        Highcharts.chart(containerId, {
            chart: {
                type: 'area',
                backgroundColor: 'transparent'
            },
            title: {
                text: title,
                style: {
                    color: '#1e293b',
                    fontWeight: 'bold'
                }
            },
            xAxis: {
                categories: categories,
                labels: {
                    style: {
                        color: '#666'
                    }
                }
            },
            yAxis: {
                title: {
                    text: 'Valeur',
                    style: {
                        color: '#666'
                    }
                },
                labels: {
                    style: {
                        color: '#666'
                    }
                }
            },
            plotOptions: {
                area: {
                    marker: {
                        enabled: false,
                        symbol: 'circle',
                        radius: 2,
                        states: {
                            hover: {
                                enabled: true
                            }
                        }
                    }
                }
            },
            series: seriesData,
            credits: {
                enabled: false
            }
        });
    }
};