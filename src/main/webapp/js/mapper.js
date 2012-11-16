/************************************************************
 **                                                        **
 ** Creates mappings from HTML Category / Module tables    **
 ** to a JSON object, then sends a POST request to the     **
 ** server with that JSON object as POST data.             **
 **                                                        **
 ************************************************************/

var mapper = (function(){
    /**
     * Creates a set of mappings from an HTML table.
     * htmlTable: The HTML table.
     * headerHtmlClass: the html class of the tag used to
     *                 find the header row of the table
     * valueHtmlClass:  the html class of the tag used to
     *                 find the value rows of the table
     */
    var _mappingsFrom = function(htmlTable, headerHtmlClass, valueHtmlClass) {
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
    };

    /**
     * Fetches a list of Category objects from an html table.
     * catTable: the root of an HTML table having the
     *           id attribute set to "CategoriesTable".
     */
    var _fetchCategoriesFrom = function(catTable) {
        return _mappingsFrom(catTable, ".CTColumnName", ".CTColumnValue");
    };

    /**
     * Fetches a list of Module objects from an html table.
     * modTable: the root of an HTML table having the
     *           id attribute set to "ModulesTable".
     */
    var _fetchModulesFrom = function(modTable) {
        return _mappingsFrom(modTable, ".MTColumnName", ".MTColumnValue");
    };

    var _fetchTotalEctsFrom = function(ectsNodeId) {
        var ectsString = $("" + ectsNodeId + " > span").text();
        return parseInt(ectsString);
    };

    return { fetchCategoriesFrom: _fetchCategoriesFrom,
             fetchModulesFrom: _fetchModulesFrom,
             fetchTotalEctsFrom: _fetchTotalEctsFrom,   }
})();



// $(document).ready(function() {
//     var jsonData = { 'categories': mapper.fetchCategoriesFrom($("#CategoriesTable")),
//                      'modules':    mapper.fetchModulesFrom($("#ModulesTable")),
//                      'totalEcts':  17 }; // TODO remove this hardcoded
//                                          // magic number
//
//     $('#send-req').click( function(){ mapper.sendRequest(jsonData) } );
//
//     $('#logTables').click(function() {
//         console.log("categories: %o", jsonData['categories']);
//         console.log("modules: %o", jsonData['modules']);
//         console.log("jsonData: %o", jsonData);
//     });
//
//     $("table").css("background-color", "blue");
// });
