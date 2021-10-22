$(function(){
    $("#gridContainer").dxDataGrid({
        dataSource: new DevExpress.data.CustomStore({
            key: "datetime",
            loadMode: "processed", // omit in the DataGrid, TreeList, PivotGrid, and Scheduler
            load: function (loadOptions) {
                skip = loadOptions.skip || 0;
                take = loadOptions.take || 10;
                query = '{"query":"query{logs(first:'+skip+',size:'+take+'){count logs{datetime ip uri}}}","variables":{}}'
                return $.ajax({
                    type: "POST",
                    url: "http://localhost:8088/api/graphql",
                    data: query,
                    dataFilter: function (data, type) {
                        json = JSON.parse(data)
                        id = skip;
                        logs = json.data.logs.logs.map(log => {
                            id++;
                            return Object.assign({}, log, { id: id });
                        });
                        return JSON.stringify({ data:logs, totalCount: json.data.logs.count });
                    }
                }).fail( function(xhr, textStatus, errorThrown) {
                    console.log('erro');
                    console.log(xhr.responseText);
                    console.log(textStatus);
                    console.log(errorThrown);
                });
                
            }
        }),
        keyExpr: "datetime",
        showBorders: true,
        customizeColumns: function (columns) {
            columns[0].width = 70;
        },
        remoteOperations: true,
        loadPanel: {
            enabled: false
        },
        scrolling: {
            mode: 'virtual'
        },
        sorting: {
            mode: "none"
        }
    });
    
});
