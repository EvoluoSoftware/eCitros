/*jshint es5: false */
/*
 * GET users listing.
 */

exports.entry = function(req, res){
	res.render('entry/index', { title: 'eQual' });
};