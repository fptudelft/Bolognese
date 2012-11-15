$(document).ready(function(){
    /*
      CATEGORIES
    */
    var categoryName = extractValueB('CategoryName');
    var categoryMin = extractValueB('CategoryMinPoints');
    var categoryMax = extractValueB('CategoryMaxPoints');

    var validNumber = function (str) { return $.isNumeric(str) && str >= 0 ; }
    var notEmpty = function(str) { return str.length !== 0; }

    var categoryNameValidB = liftB(notEmpty, categoryName);
    var categoryMinValidB = liftB(validNumber, categoryMin);
    var categoryMaxValidB = liftB(validNumber, categoryMax);

    var categoryFormValidB = andB(categoryMinValidB,
                                  categoryMaxValidB,
                                  categoryNameValidB);

    insertValueB(
        ifB(categoryNameValidB, '#00FF00', '#FF0000'),
        'CategoryName', 'style', 'border-color');

    insertValueB(
        ifB(categoryMinValidB, '#00FF00', '#FF0000'),
        'CategoryMinPoints', 'style', 'border-color');

    insertValueB(
        ifB(categoryMaxValidB, '#00FF00', '#FF0000'),
        'CategoryMaxPoints', 'style', 'border-color');

    insertValueB(
        ifB(categoryFormValidB, '#00FF00', '#FFFFFF'),
        'CategoryInputTable', 'style', 'background-color');

    insertValueB(
        ifB(categoryFormValidB, 'inline', 'none'),
        'CategorySubmit', 'style', 'display');

    var clickedCategoriesE = extractEventE('CategorySubmit', 'click');
    var close = constantB(-1); // The value was chosen because it
                               // is never used: Points can't be negative

    // Serialize events
    var categoryNameE = clickedCategoriesE.snapshotE(categoryName).delayE(1);
    var categoryMinE = clickedCategoriesE.snapshotE(categoryMin).delayE(2);
    var categoryMaxE = clickedCategoriesE.snapshotE(categoryMax).delayE(3);
    var categoriesDelE = clickedCategoriesE.snapshotE(close).delayE(4);

    var categoriesAllE = categoryNameE.mergeE(categoryMinE,
                                              categoryMaxE,
                                              categoriesDelE).delayE(5);

    var categoriesCollectedE = categoriesAllE.collectE([], function(item, rest) {
        /*
          collectE is not implemented in a way such that we
          we could call a "second" and "third" on the rest and than
          recurse with the "fourth" element. We resort to the
          following rather ugly hack. Enjoy.
        */
        if(item === -1) {
            var a = rest.pop();
            var b = rest.pop();
            var c = rest.pop();
            rest.push(TR({className: "CTColumnValue"}, c, b, a));
        } else {
            rest.push(TD(item));
        }
        return rest;
    }).mapE(function(a) {
        return TABLE(TR({className: "CTColumnValue"}, TH("Name"),
                         TH("Minimum Points"),
                         TH("Maximum Points") ),
                     a);
    });

    insertDomE(categoriesCollectedE, 'CategoriesTable');

    /*
      MODULES (Copy pasted....)
    */
    var moduleName = extractValueB('ModulesName');
    var modulePoints = extractValueB('ModulesPoints');
    var moduleBookableCategories = extractValueB('ModulesBookableCategories');

    var moduleNameValidB = liftB(notEmpty, moduleName);
    var modulePointsValidB = liftB(validNumber, modulePoints);
    var moduleBookableCategoriesB = liftB(notEmpty, moduleBookableCategories);

    var modulesFormValidB = andB(moduleNameValidB,
                                 modulePointsValidB,
                                 moduleBookableCategories);

    insertValueB(
        ifB(moduleNameValidB, '#00FF00', '#FF0000'),
        'ModulesName', 'style', 'border-color');

    insertValueB(
        ifB(modulePointsValidB, '#00FF00', '#FF0000'),
        'ModulesPoints', 'style', 'border-color');

    insertValueB(
        ifB(moduleBookableCategoriesB, '#00FF00', '#FF0000'),
        'ModulesBookableCategories', 'style', 'border-color');

    insertValueB(
        ifB(modulesFormValidB, '#00FF00', '#FFFFFF'),
        'ModulesInputTable', 'style', 'background-color');

    insertValueB(
        ifB(modulesFormValidB, 'inline', 'none'),
        'ModulesSubmit', 'style', 'display');

    var clickedModulesE = extractEventE('ModulesSubmit', 'click');

    // Serialize events
    var modulesNameE  = clickedModulesE.snapshotE(moduleName).delayE(1);
    var modulesPointsE = clickedModulesE.snapshotE(modulePoints).delayE(2);
    var modulesBookableCategoriesE =
        clickedModulesE.snapshotE(moduleBookableCategories).delayE(3);
    var modulesDelE = clickedModulesE.snapshotE(close).delayE(4);

    var modulesAllE = modulesNameE.mergeE(modulesPointsE,
                                          modulesBookableCategoriesE,
                                          modulesDelE).delayE(5);

    var modulesCollectedE = modulesAllE.collectE([], function(item, rest) {
        if(item === -1) {
            var a = rest.pop();
            var b = rest.pop();
            var c = rest.pop();
            rest.push(TR({className: "MTColumnValue"}, c, b, a));
        } else {
            rest.push(TD(item));
        }
        return rest;
    }).mapE(function(a) {
        return TABLE(TR({className: "MTColumnValue"},
                         TH("Name"),
                         TH("Points"),
                         TH("Bookable Categories") ),
                     a);
    });

    insertDomE(modulesCollectedE, 'ModulesTable');

    totalE = clickedModulesE
        .snapshotE(modulePoints)
        .collectE(0, function(item, count) {
            return parseInt(item) + parseInt(count);
        });

    insertDomE(totalE, 'PointTotal');

    // Add sum of points!!
});
