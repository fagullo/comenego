function loginController($http, $location, atomicNotifyService, sharedService) {

    var self = this;

    self.model = {
        name: "",
        password: ""
    };

    self.submit = function() {
        var request = {
            method: 'POST',
            url: '/searcher/services/login/login',
            headers: {
                'Content-Type': "application/json; charset=ISO-8859-1"
            },
            data: JSON.stringify(self.model)
        };
        $http(request).then(self.loginSuccess, self.loginError);
    };

    self.loginSuccess = function(data, status) {
        sharedService.login();
        $location.path("search");
    };

    self.loginError = function(error) {
        var msg = "Nombre de usuario o contraseña incorrectos";
        atomicNotifyService.error(msg, 5000);
    };
}

angular.module('comenego').controller('LoginController', loginController);
loginController.$inject = ['$http', '$location', 'atomicNotifyService', 'broadcastService'];