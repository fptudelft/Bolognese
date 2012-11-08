// jsonify :: HTML -> JSON
// HTML table -> Bolognese JSON
// add column
// delete column
$(document).ready(function(){
  var categoryName = extractValueB('CategoryName');
  var categoryMin = extractValueB('CategoryMinPoints');
  var categoryMax = extractValueB('CategoryMaxPoints');

  var validNumber = function (str) { return $.isNumeric(str); }
  var notEmpty = function(str) { return str.length !== 0; }

  var categoryMinValidB = liftB(validNumber, categoryMin);
  var categoryMaxValidB = liftB(validNumber, categoryMax);
  var categoryNameValidB = liftB(notEmpty, categoryName);

  var categoryFormValidB = andB(categoryMinValidB, categoryMaxValidB,
                                categoryNameValidB);
  insertValueB(
    ifB(categoryFormValidB, '#00FF00', '#FF0000'),
    'CategoryFormField', 'style', 'borderColor');

  //insertDomB(TR(), 'CategoriesTable', 'after');


});