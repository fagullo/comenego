function footerController($translate) {
    
    var self = this;
    
    self.model = {};
    self.model.language = "es";
    
    self.changeLanguage = function(lang) {
        self.model.language = lang;
        $translate.use(lang);
    };
    
    
}

angular.module('comenego').controller('FooterController', footerController);
footerController.$inject = ['$translate'];