'use strict';

angular.module('docs').controller('SettingsUserRegisterRequest', function($scope, Restangular) {
  function loadRequests() {
    Restangular.one('user/register_request/list').get().then(function(data) {
      $scope.requests = data;
    });
  }
  $scope.approve = function(req) {
    Restangular.one('user/register_request', req.id).all('approve').post().then(function(res) {
      if (res.success) {
        $scope.successMsg = res.message;
        $scope.errorMsg = "";
        loadRequests();
      } else {
        $scope.errorMsg = res.message || "操作失败";
        $scope.successMsg = "";
      }
    }, function(response) {
      var msg = "操作失败";
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
  $scope.reject = function(req) {
    Restangular.one('user/register_request', req.id).all('reject').post().then(function(res) {
      if (res.success) {
        $scope.successMsg = res.message;
        $scope.errorMsg = "";
        loadRequests();
      } else {
        $scope.errorMsg = res.message || "操作失败";
        $scope.successMsg = "";
      }
    }, function(response) {
      var msg = "操作失败";
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
  loadRequests();
});
