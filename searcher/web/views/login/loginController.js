function loginController($scope, $http, $location, atomicNotifyService) {

    $scope.loginData = {
        user: "",
        password: ""
    };

    $scope.submit = function () {
        $http.post("/lsrecomendation/services/login/login", JSON.stringify($scope.loginData)).then($scope.loginSuccess, $scope.loginError);
    };

    $scope.loginSuccess = function (data, status) {
        $location.path("recomendation");
//        window.location.replace("/lsrecomendation/recomendation.html");
    };

    $scope.loginError = function (error) {
        var msg = error.status + " " + error.statusText + ": " + error.data;
        atomicNotifyService.error(msg, 5000);
    };
}

angular.module('comenego').controller('LoginController', loginController);
loginController.$inject = ['$scope', '$http', '$location', 'atomicNotifyService'];