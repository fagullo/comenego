var AppRouter = Backbone.Router.extend({
    routes: {
        'search/:text(/page/:page)(/letter/:letter)': 'searcher'
    }, searcher: function(text, page, letter) {
        if (!page) {
            page = 1;
        }
        if (!letter) {
            letter = "";
        }
        search(page, letter);
    }
});

var app_router = new AppRouter;
Backbone.history.start();