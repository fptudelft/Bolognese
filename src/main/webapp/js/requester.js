/********************************************************************************
 **                                                                            **
 ** Does one thing and does it well: send a POST request to the server         **
 ** with JSON data attached that is harvested from the HTML page with mapper.  **
 **                                                                            **
 ********************************************************************************/

var requester = (function() {
    var _sendRequest = function() {
        var jsonData = {
            'categories': mapper.fetchCategoriesFrom($(".CategoriesTable")),
            'modules':    mapper.fetchModulesFrom($(".ModulesTable")),
            'totalEcts':  mapper.fetchTotalEctsFrom(".TotalEcts") };

        $.ajax({
            url: 'http://localhost:8080/solve',
            type: 'POST',
            contentType: 'text/plain; charset=UTF-8',
            crossDomain: true,
            dataType: 'json',
            data: JSON.stringify(jsonData),
            success: function(data, textStatus, jqXHR) {
                if (window.console && window.console.log) {
                    window.console.log('Take in the sweet smell of success:');
                    window.console.log('\tdata: %o', data)
                    window.console.log('\ttextStatus: %o', textStatus)
                    window.console.log('\tjqXHR: %o', jqXHR)
                }
            },
            error: function(jqXHR, textStatus, errorThrown) {
                if (window.console && window.console.log) {
                    window.console.log('Look at the shame of failure:');
                    window.console.log('\tjqXHR: %o', jqXHR)
                    window.console.log('\ttextStatus: %o', textStatus)
                    window.console.log('\terrorThrown: %o', errorThrown)
                }
            },
        });
    };

    return { sendPostRequest: _sendRequest, }
})();
