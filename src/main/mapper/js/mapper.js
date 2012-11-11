/************************************************************
 **                                                        **
 **                                                        **
 **                                                        **
 ************************************************************/

/**
 * Creates a set of mappings from an HTML table.
 * htmlTable: The HTML table.
 * headerHtmlClass: the html class of the tag used to
 *                 find the header row of the table
 * valueHtmlClass:  the html class of the tag used to
 *                 find the value rows of the table
 */
function mappingsFrom(htmlTable, headerHtmlClass, valueHtmlClass) {
    // Let's build a little language to express what we want more clearly

    // textFrom extracts the text as a string
    var textFrom = function(item) { return item.textContent; };

    // textItemsFrom splits the text on commas, puts the
    // result into a list and trims the list elements
    var textItemsFrom = function(item) {
        var trimString = function(c) { return c.trim(c); }
        return _.map(textFrom(item).split(','), trimString);
    };

    var valuesFrom = function(htmlValueRow) {
        return _.map(htmlValueRow.children, textFrom);
    };

    var valuesAsListsFrom = function(htmlValueRow) {
        return _.map(htmlValueRow.children, textItemsFrom);
    };

    /**
     * Appending to a map - Gangna... eh Functional Style!
     */
    var addTo = function(map, kvPair) {
        var clone = _.clone(map);
        clone[kvPair[0]] = kvPair[1]; // a kvPair is [key, value]
        return clone;
    }

    /**
     * Create a new mapping based on a row
     * of values extracted from htmlTable
     */
    var newMapping = function(vRow) {
        var zipReduce = function(orderedValues) {
            // This closure works because headerRow has
            // a proper value by the time it is called
            var assocList = _.zip(headerRow, orderedValues);
            return _.reduce(assocList, addTo, {});
        };
        return zipReduce(vRow);
    };

    // By definition there's exactly one header row, so the indexing op is safe
    var headerRow = _.map(htmlTable.find(headerHtmlClass), valuesFrom)[0];
    var valueRows = _.map(htmlTable.find(valueHtmlClass), valuesAsListsFrom);
    return _.map(valueRows, newMapping);
}

/**
 * Fetches a list of Category objects from an html table.
 * catTable: the root of an HTML table having the
 *           id attribute set to "CategoriesTable".
 */
function fetchCategoriesFrom(catTable) {
    return mappingsFrom(catTable, ".CTColumnName", ".CTColumnValue");
}

/**
 * Fetches a list of Module objects from an html table.
 * modTable: the root of an HTML table having the
 *           id attribute set to "ModulesTable".
 */
function fetchModulesFrom(modTable) {
    return mappingsFrom(modTable, ".MTColumnName", ".MTColumnValue");
}

function sendRequest(jsonData) {
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
}

$(document).ready(function() {
    var jsonData = { 'categories': fetchCategoriesFrom($("#CategoriesTable")),
                     'modules':    fetchModulesFrom($("#ModulesTable")),
                     'totalEcts':  17 }; // TODO remove this hardcoded
                                         // magic number

    $('#send-req').click( function(){ sendRequest(jsonData) } );

    $('#logTables').click(function() {
        console.log("categories: %o", jsonData['categories']);
        console.log("modules: %o", jsonData['modules']);
        console.log("jsonData: %o", jsonData);
    });

    $("table").css("background-color", "blue");
});