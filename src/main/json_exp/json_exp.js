// (function() {
//     var ref = document.getElementsByTagName('script')[0];
//     var js = document.createElement('script');
//     // js.src = '//connect.facebook.net/en_US/all' + (debug ? '/debug' : '') + '.js';
//     // js.src = 'http://code.jquery.com/jquery-1.8.2.js';
//     js.src = './jquery-1.8.2.js';
//     ref.parentNode.insertBefore(js, ref);
// })();

/**
 * Creates a new category and returns it as a dictionary.
 * name    : the name of the category
 * minEcts : the min number of ECTS to be booked in this category
 * maxEcts : the max number of ECTS to be booked in this category
 */
function newCategory(name, minEcts, maxEcts) {
    return { 'name'    : name,
             'minEcts' : minEcts,
             'maxEcts' : maxEcts }
}

/**
 * Creates a new module and returns it as a dictionary.
 * name     : the name of the module
 * numEcts  : the number of ECTS the module is worth
 * category : the name of the category this module should be booked into
 */
function newModule(name, numEcts, category) {
    return { 'name'     : name,
             'numEcts'  : numEcts,
             'category' : category }
}

// function onSuccess(data, textStatus, jqXHR) {
//     alert(data + '\n' + textStatus + '\n' + jqXHR)
// }

// function sendReq() {
//     $.getJSON( 'localhost:8080/solve', function ( data ) { console.log ( data ); } );
// }

function sendRequest() {
    $.ajax({
        url : 'http://localhost:8080/solve',
        // url : 'http://192.168.1.110:8080/solve',
        type : 'POST',
        contentType: 'text/plain; charset=UTF-8',
        // crossDomain: true,
        dataType: 'json',
        // data : { 'cat' : [ { 'name' : 'Compulsory', 'minEcts' : 10, 'maxEcts' : 33},
        //                    { 'name' : 'Specialization', 'minEcts' : 7, 'maxEcts' : 7} ],
        //          'mod' : [ { 'name' : 'Methodology of Science and Engineering', 'numEcts' : 5, 'category' : 'Compulsory'},
        //                    { 'name' : 'Computer Architecture', 'numEcts' : 5, 'category' : 'Compulsory'},
        //                    { 'name' : 'Computer Arithemtics', 'numEcts' : 5, 'category' : 'Compulsory'},
        //                    { 'name' : 'Processor Design Project', 'numEcts' : 5, 'category' : 'Specialization'},
        //                    { 'name' : 'Intro Computer Engineering', 'numEcts' : 2, 'category' : 'Specialization'},
        //                    { 'name' : 'Parallel Algorithms', 'numEcts' : 6, 'category' : 'Compulsory'} ] },

        data: { 'cat': [ newCategory('Compulsory', 10, 33),
                         newCategory('Specialization', 7, 7) ],
                'mod': [ newModule('Methodology of Science and Engineering',
                                   5, 'Compulsory'),
                         newModule('Computer Architecture',
                                   5, 'Compulsory'),
                         newModule('Computer Arithemtics',
                                   5, 'Compulsory'),
                         newModule('Processor Design Project',
                                   5, 'Specialization'),
                         newModule('Intro Computer Engineering',
                                   2, 'Specialization'),
                         newModule('Parallel Algorithms',
                                   6, 'Compulsory') ] },
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
                console.log('\tdata: %o', data)
                console.log('\ttextStatus: %o', textStatus)
                console.log('\tjqXHR: %o', jqXHR)
            }
        },
    });
}

$(document).ready(function(){
    $('#send-req').click(function(event) {
        // if (console && console.log) {
        //     console.log('hello')
        // }

        // $.ajax({
        //     url: ""
        // });

        sendRequest()
    });
});
