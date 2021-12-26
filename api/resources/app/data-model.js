(function ($) {
    /***
     * A sample AJAX data store implementation.
     * Right now, it's hooked up to load search results from Octopart, but can
     * easily be extended to support any JSONP-compatible backend that accepts paging parameters.
     */
    function RemoteDataModel() {
      // private
      var PAGESIZE = 50;
      var data = {length: 0};
      var searchstr = "";
      var sortcol = null;
      var sortdir = 1;
      var h_request = null;
      var req = null; // ajax request
  
      // events
      var onDataLoading = new Slick.Event();
      var onDataLoaded = new Slick.Event();
  
      function init() {
      }
  
  
      function isDataLoaded(from, to) {
        for (var i = from; i <= to; i++) {
          if (data[i] == undefined || data[i] == null) {
            return false;
          }
        }
  
        return true;
      }
  
  
      function clear() {
        for (var key in data) {
          delete data[key];
        }
        data.length = 0;
      }
  
  
      function ensureData(from, to) {
        if (req) {
          //req.abort();
          for (var i = req.fromPage; i <= req.toPage; i++)
            data[i * PAGESIZE] = undefined;
        }
  
        if (from < 0) {
          from = 0;
        }
  
        if (data.length > 0) {
          to = Math.min(to, data.length - 1);
        }
  
        var fromPage = Math.floor(from / PAGESIZE);
        var toPage = Math.floor(to / PAGESIZE);
  
        while (data[fromPage * PAGESIZE] !== undefined && fromPage < toPage)
          fromPage++;
  
        while (data[toPage * PAGESIZE] !== undefined && fromPage < toPage)
          toPage--;
  
        if (fromPage > toPage || ((fromPage == toPage) && data[fromPage * PAGESIZE] !== undefined)) {
          // TODO:  look-ahead
        }
  
        var url = "http://localhost:8888/api/q=" + searchstr + "&start=" + (fromPage * PAGESIZE) + "&limit=" + (((toPage - fromPage) * PAGESIZE) + PAGESIZE);
  
        if (sortcol != null) {
          url += ("&sortby=" + sortcol + ((sortdir > 0) ? "+asc" : "+desc"));
        }
  
        if (h_request != null) {
          clearTimeout(h_request);
        }
  
        h_request = setTimeout(function () {
          for (var i = fromPage; i <= toPage; i++)
            data[i * PAGESIZE] = null; // null indicates a 'requested but not available yet'

          onDataLoading.notify({from: from, to: to});
  
          req = {
            fromPage,
            toPage
          }

          searchParam = ""
          if (searchstr.length > 2) {
            searchParam = `, search: ${JSON.stringify(searchstr.replaceAll('"', "").replaceAll("\\",""))}`
          }

          fetch('http://localhost:8088/api/graphql', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              query: `
                query {
                  logs(first: ${from}, size: ${to - from + 1}${searchParam}) {
                      datetime ip uri 
                  }  
                  logsCount(${searchParam})
                }              
                `,
              variables: {},
            }),
          })
            .then((res) => res.json())
            .then((res) => onSuccess({
              request: {
                start: from
              },
              results: res
            }))
            .catch((e) => onError(fromPage, toPage));
      
          req.fromPage = fromPage;
          req.toPage = toPage;
        }, 50);
      }
  
  
      function onError(fromPage, toPage) {
        alert("error loading pages " + fromPage + " to " + toPage);
      }
  
      function onSuccess(resp) {
        
        start = Date.now()
        
        var from = resp.request.start, to = from + resp.results.data.logs.length;
        data.recordsCount = resp.results.data.logsCount
        data.length = Math.min(data.recordsCount, 10000) // limitation of ES API 
        logs = resp.results.data.logs;

        for (var i = 0; i < logs.length; i++) {
          if (logs[i]) {
            data[from + i] = logs[i];
            data[from + i].index = from + i;
          }
          
        }
        
        req = null;

        onDataLoaded.notify({ from: from, to: to });     
      }
  
  
      function reloadData(from, to) {
        for (var i = from; i <= to; i++)
          delete data[i];
  
        ensureData(from, to);
      }
  
  
      function setSort(column, dir) {
        sortcol = column;
        sortdir = dir;
        clear();
      }
  
      function setSearch(str) {
        searchstr = str;
        clear();
      }
  
  
      init();
  
      return {
        // properties
        "data": data,
  
        // methods
        "clear": clear,
        "isDataLoaded": isDataLoaded,
        "ensureData": ensureData,
        "reloadData": reloadData,
        "setSort": setSort,
        "setSearch": setSearch,
  
        // events
        "onDataLoading": onDataLoading,
        "onDataLoaded": onDataLoaded
      };
    }
  
    // exports
    $.extend(true, window, { 
      Slick: { 
        Data: { 
            RemoteModel: RemoteDataModel 
        }
      }
    });
  })(jQuery);