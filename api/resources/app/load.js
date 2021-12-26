function load(loadOptions) {
    skip = loadOptions.skip || 0;
    take = loadOptions.take || 10;
    query = '{"query":"query{logs(first:' + skip + ',size:' + take + '){datetime ip uri} logsCount(first:0, size: 10)}","variables":{}}'
    return $.ajax({
        type: "POST",
        url: "http://localhost:8088/api/graphql",
        data: query,
        dataFilter: function (data, type) {
            json = JSON.parse(data)
            id = skip;
            logs = json.data.logs.map(log => {
                id++;
                return Object.assign({}, log, { id: id });
            });
            return JSON.stringify({ data: logs, totalCount: json.data.logsCount });
        }
    }).fail(function (xhr, textStatus, errorThrown) {
        console.log('erro');
        console.log(xhr.responseText);
        console.log(textStatus);
        console.log(errorThrown);
    });
}