/*jshint es5: false */
/**
 * Module dependencies.
 */

var express = require('express'),
	favicon = require('serve-favicon'),
	morgan = require('morgan'),
	bodyParser = require('body-parser'),
	methodOverride = require('method-override'),
	errorhandler = require('errorhandler'),
	routes = require('./routes'),
	user = require('./routes/user'),
	http = require('http'),
	path = require('path'),
	ejs = require('ejs'),
	engine = require('ejs-locals'),
	i18n = require('i18next');

//locale config
i18n.init({
	ns: { namespaces: ['translation'],
	defaultNs: 'translation'},
	resSetPath: 'locales/__lng__/new.__ns__.json',
	saveMissing: true,
	debug: true,
	sendMissingTo: 'fallback'
});

var app = express();

// all environments
app.set('port', process.env.PORT || 3000);
app.set('views', __dirname + '/views');
app.engine('ejs',engine);
app.set('view engine', 'ejs');
app.use(favicon(__dirname + '/public/eCitros/img/icon.png'));
app.use(morgan('dev'));
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.use(methodOverride('X-HTTP-Method-Override'));
app.use(express.static(path.join(__dirname, 'public')));

app.use(i18n.handle);
i18n.serveWebTranslate(app, {
	i18nextWTOptions: {
		languages: ['pt-BR', 'de-DE', 'en-US',  'dev'],
		namespaces: ['translation'],
		resGetPath: "locales/resources.json?lng=__lng__&ns=__ns__",
		resChangePath: 'locales/change/__lng__/__ns__',
		resRemovePath: 'locales/remove/__lng__/__ns__',
		fallbackLng: "dev",
		dynamicLoad: false
	}
});

i18n.registerAppHelper(app);

//Disponibilizando i18n ao template
app.locals.t = i18n.t;

if (process.env.NODE_ENV === 'development') {
	// only use in development
	app.use(errorhandler());
}

app.get('/', routes.index);
app.get('/entry', user.entry);

http.createServer(app).listen(app.get('port'), function(){
  console.log('Express server listening on port ' + app.get('port'));
});
