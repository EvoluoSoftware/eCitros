/*jshint es5: false */
/*
 * GET home page.
 */

exports.index = function(req, res){
  res.render('index', { title: 'eQual' });
};