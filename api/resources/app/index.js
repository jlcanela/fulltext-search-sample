$(function(){
    $("#gridContainer").dxDataGrid({
        dataSource: new DevExpress.data.CustomStore({
            key: "datetime",
            loadMode: "processed", // omit in the DataGrid, TreeList, PivotGrid, and Scheduler
            load: load
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
