angular.module('docs').controller('Register', function($scope, Restangular) {
    $scope.user = {};
    $scope.register = function() {
      if ($scope.user.password !== $scope.user.passwordconfirm) {
        $scope.errorMsg = "两次密码不一致";
        $scope.successMsg = "";
        return;
      }
      Restangular.all('user/register_request').post({
        username: $scope.user.username,
        password: $scope.user.password,
        email: $scope.user.email
      }).then(function(res) {
        if (res.success) {
          $scope.successMsg = res.message;
          $scope.errorMsg = "";
        } else {
          $scope.errorMsg = res.message || "注册失败";
          $scope.successMsg = "";
        }
      }, function(response) {
        var msg = "注册失败";
        if (response.data) {
          if (typeof response.data === "object") {
            msg = response.data.message || msg;
          } else if (typeof response.data === "string") {
            try {
              var obj = JSON.parse(response.data);
              msg = obj.message || msg;
            } catch (e) {
              msg = response.data;
            }
          }
        }
        $scope.errorMsg = msg;
        $scope.successMsg = "";
      });
    };
  });
  