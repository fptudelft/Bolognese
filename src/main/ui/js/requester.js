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

        console.log("jsonData: %o", jsonData);

        $.ajax({
            url: 'http://localhost:8080/solve',
            type: 'POST',
            contentType: 'text/plain; charset=UTF-8',
            crossDomain: true,
            dataType: 'json',
            data: JSON.stringify(jsonData),
            success: function(data, textStatus, jqXHR) {
                if (console && console.log) {
                    console.log('Take in the sweet smell of success:');
                    console.log('\tdata: %o', data)
                    console.log('\ttextStatus: %o', textStatus)
                    console.log('\tjqXHR: %o', jqXHR)
                }
            },
            error: function(jqXHR, textStatus, errorThrown) {
                if (console && console.log) {
                    console.log('Look at the shame of failure:');
                    console.log('\tjqXHR: %o', jqXHR)
                    console.log('\ttextStatus: %o', textStatus)
                    console.log('\terrorThrown: %o', errorThrown)
                }
            },
        });
    };

    return { sendPostRequest: _sendRequest, }
})();
